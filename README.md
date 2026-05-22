# MountainGuessr Server

[![Quality gate](https://sonarcloud.io/api/project_badges/quality_gate?project=bergwald_sopra-fs26-group-13-server)](https://sonarcloud.io/summary/new_code?id=bergwald_sopra-fs26-group-13-server)

## Introduction

MountainGuessr is a GeoGuessr-style web game built for the Software Engineering Praktikum course at UZH. Players explore a Google Street View panorama, place a guess on a world map, and receive a score based on the distance to the real location. The server exposes the REST API that powers registration, login, profile data, game sessions, multiplayer lobbies, round data, guess evaluation, and score persistence.

The motivation is to make a fun multiplayer game about recognising mountains.

Deployed backend: https://sopra-fs26-group-13-server.oa.r.appspot.com  
Deployed frontend: https://sopra-fs26-group-13-client.vercel.app

## Technologies

- Java 17
- Spring Boot 4 with Spring Web MVC
- Spring Data JPA and Hibernate
- H2 in-memory database
- Google Maps APIs and Google Cloud AppEngine

## High-Level Components

- [Application entry point](src/main/java/ch/uzh/ifi/hase/soprafs26/Application.java): starts the Spring Boot application.
- [REST controllers](src/main/java/ch/uzh/ifi/hase/soprafs26/controller): expose the public API. [UserController](src/main/java/ch/uzh/ifi/hase/soprafs26/controller/UserController.java) handles accounts and authentication, [SessionController](src/main/java/ch/uzh/ifi/hase/soprafs26/controller/SessionController.java) handles session creation and multiplayer membership, and [GameController](src/main/java/ch/uzh/ifi/hase/soprafs26/controller/GameController.java) serves round data and receives guesses.
- [Service layer](src/main/java/ch/uzh/ifi/hase/soprafs26/service): contains business logic. [UserService](src/main/java/ch/uzh/ifi/hase/soprafs26/service/UserService.java) validates users and tokens, [SessionService](src/main/java/ch/uzh/ifi/hase/soprafs26/service/SessionService.java) manages sessions and rounds, [GameService](src/main/java/ch/uzh/ifi/hase/soprafs26/service/GameService.java) stores scores and guesses, and [GuessEvaluationService](src/main/java/ch/uzh/ifi/hase/soprafs26/service/GuessEvaluationService.java) calculates distance and score.
- [Persistence layer](src/main/java/ch/uzh/ifi/hase/soprafs26/repository): JPA repositories store [users](src/main/java/ch/uzh/ifi/hase/soprafs26/entity/User.java), sessions, session users, and per-round game data.
- [DTO and mapping layer](src/main/java/ch/uzh/ifi/hase/soprafs26/rest): DTOs define the REST request/response shapes, while [DTOMapper](src/main/java/ch/uzh/ifi/hase/soprafs26/rest/mapper/DTOMapper.java) converts between API objects and JPA entities.
- [Google panorama integration](src/main/java/ch/uzh/ifi/hase/soprafs26/service/GooglePanoramaService.java): finds usable Street View panoramas for configured regions.

The controllers validate HTTP input and authorization, then delegate to services. Services enforce game rules and use repositories for persistence. DTOs keep the API shape separate from database entities.

## Launch & Deployment

### Prerequisites

- Java 17
- Gradle wrapper from this repository
- Optional Google Maps server API key for real panorama lookup
- No external database is required for local development; the app uses an in-memory H2 database.

### Local Development

Create a gitignored `local.properties` file in the server project root when using Google Maps locally:

```properties
google.maps.api-key=GOOGLE_MAPS_SERVER_API_KEY
```

Build the application:

```bash
./gradlew build
```

Start the development server:

```bash
./gradlew bootRun
```

If the wrapper is not executable on your system, use:

```bash
bash gradlew bootRun
```

The server runs at http://localhost:8080.

### Tests and Checks

Run all tests:

```bash
./gradlew test
```

Run tests with coverage report generation:

```bash
./gradlew test jacocoTestReport
```

Build the deployable jar:

```bash
./gradlew bootJar
```

### Releases

Pushes to `main` trigger the App Engine deployment workflow in [.github/workflows/main.yml](.github/workflows/main.yml). The workflow uses `GCP_SERVICE_CREDENTIALS` and deploys the `app.yaml` deliverable.

## Roadmap

- Replace the local H2 setup with a persistent production database and migration tooling.
- Improve multiplayer synchronization with a push-based mechanism such as WebSockets or server-sent events.
- Add panorama caching and stronger region-selection controls to reduce Google Maps API calls and improve location quality.

## Authors and Acknowledgment

Authors:

- @bergwald (Thomas)
- @PAKaeser (Patricia)
- @juliand924 (Julian)
- @plaiimade (Robin)

This project was created for the University of Zurich *Software Engineering Praktikum* course. We thank Yunyi Zhang and the SoPra team at UZH for their supervision, support, and guidance.

## License

This project is licensed under the Apache License 2.0. See [LICENSE](LICENSE) for details.
