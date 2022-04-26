package bookstore.domain

import io.circe._
import io.circe.generic.semiauto._


object books {
  case class Book(bookId: Long,
                  title: String,
                  isbn: String, 
                  authorId: Long)

  implicit val bookDecoder: Decoder[Book] = deriveDecoder[Book]
  implicit val bookEncoder: Encoder[Book] = deriveEncoder[Book]

  case class BookOrder(bookId: Long, quantity: Int)

  implicit val bookOrderDecoder: Decoder[BookOrder] = deriveDecoder[BookOrder]
  implicit val bookOrderEncoder: Encoder[BookOrder] = deriveEncoder[BookOrder]
}
