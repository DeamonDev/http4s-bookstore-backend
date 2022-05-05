package bookstore.http.auth.jwt

import dev.profunktor.auth.jwt._
import bookstore.effects.GenUUID
import cats.Monad
import bookstore.domain.tokens._
import io.circe.syntax._
import pdi.jwt.JwtAlgorithm
import cats.syntax.all._
import pdi.jwt.JwtClaim
import bookstore.tokens.data._
import java.util.Random

trait Tokens[F[_]] {
  def create: F[JwtToken]
}

object Tokens {
  def make[F[_]: GenUUID: Monad](
      jwtExpire: JwtExpire[F],
      config: JwtAccessTokenKeyConfig,
      exp: TokenExpiration,
      claim: Claim
  ): Tokens[F] =
    new Tokens[F] {

      override def create: F[JwtToken] =
        for {
          uuid <- GenUUID[F].make
          secretKey = JwtSecretKey(config.secret)
          token <- jwtEncode[F](JwtClaim(claim.asJson.toString()), secretKey, JwtAlgorithm.HS256)
        } yield token
    }
}
