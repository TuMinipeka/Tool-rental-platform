# Construction Tool and Equipment Rental Platform

> English transcription of the original project brief. Project-specific decisions documented in `README.md` take precedence where they intentionally differ, such as Java 21 and PostgreSQL.

## Mandatory constraints

1. Implement hexagonal architecture, DDD, and a modular project.
2. Provide a main README, build documentation, and deployment documentation for Docker or native Tomcat.
3. The frontend may use React, Angular, Vue, or vanilla JavaScript.
4. Normalize the relational model through fourth normal form (4NF).

## Project context

Contractors, companies, and individuals often need specialized construction equipment without buying it. Reservation, availability, return, and payment processes are frequently manual or poorly optimized.

The platform must allow suppliers to register inventory and customers to rent tools, schedule deliveries, and manage payments efficiently.

Key capabilities:

- register and manage tools, availability, condition, and rental cost;
- reserve equipment, pay online, and generate invoices;
- provide supplier and customer dashboards with rental history;
- manage returns and the condition of rented equipment;
- automate inventory and rental control.

## User roles

### Platform administrator

- Register and manage suppliers and customers.
- Inspect rented-equipment status and rental history.
- Manage damage reports.
- Monitor payments and invoicing.
- Review revenue, most-rented equipment, usage, and profitability reports.

### Equipment supplier

- Add, edit, and remove tools.
- Define availability and rental prices.
- Accept or reject rental requests.
- Confirm returns and report damage.
- Generate invoices for completed rentals.

### Customer

- Register and sign in with JWT.
- Browse available tools and prices.
- Choose dates and request a rental.
- Pay online and download invoices.
- Review rental history.

## Frontend requirements

- Responsive behavior on mobile, tablet, and desktop.
- Clear, modern, and intuitive navigation.
- Role-specific dashboards for administrators, suppliers, and customers.
- Authentication and API interaction through JWT.
- HTML, CSS, and JavaScript are allowed; Bootstrap, Tailwind CSS, Vue, or React may be used when authorized by the instructor.

Expected views include:

- administrator: users, global payments/invoices, rentals, returns, incidents, and reports;
- supplier: inventory, images, descriptions, reservation requests, returns, damage, and invoices;
- customer: category/availability search, prices, reservation dates, status, payments, invoices, and history.

## Requested backend technology

- Spring Boot, current stable version.
- Java 17 or newer; this repository standardizes on Java 21.
- Spring Security with JWT.
- Spring Data JPA.
- MySQL in the original brief; this repository intentionally standardizes on PostgreSQL.
- Swagger/OpenAPI documentation.
- Apache Tomcat deployment.

## Backend requirements

- JWT authentication and `ADMIN`, `SUPPLIER`, and `CUSTOMER` roles.
- CRUD operations for customers, suppliers, tools, reservations, and payments.
- Access restrictions through Spring Security.
- Custom error handling.
- Payments and automatic invoicing.
- Equipment availability control.
- CORS configuration for frontend communication.
- Modular and documented code with clear controller, service/use-case, repository, DTO, and entity responsibilities.

## Database requirements

- Creation and seed scripts.
- At least 20 records per entity for the final delivery.
- Defined relationships among users, suppliers, customers, tools, reservations, and payments.
- A relational diagram.
- Appropriate indexes and query optimization.
- Normalization through 4NF.

The repository's physical implementation will use PostgreSQL, versioned Flyway migrations, plural `snake_case` table names, and singular `snake_case` columns.

## Documentation requirements

The project documentation must include:

- project name and description;
- technology choices;
- clone, dependency installation, build, test, and run instructions;
- application configuration example;
- relational database diagram;
- API endpoint list with JSON examples;
- authentication and role explanation;
- unit and integration test instructions;
- Swagger/OpenAPI request and response examples.

## Functional expectations

### Supplier and customer management

- Register, edit, deactivate, and manage permissions by role.

### Tool and equipment management

- CRUD operations, condition, availability, images, and detailed descriptions.

### Reservations and rentals

- Date selection and confirmation.
- Availability validation before confirmation.
- Delivery and return records.

### Payments and invoicing

- Payment registration and validation.
- Automatic invoices and payment receipts.
- Payment history and reservation expiration.

### Reporting

- Rental revenue.
- Most-rented tools and frequent customers.
- Equipment availability and condition.

## Validation and delivery

- Exercise POST, GET, PUT, and DELETE requests with Postman.
- Test JWT authentication and protected routes.
- Simulate complete rental and payment flows.
- Configure embedded Tomcat for the backend.
- Deliver the source in GitHub with a structured README, SQL scripts, relational diagram, Swagger documentation, modular code, and frequent commits.

## Evaluation areas

- Project design and architecture.
- Logical and physical database models.
- CRUD operations and business logic.
- JWT security and authentication.
- Payments and invoicing.
- Reservations and availability.
- Reporting dashboard.
- README and Swagger documentation.
- GitHub repository management and commit history.
