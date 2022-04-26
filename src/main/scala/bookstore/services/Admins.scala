package bookstore.services

import bookstore.domain.admins._
import cats.effect.kernel.Async
import cats.Monad
import doobie.util.transactor._
import doobie.util.query._
import doobie.implicits._

trait Admins[F[_]] {
  def findByAdminName(adminName: String): F[Option[Admin]]
  def findByAdminId(adminId: Long): F[Option[Admin]]
}

object Admins {
  import AdminsSql._

  def make[F[_]: Async: Monad](postgres: Transactor[F]): F[Admins[F]] =
    Async[F].pure(new Admins[F] {
      override def findByAdminName(adminName: String): F[Option[Admin]] =
        findAdminByNameQuery(adminName).option.transact(postgres)

      override def findByAdminId(adminId: Long): F[Option[Admin]] =
        findByAdminIdQuery(adminId).option.transact(postgres)
    })
}

object AdminsSql {
  def findAdminByNameQuery(adminName: String) =
    sql"SELECT * FROM admins WHERE admin_name = $adminName".query[Admin]

  def findByAdminIdQuery(adminId: Long) =
    sql"SELECT * FROM admins WHERE admin_id = $adminId".query[Admin]
}
