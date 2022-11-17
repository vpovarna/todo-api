package org.example.todo.repository

import cats.effect.IO
import doobie.util.transactor.Transactor

class ToDoRepository(xa: Transactor[IO]) {}
