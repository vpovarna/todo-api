package org.example.todo

import scala.concurrent.ExecutionContext

import cats.data.Kleisli
import cats.effect._
import cats.effect.kernel.Resource
import doobie.ExecutionContexts
import doobie.util.transactor.Transactor
import org.example.todo.api.Apis
import org.example.todo.config.{AppConfig, Config}
import org.example.todo.db.Database
import org.example.todo.repository.Dao
import org.example.todo.service.Services
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.{Request, Response}

object HttpServer {

  def create(configFile: String = "application.conf"): IO[ExitCode] =
    resources(configFile).use(create)

  private def resources(configFile: String): Resource[IO, Resources] = for {
    config <- AppConfig.load(configFile)
    ec <- ExecutionContexts.fixedThreadPool[IO](config.database.threadPoolSize)
    xa <- Database.transactor(config.database, ec)
  } yield Resources(config, xa)

  private def create(resources: Resources): IO[ExitCode] =
    for {
      exitCode <- BlazeServerBuilder[IO](ExecutionContext.global)
        .bindHttp(
          port = resources.config.server.port,
          host = resources.config.server.host
        )
        .withHttpApp(httpApp = getHttpRoutes(resources.transactor))
        .serve
        .compile
        .lastOrError
    } yield exitCode

  // Wiring
  private def getHttpRoutes(
      xa: Transactor[IO]
  ): Kleisli[IO, Request[IO], Response[IO]] = {
    val dao: Dao = new Dao(xa)
    val services: Services = new Services(dao)
    val api: Apis = new Apis(services)
    api.httpRoutes
  }

  final case class Resources(config: Config, transactor: Transactor[IO])

}
