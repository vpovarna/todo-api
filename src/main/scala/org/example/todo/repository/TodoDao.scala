package org.example.todo.repository

import cats.effect._
import doobie._
import doobie.implicits._
import fs2.Stream
import org.example.todo.model.{Importance, Todo, TodoNotFoundError}

class TodoDao(transactor: Transactor[IO]) {
  // Required for the Importance ADT
  private implicit val importanceMeta: Meta[Importance] =
    Meta[String].timap(Importance.unsafeFromString)(_.value)

  def getToDos: Stream[IO, Todo] =
    sql"SELECT id, description, importance FROM notes"
      .query[Todo]
      .stream
      .transact(transactor)

  def getTodo(id: Long): IO[Either[TodoNotFoundError.type, Todo]] = {
    sql"SELECT id, description, importance FROM notes WHERE id = $id"
      .query[Todo]
      .option
      .transact(transactor)
      .map {
        case Some(todo) => Right(todo)
        case None       => Left(TodoNotFoundError)
      }
  }

  def createTodo(todo: Todo): IO[Todo] = {
    sql"INSERT INTO notes (description, importance) VALUES (${todo.description}, ${todo.importance})".update
      .withUniqueGeneratedKeys[Long]("id")
      .transact(transactor)
      .map { id => todo.copy(id = Some(id)) }
  }

  def deleteTodo(id: Long): IO[Either[TodoNotFoundError.type, Unit]] = {
    sql"DELETE FROM notes WHERE id = $id".update.run.transact(transactor).map {
      affectedRowsNr =>
        if (affectedRowsNr == 1) Right(())
        else Left(TodoNotFoundError)
    }
  }

  def updateTodo(
      id: Long,
      todo: Todo
  ): IO[Either[TodoNotFoundError.type, Todo]] = {
    sql"UPDATE notes SET description = ${todo.description}, importance = ${todo.importance} where id=$id".update.run
      .transact(transactor)
      .map { affectedRowsNr =>
        if (affectedRowsNr == 1) Right(todo.copy(id = Some(id)))
        else Left(TodoNotFoundError)
      }
  }

}
