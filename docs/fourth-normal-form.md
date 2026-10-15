# Normalization through fourth normal form

## Analysis basis

This analysis starts from the [conceptual model](conceptual-model.md). PostgreSQL is the selected database, and reservation periods use inclusive dates. Normalization evaluates relations and dependencies, not screens or diagrams.

- `X -> Y` means functional dependency.
- `X ->> Y` means multivalued dependency.
- A superkey determines an entire row.
- A candidate key is a minimal superkey.

The installed `database-normalization` skill, its 4NF reference, and its PostgreSQL example informed this review. Fourth normal form requires every non-trivial multivalued dependency to be determined by a superkey. It prevents independent one-to-many facts from being stored as a false cross-product.

## Database naming convention

PostgreSQL identifiers follow these rules:

- tables: English, plural, `snake_case`;
- columns: English, singular, `snake_case`;
- primary keys: `<singular_entity>_id` unless a composite natural relationship key is clearer;
- foreign keys reuse the referenced key name;
- constraints and indexes use descriptive `snake_case` names;
- natural English plurals are required: `category -> categories`, `country -> countries`, `person -> people`.

A country lookup entity would therefore use table `countries` and key `country_id`. In the current model, country is only an address value, so `unit_deliveries.country` remains a singular column rather than introducing an unnecessary table.

## Illustrative unnormalized relation

The assignment does not provide an original table. A single-row reservation document might look like:

```text
ReservationRecord(
  reservationId, dates, status, customerData,
  tools[{toolId, name, category, supplierName, currentRate,
         images[imageUrl], equipmentUnits[inventoryCode]}],
  reservedUnits[{equipmentUnitId, agreedRate}],
  payments[{externalReference, amount, status}],
  invoice{header, items[...]}
)
```

This shape cannot insert inventory without a reservation, repeats supplier/catalog data, and may delete the last copy of a tool description with the last reservation. Independent image and equipment-unit sets also create a false cross-product.

## First normal form

Each row records one fact and each column contains one value. Roles, images, reservation items, payments, and invoice items become separate relations. Dates become separate `start_date` and `return_date` columns; address components are explicit columns.

A provisional flat relation would be:

```text
reservation_units(
  reservation_id, equipment_unit_id, start_date, return_date,
  customer_id, customer_name, tool_id, tool_name, agreed_daily_rate
)

candidate key: (reservation_id, equipment_unit_id)
```

It is atomic but repeats header and catalog values.

## Second normal form

The provisional relation has partial dependencies:

```text
reservation_id -> start_date, return_date, customer_id, customer_name
equipment_unit_id -> tool_id, tool_name
(reservation_id, equipment_unit_id) -> agreed_daily_rate
```

Split reservation facts, equipment-unit facts, and the relationship:

```text
reservations(...)
equipment_units(...)
reservation_items(reservation_id, equipment_unit_id, agreed_daily_rate)
```

The agreed rate depends on the pair and represents a historical fact distinct from the catalog's current rate.

## Third normal form

Remaining transitive dependencies include:

```text
reservation_id -> customer_id -> billing_name, document_number
equipment_unit_id -> tool_id -> tool_name, supplier_id, category_id
tool_id -> supplier_id -> business_name
tool_id -> category_id -> category_name
```

Extract customers, tools, suppliers, and categories. Reservations retain only `customer_id`; equipment units retain `tool_id`; tools retain `supplier_id` and `category_id`. Reservations do not store live customer or catalog names.

Invoice snapshot values are deliberately historical attributes of the issued document and do not depend on current profile names.

## Boyce-Codd normal form

For every non-trivial functional dependency `X -> Y`, `X` must be a superkey. Review alternate identifiers such as email, role code, documents, invoice number, and processor reference—not only surrogate IDs.

Avoid this redundant relation:

```text
expanded_reservation_items(
  reservation_id, equipment_unit_id, tool_id, agreed_daily_rate
)

equipment_unit_id -> tool_id
```

`equipment_unit_id` does not identify a row across reservations, so the relation violates BCNF. Obtain the tool through `equipment_units` instead.

## Fourth normal form

Tool images and physical units are independent sets. The hypothetical relation:

```text
expanded_catalog(tool_id, image_url, equipment_unit_id)
```

contains these multivalued dependencies:

```text
tool_id ->> image_url
tool_id ->> equipment_unit_id
```

Three images and two units force six rows even though no image/unit pairing is a business fact. Adding one unit requires one row per image, and deleting combinations can accidentally lose an image.

Decompose it into:

```text
tool_images(tool_id, image_url)
equipment_units(equipment_unit_id, tool_id, inventory_code, ...)
```

Each independent fact is inserted once. This is a lossless decomposition under the stated independence rule. If an image later describes one specific physical unit, that becomes a different relation such as `equipment_unit_images`; it must not recreate every tool-image/unit combination.

Other independent sets also stay separate: `user_roles` from customer/supplier profiles, and `reservation_items` from `payments`. An N:M relation is not automatically a 4NF violation: `agreed_daily_rate` genuinely belongs to the reservation/unit pair.

## Resulting logical schema

PK = primary key, AK = alternate candidate key, FK = foreign key. `?` means optional. Flyway implements this model in [`V1__create_initial_schema.sql`](../infrastructure/src/main/resources/db/migration/V1__create_initial_schema.sql); the notation below remains the compact logical view.

