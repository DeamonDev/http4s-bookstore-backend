package bookstore.http.routes

import bookstore.RedisApp
import bookstore.domain.tokens._
import bookstore.domain.users._
import bookstore.http.auth.TokenAuth
import bookstore.http.auth.jwt.JwtExpire
import bookstore.http.auth.jwt.Tokens
import bookstore.tokens.data._
import bookstore.services.ShoppingCarts
import bookstore.services.Users
import cats.Monad
import cats._
import cats.effect.kernel.Async
import cats.effect.kernel.Resource
import cats.implicits._
import dev.profunktor.auth.jwt
import dev.profunktor.redis4cats.RedisCommands
import io.circe.Encoder
import io.circe._
import io.circe.generic.semiauto._
import io.circe.syntax._
import org.http4s.AuthedRoutes
import org.http4s.Header
import org.http4s.Headers
import org.http4s.HttpRoutes
import org.http4s.Request
import org.http4s.Response
import org.http4s.Status
import org.http4s.circe.CirceEntityDecoder._
import org.http4s.circe.CirceEntityEncoder._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.Cookie

import scala.concurrent.duration._
import scala.util.Random
import bookstore.tokens

final case class JwtAuthRoutes[F[_]: Monad: Async](
    tokenAuth: TokenAuth[F],
    usersService: Users[F],
    redisR: Resource[F, RedisCommands[F, String, String]],
    shoppingCarts: ShoppingCarts[F]
) extends Http4sDsl[F] {

  val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    /*
     In this method we should do the following steps:
     1) check if user not exist (we omit this step for simplicity)
     2) add user to database
     3) generate jwt token and send in in the request body (client will do what he want)
     4) add to the redis shopping cart
     */
    case req @ POST -> Root / "registration" =>
      for {
        userRegistration <- req.as[UserRegistration]
        username = userRegistration.username
        password = userRegistration.password
        firstName = userRegistration.firstName
        lastName = userRegistration.lastName
        email = userRegistration.email
        user <- usersService.create(
          username,
          password,
          firstName,
          lastName,
          email,
          false
        )
        userId = user.userId
        cartId = Random.nextLong()
        _ <- shoppingCarts.create(cartId, userId.toString())
        claim = Claim(
          user.userId.toString(),
          username,
          password,
          firstName,
          lastName,
          email,
          false,
          cartId.toString()
        )
        token <- Tokens.make[F](claim = claim).create
      } yield Response[F](
        status = Status.Created,
        headers = Headers("X-Auth-Token" -> token.value)
      )

    case req @ POST -> Root / "login" =>
      for {
        userLogin <- req.as[UserLogin]
        user <- usersService.findByUserName(userLogin.username).map(_.get)
        userId = user.userId
        username = user.username
        password = user.password
        firstName = user.firstName
        lastName = user.lastName
        email = user.email
        verified = user.verified
        cartId <- shoppingCarts.getCartId(userId.toString())
        claim = Claim(
          userId.toString(),
          username,
          password,
          firstName,
          lastName,
          email,
          verified,
          cartId
        )
        token <- Tokens.make[F](claim = claim).create
      } yield Response[F](
        status = Status.Ok,
        headers = Headers("X-Auth-Token" -> token.value)
      )
  }

  val authedRoutes: AuthedRoutes[User, F] =
    AuthedRoutes.of { case GET -> Root / "jwtdashboard" as user =>
      Ok(
        s"Welcome, ${user.username}. You are able to see this thanks to JWT technology."
      )
    }

  val authedHttpRoutes: HttpRoutes[F] = tokenAuth.authRoutes(authedRoutes)
}
