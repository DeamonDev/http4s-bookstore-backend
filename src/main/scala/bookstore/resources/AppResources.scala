package bookstore.resources

import doobie.util.transactor._
import bookstore.config.types._
import cats.effect.kernel.Async

sealed abstract class AppResources[F[_]] {
  def getPostgresTransactor(): F[Transactor[F]] 
  // TODO add redis resource
}

object AppResources {
  def make[F[_]: Async](appConfig: AppConfig): F[AppResources[F]] = Async[F].pure(
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
