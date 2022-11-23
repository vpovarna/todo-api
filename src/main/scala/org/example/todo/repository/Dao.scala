package org.example.todo.repository

import cats.effect.IO
import doobie.util.transactor.Transactor

final class Dao(transactor: Transactor[IO]) {

  lazy val healthcheckDao = new HealthCheckDao(transactor);
  lazy val todoDao = new TodoDao(transactor)

}
