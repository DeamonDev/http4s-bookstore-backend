package bookstore.services

import org.http4s.ember.server.EmberServerBuilder
import cats.effect.kernel.Async
import bookstore.config.types
import com.comcast.ip4s.Literals
import com.comcast.ip4s.Host
import com.comcast.ip4s.Port
import org.http4s.HttpRoutes

import org.http4s._
import org.http4s.dsl.io._
import org.http4s.dsl.Http4sDsl
import cats.effect._
import org.http4s.implicits._
import org.http4s.syntax._
import cats.Monad
import org.http4s.server.Server

object HttpServer {

  case class PolymorphicRoutes[F[_]: Monad]() extends Http4sDsl[F] {
    val httpRoutes: HttpApp[F] = HttpRoutes.of[F] {
      case GET -> Root / "welcome" / name => 
        Ok(s"Welcome, $name")
    }.orNotFound
  }

  def make[F[_]: Async](appCfg: types.AppConfig) = 
    EmberServerBuilder
    .default[F]
    .withHost(Host.fromString(appCfg.httpConfig.host).get)
    .withPort(Port.fromString(appCfg.httpConfig.port).get)
    .withHttpApp(PolymorphicRoutes[F]().httpRoutes)
    .build
}