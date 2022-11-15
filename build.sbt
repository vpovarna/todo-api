val scala213 = "2.13.8"

ThisBuild / scalaVersion := scala213
ThisBuild / version := "1.0"
ThisBuild / organization := "org.example.todo"

val catsVersion = "2.1.1"
val catsEffectVersion = "3.2.0"
val http4sVersion = "1.0.0-M21"
val circeVersion = "0.14.0-M5"
val logbackVersion = "1.2.3"
val scalaTestVersion = "3.2.9"
val doobieVersion = "1.0.0-RC1"

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-core" % catsVersion,
  "org.typelevel" %% "cats-effect" % catsEffectVersion,
  "org.http4s" %% "http4s-blaze-server" % http4sVersion,
  "org.http4s" %% "http4s-circe" % http4sVersion,
  "org.http4s" %% "http4s-dsl" % http4sVersion,
  "io.circe" %% "circe-generic" % circeVersion,

  // Repository
  "org.tpolecat" %% "doobie-core" % doobieVersion,
  "org.tpolecat" %% "doobie-postgres" % doobieVersion,
  "org.tpolecat" %% "doobie-hikari" % doobieVersion,

  // Logs
  "ch.qos.logback" % "logback-classic" % logbackVersion,

  // Test libs
  "org.scalatest" %% "scalatest" % scalaTestVersion % "test"
)

scalacOptions ++= Seq(
  "-language:higherKinds"
)
