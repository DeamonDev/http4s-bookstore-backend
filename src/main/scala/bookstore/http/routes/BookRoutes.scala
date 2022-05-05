package bookstore.http.routes

import bookstore.domain.books._
import bookstore.services.Authors
import bookstore.services.Books
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

import QueryParamMatchers._

final case class BookRoutes[F[_]: Monad: Async](booksService: Books[F])
    extends Http4sDsl[F] {

  val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / "book" :? LimitQueryParamMatcher(limit) =>
      for {
        books <- booksService.findNBooks(limit)
        response <- Ok(books.asJson)
      } yield response


    case GET -> Root / "book" :? AuthorQueryParamMatcher(authorId) =>
      for {
        listOfBooks <- booksService.findByAuthorId(authorId)
        response <- listOfBooks.length match {
          case 0 => NotFound()
          case _ => Ok(listOfBooks.asJson)
        }
      } yield response

    case GET -> Root / "book" =>
      for {
        allBooks <- booksService.findAllBooks()
        response <- Ok(allBooks.asJson)
      } yield response

    case GET -> Root / "book" / title / isbn =>
      for {
        bookOption <- booksService.find(title, isbn)
        response <- bookOption match {
          case Some(book) => Ok(book.asJson)
          case None       => NotFound()
        }
      } yield response

    case req @ POST -> Root / "book" =>
      for {
        newBook <- req.as[Book]
        exists <- booksService.checkIfBookExist(newBook)
        response =
          if (!exists) Response[F](status = Status.Created)
          else Response[F](status = Status.NotModified)
      } yield response
  }
}
