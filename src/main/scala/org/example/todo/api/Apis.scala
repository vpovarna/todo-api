package org.example.todo.api

import cats.data.Kleisli
import cats.effect.IO
import org.example.todo.HttpServer.MetricsComponents
import org.example.todo.service.Services
import org.http4s.implicits._
import org.http4s.metrics.prometheus.PrometheusExportService
import org.http4s.server.Router
import org.http4s.server.middleware.Metrics
import org.http4s.{HttpRoutes, Request, Response}

class Apis(service: Services, metricsComponents: MetricsComponents) {
  private val genericApi: GenericApi = new GenericApi(service.genericService)
  private val todoApi: TodoApi = new TodoApi(service.todoService)
  private val metricsService: PrometheusExportService[IO] =
    metricsComponents.metricsService

  val httpRoutes: Kleisli[IO, Request[IO], Response[IO]] = Router(
    "/" -> metricsService.routes,
    "/" -> genericApi.routes,
    "todos" -> Metrics[IO](metricsComponents.metricsOps)(todoApi.routes)
  ).orNotFound

}
