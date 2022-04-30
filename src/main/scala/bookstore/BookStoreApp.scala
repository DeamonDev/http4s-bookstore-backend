package bookstore

import bookstore.config.Config
import bookstore.domain.users
import bookstore.http.HttpApi
import bookstore.http.auth.AdminAuth
import bookstore.http.auth.Auth
import bookstore.http.routes.AdminRoutes
import bookstore.http.routes.AuthorizationRoutes
import bookstore.resources.AppResources
import bookstore.services.Admins
import bookstore.services.Authors
import bookstore.services.Books
import bookstore.services.HttpServer
import bookstore.services.Users
import cats._
import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
import cats.effect._
import cats.implicits._
import dev.profunktor.redis4cats.effect.MkRedis
import dev.profunktor.redis4cats.log4cats._
import doobie._
import doobie.implicits._
import doobie.util.transactor
import org.http4s.client.middleware
import org.http4s.implicits._
import org.http4s.server
import org.http4s.server.Router
import org.http4s.server.middleware.CORS
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

import scala.concurrent.duration._
import bookstore.http.AuthedHttpApi

object BookStoreApp extends IOApp.Simple {

  implicit val logger = Slf4jLogger.getLogger[IO]

  val forProgram =
    for {
      appConfig <- Config.load[IO]
      appResources <- AppResources.make[IO](appConfig)
      transactor <- appResources.getPostgresTransactor()
      redisCommandsR <- appResources.getRedisCommands()
      _ <- redisCommandsR.use { redisCommands =>
        redisCommands.set("book_store_app", "ON")
      }
      booksService <- Books.make[IO](transactor)
      authorsService <- Authors.make[IO](transactor)
      httpRoutes = HttpApi.make[IO](transactor, authorsService, booksService).routes
      auth <- Auth.make[IO](transactor)
      adminAuth <- AdminAuth.make[IO](transactor)
      userRoutes = AuthedHttpApi.make[IO](auth, adminAuth).userRoutes
      adminRoutes = AuthedHttpApi.make[IO](auth, adminAuth).adminRoutes
      routed = Router(
        "/" -> (httpRoutes <+> userRoutes),
        "admin" -> adminRoutes)
      _ <- HttpServer
        .make[IO](
          appConfig,
          CORS(
            server.middleware.Logger
              .httpApp(logHeaders = true, logBody = true)(routed.orNotFound)
          )
        )
        .use { _ =>
          IO.never
        }
    } yield ()

  override def run: IO[Unit] =
    forProgram
}
