package bookstore.services

import bookstore.domain.books._
import cats.Monad
import doobie.util.transactor._
import cats.effect.IO
import doobie.implicits._
import cats.implicits._
import doobie.util.ExecutionContexts
import cats.effect.kernel.Async
import fs2.Stream
import doobie.util.query.Query0
import doobie.util.pos


trait Books[F[_]] { 
  def find(title: String, isbn: String): F[Option[Book]]
  def findByAuthorId(authorId: Long): F[List[Book]]
  def findAllBooks(): F[List[Book]]
  def findNBooks(limit: Int): F[List[Book]]
  def create(title: String, isbn: String, authorId: Long): F[Int]
}

object Books {
  import BooksSql._

  def make[F[_]: Monad: Async](postgres: Transactor[F]): F[Books[F]] = 
    Async[F].pure(new Books[F] {

      private def getCurrentIndex() = 
        currentIndexQuery.unique.transact(postgres)
      
      override def findAllBooks(): F[List[Book]] = 
        findAllBoksQuery.to[List].transact(postgres)

      override def findNBooks(limit: Int): F[List[Book]] =
        findNBooksQuery(limit).to[List].transact(postgres)
      override def find(title: String, isbn: String): F[Option[Book]] = 
       selectBook(title, isbn).option.transact(postgres)

      override def findByAuthorId(authorId: Long): F[List[Book]] = 
        findByAuthorIdQuery(authorId).to[List].transact(postgres)

      override def create(title: String, isbn: String, authorId: Long): F[Int] =
        for {
          currentIndex <- getCurrentIndex()
          b            <-  createBook(currentIndex + 1, title, isbn, authorId).run.transact(postgres)
        } yield b
    })
}

private object BooksSql {

  val currentIndexQuery: Query0[Int] = 
    sql"SELECT book_id FROM books ORDER BY book_id DESC LIMIT 1".query[Int]

  val findAllBoksQuery: Query0[Book] = 
    sql"SELECT * FROM books".query[Book]

  def findNBooksQuery(limit: Int): Query0[Book] = 
    sql"SELECT * FROM books LIMIT $limit".query[Book]

  def selectBook(title: String, isbn: String) = 
    sql"""SELECT book_id, title, isbn, author_id FROM books
       WHERE title = $title AND isbn = $isbn""".query[Book]

  def findByAuthorIdQuery(authorId: Long) = 
    sql"""SELECT book_id, title, isbn, author_id 
          FROM books b
          WHERE b.author_id = $authorId""".query[Book]

  def createBook(currentIndex: Int, title: String, isbn: String, authorId: Long) = 
    sql"""INSERT INTO books (book_id, title, isbn, author_id) 
          VALUES ($currentIndex, $title, $isbn, $authorId)""".update
      

}