package bookstore.domain

import bookstore.domain.books._

import io.circe._
import io.circe.generic.semiauto._

object shoppingcart {
  type BookId = Int

  case class ShoppingCart(id: Long, order: Map[String, String])

  implicit val shoppingCartDecoder: Decoder[ShoppingCart] = deriveDecoder[ShoppingCart]
  implicit val shoppingCartEncoder: Encoder[ShoppingCart] = deriveEncoder[ShoppingCart]
}
