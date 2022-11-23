package org.example.todo.service

import cats.effect.IO
import fs2.Stream
import io.circe.{Decoder, Encoder}
import io.circe.generic.auto._
import io.circe.syntax._
import org.example.todo.model
import org.example.todo.model.{Importance, Todo, TodoNotFoundError}
import org.example.todo.repository.TodoDao

class TodoService(dao: TodoDao) {
  private implicit val encoderImportance: Encoder[Importance] =
    Encoder.encodeString.contramap[Importance](_.value)

  def getTodos: Stream[IO, String] = {
    Stream("[") ++
      dao.getToDos
        .map(_.asJson.noSpaces)
        .intersperse(",") ++
      Stream("]")
  }

  def getTodo(todoId: Long): IO[Either[TodoNotFoundError.type, model.Todo]] =
    dao.getTodo(todoId)

  def deleteTodo(todoId: Long): IO[Either[TodoNotFoundError.type, Unit]] =
    dao.deleteTodo(todoId)

  def addTodo(todo: Todo): IO[Todo] = dao.createTodo(todo)

  def updateTodo(
      id: Long,
      todo: Todo
  ): IO[Either[TodoNotFoundError.type, Todo]] = dao.updateTodo(id, todo)
}
