package bookstore.domain

import io.circe._
import io.circe.generic.semiauto._

object authors {

  case class Author(authorId: Long, firstName: String, lastName: String)

  // implicit val authorDecoder: Decoder[Author] = deriveDecoder[Author]
  // implicit val authorEncoder: Encoder[Author] = deriveEncoder[Author]
}