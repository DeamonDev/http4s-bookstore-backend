package bookstore.tokens

import cats.effect._
import dev.profunktor.auth.jwt._
import io.circe.Decoder
import io.circe.parser.{decode => jsonDecode}
import io.estatico.newtype.macros._
import pdi.jwt._
import io.circe.Encoder
import io.circe.Json

object TokenGenerator extends IOApp {
  import data._

  def putStrLn[A](a: A): IO[Unit] = IO(println(a))

  /* ---- Encoding stuff ---- */

  val claim = JwtClaim(
    """
        {"claim": "super-secret-claim"}
    """
  )

  val secretKey = JwtSecretKey("any-secret")
  val algo = JwtAlgorithm.HS256

  val mkToken: IO[JwtToken] =
    jwtEncode[IO](claim, secretKey, algo)

  /* ---- Decoding stuff ---- */
  val jwtAuth = JwtAuth.hmac(secretKey.value, algo)

  def decodeToken(token: JwtToken): IO[Claim] =
    jwtDecode[IO](token, jwtAuth).flatMap { c =>
      IO.fromEither(jsonDecode[Claim](c.content))
    }

  override def run(args: List[String]): IO[ExitCode] =
    for {
      t <- mkToken
      _ <- putStrLn(t)
      c <- decodeToken(t)
      _ <- putStrLn(c)
    } yield ExitCode.Success
}

object data {
  case class Claim(userId: String, username: String)

  object Claim {
    implicit val jsonDecoder: Decoder[Claim] =
      Decoder.forProduct2("user_id", "username")(Claim.apply)

    implicit val jsonEncoder: Encoder[Claim] =
      new Encoder[Claim] {
        final def apply(c: Claim) = Json.obj(
          ("user_id", Json.fromString(c.userId)),
          ("username", Json.fromString(c.username))
        )
      }
  }
}
