package example

import bookstore.domain.books._
import bookstore.http.routes.BookRoutes
import cats._
import cats.effect._
import cats.implicits._
import cats.instances.all._
import cats.syntax._
import cats.syntax.all._
import cats.syntax.show._
import example.HttpSuite
import io.circe.Decoder
import io.circe._
import io.circe.syntax._
import io.circe.generic.semiauto._
import org.http4s.Method._
import org.http4s._
import org.http4s.circe._
import org.http4s.client.dsl.io._
import org.http4s.syntax.literals._
import org.scalacheck.Gen

import generators._

object BookRoutesSuite extends HttpSuite {

  implicit val bookShow: Show[Book] = Show.show(b => b.title)

  test("GET all books [GET]") {
    forall(Gen.listOf(bookGen)) { books =>
      val req = GET(uri"/book")
      val routes = BookRoutes[IO](TestBooks.make(books)).httpRoutes
      expectHttpBodyAndStatus(routes, req)(books, Status.Ok)
    }
  }

  test("Get book by title and isbn [GET]") {
    forall(bookGen) { book =>
      val bookTitle = book.title
      val bookIsbn = book.isbn
      val req = GET(Uri.fromString(s"/book/$bookTitle/$bookIsbn").getOrElse(uri"/index"))
      val routes = BookRoutes[IO](TestBooks.make(List(book))).httpRoutes
      expectHttpBodyAndStatus(routes, req)(book, Status.Ok)
    }
  }

  test("Get book with optional author id parameter [GET]") {
    forall(bookGen) { book =>
      val authorId = book.authorId
      val req = GET(Uri.fromString(s"/book?author_id=$authorId").getOrElse(uri"/index"))
      val routes = BookRoutes[IO](TestBooks.make(List(book))).httpRoutes
      expectHttpBodyAndStatus(routes, req)(List(book), Status.Ok)
    }
  }

  test("Get limited number of books [GET]") {
    forall(Gen.listOf(bookGen)) { books =>
      val len = books.length
      val req = GET(Uri.fromString(s"/book?limit=$len").getOrElse(uri"/index"))
      val routes = BookRoutes[IO](TestBooks.make(books)).httpRoutes
      expectHttpBodyAndStatus(routes, req)(books, Status.Ok)
    }
  }

  test("X [POST]") {
    forall(bookGen) { book =>
      forall(Gen.listOf(bookGen)) { oldBooks =>
        val req = POST(book.asJson, uri"/book")
        val routes = BookRoutes[IO](TestBooks.make(oldBooks)).httpRoutes
        expectHttpStatus(routes, req)(Status.Created)
      }
    }
  }
}










