package bookstore.services

import bookstore.domain.books._
import cats.Monad
import cats.effect.kernel.Async
import cats.effect.kernel.Resource
import dev.profunktor.redis4cats.RedisCommands
import dev.profunktor.redis4cats.connection.RedisConnection
import cats.syntax.all._

trait ShoppingCarts[F[_]] {
  def create(cartId: Long, userId: String): F[Boolean]
  def getCartId(userId: String): F[String]
}

object ShoppingCarts {
  def make[F[_]: Monad: Async](
      redisR: Resource[F, RedisCommands[F, String, String]]
  ): F[ShoppingCarts[F]] =
    Async[F].pure(new ShoppingCarts[F] {
      override def create(cartId: Long, userId: String): F[Boolean] =
        redisR.use { redis =>
          redis.hSet(cartId.toString(), "dummy", "dummy") *> redis.hSet(
            userId,
            "cart_id",
            cartId.toString()
          )
        }

      override def getCartId(userId: String): F[String] =
        redisR.use { redis =>
          redis.hGet(userId, "cart_id").map(_.get)
        }
    })
}
