# Tool Rental Platform

Java 21 backend for construction-equipment rentals, with three Maven modules, hexagonal architecture, PostgreSQL, Flyway, JPA, and REST. Business rules remain independent of Spring and persistence.

## Current delivery

The 18 existing database tables have corresponding domain, application, and infrastructure contexts. REST supports identity administration, catalog management, reservation transitions, rental execution, delivery/return records, damage assessment, manual payment records, and invoice snapshots.

Authentication/JWT, a payment gateway, tax calculations, protection/evidence/consent extensions, OpenAPI, and frontend integration remain future work. Existing product-scope decisions are preserved; this delivery does not implement their unresolved commercial calculations.

## Conceptual model

![Conceptual entity–relationship model](docs/diagrams/conceptual-er-model.svg)

[Editable Excalidraw diagram](docs/diagrams/conceptual-er-model.excalidraw) · [Conceptual model](docs/conceptual-model.md) · [Fourth normal form](docs/fourth-normal-form.md)

The diagram retains the base 18 entities and their cardinalities. The [original assignment](ejercicioDemo.md) is preserved; PostgreSQL replaces its MySQL requirement by explicit project decision.

## Architecture

```text
domain/          # Aggregates, typed identities, invariants, events, and ports
application/     # Immutable commands/responses and coordinated use cases
infrastructure/  # REST, JPA entities/mappers/adapters, configuration, Flyway
docs/            # Model, architecture, API, and delivery documentation
```

Dependencies: `infrastructure -> application -> domain`; infrastructure also uses domain ports directly. Java release 21 and Spring Boot 3.5.16 remain centrally configured. Domain and application import only Java and the inner modules.

One table per context is this project's organizational convention. It does not permit independent writes that bypass a reservation or invoice transaction. `common` contains shared contracts and is not a nineteenth business context.

| Table | Context | Domain type |
| --- | --- | --- |
| `users` | `user` | `User` |
| `roles` | `role` | `Role` |
| `user_roles` | `userrole` | `UserRole` |
| `customers` | `customer` | `Customer` |
| `suppliers` | `supplier` | `Supplier` |
| `categories` | `category` | `Category` |
| `tools` | `tool` | `Tool` |
| `tool_images` | `toolimage` | `ToolImage` |
| `equipment_units` | `equipmentunit` | `EquipmentUnit` |
| `reservations` | `reservation` | `Reservation` |
| `reservation_items` | `reservationitem` | `ReservationItem` |
| `rentals` | `rental` | `Rental` |
| `unit_deliveries` | `unitdelivery` | `UnitDelivery` |
| `unit_returns` | `unitreturn` | `UnitReturn` |
| `damage_reports` | `damagereport` | `DamageReport` |
| `payments` | `payment` | `Payment` |
| `invoices` | `invoice` | `Invoice` |
| `invoice_items` | `invoiceitem` | `InvoiceItem` |

Each context follows `model/aggregate`, `model/valueobject`, `event`, `port/repository`; application uses `command`, `dto`, `exception`, `usecase`. Infrastructure places controllers/DTOs under `adapters/in/rest`, JPA entities/mappers/Spring Data interfaces under `adapters/out/persistence/{entity,mapper,repository}`, and port adapters directly under `adapters/out/persistence`.

## Protected business rules

- An equipment unit is a physical asset; tool images are independent offer records.
- Reservations contain distinct units from one supplier and one currency. Calendar dates are inclusive: October 14–15 bills two days; the same day bills one.
- Pending requests do not occupy inventory. Confirmation checks availability and saves frozen rates inside one transaction with ordered locks. Shared endpoints overlap.
- Reservation-item writes belong to reservation operations. Read-only item endpoints cannot change confirmed rates.
- Rejection accepts pending requests. Cancellation accepts pending/confirmed requests before a rental exists. Terminal states reject further changes.
- Returns require completed delivery. Rental completion requires every reserved unit to have been returned. A delivered rental cannot be cancelled.
- Payments are administrative records with positive amounts and explicit processor references. No external charge or refund is executed.
- Invoice headers and lines are created atomically. Customer/supplier names and documents are historical snapshots; issued lines cannot be edited independently.
- Identity profiles share their user key. Role assignments and other composite identities preserve the SQL keys. User responses never contain password hashes; new passwords use BCrypt.

