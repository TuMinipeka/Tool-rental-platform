# Development guide

## Purpose

This guide is the source of truth for extending Tool Rental Platform without eroding its architecture. The directory layout expresses ownership, dependency direction, and the location of business rules.

```text
infrastructure -> application -> domain
```

Domain is technology-independent. Application coordinates business behavior through ports. Infrastructure connects HTTP, Spring, PostgreSQL, and external services to those contracts.

Business scope, payment timing, tax safeguards, damage protection, and the future Terms and Conditions page are defined in [Product scope](product-scope.md). Unresolved decisions remain in [Project roadmap and pending work](pendientes.md); developers must not replace them with undocumented assumptions.

## Required structure

```text
domain/src/main/java/com/toolrental/domain/
  common/
  <context>/
    model/aggregate/
    model/entity/          # Only entities internal to an aggregate
    model/valueobject/
    event/
    port/repository/

application/src/main/java/com/toolrental/application/
  common/exception/
  <context>/
    command/
    dto/
    exception/
    usecase/

infrastructure/src/main/java/com/toolrental/infrastructure/
  common/
  <context>/
    adapters/in/rest/controllers/
    adapters/in/rest/dtos/
    adapters/out/persistence/entity/
    adapters/out/persistence/mapper/
    adapters/out/persistence/repository/
    config/
```

Current contexts are `catalog` and `reservation`. Add a context only for a coherent business capability, never merely because a table exists. Do not add speculative empty packages or classes; the existing `.gitkeep` adapter directories are the only planned placeholders.

## Layer responsibilities

### Domain

Domain contains:

- aggregates that protect invariants and valid state transitions;
- entities that exist only within an aggregate boundary;
- immutable value objects and typed IDs;
- events describing facts that already happened;
- ports expressing what business logic needs from the outside;
- domain exceptions.

Domain must not import Spring, JPA, JSON, HTTP, or PostgreSQL. It does not know how objects are persisted or presented. Collections are defensively copied and exposed as immutable values. Generic setters are not allowed.

### Application

Application contains use cases. A use case loads data through ports, validates actors and references, invokes domain behavior, and coordinates one atomic unit of work.

- `command`: immutable input for a state-changing operation;
- `dto`: immutable response for external consumers;
- `usecase`: orchestration of one user intention;
- `exception`: application failures such as a missing reference.

A use case does not know controllers, JPA entities, or Spring Data repositories. It does not expose aggregates for another layer to mutate.

### Infrastructure

Infrastructure contains replaceable technical details:

- controllers and HTTP DTOs;
- JPA entities and mappers;
- domain-port implementations;
- Spring configuration;
- database and external-service integrations.

Controllers stay thin. Persistence mappers call `restore` factories and must not emit creation events when reading existing data.

## Naming conventions

All repository-owned content is written in English.

| Element | Convention | Example |
| --- | --- | --- |
| Root package | `com.toolrental.<module>.<context>` | `com.toolrental.application.catalog` |
| Use case | intention + `UseCase` | `ConfirmReservationUseCase` |
| Command | intention + `Command` | `RequestReservationCommand` |
| Response | concept + `Response` | `ReservationResponse` |
| Data port | concept + `Repository` | `ToolRepository` |
| HTTP adapter | resource + `Controller` | `ReservationController` |
| Configuration | context + `Configuration` | `ReservationBeanConfiguration` |
| PostgreSQL table | plural `snake_case` | `equipment_units` |
| PostgreSQL column | singular `snake_case` | `supplier_id` |

Use natural English plurals. Examples: `category -> categories`, `country -> countries`, `person -> people`, `equipment_unit -> equipment_units`. Join tables combine plural concepts when that reads naturally, for example `user_roles`.

Use Java 21, immutability, composition, `final` classes when inheritance is not intended, records for data carriers, and explicit constructor dependencies. Google Java Format through Spotless defines Java formatting.

## Decisions requiring special care

### Transactions and concurrency

Availability lookup and confirmation persistence are one operation. `AtomicReservationManagement` exists to prevent two requests from confirming the same unit concurrently. A PostgreSQL adapter must lock or version explicitly and use a stable lock order. In-memory tests do not prove database concurrency.

### Money, dates, and time

- Represent amounts with `Money`, `BigDecimal`, and an explicit currency; never use `double`.
- `ReservationPeriod` uses inclusive `LocalDate` boundaries. Any shared date overlaps.
- Technical timestamps use `Instant` and an injected `Clock`.
- Do not introduce currency conversion, timezone conversion, taxes, or implicit rounding without a documented business decision.

### Persistence

JPA entities are not domain objects. Keep them in infrastructure and map explicitly. Change the schema through Flyway and validate it at startup. Important uniqueness and reference rules must also exist in PostgreSQL.

### Security

Ownership and role checks remain in use cases. When JWT exists, the input adapter obtains identity from the authenticated principal. A freely supplied `actorId` request field is not authentication.

### Events

Domain events record aggregate facts. No external publisher or Outbox exists yet. A future implementation must make aggregate persistence and event publication registration consistent.

## Feature workflow

1. Describe the intention and rules in business language.
2. Identify the responsible aggregate.
3. Add or modify value objects, behavior, and events in domain.
4. Define only the external ports that are actually required.
5. Add the application command, response, exceptions, and use case.
6. Add infrastructure adapters without leaking HTTP/JPA types inward.
7. Add unit, application, integration, and concurrency tests as applicable.
8. Update documentation and run the complete verification.

## Completion checklist

- [ ] New types are in the correct module, context, and package.
- [ ] Dependencies still point inward.
- [ ] Business rules live in the correct aggregate/value object or are coordinated by the appropriate use case.
- [ ] No mutable collection, JPA entity, or HTTP detail leaks inward.
- [ ] Money, dates, time, authorization, and concurrency were handled explicitly when relevant.
- [ ] PostgreSQL names use English plural `snake_case` tables and singular columns.
- [ ] New behavior and important failure paths are tested.
- [ ] Documentation matches the actual implementation status.
- [ ] `mvnw clean verify` succeeds.

ArchUnit checks dependency direction and core hierarchies. Spotless checks Java formatting during `verify`. These safeguards do not replace review of aggregate boundaries or transaction consistency.
