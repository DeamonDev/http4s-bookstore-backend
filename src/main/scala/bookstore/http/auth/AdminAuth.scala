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
import bookstore.domain.admins
import bookstore.services.Admins
import org.http4s.Http4s



sealed abstract class AdminAuth[F[_]: Monad: Async](
  postgres: Transactor[F]) extends Http4sDsl[F] { 
    def authAdminCookie(): Kleisli[F, Request[F], Either[String, admins.Admin]]
    def onFailure(): AuthedRoutes[String, F]
    def adminMiddleware(routes: AuthedRoutes[admins.Admin, F]): HttpRoutes[F]
  }

object AdminAuth { 
  def make[F[_]: Monad: Async](
    postgres: Transactor[F]
  ): F[AdminAuth[F]] = 
    Async[F].pure(new AdminAuth[F](postgres) {

      val key = PrivateKey(Codec.toUTF8(Random.alphanumeric.take(20).mkString("")))
      val crypto = CryptoBits(key)
      val clock = java.time.Clock.systemUTC

      override def authAdminCookie(): Kleisli[F, Request[F], Either[String, admins.Admin]] = 
        Kleisli { request => 
          val message = 
            for { 
              header <- request.headers.get[Cookie]
                                       .toRight("Cookie parsing error")
              cookie <- header.values.toList.find(_.name == "admincookie")
                                            .toRight("Couldn't find the cookie")
              token  <- crypto.validateSignedToken(cookie.content)
                              .toRight("Cookie invalid")
              msg    <- Either.catchOnly[NumberFormatException](token.toLong)
                              .leftMap(_.toString())
            } yield msg

            message.traverse(retrieveAdmin.run)
        }
      override def onFailure(): AuthedRoutes[String, F] = Kleisli( req =>
        OptionT.liftF(Forbidden(req.context))
      )

      private val retrieveAdmin: Kleisli[F, Long, admins.Admin] = Kleisli( adminId => 
        for { 
          adminsService <- Admins.make[F](postgres)
          admin         <- adminsService.findByAdminId(adminId)
        } yield admin.get
      )

      val middleware: AuthMiddleware[F, admins.Admin] = 
        AuthMiddleware(authAdminCookie(), onFailure())

      override def adminMiddleware(routes: AuthedRoutes[admins.Admin,F]): HttpRoutes[F] = 
        middleware(routes)
    })
}