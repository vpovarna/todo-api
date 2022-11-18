package org.example.todo.repository

import cats.effect.IO
import doobie.implicits._
import doobie.util.transactor.Transactor

class HealthCheckDao(xa: Transactor[IO]) {

  def testConnection: IO[Boolean] = {
    val query = sql"select 1".query[Int]
    val action = query.unique.map(_ == 1)
    action.transact(xa)
  }

}
