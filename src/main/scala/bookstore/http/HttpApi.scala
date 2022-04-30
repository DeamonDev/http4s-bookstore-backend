package bookstore.http

import cats.effect.kernel.Async
import cats.Monad
import doobie.util.transactor._
import bookstore.http.routes.AuthorRoutes
import bookstore.http.routes.BookRoutes
import doobie.util.pos

import cats.syntax.all._
import org.http4s._
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.middleware._
import bookstore.http.routes.AuthorizationRoutes
import bookstore.services.Books
import bookstore.services.Authors

object HttpApi {
  def make[F[_]: Monad: Async](
    postgres: Transactor[F],
    authorsService: Authors[F],
    booksService: Books[F]
  ): HttpApi[F] =
    new HttpApi[F](postgres, authorsService, booksService) {}
}

sealed abstract class HttpApi[F[_]: Monad: Async](
  postgres: Transactor[F],
  authorsService: Authors[F],
  booksService: Books[F]
) {
  private val authorRoutes: HttpRoutes[F] = AuthorRoutes[F](authorsService).httpRoutes
  private val bookRoutes: HttpRoutes[F] = BookRoutes[F](booksService).httpRoutes

  val routes: HttpRoutes[F] = authorRoutes <+> bookRoutes

}
