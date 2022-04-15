package bookstore.http.routes

import cats.Monad
import cats.effect.kernel.Async
import doobie.util.transactor
import org.http4s.dsl.Http4sDsl


final case class AuthorizationRoutes[F[_]: Monad: Async](postgres: transactor.Transactor[F])
  extends Http4sDsl[F] {
    
} 