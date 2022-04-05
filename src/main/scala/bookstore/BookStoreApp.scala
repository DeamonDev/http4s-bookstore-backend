package bookstore

import cats.effect.IOApp
import cats.effect.{ExitCode, IO}
import bookstore.config.Config

object BookStoreApp extends IOApp.Simple {

  val simplestProgram: IO[Unit] = 
    for {
      _     <- IO.print("Hello, what's your name? ")
      name  <- IO.readLine
      _     <- IO.println(s"Welcome $name") 
    } yield ()

  val program: IO[Unit] = 
    Config.load[IO].map(appConfig => 
      println(appConfig.toString()))

  override def run: IO[Unit] = 
    program
}

