package org.example.todo.api

import cats.data.Kleisli
import cats.effect.IO
import org.example.todo.service.Services
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.{Request, Response}

class Apis(service: Services) {
  val genericApi = new GenericApi(service.genericService)
  val todoApi = new TodoApi(service.todoService)

  val httpRoutes: Kleisli[IO, Request[IO], Response[IO]] = Router(
    "/" -> genericApi.routes,
    "todos" -> todoApi.routes
  ).orNotFound

}
