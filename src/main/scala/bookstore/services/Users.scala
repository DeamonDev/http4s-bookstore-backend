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
  def findUserById(userId: Long): F[Option[User]]
}

object Users {
  import UsersSql._

  def make[F[_]: Async: Monad](postgres: Transactor[F]): F[Users[F]] = 
    Async[F].pure(new Users[F] {
      override def findUserById(userId: Long): F[Option[User]] = 
        findUserByIdQuery(userId).option.transact(postgres)
    })
}

private object UsersSql {
  def findUserByIdQuery(userId: Long) = 
    sql"""" SELECT * 
            FROM users
            WHERE user_id = $userId""".query[User]
      
}