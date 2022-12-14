# TODO-API
A simple project of a microservice using http4s, doobie and circe.

The microservice allows CRUD of todo items with a description and an importance (high, medium, low).

## Local development

Running integration tests
```
$ sbt "it:testOnly"
```

Building the project
```
$ sbt compile
```

Building fat jar
```
$ sbt assembly
```

Running the application locally
- Start Postgresql DB
```
$ cd docker
$ docker compose up
```

- Start the application:
```
$ sbt runt
```