package org.example.todo.api

import cats.data.Kleisli
import cats.effect.IO
import org.example.todo.HttpServer.MetricsComponents
import org.example.todo.service.Services
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.middleware.Metrics
import org.http4s.{Request, Response}

class Apis(service: Services, metricsComponents: MetricsComponents) {
  private val genericApi: GenericApi = new GenericApi(service.genericService)
  private val todoApi: TodoApi = new TodoApi(service.todoService)

  val httpRoutes: Kleisli[IO, Request[IO], Response[IO]] = Router(
    "/" -> metricsComponents.metricsService.routes,
    "/" -> genericApi.routes,
    "todos" -> Metrics[IO](metricsComponents.metricsOps)(todoApi.routes)
  ).orNotFound

}
