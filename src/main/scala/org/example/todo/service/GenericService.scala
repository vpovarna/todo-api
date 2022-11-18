package org.example.todo.service

import cats.effect.IO
import org.example.todo.repository.HealthCheckDao

class GenericService(dao: HealthCheckDao) {
  def healthCheck: IO[String] =
    dao.testConnection.map(status =>
      s"Database Connectivity: ${if (status) "OK" else "FAILURE"} "
    )
}
