package bookstore.services

/**
  * This is simple prototype - I wrapped 
  javax-mailer Future[_] to cats IO - monad IO[_]
  in order to guarantee referential transparency 
  and pure functional programming approach. One could 
  try to interchange this implementation with fs2-mail
  or other FP SMTP client 
  */

import cats.Monad
import cats.effect.kernel.Async

import javax.mail._
import javax.mail.internet._
import java.util.Date
import java.util.Properties
import scala.jdk.CollectionConverters

trait Email[F[_]] {
  def send(whom: String, content: String)
}

object Email {
  def make[F[_]: Monad: Async]: F[Email[F]] = 
    Async[F].pure(new Email[F] {
      override def send(whom: String, content: String): Unit = ???
    
    })  
}