package bookstore.services

import bookstore.domain.books._
import cats.Monad
import cats.effect.kernel.Async
import cats.effect.kernel.Resource
import dev.profunktor.redis4cats.RedisCommands
import dev.profunktor.redis4cats.connection.RedisConnection

trait ShoppingCarts[F[_]] {
  def create(userId: Long, bookOrders: List[BookOrder] = List.empty[BookOrder])
}

object ShoppingCarts {
  def make[F[_]: Monad: Async](
      redis: Resource[F, RedisCommands[F, String, String]]
  ): F[ShoppingCarts[F]] =
    Async[F].pure(new ShoppingCarts[F] {
      override def create(userId: Long, bookOrders: List[BookOrder]): Unit = ???

    })
}
