package bookstore.http

import cats.effect.kernel.Async
import cats.Monad
import doobie.util.transactor
import bookstore.http.routes.AuthorRoutes
import bookstore.http.routes.BookRoutes
import doobie.util.pos

import cats.syntax.all._
import org.http4s._
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.middleware._
import bookstore.http.routes.AuthorizationRoutes

object HttpApi {
  def make[F[_]: Monad: Async](
    postgres: transactor.Transactor[F]
  ): HttpApi[F] = 
    new HttpApi[F](postgres) {}
}

sealed abstract class HttpApi[F[_]: Monad: Async](
  postgres: transactor.Transactor[F]
) {
  private val authorRoutes: HttpRoutes[F] = AuthorRoutes[F](postgres).httpRoutes
  private val bookRoutes: HttpRoutes[F] = BookRoutes[F](postgres).httpRoutes

  val routes = authorRoutes <+> bookRoutes 
}