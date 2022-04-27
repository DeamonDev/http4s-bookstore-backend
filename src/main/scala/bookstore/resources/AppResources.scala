package bookstore.resources

import bookstore.config.types._
import cats.effect.kernel.Async
import cats.effect.kernel.Resource
import dev.profunktor.redis4cats.RedisCommands
import doobie.util.transactor._
import dev.profunktor.redis4cats.Redis
import org.log4s
import dev.profunktor.redis4cats.effect.MkRedis

sealed abstract class AppResources[F[_]] {
  // TODO it would be better to have F[Resource[F, Transactor[F]]]
  def getPostgresTransactor(): F[Transactor[F]]
  def getRedisCommands(): F[Resource[F, RedisCommands[F, String, String]]]
}

object AppResources {
  def make[F[_]: Async: MkRedis](appConfig: AppConfig): F[AppResources[F]] =
    Async[F].pure(
      new AppResources[F] {
        val dbConfig = appConfig.dbConfig
        val httpConfig = appConfig.httpConfig
        val redisConfig = appConfig.redisConfig
        override def getPostgresTransactor(): F[Transactor[F]] =
          Async[F].pure(
            Transactor.fromDriverManager[F](
              dbConfig.driver,
              dbConfig.dbName,
              dbConfig.userName,
              dbConfig.password
            )
          )
        override def getRedisCommands()
            : F[Resource[F, RedisCommands[F, String, String]]] =
          Async[F].pure(Redis[F].utf8(redisConfig.redisPort))
      }
    )
}
