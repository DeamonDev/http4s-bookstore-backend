package bookstore.config

import types._

import pureconfig.ConfigSource
import pureconfig._
import pureconfig.generic.auto._
import cats.effect.kernel.Sync
import cats.syntax.all._
import cats.effect.syntax.all._

object Config {
  def load[F[_]: Sync]: F[AppConfig] = 
    for {
      httpConfigEither   <- Sync[F].pure(
        ConfigSource.default
                    .load[HttpConfig]
                    .fold(configReaderFailures => Left(new Throwable(configReaderFailures.toString())), 
                          Right(_)))
      httpConfig         <- Sync[F].fromEither(httpConfigEither)
      dbConfigEither     <- Sync[F].pure(
        ConfigSource.default
                    .load[DbConfig]
                    .fold(configReaderFailures => Left(new Throwable(configReaderFailures.toString())),
                    Right(_)))
      dbConfig           <- Sync[F].fromEither(dbConfigEither)
    } yield AppConfig(httpConfig, dbConfig)
}