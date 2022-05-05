package bookstore.domain

import io.estatico.newtype.macros.newtype
import scala.concurrent.duration.FiniteDuration
import io.circe.Encoder
import dev.profunktor.auth.jwt

object tokens {
  @newtype case class TokenExpiration(value: FiniteDuration)

  implicit val tokenEncoder: Encoder[jwt.JwtToken] =
    Encoder.forProduct1("access_token")(_.value)

  @newtype case class JwtAccessTokenKeyConfig(secret: String)
}
