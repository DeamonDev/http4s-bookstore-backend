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
import bookstore.domain.books._

import org.http4s.circe.CirceEntityCodec._
import bookstore.services.Authors
import bookstore.services.Books

import QueryParamMatchers._

final case class BookRoutes[F[_]: Monad: Async](postgres: Transactor[F])
    extends Http4sDsl[F] {

  val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / "book" :? LimitQueryParamMatcher(limit) =>
      for {
        bookService <- Books.make[F](postgres)
        books <- bookService.findNBooks(limit)
        response <- Ok(books.asJson)
      } yield response

    case GET -> Root / "book" =>
      for {
        bookService <- Books.make[F](postgres)
        allBooks <- bookService.findAllBooks()
        response <- Ok(allBooks.asJson)
      } yield response

    case GET -> Root / "book" :? AuthorQueryParamMatcher(authorId) =>
      for {
        bookService <- Books.make[F](postgres)
        listOfBooks <- bookService.findByAuthorId(authorId)
        response <- listOfBooks.length match {
          case 0 => NotFound()
          case _ => Ok(listOfBooks.asJson)
        }
      } yield response

    case GET -> Root / "book" / title / isbn =>
      for {
        bookService <- Books.make[F](postgres)
        bookOption <- bookService.find(title, isbn)
        response <- bookOption match {
          case Some(book) => Ok(book.asJson)
          case None       => NotFound()
        }
      } yield response
  }
}
