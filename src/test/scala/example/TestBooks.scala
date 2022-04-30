package example

import bookstore.services.Books
import cats.effect.IO
import bookstore.domain.books._

class TestBooks(books: List[Book]) extends Books[IO] {
  override def find(title: String, isbn: String): IO[Option[Book]] = ???
  override def findByAuthorId(authorId: Long): IO[List[Book]] = ???
  override def findAllBooks(): IO[List[Book]] = IO.pure(books)
  override def findNBooks(limit: Int): IO[List[Book]] = ???
  override def create(title: String, isbn: String, authorId: Long, quantity: Int, price: Double): IO[Int] = ???
}

object TestBooks {
  def make(books: List[Book]): TestBooks = new TestBooks(books)
}

