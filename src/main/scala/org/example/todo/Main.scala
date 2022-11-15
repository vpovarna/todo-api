package org.example.todo

import cats.effect.{ExitCode, IO, IOApp}

object Main extends IOApp {
  override def run(args: List[String]): IO[ExitCode] =
    IO(println(42)).as(ExitCode.Success)
}
