# Tool Rental Platform

Backend for renting construction tools and equipment, built with Java 21, hexagonal architecture, and domain-driven design. The project keeps business rules independent from Spring, JPA, HTTP, and security concerns.

## Current status

Implemented: a multi-module Maven structure, catalog and reservation aggregates, application use cases, domain ports, domain events, and automated tests. Spring Boot starts without a database or HTTP server. Test-only in-memory adapters exercise the use cases; production adapters do not exist yet.

Pending: PostgreSQL/JPA adapters, Flyway migrations, REST API, JWT identity and authorization, rentals, payments, invoicing, reporting, deployment, and frontend.

## Technology

| Component | Choice |
| --- | --- |
| Java | 21 |
| Spring Boot | 3.5.16 |
| Build | Maven multi-module project; Maven Wrapper 3.9.9 |
| Tests | JUnit 5, Spring Boot Test, ArchUnit |
| Formatting | Spotless 2.43.0 with Google Java Format 1.27.0 |
| Planned database | PostgreSQL |

The [original assignment (Spanish)](ejercicioDemo.md) requested MySQL. PostgreSQL replaces that requirement by explicit project decision; no JDBC driver or physical schema has been added yet.

## Architecture

```text
domain/          # Aggregates, entities, value objects, events, and ports
application/     # Commands, responses, exceptions, and use cases
infrastructure/  # Spring Boot entry point, configuration, and future adapters
docs/            # Conceptual model, 4NF analysis, architecture, and conventions
pom.xml          # Maven reactor and centralized versions
```

Dependencies point inward:

```text
infrastructure -> application -> domain
```

Root packages are `com.toolrental.domain`, `com.toolrental.application`, and `com.toolrental.infrastructure`. Code is grouped by the `catalog` and `reservation` business contexts, then by technical responsibility. A relational table does not automatically become a DDD aggregate.

All source identifiers are English. Java types use `PascalCase`, members use `camelCase`, packages use lowercase names, and PostgreSQL identifiers use plural `snake_case` table names with singular `snake_case` columns. Irregular plurals are written naturally, for example `country -> countries`.

See [Backend architecture](docs/backend-architecture.md) and the [Development guide](docs/development-guide.md).

## Implemented business rules

- Aggregate roots: `Category`, `Tool`, `EquipmentUnit`, and `Reservation`.
- Images belong to a tool offer; each equipment unit represents one physical asset.
- A reservation contains distinct units from one supplier and uses one currency.
- Start and return dates are inclusive. October 14–15 bills two days; October 14–14 bills one day. Sharing any date conflicts with a confirmed reservation for the same unit.
- Pending requests do not block units. Confirmation revalidates the actor, active profiles, publication, operational status, currency, and availability inside one atomic port.
- The current daily rate is frozen into reservation items at confirmation. Later catalog changes do not alter the agreed price.
- Rejection accepts only pending reservations. Cancellation accepts pending or confirmed reservations. Terminal states reject further transitions.
- Actor identifiers are application inputs for now; authentication has not been implemented.

## Java 21

```powershell
git clone https://github.com/TuMinipeka/Tool-rental-platform.git
cd Tool-rental-platform
java -version
.\mvnw.cmd -version
```

Set `JAVA_HOME` to a local JDK 21 installation when the default Java version differs. The Maven Wrapper downloads Maven but does not install Java.

## Build and run

Windows:

```powershell
.\mvnw.cmd clean verify
java -jar infrastructure\target\infrastructure-0.1.0-SNAPSHOT.jar
```

Linux/macOS:

```bash
./mvnw clean verify
java -jar infrastructure/target/infrastructure-0.1.0-SNAPSHOT.jar
```

`verify` runs tests and validates Java formatting. Apply formatting with:

```powershell
.\mvnw.cmd spotless:apply
```

The application currently starts a Spring context and exits normally because it has no web server or background task. It needs no database credentials at this stage.

## Documentation

1. [Original assignment (Spanish)](ejercicioDemo.md)
2. [Conceptual model](docs/conceptual-model.md)
3. [Fourth normal form](docs/fourth-normal-form.md)
4. [Backend architecture](docs/backend-architecture.md)
5. [Development guide](docs/development-guide.md)
6. [Infrastructure structure](infrastructure/STRUCTURE.md)

`AGENTS.md` records repository-wide rules for future automated work. ArchUnit protects dependency direction and key hierarchies; Spotless protects Java formatting.

## Next delivery

The next phase will add PostgreSQL, Flyway, JPA entities and mappers isolated in infrastructure, `ddl-auto: validate`, physical constraints, and integration/concurrency tests. REST and JWT security follow after persistence. Frontend technology will be selected separately.

The installed [database-normalization skill](.agents/skills/database-normalization/SKILL.md) supports the relational analysis. Its source and license are tracked by `skills-lock.json`.

## Commit convention

Use Conventional Commits in English:

```text
feat: add reservation confirmation endpoint
fix: prevent overlapping confirmed reservations
docs: document database naming conventions
refactor: rename catalog types in English
```
