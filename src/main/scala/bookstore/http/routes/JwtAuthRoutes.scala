package bookstore.http.routes

import cats.Monad
import cats.effect.kernel.Async
import bookstore.http.auth.jwt.Tokens
import org.http4s.dsl.Http4sDsl
import org.http4s.HttpRoutes
import bookstore.http.auth.jwt.JwtExpire
import bookstore.domain.tokens
import cats._
import cats.implicits._

import scala.concurrent.duration._
import io.circe.Encoder
import dev.profunktor.auth.jwt
import io.circe._
import io.circe.syntax._
import io.circe.generic.semiauto._
import org.http4s.circe._
import org.http4s.circe.CirceEntityEncoder._
import org.http4s.Header
import org.http4s.headers.Cookie
import fs2.compression.ZLibParams

final case class JwtAuthRoutes[F[_]: Monad: Async]()
    extends Http4sDsl[F] {

  implicit val tokenEncoder: Encoder[jwt.JwtToken] =
    Encoder.forProduct1("access_token")(_.value)

  val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / "getmejwt" =>
      for {
        jwte <- JwtExpire.make[F]
        config = tokens.JwtAccessTokenKeyConfig("secretkey")
        exp = tokens.TokenExpiration(20.days)
        token <- Tokens.make[F](jwte, config, exp).create
        resp <- Ok("Ok response").map(_.addCookie("jwtcookie", token.value))
      } yield resp
  }
}
