# Backend architecture

## Dependency and context boundaries

The Java 21 / Spring Boot 3.5.16 Maven reactor follows [SpringBootDataJPA-DDD](https://github.com/TuMinipeka/SpringBootDataJPA-DDD). The modules remain `domain`, `application`, and `infrastructure`, with inward dependencies only.

The project explicitly organizes the existing 18 tables as 18 contexts. This is a package convention, not a general DDD rule that tables always define business boundaries. See the README for the full mapping. `common` is shared code, not a table context.

```text
domain/<context>/{event,model/aggregate,model/valueobject,port/repository}
application/<context>/{command,dto,exception,usecase}
infrastructure/<context>/adapters/in/rest/{controllers,dtos}
infrastructure/<context>/adapters/out/persistence/{entity,mapper,repository}
infrastructure/<context>/config
```

Port implementations sit directly in `adapters/out/persistence`; its `repository` child contains Spring Data interfaces. Empty classes are not used to manufacture a package tree. Read-only child contexts do not contain commands that would bypass owner invariants.

## Domain and application

Aggregate roots extend `AggregateRoot`; events implement `DomainEvent`. Explicit `restore` factories validate persisted facts without emitting creation events. Immutable commands and responses do not expose JPA entities. Typed UUID, numeric, shared, and composite identities preserve existing SQL keys. Money uses exact decimals and a currency. Technical timestamps come from `Clock`.

`ApplicationException` is abstract with protected message/cause constructors. Concrete not-found exceptions belong to their context; shared authorization/reference failures have specific application exception types. Domain rules use the domain exception hierarchy. HTTP translation resides in infrastructure.

`UnitOfWork` executes coordinated use cases through a Spring transaction adapter. Profile, payment, rental, delivery, return, damage, and invoice operations validate references and ownership before persistence. Financial transitions do not execute payment-provider operations.

`ReservationLine` is an immutable unit/rate snapshot used by the reservation aggregate. The separate `reservationitem` context owns its table mapping and read API; writes remain coordinated by reservation operations. The equivalent invoice-item context is read-only through REST and participates in invoice creation. Tool images have their own key, repository, and assignment/removal API.

## Atomic operations

`AtomicReservationManagement` locks the reservation, sorted equipment units, sorted tools, and sorted participant identities. It reloads the items through their sole JPA entity, rechecks profiles, publication, currency and operational status, checks inclusive overlaps, and saves the reservation and line rates in the same transaction.

Pending reservations do not occupy units. Confirmation freezes current catalog rates. A shared start/return date overlaps. Failed availability validation saves no confirmation. Cancellation releases inventory before rental creation; reservations with rentals cannot be cancelled through that operation.

`AtomicEquipmentUnitRegistration` serializes supplier-scoped inventory checks. PostgreSQL also enforces the case-insensitive supplier/inventory-code constraint through the existing trigger.

Operational use cases lock referenced records inside their transaction. Returns require completed deliveries; rental completion requires all reserved units returned. Invoice creation stores header and lines atomically with customer/supplier snapshots from their profiles. Issued invoice lines and recorded returns have no generic edit/delete API.

## Persistence and HTTP

There is one JPA entity per table, no duplicated element-collection mapping, and scalar identity references between contexts. Composite keys use infrastructure-only `@EmbeddedId` types. Role IDs retain generated `smallint`; customer, supplier, and rental keys remain shared. Mappers preserve category description, equipment serial/condition, reservation expiry, and every other mapped column.

Flyway keeps one migration per base table. Later incremental migrations align currency mappings and seed `ADMIN` without rewriting applied history. Hibernate validates the schema instead of creating it. Local and production configuration filenames match their profile names.

REST controllers map HTTP input to commands and delegate to application. `/api/**` requires an authenticated UUID Principal; no JWT or login endpoint exists yet. Anonymous callers receive 401; unauthorized active users receive 403; invalid contracts return 400, missing resources 404, and business/persistence conflicts 409. Raw database details and password hashes are never included in error or user responses.

## Validation and remaining work

Architecture checks cover module independence and exception/event/aggregate hierarchies. Mapping contracts compare every V1 column with exactly one JPA owner. PostgreSQL integration tests exercise startup, Flyway, HTTP, composite keys, password hashing, immutable rates, invoice snapshots, rollback, and concurrent confirmations. Docker/Testcontainers is the default; an explicit isolated PostgreSQL URL supports validation when Docker is unavailable.

JWT authentication, OpenAPI, gateway settlement, advanced taxes, protection plans, evidence, consent versioning, deployment hardening and frontend integration remain future deliveries. No external event publisher or Outbox is installed.
