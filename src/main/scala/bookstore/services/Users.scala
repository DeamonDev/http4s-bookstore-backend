package bookstore.services

import bookstore.domain.users._
import cats.Monad
import cats.effect.IO
import cats.effect.kernel.Async
import cats.implicits._
import doobie.implicits._
import doobie.util.ExecutionContexts
import doobie.util.pos
import doobie.util.query.Query0
import doobie.util.transactor._
import fs2.Stream

trait Users[F[_]] {
  def getCurrentIndex(): F[Int]
  def findUserById(userId: Long): F[Option[User]]
  def findByUserName(username: String): F[Option[User]]
  def create(
      username: String,
      password: String,
      firstName: String,
      lastName: String,
      email: String,
      verified: Boolean
  ): F[User]
}

object Users {
  import UsersSql._

  def make[F[_]: Async: Monad](postgres: Transactor[F]): F[Users[F]] =
    Async[F].pure(new Users[F] {
      override def getCurrentIndex() =
        currentIndexQuery.unique.transact(postgres)
      override def findUserById(userId: Long): F[Option[User]] =
        findUserByIdQuery(userId).option.transact(postgres)

      override def findByUserName(username: String): F[Option[User]] =
        findByUserNameQuery(username).option.transact(postgres)
      override def create(
          username: String,
          password: String,
          firstName: String,
          lastName: String,
          email: String,
          verified: Boolean
      ) = {
        // by monadically stacking these ConnectionIO's we can do
        // single SQL-transaction in the return statement.
        // One can also use SMT for more advanced transactions.
        // See 4 example: https://timwspence.github.io/cats-stm/
        val userConnectionIO = for {
          lv <- currentIndexQuery.unique
          _ <- createQuery(
            lv.toInt + 1,
            username,
            password,
            firstName,
            lastName,
            email,
            verified
          ).run
          u <- findUserByIdQuery(lv + 1).unique
        } yield u

        userConnectionIO.transact(postgres)
      }
    })
}

private object UsersSql {

  val lastValQuery: Query0[Long] =
    sql"SELECT lastval()".query[Long]

  val currentIndexQuery: Query0[Int] =
    sql"SELECT user_id FROM users ORDER BY user_id DESC LIMIT 1".query[Int]

  def findUserByIdQuery(userId: Long) =
    sql"""SELECT user_id, username, password, first_name, last_name, email, verified
            FROM users u 
            WHERE u.user_id = $userId""".query[User]

  def findByUserNameQuery(username: String) =
    sql"SELECT * FROM users WHERE username = '$username'".query[User]

  def createQuery(
      currentIndex: Int,
      username: String,
      password: String,
      firstName: String,
      lastName: String,
      email: String,
      verified: Boolean
  ) =
    sql"""INSERT INTO users (user_id, username, password, first_name, last_name, email, verified)
          VALUES ($currentIndex, $username, $password, $firstName, $lastName, $email, $verified)""".update

}
