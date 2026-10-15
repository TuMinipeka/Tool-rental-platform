# Product scope

## Purpose

Tool Rental Platform is a realistic university simulation of a Colombian marketplace for renting construction tools and equipment. The target product must support the complete rental lifecycle while keeping the academic environment clearly separated from a real commercial operation.

Phase 0 defines the product contract. A capability described here is not implemented until the repository status says so explicitly.

## Confirmed actors

The first complete release has three roles:

| Role | Responsibilities |
| --- | --- |
| Customer | Browse the catalog, request reservations, select optional damage protection, make simulated payments, receive tools, return tools, review damage assessments, and access simulated invoices. |
| Supplier | Publish tools, manage physical units and availability, confirm or reject reservations, record delivery and return conditions, report damage, and review rental payments. |
| Administrator | Manage users and roles, moderate catalog data, configure commercial parameters, review disputes and damage evidence, and monitor the platform. |

The supplier performs delivery, return, and damage-handling activities in the initial scope. There is no separate logistics role.

## Confirmed functional scope

The software must eventually provide all of the following as working, integrated capabilities:

1. Identity, authentication, and authorization for the three roles.
2. Tool catalog, categories, images, physical equipment units, publication, and availability.
3. Reservation request, supplier confirmation or rejection, customer cancellation, and prevention of overlapping confirmed reservations.
4. Rental activation, delivery scheduling, delivery evidence, return registration, and condition comparison.
5. Optional damage protection, damage evidence, assessment, customer notification, dispute handling, and additional charges where justified.
6. Simulated payment processing and refunds.
7. Simulated invoicing with itemized prices and taxes.
8. Administration, operational reporting, auditability, and legal-document acceptance.
9. A responsive React interface consuming the published REST/OpenAPI contract.

## Technology decisions

| Area | Decision |
| --- | --- |
| Backend language | Java 21 |
| Backend framework | Spring Boot 3.5.16 |
| Server | Embedded Apache Tomcat supplied by Spring Boot |
| Backend architecture | Hexagonal architecture and domain-driven design |
| Database | PostgreSQL |
| Persistence | Spring Data JPA, Hibernate, and Flyway |
| API | Versioned REST API documented with OpenAPI and Swagger UI |
| Frontend | React, with Node.js used only for frontend tooling |
| Markup and styling | HTML and CSS within the React application |

Node.js is not a second backend. Java and Spring Boot remain responsible for business logic, persistence, security, and the HTTP API.

## Reservation and payment rules

- A reservation is requested by a customer and confirmed or rejected by the supplier.
- Confirmation must revalidate availability atomically.
- The first simulated charge is 50% of the displayed reservation total when the supplier confirms the reservation.
- The remaining 50% is charged when the rental is completed and the return is accepted.
- Approved extensions, damage, loss, or other authorized adjustments are separate, itemized charges; they must never be hidden inside the remaining balance.
- Supported simulated payment presentations are debit card, credit card, and PayPal. Card simulations may display Visa or Mastercard as the card network.
- The platform stores only a simulated processor token, network, last four digits, and expiration metadata needed by the demonstration. It never stores a complete card number or CVV.
- Payment states must distinguish at least pending, approved, rejected, cancelled, and refunded transactions.
- Every payment and refund must be idempotent and auditable.

## Prices and Colombian tax rules

- Monetary values are expressed in Colombian pesos (`COP`) unless an explicitly supported currency is added later.
- The current general VAT reference is 19%. This is a legal rate, not a configurable commercial percentage.
- The VAT calculation must not assume that the full daily rental fee is always the taxable base. Article 1.3.1.7.1 of Decree 1625 of 2016 defines a special taxable base for rentals of tangible movable property. The production formula must be validated by a Colombian accountant or tax lawyer before real invoicing.
- The configured VAT rate, taxable-base method, rounding result, and legal rule version used for a transaction must be recorded or snapshotted so later configuration changes do not alter historical totals.
- The checkout must show the rental amount, delivery charges, optional protection, discounts, taxable base, VAT, adjustments, and final total before acceptance.
- Customer-facing prices must be clear and must not reveal mandatory charges only after confirmation.
- The current project issues academic simulation documents only. They must state `Academic simulation - not a tax-valid DIAN invoice` until a real DIAN electronic-invoicing integration and taxpayer configuration exist.

## Optional damage protection

The feature is named **Optional Damage Protection Plan**. It must not be marketed as insurance, a policy, or coverage issued by an insurer. Insurance activity in Colombia requires an authorized insurer or intermediary.

