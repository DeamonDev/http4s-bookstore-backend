package bookstore.services

import bookstore.domain.users._
import cats.Monad
import doobie.util.transactor._
import cats.effect.IO
import doobie.implicits._
import cats.implicits._
import doobie.util.ExecutionContexts
import cats.effect.kernel.Async
import fs2.Stream
import doobie.util.query.Query0
import doobie.util.pos


trait Users[F[_]] {
  def getCurrentIndex(): F[Int]
  def findUserById(userId: Long): F[Option[User]]
  def create(username: String,
             password: String, 
             firstName: String,
             lastName: String,
             email: String,
             verified: Boolean): F[Int] 
}

object Users {
  import UsersSql._

  def make[F[_]: Async: Monad](postgres: Transactor[F]): F[Users[F]] = 
    Async[F].pure(new Users[F] {
     override def getCurrentIndex() = 
        currentIndexQuery.unique.transact(postgres)
      override def findUserById(userId: Long): F[Option[User]] = 
        findUserByIdQuery(userId).option.transact(postgres)

      override def create(username: String, password: String, firstName: String, lastName: String, email: String, verified: Boolean): F[Int] = 
        for { 
          currentIndex <- getCurrentIndex()
          u            <- createQuery(currentIndex + 1, username, password, firstName, lastName, email, verified).run.transact(postgres)
        } yield u
    })
}

private object UsersSql {

  val currentIndexQuery: Query0[Int] = 
    sql"SELECT user_id FROM users ORDER BY user_id DESC LIMIT 1".query[Int]

  def findUserByIdQuery(userId: Long) = 
    sql"""SELECT user_id, username, password, first_name, last_name, email, verified
            FROM users u 
            WHERE u.user_id = $userId""".query[User]

  def createQuery(currentIndex: Int, username: String, password: String, firstName: String, lastName: String, email: String, verified: Boolean) = 
    sql"""INSERT INTO users (user_id, username, password, first_name, last_name, email, verified)
          VALUES ($currentIndex, $username, $password, $firstName, $lastName, $email, $verified)""".update
      
}