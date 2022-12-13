lazy val commonSettings = Seq(
  name := "todo-api",
  version := "1.0-SNAPSHOT",
  scalaVersion := "2.13.8",
  organization := "org.example.todo",
  scalacOptions ++= Seq(
    "-deprecation",
    "-language:higherKinds",
    "-Xfatal-warnings",
    "-Ywarn-value-discard",
    "-Xlint:missing-interpolator"
  )
)

lazy val http4sVersion = "1.0.0-M21"
lazy val circeVersion = "0.14.1"
lazy val logbackVersion = "1.2.11"
lazy val scalaTestVersion = "3.2.14"
lazy val doobieVersion = "1.0.0-RC1"
lazy val pureConfigVersion = "0.17.2"
lazy val scalaMockVersion = "5.2.0"
lazy val testcontainersPostgresVersion = "1.15.3"

lazy val root = (project in file("."))
  .configs(IntegrationTest)
  .settings(
    commonSettings,
    Defaults.itSettings,
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-blaze-server" % http4sVersion,
      "org.http4s" %% "http4s-circe" % http4sVersion,
      "org.http4s" %% "http4s-dsl" % http4sVersion,
      "org.http4s" %% "http4s-blaze-client" % http4sVersion % "it,test",

      // circe
      "io.circe" %% "circe-generic" % circeVersion,
      "io.circe" %% "circe-literal" % circeVersion % "it,test",
      "io.circe" %% "circe-optics" % circeVersion % "it",

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

      // Test containers
      "org.testcontainers" % "postgresql" % testcontainersPostgresVersion % "it, test",

      // Test libs
      "org.scalatest" %% "scalatest" % scalaTestVersion % "it,test",
      "org.scalamock" %% "scalamock" % scalaMockVersion % "test"
    )
  )
