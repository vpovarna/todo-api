package org.example.todo.api

import cats.effect.IO
import org.example.todo.service.GenericService
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl

final class GenericApi(genericService: GenericService) extends Http4sDsl[IO] {
  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root / "ping"   => Ok("pong")
    case GET -> Root / "health" => Ok(genericService.healthCheck)
  }
}
