package bookstore.http.routes

import bookstore.domain.authors._
import bookstore.services.Authors
import cats.Monad
import cats.effect._
import cats.implicits._
import doobie.util.transactor._
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe.CirceEntityCodec._
import org.http4s.dsl.Http4sDsl
import org.http4s.implicits._

final case class AuthorRoutes[F[_]: Monad: Async](authorsService: Authors[F])
    extends Http4sDsl[F] {
  val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / "welcome" / name =>
      Ok(s"Welcome, $name")
    case GET -> Root / "author" / firstName / lastName =>
      for {
        authorOption <- authorsService.find(firstName, lastName)
        author = authorOption match {
          case Some(author) => author
          case None         => Author(-1, "X", "Y")
        }
        response <- Ok(author.asJson)
      } yield response
  }
}
