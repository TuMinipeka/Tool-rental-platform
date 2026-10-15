# Pending database work

## Current state

The base 18 tables have complete JPA mappings, scalar references, shared/composite keys, application use cases, and REST adapters. Flyway V1/V2 are unchanged; V3 seeds ADMIN. The integration suite validates fresh migration, Hibernate schema validation, HTTP contracts, rate snapshots, invoice snapshots, rollback and concurrent confirmations against real PostgreSQL.

Docker/Testcontainers is the default test path. An explicit dedicated PostgreSQL URL supports the same assertions without Docker. Operational environment provisioning, migration upgrades from representative production data, least-privilege database roles, backup/restore, retention, monitoring and timeout tuning remain pending.

The manual demo dataset remains separate from Flyway and is not production account provisioning.

## Schema additions required by the approved product scope

These are design requirements, not implemented tables. They require versioned Flyway migrations after the remaining Phase 0 parameters are approved.

### Pricing and taxes

- Store a reservation pricing snapshot with rental subtotal, delivery charge, protection charge, discounts, taxable base, VAT rate, VAT amount, rounding, and final total.
- Preserve the tax-rule or calculation-method version used for each transaction.
- Preserve the equipment cost or other input required by the legally validated taxable-base calculation without allowing later catalog changes to rewrite historical totals.
- Store invoice line tax detail instead of deriving historical tax from the current configuration.

### Optional Damage Protection Plan

- Version Protection Plan definitions separately from reservations.
- Store the selected plan version, price, deductible, limit, accepted exclusions, and acceptance timestamp with each protected reservation.
- Keep plan terms independent from damage reports. Combining multiple plan clauses and multiple damage reports in one row would create false cross-products and update anomalies; separate relations preserve the current 4NF direction.

### Delivery, return, evidence, and disputes

- Support multiple evidence items per delivery, return, and damage report through separate evidence relations rather than URL lists or repeated columns.
- Store content integrity metadata, media type, creation timestamp, author, and storage reference for each evidence item.
- Add repair estimates, replacement-value snapshots, assessment decisions, customer notices, dispute records, administrator decisions, and adjustment history.
- Ensure one loss is not charged as both full repair and full replacement.

### Payments and refunds

- Replace the current generic payment shape with an auditable payment-intent and transaction model that supports the 50/50 schedule, additional charges, refunds, idempotency keys, attempts, and failure reasons.
- Store simulated method type, processor, card network, token, last four digits, and expiration metadata without storing a complete card number or CVV.
- Preserve the relationship between every refund or adjustment and its original charge.

### Legal documents and consent

- Version Terms and Conditions, privacy notices, and Protection Plan terms as immutable published documents.
- Record each user's explicit acceptance by document version, purpose, timestamp, related transaction where applicable, and integrity reference.
- Keep required transaction acceptance, personal-data authorization, optional marketing consent, and optional protection selection as independent facts. A combined consent row would force unrelated multi-valued facts into false combinations and undermine revocation history.

### Simulated invoices

- Add subtotal, taxable base, VAT, total, currency, and academic-document markers.
- Keep supplier and customer identity snapshots so historical documents are not rewritten when profiles change.
- Prevent simulated documents from being represented as DIAN-valid invoices.

## Capabilities that still lack persistence adapters

- User, role, customer, and supplier administration.
- Rentals, deliveries, returns, and damage reports.
- Protection definitions, selections, assessments, evidence, and disputes.
- Payment intents, attempts, refunds, and adjustments.
- Taxes, pricing snapshots, invoices, and invoice lines.
- Legal-document publication and acceptance.
- Durable event publication through an Outbox or another transactional mechanism.
- Administrative reports and optimized operational queries.

Existing tables for some of these capabilities preserve the earlier relational model, but they must not be presented as implemented functionality until their domain behavior, use cases, adapters, and updated constraints exist.

## Completion criterion

The database is ready only when a new PostgreSQL instance can apply every migration, start the application with `ddl-auto: validate`, pass the approved integration and concurrency scenarios, support the complete product contract, and use verified backup, restore, security, and deployment procedures.
