package bookstore.config

import cats.effect.kernel.Sync
import cats.effect.syntax.all._
import cats.syntax.all._
import pureconfig._
import pureconfig.generic.auto._

import types._

object Config {
  def load[F[_]: Sync]: F[AppConfig] =
    for {
      httpConfigEither <- loadPart[F, HttpConfig]
      httpConfig <- Sync[F].fromEither(httpConfigEither)
      dbConfigEither <- loadPart[F, DbConfig]
      dbConfig <- Sync[F].fromEither(dbConfigEither)
      redisConfigEither <- loadPart[F, RedisConfig]
      redisConfig <- Sync[F].fromEither(redisConfigEither)
    } yield AppConfig(httpConfig, dbConfig, redisConfig)

  private def loadPart[F[_]: Sync, A](implicit reader: ConfigReader[A]) =
    Sync[F].pure(
      ConfigSource.default
        .load[A]
        .fold(
          configReaderFailures =>
            Left(new Throwable(configReaderFailures.toString())),
          Right(_)
        )
    )
}
