package bookstore.http.auth

import bookstore.domain.tokens
import bookstore.domain.users._
import bookstore.http.auth.jwt.JwtExpire
import bookstore.http.auth.jwt.Tokens
import bookstore.services.Users
import bookstore.tokens.data
import cats.Monad
import cats.data.Kleisli
import cats.data.OptionT
import cats.effect.kernel.Async
import cats.syntax.all._
import dev.profunktor.auth
import dev.profunktor.auth.jwt._
import dev.profunktor.redis4cats.RedisCommands
import io.circe.Decoder
import io.circe.generic.semiauto._
import io.circe.parser.{decode => jsonDecode}
import io.circe.syntax._
import org.http4s.AuthedRoutes
import org.http4s.HttpRoutes
import org.http4s.Request
import org.http4s.Response
import org.http4s.ResponseCookie
import org.http4s.circe.CirceEntityDecoder._
import org.http4s.circe.CirceEntityEncoder._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.Cookie
import org.http4s.implicits._
import org.http4s.server.AuthMiddleware
import pdi.jwt.JwtAlgorithm
import pdi.jwt.JwtClaim
import pdi.jwt.exceptions.JwtNonStringException
import tokens._

sealed abstract class TokenAuth[F[_]: Monad: Async](
    usersService: Users[F],
    jwtExpire: JwtExpire[F],
    jwtConfig: tokens.JwtAccessTokenKeyConfig,
    exp: TokenExpiration
) extends Http4sDsl[F] {
  def authRoutes(authedRoutes: AuthedRoutes[User, F]): HttpRoutes[F]
}

object TokenAuth {

  def make[F[_]: Monad: Async](
      usersService: Users[F],
      jwtExpire: JwtExpire[F],
      jwtConfig: tokens.JwtAccessTokenKeyConfig,
      exp: tokens.TokenExpiration
  ): F[TokenAuth[F]] =
    Async[F].pure(
      new TokenAuth[F](usersService, jwtExpire, jwtConfig, exp) {
        override def authRoutes(
            authedRoutes: AuthedRoutes[User, F]
        ): HttpRoutes[F] = middleware(authedRoutes)

        private val jwtAuth = JwtAuth.hmac(jwtConfig.secret, JwtAlgorithm.HS256)

        val claimToUserConverter: data.Claim => Option[User] = claim =>
          Some(
            User(
              claim.userId.toLong,
              claim.username,
              claim.password,
              claim.firstName,
              claim.lastName,
              claim.email,
              claim.verified
            )
          )

        private val authenticate
            : auth.jwt.JwtToken => JwtClaim => F[Option[User]] =
          token =>
            claim =>
              jwtDecode[F](token, jwtAuth).flatMap { c =>
                Async[F]
                  .fromEither(jsonDecode[data.Claim](c.content))
                  .map(claim => claimToUserConverter(claim))
              }

        private val middleware =
          auth.JwtAuthMiddleware[F, User](jwtAuth, authenticate)

      }
    )
}
