package org.example.todo

import scala.concurrent.ExecutionContext
import scala.language.postfixOps

import cats.effect.unsafe.IORuntime
import cats.effect.{ExitCode, IO}
import io.circe.Json
import io.circe.literal._
import io.circe.optics.JsonPath.root
import org.example.todo.config.AppConfig
import org.http4s.circe._
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.{Method, Request, Status, Uri}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.Eventually.eventually
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.slf4j.LoggerFactory

//TODO: Use embedded postgres or test container
class TodoServerSpec extends AnyWordSpec with Matchers with BeforeAndAfterAll {
  private lazy val httpClient =
    BlazeClientBuilder[IO](ExecutionContext.global).resource
  private val configFile = "test.conf"

  private implicit val runtime: IORuntime = cats.effect.unsafe.IORuntime.global
  private lazy val config =
    AppConfig.load(configFile).use(config => IO.pure(config)).unsafeRunSync()
  private lazy val testEndpointUrl =
    s"http://${config.server.host}:${config.server.port}"

  private val logger = LoggerFactory.getLogger(classOf[TodoServerSpec])

  override def beforeAll(): Unit = {
    HttpServer.create(configFile).unsafeRunAsync(resultHandler)
    eventually {
      httpClient
        .use(
          _.statusFromUri(Uri.unsafeFromString(s"$testEndpointUrl/health"))
        )
        .unsafeRunSync() shouldBe Status.Ok
    }
    ()
  }

  "Todo service" should {
    "creat a todo test" in {
      val description = "my first test todo"
      val importance = "high"
      val createJson = json"""
        {
          "description": $description,
          "importance": $importance
        }"""
      val request = Request[IO](
        method = Method.POST,
        uri = Uri.unsafeFromString(s"$testEndpointUrl/todos")
      ).withEntity(createJson)

      val json = httpClient.use(_.expect[Json](request)).unsafeRunSync()

      root.id.long.getOption(json).nonEmpty shouldBe true
      root.description.string.getOption(json) shouldBe Some(description)
      root.importance.string.getOption(json) shouldBe Some(importance)
    }

    "update todo test" in {
      val id = createTodo("my second test todo", "low")
      val description = "updated description"
      val importance = "high"
      val updatedJson = json"""
         {
            "description" : $description,
            "importance" : $importance
         }"""

      val request =
        Request[IO](
          method = Method.PUT,
          uri = Uri.unsafeFromString(s"$testEndpointUrl/todos/$id")
        ).withEntity(updatedJson)

      val response = httpClient.use(_.expect[Json](request)).unsafeRunSync()
      response shouldBe
        json"""
          {
            "id": $id,
            "description": $description,
            "importance": $importance
          }
          """
    }

    "delete a todo test" in {
      val description = "my second test todo"
      val importance = "medium"
      val id = createTodo(description, importance)
      val deleteRequest = Request[IO](
        method = Method.DELETE,
        uri = Uri.unsafeFromString(s"$testEndpointUrl/todos/$id")
      )

      httpClient
        .use(_.status(deleteRequest))
        .unsafeRunSync() shouldBe Status.NoContent
    }

    "return a single todo test" in {
      val description = "my fourth test todo"
      val importance = "low"
      val id = createTodo(description, importance)
      val request = Request[IO](
        method = Method.GET,
        Uri.unsafeFromString(s"$testEndpointUrl/todos/$id")
      )
      val response = httpClient.use(_.expect[Json](request)).unsafeRunSync()

      response shouldBe
        json"""
          {
            "id": $id,
            "description": $description,
            "importance": $importance
          }
          """
    }

    "return all todos test" in {
      // cleanUp existing Todos
      val json = httpClient
        .use(_.expect[Json](Uri.unsafeFromString(s"$testEndpointUrl/todos")))
        .unsafeRunSync()
      root.each.id.long.getAll(json).foreach { id =>
        val deleteRequest = Request[IO](
          method = Method.DELETE,
          uri = Uri.unsafeFromString(s"$testEndpointUrl/todos/$id")
        )
        httpClient
          .use(_.status(deleteRequest))
          .unsafeRunSync() shouldBe Status.NoContent
      }

      // Add new todos
      val description1 = "test todo 1"
      val description2 = "test todo 2"
      val importance1 = "high"
      val importance2 = "low"

      val id1 = createTodo(description1, importance1)
      val id2 = createTodo(description2, importance2)

      val getAllRequest = Request[IO](
        method = Method.GET,
        uri = Uri.unsafeFromString(s"$testEndpointUrl/todos")
      )
      val response =
        httpClient.use(_.expect[Json](getAllRequest)).unsafeRunSync()
      response shouldBe
        json"""
          [
            {
              "id": $id1,
              "description": $description1,
              "importance": $importance1
            },
            {
              "id": $id2,
              "description": $description2,
              "importance": $importance2
            }
          ]"""
    }
  }

  "Metrics service" should {
    "expose create and expose metrics endpoint successfully" in {
      val metrics: Status = httpClient
        .use(
          _.statusFromUri(Uri.unsafeFromString(s"$testEndpointUrl/metrics"))
        )
        .unsafeRunSync()
      metrics shouldBe Status.Ok
    }

    "expose system metrics" in {
      val getAllRequest = Request[IO](
        method = Method.GET,
        uri = Uri.unsafeFromString(s"$testEndpointUrl/metrics")
      )
      val response =
        httpClient.use(_.expect[String](getAllRequest)).unsafeRunSync()

      response.contains(
        "# HELP jvm_threads_daemon Daemon thread count of a JVM"
      ) shouldBe true
      response.contains(
        "# HELP process_open_fds Number of open file descriptors."
      ) shouldBe true
      response.contains("# TYPE process_open_fds gauge") shouldBe true
    }

  }

  private def createTodo(description: String, importance: String): Long = {
    val createJson =
      json"""
          {
             "description" : $description,
             "importance" : $importance
          }
          """
    val request =
      Request[IO](
        method = Method.POST,
        uri = Uri.unsafeFromString(s"$testEndpointUrl/todos")
      ).withEntity(createJson)
    val json = httpClient.use(_.expect[Json](request)).unsafeRunSync()
    root.id.long.getOption(json).nonEmpty shouldBe true
    root.id.long.getOption(json).get
  }

  private def resultHandler(result: Either[Throwable, ExitCode]): Unit = {
    result.left.foreach(t =>
      logger.error("Executing the http server failed", t)
    )
  }

}
