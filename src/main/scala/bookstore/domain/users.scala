package bookstore.domain

import io.circe._
import io.circe.generic.semiauto._

import org.http4s.circe._
import cats.effect.kernel.Async
import cats.Monad

object users { 

    implicit val userDecoder: Decoder[User] = deriveDecoder[User]
  implicit val userEncoder: Encoder[User] = deriveEncoder[User]

  case class User(userId: Long,
                  username: String,
                  password: String,
                  firstName: String,
                  lastName: String,
                  verified: Boolean)

  implicit val userRegistrationDecoder: Decoder[UserRegistration] = deriveDecoder[UserRegistration]
  implicit val userRegistrationEncoder: Encoder[UserRegistration] = deriveEncoder[UserRegistration]
//  implicit def decoder[F[_]](implicit F: Async[F]) = jsonOf[F, UserRegistration]

  case class UserRegistration(username: String, 
                              password: String,
                              firstName: String,
                              lastName: String,
                              email: String,
                              verified: Boolean)
}