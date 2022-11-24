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
import org.http4s.metrics.MetricsOps
import org.http4s.metrics.prometheus.{Prometheus, PrometheusExportService}
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.{HttpRoutes, Request, Response}

object HttpServer {

  def create(configFile: String = "application.conf"): IO[ExitCode] =
    resources(configFile).use(create)

  private def resources(configFile: String): Resource[IO, Resources] = for {
    config <- AppConfig.load(configFile)
    ec <- ExecutionContexts.fixedThreadPool[IO](config.database.threadPoolSize)
    xa <- Database.transactor(config.database, ec)
    metricsService <- PrometheusExportService.build[IO]
    metrics <- Prometheus.metricsOps[IO](
      metricsService.collectorRegistry,
      config.metrics.prefixName
    )
  } yield Resources(config, xa, MetricsComponents(metrics, metricsService))

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
    val metricsRoutes: HttpRoutes[IO] = metricsResources.metricsService.routes
    val todoHttpRoutes: Kleisli[IO, Request[IO], Response[IO]] = api.httpRoutes
    todoHttpRoutes
  }

  final case class Resources(
      config: Config,
      transactor: Transactor[IO],
      metricsComponents: MetricsComponents
  )

  final case class MetricsComponents(
      metricsOps: MetricsOps[IO],
      metricsService: PrometheusExportService[IO]
  )

}
