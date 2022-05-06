package bookstore.services

import bookstore.domain.books._
import cats.Monad
import cats.effect.kernel.Async
import cats.effect.kernel.Resource
import dev.profunktor.redis4cats.RedisCommands
import dev.profunktor.redis4cats.connection.RedisConnection

trait ShoppingCarts[F[_]] {
  def create(cartId: Long): F[Boolean]
}

object ShoppingCarts {
  def make[F[_]: Monad: Async](
      redisR: Resource[F, RedisCommands[F, String, String]]
  ): F[ShoppingCarts[F]] =
    Async[F].pure(new ShoppingCarts[F] {
      override def create(cartId: Long): F[Boolean] = redisR.use { redis =>
        redis.hSet(cartId.toString(), "dummy", "dummy")
      }
    })
}
