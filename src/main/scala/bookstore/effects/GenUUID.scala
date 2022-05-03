package bookstore.effects

import java.util.UUID
import cats.effect.kernel.Sync
import cats.ApplicativeThrow

trait GenUUID[F[_]] {
  def make: F[UUID]
  def read(str: String): F[UUID]
}

object GenUUID {
  def apply[F[_]: GenUUID]: GenUUID[F] = implicitly

  implicit def forSync[F[_]: Sync]: GenUUID[F] =
    new GenUUID[F] {
      override def make: F[UUID] = Sync[F].delay(UUID.randomUUID())

      override def read(str: String): F[UUID] =
        ApplicativeThrow[F].catchNonFatal(UUID.fromString(str))
    }
}

