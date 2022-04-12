package bookstore.services

import org.http4s.ember.server.EmberServerBuilder
import cats.effect.kernel.Async
import bookstore.config.types
import com.comcast.ip4s.Literals
import com.comcast.ip4s.Host
import com.comcast.ip4s.Port
import org.http4s.HttpRoutes
import cats.implicits._
import io.circe.syntax._
import io.circe.generic.auto._
import org.http4s.circe._
import org.http4s._
import org.http4s.implicits._
import io.circe.literal._

import org.http4s._
import org.http4s.dsl.io._
import org.http4s.dsl.Http4sDsl
import cats.effect._
import org.http4s.implicits._
import org.http4s.syntax._
import cats.Monad
import org.http4s.server.Server

import bookstore.config.types._
import doobie.util.transactor._
import bookstore.domain.authors._

import org.http4s.circe.CirceEntityCodec._

object HttpServer {

  case class PolymorphicRoutes[F[_]: Monad: Async](postgres: Transactor[F]) extends Http4sDsl[F] {
    val httpRoutes: HttpApp[F] = HttpRoutes.of[F] {
      case GET -> Root / "welcome" / name => 
        Ok(s"Welcome, $name")
      case GET -> Root / "json" / "test" => 
        Ok(Author(-1, "X", "Y").asJson)
      case GET -> Root / "author" / firstName / lastName => 
        val t = for {
          authorService <- Authors.make[F](postgres)
          authorOption <- authorService.find(firstName, lastName)
          author = authorOption match {
                case Some(author) => author
                case None => Author(-1, "X", "Y")
               }
        } yield author.asJson

        Ok(t)
    }.orNotFound
  }

  def make[F[_]: Async](appCfg: AppConfig, routes: HttpApp[F]) = 
    EmberServerBuilder
    .default[F]
    .withHost(Host.fromString(appCfg.httpConfig.host).get)
    .withPort(Port.fromString(appCfg.httpConfig.port).get)
    .withHttpApp(routes)
    .build
}