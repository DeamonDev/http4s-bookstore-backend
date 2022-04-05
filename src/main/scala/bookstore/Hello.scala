package bookstore

import cats.effect.IOApp
import cats.effect.{ExitCode, IO}

object Hello extends IOApp.Simple {

  val simplestProgram: IO[Unit] = 
    for {
      _     <- IO.print("Hello, what's your name? ")
      name  <- IO.readLine
      _     <- IO.println(s"Welcome $name") 
    } yield ()
  override def run: IO[Unit] = 
    simplestProgram
}

