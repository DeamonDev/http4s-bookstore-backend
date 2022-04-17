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

object BookStoreApp extends IOApp.Simple {

  implicit val logger = Slf4jLogger.getLogger[IO]

  val forProgram = 
    for { 
      appConfig     <- Config.load[IO]
      appResources  <- AppResources.make[IO](appConfig)
      transactor    <- appResources.getPostgresTransactor()
      httpRoutes     = HttpApi.make[IO](transactor).routes
      auth          <- Auth.make[IO](transactor)
      authRoutes     = AuthorizationRoutes[IO](auth).httpRoutes
      usersService <- Users.make[IO](transactor)
      u <- usersService.create("AA", "BB", "CC", "DD", "FF", false)
      _ <- IO.println(u)
      _             <- HttpServer.make[IO](appConfig, CORS((authRoutes <+> httpRoutes).orNotFound)).use { _ =>
                         IO.never 
                       }
    } yield ()

  override def run: IO[Unit] = 
    forProgram
}

