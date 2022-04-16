package bookstore.http.routes

import cats.Monad
import cats.effect.kernel.Async
import doobie.util.transactor
import org.http4s.dsl.Http4sDsl
import org.http4s.HttpRoutes

import bookstore.http.auth.Auth._
import bookstore.http.auth.Auth


final case class AuthorizationRoutes[F[_]: Monad: Async](auth: Auth[F])
  extends Http4sDsl[F] {

    val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
      case req @ POST -> Root / "registration" => 
        auth.register().run(req)
      case req @ POST -> Root / "login" =>
        ???
    }

    
} 