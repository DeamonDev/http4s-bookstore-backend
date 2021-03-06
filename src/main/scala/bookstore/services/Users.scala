package bookstore.services

import bookstore.domain.users._
import cats.Monad
import cats.effect.IO
import cats.effect.kernel.Async
import cats.implicits._
import doobie.implicits._
import doobie.syntax._
import doobie.util.ExecutionContexts
import doobie.util.pos
import doobie.util.query.Query0
import doobie.postgres._
import fs2.Stream
import org.typelevel.log4cats._

import doobie._

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
  def create_v2(
      username: String,
      password: String,
      firstName: String,
      lastName: String,
      email: String,
      verified: Boolean
  ): F[Either[String, User]]
}

object Users {
  import UsersSql._

  def make[F[_]: Async: Monad: Logger](postgres: Transactor[F]): F[Users[F]] =
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
        // One can also use STM for more advanced transactions.
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
      override def create_v2(
          username: String,
          password: String,
          firstName: String,
          lastName: String,
          email: String,
          verified: Boolean
      ): F[Either[String, User]] = {
        val userConnectionIO = for {
          lv <- currentIndexQuery.unique
          e <- createQuery_v2(
            lv.toInt + 1,
            username,
            password,
            firstName,
            lastName,
            email,
            verified
          )
            .attemptSomeSqlState { case sqlstate.class23.UNIQUE_VIOLATION =>
              "user is already in the databse."
            }
        } yield e

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

  def createQuery_v2(
      currentIndex: Int,
      username: String,
      password: String,
      firstName: String,
      lastName: String,
      email: String,
      verified: Boolean
  ): ConnectionIO[User] =
    sql"""INSERT INTO users (user_id, username, password, first_name, last_name, email, verified)
            VALUES ($currentIndex, $username, $password, $firstName, $lastName, $email, $verified)""".update
      .withUniqueGeneratedKeys("username")

}
