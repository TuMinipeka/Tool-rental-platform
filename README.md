# Tool Rental Platform

Backend for renting construction tools and equipment, built with Java 21, hexagonal architecture, and domain-driven design. The project keeps business rules independent from Spring, JPA, HTTP, and security concerns.

## Current status

Implemented: a multi-module Maven structure, catalog and reservation aggregates, application use cases, domain ports, domain events, PostgreSQL/Flyway configuration, an executable 4NF schema, JPA mappings, and production persistence adapters. The application still has no HTTP server.

Pending: the remaining Phase 0 commercial decisions, PostgreSQL integration and concurrency validation, database provisioning, identity writes, REST/OpenAPI, JWT authorization, rental/payment/protection/invoice adapters, reporting, deployment, and the React frontend. See the [product roadmap](docs/pendientes.md) and [pending database work](docs/pendientes-base-datos.md).

## Technology

| Component | Choice |
| --- | --- |
| Java | 21 |
| Spring Boot | 3.5.16 |
| Build | Maven multi-module project; Maven Wrapper 3.9.9 |
| Tests | JUnit 5, Spring Boot Test, ArchUnit |
| Formatting | Spotless 2.43.0 with Google Java Format 1.27.0 |
| Database | PostgreSQL, Spring Data JPA, Flyway |
| HTTP server | Embedded Apache Tomcat through Spring Boot |
| API | REST with OpenAPI and Swagger UI; confirmed but not implemented |
| Frontend | React with Node.js tooling, HTML, and CSS; confirmed but not created |

The [original assignment (Spanish)](ejercicioDemo.md) requested MySQL. PostgreSQL replaces that requirement by explicit project decision.

## Architecture

```text
domain/          # Aggregates, entities, value objects, events, and ports
application/     # Commands, responses, exceptions, and use cases
infrastructure/  # Spring Boot entry point, JPA adapters, and Flyway migrations
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

The default `local` profile connects to `jdbc:postgresql://localhost:5432/tool_rental` with user and password `tool_rental`. Override any value through environment variables:

```powershell
$env:SPRING_PROFILES_ACTIVE = 'local'
$env:DB_URL = 'jdbc:postgresql://localhost:5432/tool_rental'
$env:DB_USERNAME = 'tool_rental'
$env:DB_PASSWORD = 'change-me'
```

The `production` profile requires `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD`. Flyway applies versioned migrations and Hibernate uses `ddl-auto: validate`; Hibernate never creates or updates the schema.

Flyway creates the schema through 18 ordered migrations, one per table. With the `local` profile it also loads 18 demonstration-data migrations, each containing at least five rows. The `production` profile only uses `db/migration` and never loads the demonstration dataset. The tool rates and their public Colombian references are recorded in [Data sources](docs/data-sources.md).

## Documentation

1. [Original assignment (Spanish)](ejercicioDemo.md)
2. [Conceptual model](docs/conceptual-model.md)
3. [Fourth normal form](docs/fourth-normal-form.md)
4. [Backend architecture](docs/backend-architecture.md)
5. [Development guide](docs/development-guide.md)
6. [Infrastructure structure](infrastructure/STRUCTURE.md)
7. [Pending database work](docs/pendientes-base-datos.md)
8. [Project roadmap and pending work](docs/pendientes.md)
9. [Product scope](docs/product-scope.md)
10. [Terms and Conditions draft](docs/terms-and-conditions.md)

`AGENTS.md` records repository-wide rules for future automated work. ArchUnit protects dependency direction and key hierarchies; Spotless protects Java formatting.

## Next delivery

The team is finishing Phase 0 business rules, beginning with cancellation and no-show handling. The next database phase will provision PostgreSQL, validate Flyway and mappings, and extend the schema for the approved tax, split-payment, damage-protection, evidence, dispute, and legal-acceptance rules. REST/OpenAPI and JWT security follow before the React frontend integrates with the backend.

The installed [database-normalization skill](.agents/skills/database-normalization/SKILL.md) supports the relational analysis. Its source and license are tracked by `skills-lock.json`.

## Convención de commits

Todos los mensajes deben escribirse en inglés con Conventional Commits, un código Gitmoji y un resumen concatenado en estilo `CamelCase`:

```text
<type>: :<gitmoji>: <CamelCaseSummary>
```

Ejemplos:

```text
feat: :card_file_box: AddImplementationMigrations
fix: :bug: PreventOverlappingReservations
docs: :memo: DocumentDatabaseConventions
refactor: :recycle: RenameCatalogTypes
```
