package bookstore.domain

import io.circe.Decoder
import io.circe.Encoder

import io.circe._
import io.circe.generic.semiauto._

object admins {
  case class Admin(adminName: String, adminPass: String, adminId: Long)
  case class AdminLogin(adminName: String, adminPass: String)

  implicit val adminDecoder: Decoder[Admin] = deriveDecoder[Admin]
  implicit val adminEncoder: Encoder[Admin] = deriveEncoder[Admin]

  implicit val adminLoginDecoder: Decoder[AdminLogin] = deriveDecoder[AdminLogin]
  implicit val adminLoginEncoder: Encoder[AdminLogin] = deriveEncoder[AdminLogin]
}