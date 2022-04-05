package bookstore

import cats.effect.IOApp
import cats.effect.{ExitCode, IO}
import bookstore.config.Config

object BookStoreApp extends IOApp.Simple {

  val program: IO[Unit] = 
    Config.load[IO].map(appConfig => 
      println(appConfig.toString()))

  override def run: IO[Unit] = 
    program
}

