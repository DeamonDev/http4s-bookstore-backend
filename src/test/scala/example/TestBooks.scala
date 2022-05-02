package example

import bookstore.services.Books
import cats.effect.IO
import bookstore.domain.books._

class TestBooks(books: List[Book]) extends Books[IO] {
  override def find(title: String, isbn: String): IO[Option[Book]] = {
    books.filter(book => book.title == title && book.isbn == isbn) match {
      case List() => IO.pure(None)
      case b :: _ => IO.pure(Some(b))
    }
  }

  override def findByAuthorId(authorId: Long): IO[List[Book]] =
    IO.pure(books.filter(_.authorId == authorId))
  override def findAllBooks(): IO[List[Book]] = IO.pure(books)
  override def findNBooks(limit: Int): IO[List[Book]] =
    IO.pure(books.take(limit))

  override def checkIfBookExist(book: Book): IO[Boolean] = IO.pure(books.contains(book))

  override def create(
      title: String,
      isbn: String,
      authorId: Long,
      quantity: Int,
      price: Double
  ): IO[TestBooks] = IO.pure(
    TestBooks.make(books :+ Book(books.length + 1, title, isbn, authorId))
  )
}

object TestBooks {
  def make(books: List[Book]): TestBooks = new TestBooks(books)
}
