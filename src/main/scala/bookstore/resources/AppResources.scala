package bookstore.resources

import cats.effect.kernel.Resource
import doobie.util.transactor._
import cats.effect.std._
import bookstore.config.types._
import cats.effect.kernel.Async
import cats.syntax.all._
import cats.implicits._
import cats.data._
import cats.Applicative
import cats.Monad
import cats.effect.kernel.Sync

sealed abstract class AppResources[F[_]] {
  def getPostgresTransactor(): F[Transactor[F]] 
  // TODO add redis resource
}

object AppResources {
  def make[F[_]: Async: Console](appConfig: AppConfig): F[AppResources[F]] = Async[F].pure(
    new AppResources[F] {
      val dbConfig = appConfig.dbConfig
      val httpConfig = appConfig.httpConfig
      override def getPostgresTransactor(): F[Transactor[F]] = 
        Async[F].pure(
          Transactor.fromDriverManager[F](
            dbConfig.driver,
            dbConfig.dbName,
            dbConfig.userName,
            dbConfig.password
          )
        )
    }
  )
}
