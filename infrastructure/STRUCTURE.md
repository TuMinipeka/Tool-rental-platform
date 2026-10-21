# Estructura de infraestructura

Cada uno de los 18 contextos sigue esta organización, incorporando tipos con responsabilidad real:

```text
<context>/
  adapters/in/rest/controllers/<Entity>Controller.java
  adapters/in/rest/dtos/
  adapters/out/persistence/<Entity>PersistenceAdapter.java
  adapters/out/persistence/entity/<Entity>JpaEntity.java
  adapters/out/persistence/entity/<Entity>JpaId.java
  adapters/out/persistence/mapper/<Entity>PersistenceMapper.java
  adapters/out/persistence/repository/<Entity>JpaRepository.java
  config/<Entity>BeanConfiguration.java
```

`JpaId` corresponde a claves compuestas. Los controladores convierten contratos HTTP e invocan casos de uso. Los adaptadores implementan puertos; las interfaces Spring Data no salen de infraestructura. Existen 18 entidades JPA, sin dos propietarios del mismo mapeo. Las escrituras de detalles siguen coordinadas por sus propietarios.

`common` reúne transacciones, autorización, Principal, excepciones HTTP, estado JDBC y `Clock`. `user` incorpora BCrypt sin implementar login/JWT. Las migraciones están en recursos: [trazabilidad](../docs/database-migrations.md).

Verifica el reactor con JDK 21 y PostgreSQL real siguiendo [compilación](../docs/build-and-test.md).
