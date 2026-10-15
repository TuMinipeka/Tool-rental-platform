# Base-context validation report

Date: 2026-10-15. Implementation commit: `8f39123`.

## Verified environment

- JDK 21.0.12.1, Spring Boot 3.5.16, Maven Wrapper.
- Real PostgreSQL 18.6 in an isolated temporary cluster bound to 127.0.0.1:55439. Existing development databases were not used.
- Docker Desktop could not start its engine because of an internal socket access error. The Testcontainers startup path was not executed here; the same integration suite ran through its explicit PostgreSQL URL option. Default container tests target PostgreSQL 17, supported by the managed Flyway version.

## Result

```powershell
.\mvnw.cmd '-Dpostgres.test.url=jdbc:postgresql://127.0.0.1:55439/toolrental_validation' clean verify
```

**BUILD SUCCESS — 46 tests, 0 failures, 0 errors, 0 skipped.** Domain: 19; application: 13; infrastructure: 14, including 10 PostgreSQL/HTTP integration scenarios. Spotless verification passed.

Coverage includes all 18 entity mappings and V1 columns; Flyway V1–V3 on a fresh schema; Hibernate validation; Principal/ownership errors; password hashing; composite/shared keys; category description and equipment serial/condition; reservation-expiry round trip; inclusive day conflicts; frozen rates; one winner under concurrent confirmations; release after cancellation; one winner under concurrent supplier-inventory registration; delivery/return ordering; rental completion; immutable invoice snapshots; and transaction rollback after a later invalid invoice line.

The original assignment and applied V1/V2 migration files were checked unchanged. `git diff --check` passed.

## Packaged application

The packaged infrastructure JAR was started with JDK 21 against the temporary PostgreSQL instance on HTTP port 18081. Flyway, Hibernate, repository wiring and Tomcat initialized successfully. An anonymous request to `/api/categories` returned **401**, matching the agreed pre-JWT authentication boundary.

Temporary application/database processes were stopped after validation. The production deployment, default Docker startup path, JWT and payment-provider behavior are not established by these checks.
