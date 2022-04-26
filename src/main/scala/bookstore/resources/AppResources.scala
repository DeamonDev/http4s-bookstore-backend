package bookstore.resources

import bookstore.config.types._
import cats.effect.kernel.Async
import cats.effect.kernel.Resource
import dev.profunktor.redis4cats.RedisCommands
import doobie.util.transactor._

sealed abstract class AppResources[F[_]] {
  def getPostgresTransactor(): F[Transactor[F]]
  // TODO add redis resource
  def getRedisCommands(): Resource[F, RedisCommands[F, String, String]]
}

object AppResources {
  def make[F[_]: Async](appConfig: AppConfig): F[AppResources[F]] =
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
            : Resource[F, RedisCommands[F, String, String]] = ???
      }
    )
}
