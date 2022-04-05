import sbt._

object Dependencies {

  object Versions {
    val http4sVersion: String = "1.0.0-M23"
  }

  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.2.11"

  import Versions._

  lazy val http4s = Seq(
    "org.http4s" %% "http4s-dsl" % http4sVersion,
    "org.http4s" %% "http4s-ember-server" % http4sVersion,
    "org.http4s" %% "http4s-ember-client" % http4sVersion
  )
}
