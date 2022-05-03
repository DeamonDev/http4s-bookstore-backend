package bookstore.http.routes

import cats.Monad
import cats.effect.kernel.Async
import doobie.util.transactor
import org.http4s.dsl.Http4sDsl
import org.http4s.HttpRoutes

import bookstore.http.auth.Auth._
import bookstore.http.auth.Auth
import org.http4s.AuthedRoutes
import bookstore.domain.users._

final case class AuthorizationRoutes[F[_]: Monad: Async](auth: Auth[F])
    extends Http4sDsl[F] {

  val authedRoutes: AuthedRoutes[User, F] =
    AuthedRoutes.of { case GET -> Root / "index" as user =>
      Ok(s"Welcome, ${user.firstName}. It's nice to see you here!")
    }

  val authedHttpRoutes: HttpRoutes[F] = auth.authRoutes(authedRoutes)

  val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case req @ POST -> Root / "registration" =>
      auth.register().run(req)
    case req @ POST -> Root / "login" =>
      ???
  }
}