## Authentication boundary

All `/api/**` routes require an authenticated `Principal` whose name is a UUID user subject. No authentication mechanism is installed in this delivery, so ordinary anonymous requests receive **401**. Tests inject principals; a request body or custom header cannot substitute for authentication.

Use cases check active users, ownership, and administrator access. Listing administrative resources requires `ADMIN`; active users can browse categories and published tools. Individual records use their ownership rules. Role codes `ADMIN`, `SUPPLIER`, and `CUSTOMER` are seeded by Flyway. No administrator account or password is seeded automatically.

See [REST API](docs/rest-api.md) for routes, request examples, and error codes.

## Build and verification

Requirements: JDK 21 and Docker with a running Linux engine for the default PostgreSQL Testcontainers suite.

```powershell
$env:JAVA_HOME = 'C:\path\to\jdk-21'
& "$env:JAVA_HOME\bin\java.exe" -version
.\mvnw.cmd -version
.\mvnw.cmd clean verify
```

The `java` executable on PATH may still be Java 17. Maven uses `JAVA_HOME`; use that same JDK's executable to run the packaged JAR. Maven Wrapper installs Maven, not Java.

If Docker is unavailable, run the same integration suite against an explicitly supplied PostgreSQL test database:

```powershell
.\mvnw.cmd '-Dpostgres.test.url=jdbc:postgresql://127.0.0.1:55439/toolrental_validation' '-Dpostgres.test.username=postgres' clean verify
```

Supply `-Dpostgres.test.password=...` when needed. Use a dedicated test database: the suite creates a unique schema and truncates its test records. It does not silently skip PostgreSQL tests when Docker is missing. Flyway/JPA startup, 18-table mapping completeness, HTTP contracts, snapshots, rollback, inclusive overlap, and concurrent confirmations are covered.

On Linux/macOS use `./mvnw`. Apply Java formatting with `mvnw.cmd spotless:apply` before verification.

## Run

```powershell
$env:SPRING_PROFILES_ACTIVE = 'local'
$env:DB_URL = 'jdbc:postgresql://localhost:5432/tool_rental'
$env:DB_USERNAME = 'tool_rental'
$env:DB_PASSWORD = 'change-me'
& "$env:JAVA_HOME\bin\java.exe" -jar infrastructure/target/infrastructure-0.1.0-SNAPSHOT.jar
```

Spring Boot starts Tomcat on port 8080. `local` uses configurable development defaults; `production` requires database environment variables. Flyway owns schema changes and Hibernate runs `ddl-auto: validate`. V1/V2 remain unchanged; V3 adds the administrator role.

The optional manual dataset is `infrastructure/src/main/resources/db/seed/demo-data.sql`. It is never loaded automatically and must not be used as production account provisioning.

## Documentation

- [Backend architecture](docs/backend-architecture.md)
- [Development guide](docs/development-guide.md)
- [Infrastructure structure](infrastructure/STRUCTURE.md)
- [REST API](docs/rest-api.md)
- [Validation report](docs/validation-report.md)
- [Database follow-up](docs/pendientes-base-datos.md)
- [Product scope](docs/product-scope.md) and [roadmap](docs/pendientes.md)

## Commit convention

Use English Conventional Commits with a Gitmoji shortcode and a compound English description:

```text
feat: :building_construction: AlignTableBoundedContexts
feat: :sparkles: ImplementRemainingBoundedContexts
feat: :memo: DocumentBoundedContextsAndConceptualDiagram
```
