import sbt._

object Dependencies {

  object Versions {
    val http4sVersion: String = "0.23.1"
    val doobieVersion: String = "1.0.0-RC1"
    val redisVersion: String = "1.1.1"
    val weaverVersion: String = "0.7.11"
    val jwtAuthVersion: String = "1.0.0"
    val newTypeVersion: String = "0.4.4"
  }

  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.2.11"

  import Versions._

  lazy val http4s = Seq(
    "org.http4s" %% "http4s-dsl" % http4sVersion,
    "org.http4s" %% "http4s-ember-server" % http4sVersion,
    "org.http4s" %% "http4s-ember-client" % http4sVersion,
    "org.http4s" %% "http4s-server" % http4sVersion
  )

  lazy val pureConfig = Seq(
    "com.github.pureconfig" %% "pureconfig" % "0.17.1"
  )

  lazy val doobie = Seq(
    "org.tpolecat" %% "doobie-core"      % doobieVersion,
    "org.tpolecat" %% "doobie-h2"        % doobieVersion,          
    "org.tpolecat" %% "doobie-hikari"    % doobieVersion,         
    "org.tpolecat" %% "doobie-postgres"  % doobieVersion,          
    "org.tpolecat" %% "doobie-specs2"    % doobieVersion % "test",
    "org.tpolecat" %% "doobie-scalatest" % doobieVersion % "test"  
  )

  lazy val circe = Seq(
    "org.http4s" %% "http4s-circe" % http4sVersion,
    "io.circe" %% "circe-generic" % "0.14.1",
    "io.circe" %% "circe-literal" % "0.14.1",
    "io.circe" %% "circe-parser" % "0.14.1"
  )

  lazy val log4Cats = Seq(
    "org.typelevel" %% "log4cats-core"    % "2.2.0",  
    "org.typelevel" %% "log4cats-slf4j"   % "2.2.0",  
  )

  lazy val cookies = Seq(
   "org.reactormonk" %% "cryptobits" % "1.3"
  )

  lazy val redis = Seq(
    "dev.profunktor" %% "redis4cats-effects" % redisVersion,
    "dev.profunktor" %% "redis4cats-log4cats" % redisVersion
  ) 

  lazy val weaverTest = Seq( 
    "com.disneystreaming" %% "weaver-cats" % weaverVersion % Test,
    "com.disneystreaming" %% "weaver-scalacheck" % weaverVersion % Test
  )

  lazy val jwtAuth = Seq(
    "dev.profunktor" %% "http4s-jwt-auth" % jwtAuthVersion
  )

  lazy val newType = Seq(
    "io.estatico" %% "newtype" % newTypeVersion
  )
}
