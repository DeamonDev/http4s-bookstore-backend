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

sealed abstract class Auth[F[_]: Monad: Async](
  postgres: Transactor[F]
) { 
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

      override def verifyRegistration(request: Request[F]): F[Either[String,User]] = 
        request.as[UserRegistration].flatMap { userRegistration =>
          // TODO connect with postgres and check 
          Async[F].pure(Right(User(1, "JP2", "vatican", "Karol", "Wojtyla", false)))
        }

      override def authUserCookie(): Kleisli[F, Request[F], Either[String, User]] = ???
      override def register(): Kleisli[F, Request[F], Response[F]] = ???

      override def onFailure(): AuthedRoutes[String, F] = ???

      val middleware: AuthMiddleware[F, User] = 
       AuthMiddleware(authUserCookie(), onFailure())
      
    }
}