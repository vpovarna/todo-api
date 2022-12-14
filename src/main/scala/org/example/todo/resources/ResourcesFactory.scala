package org.example.todo.resources

import cats.effect.IO
import cats.effect.kernel.Resource
import doobie.util.ExecutionContexts
import doobie.util.transactor.Transactor
import org.example.todo.HttpServer.MetricsComponents
import org.example.todo.config.{AppConfig, Config}
import org.example.todo.db.Database
import org.http4s.metrics.prometheus.{Prometheus, PrometheusExportService}

object ResourcesFactory {

  def getResources(configFile: String): Resource[IO, Resources] =
    resources(AppConfig.load(configFile))

  def getResources(config: Config): Resource[IO, Resources] =
    resources(IO.pure(config))

  private def resources(config: IO[Config]): Resource[IO, Resources] = for {
    config <- Resource.eval(config)
    ec <- ExecutionContexts.fixedThreadPool[IO](config.database.threadPoolSize)
    xa <- Database.transactor(config.database, ec)
    metricsService <- PrometheusExportService.build[IO]
    metrics <- Prometheus.metricsOps[IO](
      metricsService.collectorRegistry,
      config.metrics.prefixName
    )
  } yield Resources(config, xa, MetricsComponents(metrics, metricsService))

  final case class Resources(
      config: Config,
      transactor: Transactor[IO],
      metricsComponents: MetricsComponents
  )

}
