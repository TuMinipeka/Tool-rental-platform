# Infrastructure structure

Each of the 18 contexts follows the reference repository:

```text
<context>/
  adapters/in/rest/controllers/<Entity>Controller.java
  adapters/in/rest/dtos/                 # HTTP requests, separate from application commands
  adapters/out/persistence/<Entity>PersistenceAdapter.java
  adapters/out/persistence/entity/<Entity>JpaEntity.java
  adapters/out/persistence/entity/<Entity>JpaId.java  # Composite keys only
  adapters/out/persistence/mapper/<Entity>PersistenceMapper.java
  adapters/out/persistence/repository/<Entity>JpaRepository.java
  config/<Entity>BeanConfiguration.java
```

Controllers validate/map input and invoke use cases. Adapters implement domain ports. Spring Data repository interfaces never escape infrastructure. Scalar foreign keys preserve context independence; child writes are coordinated by reservation/invoice owners. There are 18 JPA entities, one for each table.

`common` supplies transaction, authorization, Principal enforcement, exception translation, and Clock configuration. `user` supplies BCrypt hashing without adding a login flow or JWT filter. `rental` implements operational lifecycle queries.

Run the reactor's `mvnw clean verify` with JDK 21 and Docker, or supply the dedicated PostgreSQL URL described in README. PostgreSQL integration is mandatory, not silently skipped.
