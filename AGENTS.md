# Repository working rules

These instructions apply to the entire repository. Read `docs/development-guide.md` and `docs/backend-architecture.md` before changing code.

## Required architecture

- Keep the Maven modules `domain`, `application`, and `infrastructure`.
- Preserve the dependency direction `infrastructure -> application -> domain`.
- `domain` may depend only on Java. Do not add Spring, JPA, HTTP, JSON, or database details.
- `application` may depend on `domain` and Java, never on `infrastructure` or frameworks.
- Ports belong to the domain; adapters that implement them belong to infrastructure.
- Organize code first by the table-aligned context, then by technical responsibility.
- Use one context per existing table as the project organizational convention. Cross-context invariants retain their atomic boundaries.

## Language and naming

- Write filenames, source identifiers, comments, messages, test names, configuration keys, and documentation in English.
- Keep the root package `com.toolrental`.
- Use `PascalCase` for Java types, `camelCase` for members, and lowercase package names.
- Use the established suffixes `UseCase`, `Command`, `Response`, `Repository`, `Controller`, and `Configuration`.
- Use plural `snake_case` PostgreSQL table names and singular `snake_case` columns.
- Use natural English plurals: `category -> categories`, `country -> countries`, `person -> people`. Do not append `s` mechanically when English requires another form.

## Required locations

```text
domain/<context>/model/{aggregate,entity,valueobject}
domain/<context>/{event,port/repository}
application/<context>/{command,dto,usecase,exception}
infrastructure/<context>/adapters/in/rest/{controllers,dtos}
infrastructure/<context>/adapters/out/persistence/{entity,mapper,repository}
infrastructure/<context>/config
```

Do not add speculative empty classes or abstractions. Empty adapter directories already agreed by the project may use `.gitkeep`.

## Domain and application design

- Put invariants in aggregates and value objects. Do not expose generic setters or mutable collections.
- Use typed IDs, `Money`/`BigDecimal`, `LocalDate` for reservation dates, and `Instant` with an injected `Clock` for timestamps.
- Use cases coordinate domain behavior and ports; they do not know HTTP or JPA.
- Application inputs and outputs are immutable. Never expose JPA entities or mutable aggregates through the API.
- Keep availability checks and reservation confirmation inside one atomic boundary.
- `restore` factories rebuild persisted state without emitting new domain events.
- An HTTP actor identifier must come from the authenticated principal, not from a freely supplied request-body field.

## Infrastructure and database

- Keep JPA entities, mappers, and repository adapters separate.
- Change the schema only through versioned Flyway migrations; do not use destructive schema generation.
- Reinforce keys, uniqueness, references, and relevant invariants in PostgreSQL.
- Keep controllers thin: validate the HTTP contract, create a command, invoke one use case, and map its result or error.
- Use constructor injection and configuration grouped by business context.

## Quality and documentation

- Add tests in the layer that owns each new behavior. Persistence and concurrency require real integration tests.
- Run `mvnw.cmd clean verify` on Windows or `./mvnw clean verify` on Linux/macOS before completion.
- Apply Java formatting with `mvnw.cmd spotless:apply` or `./mvnw spotless:apply` when required.
- Update documentation whenever a rule, contract, structure, technology, or implementation status changes.
- Do not describe templates or planned work as implemented.
- Treat `docs/product-scope.md` as the approved product contract and `docs/pendientes.md` as the delivery roadmap. Record unresolved commercial rules instead of inventing values in code.
- Keep Terms and Conditions, privacy notices, protection terms, and user acceptances versioned. Never preselect optional consent or describe the internal Damage Protection Plan as insurance.
- Keep tax rates, tax-base rules, protection prices, deductibles, and monetary limits explicit and historically reproducible; do not bury changeable commercial or legal values in application code.
- Use the English Conventional Commit format shown in `README.md`.
