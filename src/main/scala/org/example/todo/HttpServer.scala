package org.example.todo

import scala.concurrent.ExecutionContext

import cats.data.Kleisli
import cats.effect._
import doobie.util.transactor.Transactor
import org.example.todo.api.Apis
import org.example.todo.config.Config
import org.example.todo.repository.Dao
import org.example.todo.resources.ResourcesFactory.{Resources, getResources}
import org.example.todo.service.Services
import org.http4s.metrics.MetricsOps
import org.http4s.metrics.prometheus.PrometheusExportService
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.{Request, Response}

object HttpServer {

  def create(configFile: String = "application.conf"): IO[ExitCode] =
    getResources(configFile).use(create)

  def create(config: Config): IO[ExitCode] =
    getResources(config).use(create)

  private def create(resources: Resources): IO[ExitCode] =
    for {
      exitCode <- BlazeServerBuilder[IO](ExecutionContext.global)
        .bindHttp(
          port = resources.config.server.port,
          host = resources.config.server.host
        )
        .withHttpApp(httpApp =
          getHttpRoutes(resources.transactor, resources.metricsComponents)
        )
        .serve
        .compile
        .lastOrError
    } yield exitCode

  // Wiring
  private def getHttpRoutes(
      xa: Transactor[IO],
      metricsResources: MetricsComponents
  ): Kleisli[IO, Request[IO], Response[IO]] = {
    val dao: Dao = new Dao(xa)
    val services: Services = new Services(dao)
    val api: Apis = new Apis(services, metricsResources)
    api.httpRoutes
  }

  final case class MetricsComponents(
      metricsOps: MetricsOps[IO],
      metricsService: PrometheusExportService[IO]
  )
}
