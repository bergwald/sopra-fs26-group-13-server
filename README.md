# SoPra RESTful Service Template FS26

## DEV Commands

`build` + `bootRun` to build the app and launch a development server. The dev server run at `localhost:8080`.

## Notes

### Java Persistence API (JPA)

JPA allows us to define a relational schema and perform operations on the database without having to tediously write SQL code.

- Provides interfaces to work with data abstraction
- Helps with Object-Relational-Mapping (ORM)
- Allows you to work directly with objects rather
than with SQL statements
- Helps you with the storing, retrieving, updating
and mapping of objects

JPA is an ORM (object relational mapping) standard/interface. The actual ORM implementation is done by a "JPA provider" such as Hibernate/EclipseLink/OpenJPA.

Mapping between database tables and objects is defined via persistence metadata. Metadata can either be via annotations in the Java class or defined in an XML file.

Example in Java class:

```java
// entity maps to a table
@Entity
public class User implements Serializable {

    // generated primary key
    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String username;
}
```

Unidirectional relationship: only one side "knows" about the other.

Bidirectional relationship: both sides "know" about each other. The association can be navigated in both directions.

```java
@Entity
public class Scoreboard {

    @Id
    @GeneratedValue
    private Long id;

    // one-directional mapping
    @OneToOne
    private Game game;

    // bidirectional mapping
    // the other table also needs to include the "mappedBy" argument
    @OneToOne(mappedBy = "scoreBoard")
    private Game game;
}
```

CRUD operations:

```java
userRepository.save(newUser);
userRepository.findById(userId);
userRepository.delete(user);
userRepository.findAll(newUser);
```

### Java Spring Boot REST API

**Spring** is a Java framework for building applications, especially backend/enterprise apps.

**Spring Boot** is built on top of Spring and is meant to make starting and running Spring apps much faster.

Spring Boot = Spring + sensible defaults + auto-setup

Core idea of Spring: *dependency injection / inversion of control* (DI / IoC): Spring manages object creation and wiring.

Classic Spring app structure:

- **Controller**: HTTP layer; handles HTTP requests/responses.
- **Service**: business logic, validation, transactions.
- **Repository**: database interaction.
- **Entity**: a class mapped to a database table.
- **Data Transfer Object (DTO)**: class used to transfer data between layers, especially for API input/output.
- **Mapper**: converts between types (usually Entity <-> DTO).

### Gradle

Gradle is a build automation system. It:

- compiles code,
- runs tests,
- assembles docs,
- packages and runs applications, and
- performs deployments.

Existing Gradle tasks in this project:

- `build`
- `bootRun`
