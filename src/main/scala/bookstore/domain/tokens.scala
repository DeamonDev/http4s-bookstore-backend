package bookstore.domain

import io.estatico.newtype.macros.newtype
import scala.concurrent.duration.FiniteDuration

object tokens {
  @newtype case class TokenExpiration(value: FiniteDuration)

  @newtype case class JwtAccessTokenKeyConfig(secret: String)
}

