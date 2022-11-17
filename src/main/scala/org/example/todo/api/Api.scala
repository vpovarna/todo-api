package org.example.todo.api

import cats.data.Kleisli
import cats.effect.IO
import org.example.todo.service.ToDoService
import org.http4s.{HttpRoutes, Request, Response}
import org.http4s.implicits._
import org.http4s.server.Router

class Api(service: ToDoService) {
  private val generic: HttpRoutes[IO] = new GenericApi().routes

  val httpRoutes: Kleisli[IO, Request[IO], Response[IO]] = Router(
    "/" -> generic
  ).orNotFound

}
