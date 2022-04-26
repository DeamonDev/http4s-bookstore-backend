package bookstore.http.routes

import cats.Monad
import cats.effect.kernel.Async
import doobie.util.transactor
import org.http4s.dsl.Http4sDsl
import org.http4s.HttpRoutes

import org.http4s.AuthedRoutes
import bookstore.domain.admins._
import bookstore.http.auth.AdminAuth

final case class AdminRoutes[F[_]: Monad: Async](adminAuth: AdminAuth[F])
    extends Http4sDsl[F] {

  val authedRoutes: AuthedRoutes[Admin, F] =
    AuthedRoutes.of { case GET -> Root / "dashboard" as admin =>
      Ok(s"Welcome, ${admin.adminName}. It's nice to see you here!")
    }

  val authedHttpRoutes: HttpRoutes[F] = adminAuth.adminMiddleware(authedRoutes)

  val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case req @ POST -> Root / "happyendpoint" / "login" =>
      adminAuth.login().run(req)
  }

}
