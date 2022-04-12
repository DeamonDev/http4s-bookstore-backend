package bookstore.http

import cats.effect.kernel.Async
import cats.Monad
import doobie.util.transactor
import bookstore.http.routes.AuthorRoutes

object HttpApi {
  def make[F[_]: Monad: Async](
    postgres: transactor.Transactor[F]
  ): HttpApi[F] = 
    new HttpApi[F](postgres) {}
}

sealed abstract class HttpApi[F[_]: Monad: Async](
  postgres: transactor.Transactor[F]
) {
  private val authorRoutes = AuthorRoutes[F](postgres).httpRoutes

  val routes = authorRoutes
}