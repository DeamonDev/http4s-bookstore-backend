package bookstore

import cats.effect._
import cats.implicits._
import dev.profunktor.redis4cats.Redis
import dev.profunktor.redis4cats.effect.Log.Stdout._
import dev.profunktor.redis4cats.RedisCommands

object RedisApp extends IOApp.Simple {

  case class ShoppingCart(cartId: Long, content: Map[Long, Int])

  trait ShoppingCarts {
    def create(cartId: Long): IO[Unit]
    def addItem(cartId: Long, itemId: Long, quantity: Int = 1): IO[Unit]
    def removeItem(itemId: Long, quantity: Int = 1): IO[Unit]
    def getCart(cartId: Long): IO[ShoppingCart]
  }

  object ShoppingCarts {
    def make(redis: RedisCommands[IO, String, String]): IO[ShoppingCarts] =
      IO.pure(new ShoppingCarts {
        override def create(cartId: Long): IO[Unit] =
          redis.hSet(cartId.toString(), Map("dummy" -> "dummy")).void

        override def addItem(
            cartId: Long,
            itemId: Long,
            quantity: Int
        ): IO[Unit] =
          redis
            .hSet(cartId.toString(), itemId.toString(), quantity.toString())
            .void

        override def removeItem(itemId: Long, quantity: Int): IO[Unit] = ???
        override def getCart(cartId: Long): IO[ShoppingCart] =
          for {
            keys <- redis.hKeys(cartId.toString()) 
            _ <- IO.println(keys.filter(_ != "dummy"))
            v <- keys.filter(_ != "dummy").traverse(key =>
              redis
                .hGet(cartId.toString(), key)
                .map(_.get)
                .map(x => (key.toLong, x.toInt))
            )
          } yield ShoppingCart(cartId, v.toMap)
      })
  }

  override def run: IO[Unit] =
    Redis[IO].utf8("redis://localhost").use { redis =>
      for {
        shoppingCarts <- ShoppingCarts.make(redis)
        _ <- shoppingCarts.addItem(52251, 88, 88)
        sc <- shoppingCarts.getCart(52251)
        _ <- IO.println(sc)
      } yield ()
    }
}
