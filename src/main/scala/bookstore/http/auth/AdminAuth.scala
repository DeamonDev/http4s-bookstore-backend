package bookstore.http.auth

import bookstore.domain.admins
import bookstore.services.Admins
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

sealed abstract class AdminAuth[F[_]: Monad: Async](postgres: Transactor[F])
    extends Http4sDsl[F] {
  def authAdminCookie(): Kleisli[F, Request[F], Either[String, admins.Admin]]
  def onFailure(): AuthedRoutes[String, F]
  def adminMiddleware(routes: AuthedRoutes[admins.Admin, F]): HttpRoutes[F]

  def verifyLogin(req: Request[F]): F[Either[String, admins.Admin]]
  def login(): Kleisli[F, Request[F], Response[F]]
}

object AdminAuth {
  def make[F[_]: Monad: Async](
      postgres: Transactor[F]
  ): F[AdminAuth[F]] =
    Async[F].pure(new AdminAuth[F](postgres) {

      import crypto.AdminCrypto._
      override def authAdminCookie()
          : Kleisli[F, Request[F], Either[String, admins.Admin]] =
        Kleisli { request =>
          val message =
            for {
              header <- request.headers
                .get[Cookie]
                .toRight("Cookie parsing error XD")
              cookie <- header.values.toList
                .find(_.name == "newcookie")
                .toRight("Couldn't find the cookie")
              token <- crypto
                .validateSignedToken(cookie.content)
                .toRight("Cookie invalid")
              msg <- Either
                .catchOnly[NumberFormatException](token.toLong)
                .leftMap(_.toString())
            } yield msg

          message.traverse(retrieveAdmin.run)
        }
      override def onFailure(): AuthedRoutes[String, F] =
        Kleisli(req => OptionT.liftF(Forbidden(req.context)))

      private val retrieveAdmin: Kleisli[F, Long, admins.Admin] =
        Kleisli(adminId =>
          for {
            adminsService <- Admins.make[F](postgres)
            admin <- adminsService
              .findByAdminId(adminId)
              .map(admin => admin.get)
          } yield admin
        )

      override def verifyLogin(
          request: Request[F]
      ): F[Either[String, admins.Admin]] =
        request.as[admins.AdminLogin].flatMap { adminLogin =>
          for {
            adminsService <- Admins.make[F](postgres)
            adminName = adminLogin.adminName
            adminPass = adminLogin.adminPass
            // TODO VERIFICATION!!!
            admin <- adminsService.findByAdminName(adminName)
          } yield Right(admin.get)
        }

      override def login(): Kleisli[F, Request[F], Response[F]] = Kleisli {
        req =>
          for {
            adminO <- verifyLogin(req)
            response <- adminO match {
              case Left(error) =>
                Forbidden(error)
              case Right(admin) =>
                val message = crypto.signToken(
                  admin.adminId.toString(),
                  clock.millis().toString()
                )
                Ok("Logged as admin!").map(
                  _.addCookie(
                    ResponseCookie(
                      name = "newcookie",
                      content = message,
                      path = Some("/")
                    )
                  )
                )
            }
          } yield response
      }

      val middleware: AuthMiddleware[F, admins.Admin] =
        AuthMiddleware(authAdminCookie(), onFailure())

      override def adminMiddleware(
          routes: AuthedRoutes[admins.Admin, F]
      ): HttpRoutes[F] =
        middleware(routes)
    })
}
