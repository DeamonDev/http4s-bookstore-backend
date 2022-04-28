package bookstore.http

import bookstore.http.auth.AdminAuth
import bookstore.http.auth.Auth
import cats.Monad
import cats.effect.kernel.Async
import bookstore.http.routes.AuthorizationRoutes
import org.http4s.HttpRoutes
import bookstore.http.routes.AdminRoutes

import cats.syntax.all._
import org.http4s._
import org.http4s.implicits._

sealed abstract class AuthedHttpApi[F[_]: Monad: Async](
    userAuth: Auth[F],
    adminAuth: AdminAuth[F]
) {

  private val _userRoutes: HttpRoutes[F] =
    AuthorizationRoutes[F](userAuth).httpRoutes
  private val _adminRoutes: HttpRoutes[F] = AdminRoutes[F](adminAuth).httpRoutes

  private val _authedUserRoutes: HttpRoutes[F] =
    AuthorizationRoutes[F](userAuth).authedHttpRoutes
  private val _authedAdminRoutes: HttpRoutes[F] =
    AdminRoutes[F](adminAuth).authedHttpRoutes

  val userRoutes: HttpRoutes[F] = _userRoutes <+> _authedUserRoutes
  val adminRoutes: HttpRoutes[F] = _adminRoutes <+> _authedAdminRoutes
}

object AuthedHttpApi {
  def make[F[_]: Monad: Async](
      userAuth: Auth[F],
      adminAuth: AdminAuth[F]
  ): AuthedHttpApi[F] =
    new AuthedHttpApi[F](userAuth, adminAuth) {}
}
