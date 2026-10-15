# Project roadmap and pending work

## Purpose

This document records the agreed delivery order, the decisions already closed, and the decisions that still block implementation. It prevents one layer from depending on an unstable contract.

## Confirmed product decisions

- The complete product includes catalog, inventory, reservations, rentals, deliveries, returns, damage management, simulated payments, simulated invoicing, administration, and reporting.
- The initial roles are Customer, Supplier, and Administrator. The Supplier handles delivery and return activities.
- A reservation charges 50% when the supplier confirms it and the remaining 50% after return and rental completion.
- Debit card, credit card, PayPal, Visa, and Mastercard are simulated. Visa and Mastercard are card networks, not separate payment processors.
- The Optional Damage Protection Plan is optional, off by default, and is not presented as insurance.
- Protection prices, deductibles, limits, and exclusions are versioned commercial parameters. They are not described as percentages required by Colombian law.
- Prices use Colombian pesos. The general VAT reference is 19%, while the special taxable-base rule for movable-property rental must be implemented and professionally validated before a real launch.
- The current environment issues academic simulation documents, not tax-valid DIAN invoices.
- The approved scope and legal safeguards are detailed in [Product scope](product-scope.md).
- The future public terms page uses the draft in [Terms and Conditions](terms-and-conditions.md).

## Confirmed technology

| Area | Technology | Current status |
| --- | --- | --- |
| Backend language | Java 21 | Configured |
| Backend framework | Spring Boot 3.5.16 | Configured |
| Application server | Embedded Apache Tomcat | Supplied by Spring Boot; no HTTP API exists yet |
| Build | Multi-module Maven | Configured |
| Architecture | Hexagonal architecture and DDD | Implemented for current contexts |
| Database | PostgreSQL | Schema and configuration prepared; instance pending |
| Persistence | Spring Data JPA and Hibernate with `ddl-auto: validate` | Implemented for catalog and reservations |
| Migrations | Flyway | Two migrations available |
| Development data | Manual SQL seed with five distinct rows per table | Prepared |
| API | REST with OpenAPI and Swagger UI | Confirmed, not implemented |
| Frontend | React with Node.js tooling, HTML, and CSS | Confirmed, not created |

Node.js is used only for the frontend toolchain. Java and Spring Boot remain the only backend stack.

## Remaining Phase 0 decisions

The following business choices must be approved before their implementation:

1. Cancellation windows, cancellation charges, no-show handling, and the relationship with statutory withdrawal rights.
2. Late-return grace period, extension approval, and late charges.
3. Delivery and collection modes, geographic limits, fees, and failed-delivery handling.
4. Initial Protection Plan price, deductible, monetary limit, exact exclusions, and dispute period. These values must be identified as project assumptions until supported by a real provider proposal.
5. Replacement-value calculation and depreciation policy for lost or irreparable equipment.
6. Identity of the future operator, customer-service channels, privacy-policy details, and retention periods.
7. Frontend language choice between JavaScript and TypeScript, state management, component library, and testing tools.
8. API versioning, error format, JWT renewal, account recovery, and session revocation.
9. Hosting provider, CI/CD, domains, secrets, observability, backups, and incident response.

Phase 0 finishes when these decisions have acceptance criteria and priority. Wireframes may start during this phase, but API integration waits for an approved OpenAPI contract.

## Phase 1: executable database

1. Provision local PostgreSQL.
2. Apply Flyway migrations to an empty database.
3. Run `infrastructure/src/main/resources/db/seed/demo-data.sql`.
4. Review keys, constraints, and representative queries against the real engine.
5. Add migrations for approved payment, tax, protection, legal-document, evidence, and dispute requirements.
6. Add Testcontainers coverage for migrations, repositories, rollback, and concurrency when the team begins the testing work.

Exit criterion: a new database can be created repeatably, the application validates its mappings, and the approved schema supports the complete product contract. See [Pending database work](pendientes-base-datos.md).

## Phase 2: backend and API contract

1. Complete the approved aggregates and use cases.
2. Implement identity, authentication, and authorization.
3. Add Spring Web, input validation, and uniform error handling.
4. Implement the complete REST API.
5. Publish and approve the OpenAPI contract through Swagger UI.
6. Implement simulated payment, protection, tax, invoicing, evidence, and dispute adapters.

Exit criterion: all approved journeys work against PostgreSQL and the versioned HTTP contract is stable.

## Phase 3: shared integration environment

Build a reproducible backend image and deploy a development or staging environment with PostgreSQL, automatic migrations, isolated secrets, controlled data, logs, and health checks.

Exit criterion: frontend and quality work can use one shared API without depending on a developer workstation.

## Phase 4: frontend

1. Create the React project using Node.js tooling.
2. Establish navigation, accessibility, loading, empty, success, and error states.
3. Implement authentication and role-aware navigation.
4. Implement catalog, availability, reservations, rentals, payments, returns, damage review, invoices, and administrative screens.
5. Implement the public `/terms-and-conditions` page and link it from the footer and all required acceptance points.
6. Integrate only against the approved OpenAPI contract.

Exit criterion: the three roles can complete their approved journeys through a responsive interface.

## Phase 5: end-to-end validation and hardening

Testing is added with each implemented behavior; this phase completes cross-cutting validation:

- domain and application unit coverage;
- PostgreSQL migration, mapping, rollback, and concurrency coverage;
- REST contract and authorization coverage;
- end-to-end coverage of each role's critical journeys;
- price, VAT, split-payment, refund, and damage-calculation scenarios;
- accessibility, security, performance, backup, and restore validation.

Exit criterion: acceptance criteria pass, no critical defect remains open, and accepted risks are documented.

## Phase 6: production deployment

Complete reproducible builds, CI/CD, managed infrastructure, TLS, domains, secrets, least-privilege access, migration and rollback procedures, monitoring, alerts, backups, restore exercises, and an incident-response plan.

A real commercial launch additionally requires professional Colombian legal, tax, privacy, and security review. Until that review and a DIAN integration are complete, deployments remain academic simulations.

## Immediate next decision

Finish Phase 0 one rule at a time. The next recommended decision is the cancellation and no-show policy because it affects reservation totals, refunds, consumer information, backend state transitions, database history, and frontend copy.
