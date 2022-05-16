package bookstore.http.auth

import bookstore.domain.users._
import bookstore.services.Users
import cats.Monad
import cats.MonadThrow
import cats.data.Kleisli
import cats.data.OptionT
import cats.effect._
import cats.effect.kernel.Async
import cats.syntax.all._
import com.comcast.ip4s._
import doobie.util.transactor._
import fs2.compression.ZLibParams
import io.circe.Decoder
import io.circe.Encoder
import io.circe.generic.semiauto._
import io.circe.syntax._
import org.http4s.AuthedRoutes
import org.http4s.Header
import org.http4s.HttpRoutes
import org.http4s.Request
import org.http4s.Response
import org.http4s.ResponseCookie
import org.http4s.Status
import org.http4s.circe.CirceEntityDecoder._
import org.http4s.circe.CirceEntityEncoder._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.dsl.io._
import org.http4s.ember.server._
import org.http4s.headers.Cookie
import org.http4s.implicits._
import org.http4s.implicits._
import org.http4s.server.AuthMiddleware
import org.reactormonk.CryptoBits
import org.reactormonk.PrivateKey

import scala.io.Codec
import scala.util.Random
import org.typelevel.log4cats.Logger

sealed abstract class Auth[F[_]: Monad: Async](
    postgres: Transactor[F]
) extends Http4sDsl[F] {
  def verifyRegistration(request: Request[F]): F[Either[String, User]]
  def authUserCookie(): Kleisli[F, Request[F], Either[String, User]]
  def register(): Kleisli[F, Request[F], Response[F]]
  def onFailure(): AuthedRoutes[String, F]
  def authRoutes(authedRoutes: AuthedRoutes[User, F]): HttpRoutes[F]
}

object Auth {
  def make[F[_]: Monad: Async: Logger](
      postgres: Transactor[F]
  ): F[Auth[F]] =
    Async[F].pure(new Auth[F](postgres) {

      import crypto.Crypto._
      override def verifyRegistration(
          request: Request[F]
      ): F[Either[String, User]] =
        request.as[UserRegistration].flatMap { userRegistration =>
          for {
            usersService <- Users.make[F](postgres)
            username = userRegistration.username
            password = userRegistration.password
            firstName = userRegistration.firstName
            lastName = userRegistration.lastName
            email = userRegistration.email
            verified = userRegistration.verified
            u <- usersService
              .create(username, password, firstName, lastName, email, verified)
          } yield Right(u)
        }

      override def authUserCookie()
          : Kleisli[F, Request[F], Either[String, User]] =
        Kleisli { request =>
          val message =
            for {
              header <- request.headers
                .get[Cookie]
                .toRight("Cookie parsing error XDD")
              cookie <- header.values.toList
                .find(_.name == "authcookie")
                .toRight("Couldn't find the cookie PLAIN")
              token <- crypto
                .validateSignedToken(cookie.content)
                .toRight("Cookie invalid PLAIN AUTH")
              msg <- Either
                .catchOnly[NumberFormatException](token.toLong)
                .leftMap(_.toString)
            } yield msg

          message.traverse(retrieveUser.run)
        }

      override def register(): Kleisli[F, Request[F], Response[F]] =
        Kleisli(request =>
          for {
            user <- verifyRegistration(request)
            response <- user match {
              case Left(error) =>
                Forbidden(error)
              case Right(registeredUser) =>
                val message = crypto.signToken(
                  registeredUser.userId.toString(),
                  clock.millis().toString()
                )
                Ok("Registered!").map(
                  _.addCookie(ResponseCookie("authcookie", message))
                )
            }
          } yield response
        )

      override def onFailure(): AuthedRoutes[String, F] =
        Kleisli(req => OptionT.liftF(Forbidden(req.context)))

      private val retrieveUser: Kleisli[F, Long, User] = Kleisli(userId =>
        for {
          usersService <- Users.make[F](postgres)
          user <- usersService.findUserById(userId).map(user => user.get)
        } yield user
      )

      val middleware: AuthMiddleware[F, User] =
        AuthMiddleware(authUserCookie(), onFailure())

      override def authRoutes(
          authedRoutes: AuthedRoutes[User, F]
      ): HttpRoutes[F] =
        middleware(authedRoutes)

    })
}
