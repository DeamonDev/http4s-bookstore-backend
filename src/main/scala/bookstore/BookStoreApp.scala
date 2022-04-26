package bookstore

import cats.effect.IOApp
import cats.effect.{ExitCode, IO}
import bookstore.config.Config
import bookstore.resources.AppResources

import cats._
import cats.effect._
import cats.implicits._

import doobie._
import doobie.implicits._

import scala.concurrent.duration._
import bookstore.services.HttpServer
import bookstore.services.Authors
import bookstore.http.HttpApi

import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import bookstore.services.Books

import org.http4s.implicits._
import org.http4s.server.middleware.CORS
import bookstore.http.auth.Auth
import bookstore.http.routes.AuthorizationRoutes
import bookstore.services.Users
import doobie.util.transactor
import bookstore.domain.users
import bookstore.http.routes.AdminRoutes
import bookstore.http.auth.AdminAuth
import bookstore.services.Admins
import org.http4s.server.Router

object BookStoreApp extends IOApp.Simple {

  implicit val logger = Slf4jLogger.getLogger[IO]

  val forProgram =
    for {
      appConfig <- Config.load[IO]
      appResources <- AppResources.make[IO](appConfig)
      transactor <- appResources.getPostgresTransactor()
      httpRoutes = HttpApi.make[IO](transactor).routes
      auth <- Auth.make[IO](transactor)
      registrationRoutes = AuthorizationRoutes[IO](auth).httpRoutes
      authedRoutes = AuthorizationRoutes[IO](auth).authedHttpRoutes
      adminAuth <- AdminAuth.make[IO](transactor)
      adminLoginRoutes = AdminRoutes[IO](adminAuth).httpRoutes
      adminAuthedRoutes = AdminRoutes[IO](adminAuth).authedHttpRoutes
      routed = Router(
        "/" -> (httpRoutes <+> registrationRoutes <+> authedRoutes),
        "admin" -> (adminLoginRoutes <+> adminAuthedRoutes)
      )
      _ <- HttpServer.make[IO](appConfig, CORS((routed).orNotFound)).use { _ =>
        IO.never
      }
    } yield ()

  override def run: IO[Unit] =
    forProgram
}
