# ToDo-Kotlin

A REST API for managing todos and their tasks, built with Kotlin and Spring Boot. Data is stored in an in-memory H2 database, so no database setup is needed.

## Tech stack

- Kotlin 2.3 on the JVM (Java 25 toolchain)
- Spring Boot 4.1 (Web MVC, Validation, Data JPA)
- H2 in-memory database, with the H2 web console enabled
- Gradle (wrapper included) with Spotless
- Tests: JUnit 5, AssertJ, MockK (springmockk), MockMvc

## Prerequisites

| Requirement | Notes |
| --- | --- |
| JDK 25 | Gradle's toolchain is set to Java 25. Check with `java -version`. |
| Git | To clone the repository. |

You don't need to install Gradle. The included wrapper (`gradlew` / `gradlew.bat`) downloads Gradle 9.7.1 on first run.

## Setup

```bash
git clone <repository-url>
cd ToDo-Kotlin
```

## Run the application

**Windows (PowerShell / cmd)**

```powershell
.\gradlew.bat bootRun
```

**macOS / Linux / Git Bash**

```bash
./gradlew bootRun
```

The app starts on <http://localhost:8080>. Stop it with `Ctrl+C`.

To run a packaged jar instead:

```bash
./gradlew bootJar
java -jar build/libs/todo-kotlin-0.0.1-SNAPSHOT.jar
```

To use a different port, add `--server.port=9090` (jar) or run `./gradlew bootRun --args="--server.port=9090"`.

### Quick check

```bash
curl http://localhost:8080/hello
# Hello
```

## Run the tests

```bash
./gradlew test
```

## Formatting

The project uses [Spotless](https://github.com/diffplug/spotless) for Kotlin formatting. Style rules come from `.editorconfig`.

```bash
./gradlew spotlessCheck   # report style violations
./gradlew spotlessApply   # auto-fix them
```

`./gradlew check` also runs `spotlessCheck`.

## Database and H2 console

The database is in-memory (`jdbc:h2:mem:tododb`). **All data is lost when the app stops.**

While the app is running, open the H2 console at <http://localhost:8080/h2-console> and connect with:

| Field | Value |
| --- | --- |
| JDBC URL | `jdbc:h2:mem:tododb` |
| User name | `sa` |
| Password | *(empty)* |

Configuration lives in `src/main/resources/application.yaml`.

## API

All IDs are UUIDs. A todo contains tasks.

### Todos

| Method | Endpoint | Description |
| --- | --- | --- |
| `GET` | `/todos?pageNo=0&pageSize=10` | List todos, paginated (`pageSize` is capped at 100) |
| `POST` | `/todos` | Create a todo |
| `GET` | `/todos/{id}` | Get a todo with its tasks |
| `PUT` | `/todos/{id}` | Update a todo |
| `DELETE` | `/todos/{id}` | Delete a todo |

### Tasks

| Method | Endpoint | Description |
| --- | --- | --- |
| `POST` | `/todos/{todoId}/tasks` | Create a task under a todo |
| `GET` | `/todos/{todoId}/tasks/{taskId}` | Get a task |
| `PUT` | `/todos/{todoId}/tasks/{taskId}` | Update a task |
| `DELETE` | `/todos/{todoId}/tasks/{taskId}` | Delete a task |

### Request body

Todos and tasks use the same body. `name` is required and must not be blank; `description` is optional.

```json
{
  "name": "Buy groceries",
  "description": "Milk, eggs, bread"
}
```

### Response format

Single-item endpoints return:

```json
{
  "success": true,
  "message": "Todo created successfully!",
  "object": {
    "id": "3f2b8c1e-0d4a-4a52-9a77-5d1c1b6e8f10",
    "name": "Buy groceries",
    "description": "Milk, eggs, bread"
  }
}
```

The list endpoint returns:

```json
{
  "success": true,
  "message": "",
  "totalItems": 1,
  "totalPage": 1,
  "currentPage": 0,
  "data": [
    { "id": "3f2b8c1e-0d4a-4a52-9a77-5d1c1b6e8f10", "name": "Buy groceries", "description": "Milk, eggs, bread" }
  ]
}
```

Errors return `{ "success": false, "message": "..." }` with these status codes:

| Status | Cause |
| --- | --- |
| `400` | Validation failure, malformed JSON, or an invalid UUID |
| `404` | Todo or task not found |

## Project structure

```
src/main/kotlin/info/somrat/todokotlin/
├── ToDoKotlinApplication.kt   # Entry point
├── controller/                # REST controllers
├── service/                   # Business logic
├── repository/                # Spring Data JPA repositories
├── model/                     # JPA entities (Todo, Task)
├── dto/                       # Request/response objects
└── exception/                 # Error handling
```
