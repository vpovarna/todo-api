val scala213 = "2.13.8"

ThisBuild / scalaVersion := scala213
ThisBuild / version := "1.0"
ThisBuild / organization := "org.example.todo"

lazy val catsVersion = "2.9.0"
lazy val catsEffectVersion = "3.4.0"
lazy val http4sVersion = "1.0.0-M21"
lazy val circeVersion = "0.14.3"
lazy val logbackVersion = "1.2.11"
lazy val scalaTestVersion = "3.2.14"
lazy val doobieVersion = "1.0.0-RC1"
lazy val pureConfigVersion = "0.17.2"

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-core" % catsVersion,
  "org.typelevel" %% "cats-effect" % catsEffectVersion,
  "org.http4s" %% "http4s-blaze-server" % http4sVersion,
  "org.http4s" %% "http4s-circe" % http4sVersion,
  "org.http4s" %% "http4s-dsl" % http4sVersion,
  "io.circe" %% "circe-generic" % circeVersion,

  // pureconfig
  "com.github.pureconfig" %% "pureconfig" % pureConfigVersion,
  "com.github.pureconfig" %% "pureconfig-cats-effect" % pureConfigVersion,

  // Repository
  "org.tpolecat" %% "doobie-core" % doobieVersion,
  "org.tpolecat" %% "doobie-postgres" % doobieVersion,
  "org.tpolecat" %% "doobie-hikari" % doobieVersion,

  // Metrics
  "org.http4s" %% "http4s-prometheus-metrics" % http4sVersion,

  // Logs
  "ch.qos.logback" % "logback-classic" % logbackVersion,

  // Test libs
  "org.scalatest" %% "scalatest" % scalaTestVersion % "test"
)

scalacOptions ++= Seq(
  "-language:higherKinds"
)
