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
      httpConfigEither   <- load[F, HttpConfig]
      httpConfig         <- Sync[F].fromEither(httpConfigEither)
      dbConfigEither     <- load[F, DbConfig]
      dbConfig           <- Sync[F].fromEither(dbConfigEither)
    } yield AppConfig(httpConfig, dbConfig)

  private def load[F[_]: Sync, A](implicit reader: ConfigReader[A]) = 
    Sync[F].pure(
      ConfigSource.default
                  .load[A]
                  .fold(crf => Left(new Throwable(crf.toString())),
                        Right(_))
    )
}