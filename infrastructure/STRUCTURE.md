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
| `domain/<context>/port/repository` | Domain-owned output contracts | Infrastructure will implement them without moving the interfaces outward. |
| `infrastructure/<context>/config` | `CatalogBeanConfiguration` and `ReservationBeanConfiguration` | Constructor-based assembly when required ports exist. |
| REST input adapters | `adapters/in/rest/{controllers,dtos}` | Directory template only; no endpoint is implemented. |
| Persistence output adapters | `adapters/out/persistence/{entity,mapper,repository}` | Directory template only; no JPA implementation exists. |

## Error hierarchies

- `ApplicationException` is the base for application failures such as missing resources.
- `DomainException` is the base for domain failures; `BusinessRuleException` represents violated invariants.
- `InfrastructureException` and `PersistenceException` reserve a technical failure hierarchy for future adapters.

Concrete exceptions stay with their context. `CustomerId` and `SupplierId` are typed references until the identity context is implemented.

## Runtime configuration

Context configurations use `@Bean` and domain ports. Conditional beans do not create fake production repositories: without adapters, only the replaceable `Clock` is configured and the application still starts without a database.

Creation and state transitions receive an explicit `Clock`. `domainEvents()` exposes pending events as an immutable list, and `clearDomainEvents()` clears them after a future publisher persists them. No broker, automatic publisher, or Outbox exists yet.

The empty REST and JPA directories contain `.gitkeep` because Git does not track empty directories. They are an agreed location, not an implementation claim.

## Naming

All source identifiers are English. Java follows `PascalCase`/`camelCase`. Future PostgreSQL tables use plural `snake_case`, including natural irregular plurals such as `categories` and `countries`.

## Verification

Run `mvnw.cmd clean verify` on Windows or `./mvnw clean verify` on Linux/macOS with Java 21. Tests cover domain rules, application coordination, event behavior, layer direction, Spring context startup, and conditional use-case assembly. They do not validate REST, JPA, PostgreSQL locking, or production transactions because those adapters are not implemented.
