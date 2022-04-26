package bookstore.domain

object books {
  case class Book(bookId: Long,
                  title: String,
                  isbn: String, 
                  authorId: Long)
}
