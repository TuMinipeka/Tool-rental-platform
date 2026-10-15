# REST API

All routes start with `/api` and require an authenticated Principal with a UUID name. Authentication itself is a later delivery; ordinary anonymous HTTP calls return 401. No `actorId` field or header grants access. HTTP tests inject Principals directly.

Administrative collection lists require administrator access. Active users can browse categories and published tools; suppliers can also see their own unpublished offers. Individual records enforce their owner/participant rules; payment and role administration require ADMIN. Tool operations require the supplier actor. Profiles require their corresponding role before creation. Passwords are accepted only for account creation, must contain at least 12 characters and at most 72 UTF-8 bytes, and are stored with BCrypt. No response includes hashes.

## Routes

| Resource | Operations |
| --- | --- |
| `/categories` | POST, GET list, GET/PUT/DELETE `/{id}`; referenced categories cannot be deleted |
| `/tools` | POST, GET list, GET/PUT `/{id}`, DELETE `/{id}` unpublishes |
| `/equipment-units` | POST, GET list, GET/PUT `/{id}`; retirement is a terminal status |
| `/reservations` | POST, GET `/{id}`, POST `/{id}/confirmation`, `/rejection`, `/cancellation` |
| `/users` | POST, GET list, GET `/{id}`, PATCH `/{id}/status` |
| `/roles` | POST, GET list, GET/PUT `/{id}` |
| `/user-roles` | POST assignment, GET list, GET/DELETE `/entry?userId=...&roleId=...` |
| `/customers`, `/suppliers` | POST, GET list, GET/PUT `/{userId}` |
| `/tool-images` | POST, GET list, GET/DELETE `/entry?toolId=...&imageUrl=...` |
| `/reservation-items` | GET list, GET `/entry?reservationId=...&equipmentUnitId=...` |
| `/rentals` | POST with reservationId, GET list, GET `/{reservationId}`, PATCH `/{reservationId}/status` |
| `/unit-deliveries` | POST, GET list, GET `/entry?reservationId=...&equipmentUnitId=...`, PATCH `/entry/completion` with those query parameters |
| `/unit-returns` | POST, GET list, GET `/entry?reservationId=...&equipmentUnitId=...` |
| `/damage-reports` | POST, GET list, GET `/{id}`, PATCH `/{id}/assessment`, PATCH `/{id}/status` |
| `/payments` | POST manual record, GET list, GET `/{id}`, PATCH `/{id}/status` |
| `/invoices` | POST header with lines, GET list, GET `/{id}`, PATCH `/{id}/status` |
| `/invoice-items` | GET list, GET `/entry?invoiceId=...&lineNumber=...` |

Composite keys use query parameters to avoid ambiguous path handling of image URLs. These queries identify a resource; they never identify the authenticated actor.

## Input examples

Reservation creation derives customer identity from Principal:

```json
{"startDate":"2026-10-20","returnDate":"2026-10-21","equipmentUnitIds":["00000000-0000-0000-0000-000000000001"]}
```

A status operation accepts `{"status":"COMPLETED"}`. Valid transitions: user ACTIVE → SUSPENDED/DISABLED and SUSPENDED → ACTIVE/DISABLED; rental ACTIVE → COMPLETED/CANCELLED; payment PENDING → CONFIRMED/FAILED and CONFIRMED → REFUNDED; damage OPEN → ASSESSED/CLOSED and ASSESSED → CLOSED; invoice ISSUED → PAID/VOIDED. Damage assessment uses `{"cost":5}` and supplies the cost required for ASSESSED. Delivery completion uses `{"condition":"GOOD"}` and records server time.

Invoice creation includes `reservationId`, `number`, and nonempty `lines` with `description`, `quantity`, `unitPrice`. Issuer/recipient snapshots come from profiles, not freely supplied names. All lines are saved in the header transaction. It records a base invoice; it does not implement fiscal numbering, tax calculations or gateway settlement.

POST returns 201, successful GET/PUT/PATCH returns 200, supported DELETE returns 204. HTTP validation returns 400; missing Principal 401; denied ownership/role 403; missing resource 404; invariant/uniqueness/reference conflicts 409. Unsupported historical edit/delete operations return 405. Database messages are not exposed.

Application responses are immutable. Existing catalog/reservation responses retain typed ID objects (`{"value":"uuid"}`); the newly introduced table contexts expose named scalar key fields (`userId`, `invoiceId`, etc.). A future API version may standardize wire representation without changing domain identities.