```text
users(
  user_id PK, email AK, display_name, password_hash,
  account_status, registered_at
)

roles(
  role_id PK, code AK, description
)

user_roles(
  user_id PK/FK -> users,
  role_id PK/FK -> roles
)

customers(
  user_id PK/FK -> users,
  person_type, document_type, document_number, billing_name,
  AK(document_type, document_number)
)

suppliers(
  user_id PK/FK -> users,
  document_type, document_number, business_name,
  AK(document_type, document_number)
)

categories(
  category_id PK, name AK, description?
)

tools(
  tool_id PK,
  supplier_id FK -> suppliers.user_id,
  category_id FK -> categories.category_id,
  name, description, current_daily_rate, rate_currency,
  publication_status
)

tool_images(
  tool_id PK/FK -> tools,
  image_url PK
)

equipment_units(
  equipment_unit_id PK,
  tool_id FK -> tools,
  inventory_code, serial_number?, current_condition,
  operational_status
)

reservations(
  reservation_id PK,
  customer_id FK -> customers.user_id,
  created_at, start_date, return_date, expires_at?,
  reservation_status, currency
)

reservation_items(
  reservation_id PK/FK -> reservations,
  equipment_unit_id PK/FK -> equipment_units,
  agreed_daily_rate
)

rentals(
  reservation_id PK/FK -> reservations,
  started_at, rental_status
)

unit_deliveries(
  reservation_id PK,
  equipment_unit_id PK,
  scheduled_at, delivered_at?, outbound_condition?,
  address_line, city, region, country,
  FK(reservation_id, equipment_unit_id) -> reservation_items,
  FK(reservation_id) -> rentals
)

unit_returns(
  reservation_id PK,
  equipment_unit_id PK,
  received_at, inbound_condition, notes?,
  FK(reservation_id, equipment_unit_id) -> unit_deliveries
)

damage_reports(
  damage_report_id PK,
  reservation_id, equipment_unit_id,
  reported_at, description, report_status, assessed_cost?,
  FK(reservation_id, equipment_unit_id) -> unit_returns
)

payments(
  payment_id PK,
  reservation_id FK -> reservations,
  processor, external_reference, amount,
  payment_status, recorded_at, confirmed_at?,
  AK(processor, external_reference)
)

invoices(
  invoice_id PK,
  reservation_id AK/FK -> rentals,
  number AK, issued_at, invoice_status,
  issued_supplier_name, issued_supplier_document,
  issued_customer_name, issued_customer_document
)

invoice_items(
  invoice_id PK/FK -> invoices,
  line_number PK,
  issued_description, quantity, issued_unit_price
)
```

## Dependency audit

| Relation | Candidate keys / important determinant | 4NF result |
| --- | --- | --- |
| `users` | `user_id`; `email -> rest` | One account fact; 4NF. |
| `roles` | `role_id`; `code -> rest` | One role fact; 4NF. |
| `user_roles` | `(user_id, role_id)` | Binary relation; only trivial MVDs. |
| `customers` | `user_id`; `(document_type, document_number) -> rest` | One customer profile; 4NF. |
| `suppliers` | `user_id`; `(document_type, document_number) -> rest` | One supplier profile; 4NF. |
| `categories` | `category_id`; `name -> rest` | One category fact; 4NF. |
| `tools` | `tool_id -> rest` | Images and units are external; 4NF. |
| `tool_images` | `(tool_id, image_url)` | Binary relation; trivial MVDs. |
| `equipment_units` | `equipment_unit_id -> rest` | One ownership/status fact; 4NF. |
| `reservations` | `reservation_id -> rest` | Items and payments are external; 4NF. |
| `reservation_items` | `(reservation_id, equipment_unit_id) -> agreed_daily_rate` | Rate belongs to the pair; 4NF. |
| `rentals` | `reservation_id -> rest` | At most one rental per reservation; 4NF. |
| `unit_deliveries` | `(reservation_id, equipment_unit_id) -> rest` | One delivery fact; 4NF. |
| `unit_returns` | `(reservation_id, equipment_unit_id) -> rest` | Damage reports are external; 4NF. |
| `damage_reports` | `damage_report_id -> rest` | One report fact; 4NF. |
| `payments` | `payment_id`; `(processor, external_reference) -> rest` | One payment attempt; 4NF. |
| `invoices` | `invoice_id`; `reservation_id`; `number -> rest` | One immutable issued document; 4NF. |
| `invoice_items` | `(invoice_id, line_number) -> rest` | One issued line; 4NF. |

Result: 18 relations are in 4NF under the documented functional and multivalued dependencies. The migration adds physical constraints and indexes, but validation against a running PostgreSQL instance remains pending.

## Rules normalization does not enforce alone

| Rule | Future enforcement |
| --- | --- |
| Keys, references, positive values, `start_date <= return_date` | PostgreSQL constraints where locally expressible |
| One supplier per reservation; inventory code unique per supplier | Atomic service plus locking, or a justified physical redesign |
| No overlapping confirmed reservations | Lock/version the unit and check/save in one transaction |
| Profiles coherent with roles; non-empty submitted documents | Domain/application rules and transaction checks |
| Rental only from confirmed reservation; return only after delivery | State transitions and atomic validation |
| Immutable historical rate/currency/invoice | Domain write policy and persistence permissions |
| Payment idempotency | Unique `(processor, external_reference)` plus adapter retry policy |

The supplier-scoped uniqueness of `equipment_units.inventory_code` crosses `equipment_units -> tools -> suppliers` in this logical design. It must not be represented as a false local unique constraint on `inventory_code` alone. The physical model must choose an explicit transaction/locking strategy or a documented denormalization that preserves consistency.
