package bookstore.services

import cats.effect.kernel.Async
import doobie.util.transactor._
import doobie.implicits._
import doobie.util.query._
import doobie.util.transactor._
import doobie.util.query.Query0

import cats.Monad
import cats.effect.IO
import cats.effect.kernel.Async
import cats.implicits._
import doobie.syntax.SqlInterpolator
import doobie.util.fragment._
import java.io.File
import scala.io.Source
import org.w3c.dom.DocumentFragment

trait DbInitializer[F[_]] {
  def initDb: F[Unit]
}

object DbInitializer {
  import DbInitializerSql._

  def make[F[_]: Async](postgres: Transactor[F]): F[DbInitializer[F]] =
    Async[F].pure(new DbInitializer[F] {
      override def initDb: F[Unit] =
        initDbQuery.unique.transact(postgres)
    })
}

object DbInitializerSql {

  val initDbQuery: Query0[Unit] = {
    val fileContents = Source.fromFile("./src/main/resources/init.sql").getLines().mkString

    Fragment.const(fileContents).query[Unit]
  }
}
