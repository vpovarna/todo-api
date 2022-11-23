package org.example.todo.service

import org.example.todo.repository.Dao

final class Services(dao: Dao) {

  lazy val genericService = new GenericService(dao.healthcheckDao)
  lazy val todoService = new TodoService(dao.todoDao)

}
