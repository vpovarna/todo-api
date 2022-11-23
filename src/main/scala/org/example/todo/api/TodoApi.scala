package org.example.todo.api

import cats.effect.IO
import io.circe.generic.auto._
import io.circe.syntax._
import io.circe.{Decoder, Encoder}
import org.example.todo.model.{Importance, Todo, TodoNotFoundError}
import org.example.todo.service.TodoService
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.{Location, `Content-Type`}
import org.http4s.{HttpRoutes, MediaType, Response, Uri}

class TodoApi(todoService: TodoService) extends Http4sDsl[IO] {
  private implicit val encoderImportance: Encoder[Importance] =
    Encoder.encodeString.contramap[Importance](_.value)
  private implicit val decodeImportance: Decoder[Importance] =
    Decoder.decodeString.map[Importance](Importance.unsafeFromString)

  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root =>
      Ok(todoService.getTodos, `Content-Type`(MediaType.application.json))

    case GET -> Root / LongVar(id) =>
      for {
        result <- todoService.getTodo(id)
        response <- todoResult(result)
      } yield response

    case req @ POST -> Root =>
      for {
        todo <- req.decodeJson[Todo]
        createdTodo <- todoService.addTodo(todo)
        response <- Created(
          createdTodo.asJson,
          Location(Uri.unsafeFromString(s"todos/${createdTodo.id.get}"))
        )
      } yield response

    case req @ PUT -> Root / LongVar(id) =>
      for {
        todo <- req.decodeJson[Todo]
        updatedResponse <- todoService.updateTodo(id, todo)
        response <- todoResult(updatedResponse)
      } yield response

    case DELETE -> Root / LongVar(id) =>
      todoService.deleteTodo(id).flatMap {
        case Left(TodoNotFoundError) => NotFound()
        case Right(_)                => NoContent()
      }
  }

  private def todoResult(
      result: Either[TodoNotFoundError.type, Todo]
  ): IO[Response[IO]] = {
    result match {
      case Left(TodoNotFoundError) => NotFound()
      case Right(todo)             => Ok(todo.asJson)
    }
  }

}
