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

object BookStoreApp extends IOApp.Simple {

  val forProgram = 
    for { 
      appConfig     <- Config.load[IO]
      appResources  <- AppResources.make[IO](appConfig)
      xa            <- appResources.getPostgresTransactor()
      httpRoutes     = HttpApi.make[IO](xa).routes
      _             <- HttpServer.make[IO](appConfig, httpRoutes).use { _ =>
                         IO.never 
                       }
    } yield ()

  override def run: IO[Unit] = 
    forProgram
}

