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

  test("GET brands success") {
    forall(Gen.listOf(bookGen)) { b =>
      val req = GET(uri"/book")
      val routes = BookRoutes[IO](TestBooks.make(b)).httpRoutes
      expectHttpBodyAndStatus(routes, req)(b, Status.Ok)
    }
  }

}