- Selection is optional, off by default, and requires an explicit customer action.
- The price, deductible, monetary limits, covered events, exclusions, and effective period must be visible before acceptance.
- The plan fee and deductible are commercial parameters. Colombian law does not establish one universal percentage for tool-rental protection, so the system must not describe a configured value as a legally required or market-wide rate.
- Parameters must be versioned. A reservation keeps the exact terms and monetary values accepted by the customer.
- The plan does not remove the customer's right to inspect evidence or dispute an assessment.
- Fraud, intentional damage, prohibited use, unauthorized transfer, unreported loss, and other approved exclusions may fall outside protection only when clearly disclosed before acceptance.

## Damage, loss, and additional-charge rules

1. Delivery and return use timestamped condition checklists and photographic evidence for each equipment unit.
2. The supplier creates a damage report describing the difference between outbound and inbound condition.
3. The assessment identifies whether the unit is repairable and attaches the repair quote or replacement-value evidence.
4. Without protection, the customer may be charged the documented repair cost, capped at the documented replacement value. The same loss cannot be charged twice.
5. With protection, the customer pays the accepted deductible or other accepted participation amount for covered damage, subject to the plan limit and exclusions.
6. Total loss or an irreparable unit uses the replacement-value snapshot agreed for the rental, not an undocumented value selected after return.
7. The customer receives the evidence and itemized calculation before an additional charge and has a documented opportunity to dispute it.
8. An administrator resolves escalated disputes. Every decision and adjustment is auditable.

## Consumer and electronic-commerce safeguards

The interface and API must support, rather than contradict, the following safeguards:

- identify the supplier and platform operator and provide contact information;
- show essential tool characteristics, availability, complete prices, payment methods, delivery conditions, and offer validity before checkout;
- provide a final order summary that the customer can review and correct before acceptance;
- retain evidence of the customer's acceptance of the applicable terms and protection-plan version;
- provide the legally applicable withdrawal, cancellation, refund, complaint, and payment-reversal channels;
- include a visible link to the Colombian consumer-protection authority;
- collect prior, informed, and consultable authorization for personal-data processing unless a statutory exception applies;
- minimize personal and payment data and provide channels to exercise data-subject rights.

## Terms and Conditions page contract

The React application must implement a public route at `/terms-and-conditions` using the approved copy in [Terms and Conditions](terms-and-conditions.md).

The page must:

- be reachable without authentication;
- display document version, publication date, operator identity, contact channels, and whether the environment is a simulation;
- be linked from the global footer, registration, checkout, reservation confirmation, and protection selection;
- provide a visible link to the Superintendence of Industry and Commerce;
- remain readable and printable on mobile and desktop;
- never use a preselected acceptance checkbox;
- record the accepted version, user, timestamp, purpose, and relevant transaction when acceptance is required;
- keep acceptance of Terms and Conditions separate from optional marketing authorization and optional damage protection.

The page is specified but not yet implemented because the repository does not yet contain the React application. Its implementation belongs to the frontend phase after the REST/OpenAPI contract is stable.

## Legal references and release limitation

The design is based on the following official sources:

- [Law 1480 of 2011: Consumer Protection Statute](https://www.funcionpublica.gov.co/eva/gestornormativo/norma.php?i=44306), particularly distance sales, withdrawal, electronic commerce, and payment reversal.
- [Decree 587 of 2016: payment reversal procedure](https://www1.funcionpublica.gov.co/eva/gestornormativo/norma.php?i=69037).
- [Law 1581 of 2012: personal-data protection](https://sedeelectronica.sic.gov.co/transparencia/normativa/ley-estatutaria-1581-de-2012).
- [Law 527 of 1999: electronic commerce and data messages](https://www.funcionpublica.gov.co/eva/gestornormativo/norma.php?i=4276).
- [Decree 1625 of 2016: VAT taxable base for tangible movable-property rental](https://normograma.dian.gov.co/dian/compilacion/docs/pdf/decreto_1625_2016.pdf).
- [Superintendence of Finance: authorized insurers and insurance intermediaries](https://www.superfinanciera.gov.co/publicaciones/15491/industrias-supervisadasindustria-aseguradorainformacion-general-aseguradoras-e-intermediarios-de-seguros-15491/).

These documents guide an academic simulation; they are not a substitute for legal, tax, accounting, or information-security review. A real commercial launch is blocked until the operator's identity, cancellation policy, protection parameters, tax treatment, invoicing obligations, privacy policy, and final legal copy are reviewed by qualified Colombian professionals.
