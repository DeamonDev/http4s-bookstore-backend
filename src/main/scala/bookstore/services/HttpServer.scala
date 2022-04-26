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

import org.http4s.server.defaults.Banner
import org.typelevel.log4cats.Logger

import org.http4s.circe.CirceEntityCodec._

object HttpServer {

  private def showEmberBanner[F[_]: Logger](s: Server): F[Unit] =
    Logger[F].info(
      s"\n${Banner.mkString("\n")}\nHTTP Server started at ${s.address}"
    )

  def make[F[_]: Async: Logger](appCfg: AppConfig, routes: HttpApp[F]) =
    EmberServerBuilder
      .default[F]
      .withHost(Host.fromString(appCfg.httpConfig.host).get)
      .withPort(Port.fromString(appCfg.httpConfig.port).get)
      .withHttpApp(routes)
      .build
      .evalTap(server => showEmberBanner[F](server))
}
