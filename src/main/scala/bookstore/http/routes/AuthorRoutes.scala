package bookstore.http.routes

import cats.implicits._
import io.circe.syntax._
import io.circe.generic.auto._
import org.http4s._

import org.http4s.dsl.Http4sDsl
import cats.effect._
import org.http4s.implicits._
import cats.Monad

import doobie.util.transactor._
import bookstore.domain.authors._

import org.http4s.circe.CirceEntityCodec._
import bookstore.services.Authors

 final case class AuthorRoutes[F[_]: Monad: Async](postgres: Transactor[F]) 
   extends Http4sDsl[F] {
    val httpRoutes: HttpApp[F] = HttpRoutes.of[F] {
      case GET -> Root / "welcome" / name => 
        Ok(s"Welcome, $name")
      case GET -> Root / "author" / firstName / lastName => 
        val t = for {
          authorService <- Authors.make[F](postgres)
          authorOption <- authorService.find(firstName, lastName)
          author = authorOption match {
                case Some(author) => author
                case None => Author(-1, "X", "Y")
               }
        } yield author.asJson

        Ok(t)
    }.orNotFound
  }