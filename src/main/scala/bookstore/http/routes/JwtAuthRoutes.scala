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
import bookstore.http.auth.TokenAuth
import org.http4s.AuthedRoutes
import bookstore.domain.users
import bookstore.services.Users
import cats.effect.kernel.Resource
import dev.profunktor.redis4cats.RedisCommands

final case class JwtAuthRoutes[F[_]: Monad: Async](
    tokenAuth: TokenAuth[F],
    usersService: Users[F],
    redisR: Resource[F, RedisCommands[F, String, String]]
) extends Http4sDsl[F] {

  val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / "getmejwt" =>
      for {
        jwte <- JwtExpire.make[F]
        config = tokens.JwtAccessTokenKeyConfig("secretkey")
        exp = tokens.TokenExpiration(20.days)
        claim = bookstore.tokens.data.Claim("100", "piotr")
        token <- Tokens.make[F](jwte, config, exp, claim).create
        resp <- Ok(
          s"Ok, created JWT token for you :) Bearerware!!! token: ${token.value}"
        )
      } yield resp

    /*
     In this method we should do the following steps:
     1) check if user not exist (we omit this step for simplicity)
     2) add user to database
     3) generate jwt token and send in in the request body (client will do what he want)
     4) add to the redis shopping cart
     */
    case req @ POST -> Root / "registration" =>
      ???
  }

  val authedRoutes: AuthedRoutes[users.User, F] =
    AuthedRoutes.of { case GET -> Root / "jwtdashboard" as user =>
      Ok(
        s"Welcome, ${user.username}. You are able to see this thanks to JWT technology."
      )
    }

  val authedHttpRoutes: HttpRoutes[F] = tokenAuth.authRoutes(authedRoutes)
}
