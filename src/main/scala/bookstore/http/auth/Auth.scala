package bookstore.http.auth

import cats.Monad
import cats.effect.kernel.Async
import doobie.util.transactor._
import org.http4s.Request
import bookstore.domain.users._
import cats.data.Kleisli
import org.http4s.Response

import cats.effect._
import cats.syntax.all._
import org.http4s.dsl.io._
import com.comcast.ip4s._
import org.http4s.HttpRoutes
import org.http4s.implicits._
import org.http4s.ember.server._
import fs2.compression.ZLibParams
import org.http4s.Header
import cats.data.Kleisli
import org.http4s.Status
import org.http4s.Request
import org.http4s.Response
import cats.data.OptionT
import org.http4s.server.AuthMiddleware
import org.http4s.AuthedRoutes

import org.reactormonk.{CryptoBits, PrivateKey}
import scala.io.Codec
import scala.util.Random
import org.http4s.ResponseCookie
import org.http4s.headers.Cookie

import io.circe.syntax._
import io.circe.generic.semiauto._
import org.http4s.implicits._
import org.http4s.circe._
import cats.MonadThrow
import io.circe.Decoder
import io.circe.Encoder

import org.http4s.circe.CirceEntityDecoder._
import org.http4s.circe.CirceEntityEncoder._

import org.http4s.dsl.Http4sDsl
import bookstore.services.Users

sealed abstract class Auth[F[_]: Monad: Async](
  postgres: Transactor[F]
) extends Http4sDsl[F] { 
  def verifyRegistration(request: Request[F]): F[Either[String, User]]
  def authUserCookie(): Kleisli[F, Request[F], Either[String, User]]
  def register(): Kleisli[F, Request[F], Response[F]]
  def onFailure(): AuthedRoutes[String, F]
}

object Auth {
  def make[F[_]: Monad: Async](
    postgres: Transactor[F]
  ): Auth[F] = 
    new Auth[F](postgres) {
      val key = PrivateKey(Codec.toUTF8(Random.alphanumeric.take(20).mkString("")))
      val crypto = CryptoBits(key)
      val clock = java.time.Clock.systemUTC

      override def verifyRegistration(request: Request[F]): F[Either[String, User]] = 
        request.as[UserRegistration].flatMap { userRegistration =>
          // TODO connect with postgres and check 
          Async[F].pure(Right(User(1, "JP2", "vatican", "Karol", "Wojtyla", false)))
        }

      override def authUserCookie(): Kleisli[F, Request[F], Either[String, User]] = 
        Kleisli{ request => 
          val message = 
            for {
              header <- request.headers.get[Cookie]
                                       .toRight("Cookie parsing error")
              cookie <- header.values.toList.find(_.name == "authcookie")
                                            .toRight("Couldn't find the cookie")
              token  <- crypto.validateSignedToken(cookie.content)
                              .toRight("Cookie invalid")
              msg    <- Either.catchOnly[NumberFormatException](token.toLong)
                              .leftMap(_.toString)
            } yield msg

            message.traverse(retrieveUser.run)
          }
                              
      override def register(): Kleisli[F, Request[F], Response[F]] = Kleisli(request =>
        for {
          user <- verifyRegistration(request)
          response <- user match {
                        case Left(error) => 
                          Forbidden(error)
                        case Right(registeredUser) =>
                          val message = crypto.signToken(registeredUser.userId.toString(), clock.millis().toString())
                          Ok("Registered!").map(_.addCookie(ResponseCookie("authcookie", message)))
                      }
        } yield response
      )
        

      override def onFailure(): AuthedRoutes[String, F] = Kleisli( req =>
        OptionT.liftF(Forbidden(req.context)))

      private val retrieveUser: Kleisli[F, Long, User] = Kleisli( userId => 
        for {
          usersService <- Users.make[F](postgres)
          user         <- usersService.findUserById(userId).map(user => user.get)
        } yield user
      )

      val middleware: AuthMiddleware[F, User] = 
       AuthMiddleware(authUserCookie(), onFailure())
      
    }
}