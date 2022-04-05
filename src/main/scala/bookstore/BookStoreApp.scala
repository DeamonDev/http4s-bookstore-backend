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

object BookStoreApp extends IOApp.Simple {

  val forProgram = 
    for { 
      appConfig     <- Config.load[IO]
      appResources  <- AppResources.make[IO](appConfig)
      xa            <- appResources.getPostgresTransactor()
      i             <- 42.pure[ConnectionIO].transact(xa)
      _             <- IO.println(s"fetched: $i")
      j             <- "Papiesz-wapiesz".pure[ConnectionIO].transact(xa)
      _             <- IO.println(s"fetched: $j")
      _             <- HttpServer.make[IO](appConfig).use { _ =>
                         IO.never 
                       }
    } yield ()

  override def run: IO[Unit] = 
    forProgram
}

