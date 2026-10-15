# Alignment with SpringBootDataJPA-DDD

Reference: [TuMinipeka/SpringBootDataJPA-DDD, main branch](https://github.com/TuMinipeka/SpringBootDataJPA-DDD). This project adopts the three-module layout and relative package conventions while preserving its own tool-rental domain and business rules.

## Mapping

| Reference pattern | Tool Rental Platform | Decision |
| --- | --- | --- |
| `application/common/exception/ApplicationException` | Same relative package and abstract type | Protected constructors for message and message/cause. |
| `application/<context>/{command,dto,usecase}` | `application/{catalog,reservation}/{command,dto,usecase}` | Contexts represent business capabilities, not tables. |
| `domain/common/event/DomainEvent` | Same relative package and interface | Uses `Instant` and injected `Clock`; common method is `occurredOn()`. |
| `domain/common/model/AggregateRoot` | Same relative package and abstract type | Protected event recording, immutable access, and event clearing. |
| `domain/<context>/event` | `domain/catalog/event` and `domain/reservation/event` | Concrete registration and reservation-transition events. |
| `register` / `restore` factories | Used by all four aggregates | `restore` preserves state and emits no new events. |
| `domain/<context>/model/{aggregate,valueobject}` | Same relative organization | `ReservationItem` remains under `model/entity` because it is internal to `Reservation`. |
| `domain/<context>/port/repository` | Domain-owned output contracts | JPA adapters implement them without moving the interfaces outward. |
| `infrastructure/<context>/config` | `CatalogBeanConfiguration` and `ReservationBeanConfiguration` | Constructor-based assembly when required ports exist. |
| REST input adapters | `adapters/in/rest/{controllers,dtos}` | Directory template only; no endpoint is implemented. |
| Persistence output adapters | `adapters/out/persistence/{entity,mapper,repository}` | Implemented for catalog, participant reads, and reservations. |

## Error hierarchies

- `ApplicationException` is the base for application failures such as missing resources.
- `DomainException` is the base for domain failures; `BusinessRuleException` represents violated invariants.
- `InfrastructureException` and `PersistenceException` define the technical failure hierarchy for adapters.

Concrete exceptions stay with their context. `CustomerId` and `SupplierId` are typed references until the identity context is implemented.

## Runtime configuration

Context configurations use `@Bean` and domain ports. Spring discovers the production persistence adapters, so the related use cases are assembled when PostgreSQL infrastructure is available.

Creation and state transitions receive an explicit `Clock`. `domainEvents()` exposes pending events as an immutable list, and `clearDomainEvents()` clears them after a future publisher persists them. No broker, automatic publisher, or Outbox exists yet.

The empty REST directories contain `.gitkeep` because Git does not track empty directories. Persistence directories now contain JPA entities, mappers, Spring Data repositories, and port adapters.

## Naming

All source identifiers are English. Java follows `PascalCase`/`camelCase`. PostgreSQL tables use plural `snake_case`, including natural irregular plurals such as `categories` and `countries`.

## Verification

Run `mvnw.cmd clean verify` on Windows or `./mvnw clean verify` on Linux/macOS with Java 21. Existing tests cover domain rules, application coordination, event behavior, and layer direction. PostgreSQL mapping, migration, transaction, and concurrency coverage remains pending.
