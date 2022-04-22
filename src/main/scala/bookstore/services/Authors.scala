package bookstore.services

import bookstore.domain.authors._
import cats.Monad
import doobie.util.transactor._
import cats.effect.IO
import doobie.implicits._
import cats.implicits._
import doobie.util.ExecutionContexts
import cats.effect.kernel.Async
import fs2.Stream
import doobie.util.query.Query0


trait Authors[F[_]] { 
  def find(firstName: String, lastName: String): F[Option[Author]]
  def create(firstName: String, lastName: String): F[Int]
}

object Authors {
  import AuthorsSql._

  def make[F[_]: Monad: Async](postgres: Transactor[F]): F[Authors[F]] = 
    Async[F].pure(new Authors[F] {

      // comment!

      private def getCurrentIndex() = 
        currentIndexQuery.unique.transact(postgres)

      override def find(firstName: String, lastName: String): F[Option[Author]] = 
       selectUser(firstName, lastName).option.transact(postgres)
      override def create(firstName: String, lastName: String): F[Int] =
        for {
          currentIndex <- getCurrentIndex()
          u            <-  createUser(currentIndex + 1, firstName, lastName).run.transact(postgres)
        } yield u
    })
}

private object AuthorsSql {

  val currentIndexQuery: Query0[Int] =
    sql"SELECT author_id FROM authors ORDER BY author_id DESC LIMIT 1".query[Int]

  def selectUser(firstName: String, lastName: String) = 
    sql"""SELECT author_id, first_name, last_name FROM authors 
       WHERE first_name = $firstName AND last_name = $lastName""".query[Author]

  def createUser(currentIndex: Int, firstName: String, lastName: String) = 
    sql"""INSERT INTO authors (author_id, first_name, last_name) 
          VALUES ($currentIndex, $firstName, $lastName)""".update
}
