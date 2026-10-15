# Reporte de validación de los contextos base

Fecha: 15 de octubre de 2026.

## Entorno verificado

- JDK 21.0.12.1, Spring Boot 3.5.16 y Maven Wrapper.
- PostgreSQL 17.11 en contenedores aislados creados por Testcontainers mediante Docker.
- Las 18 tablas, los datos de demostración y los contextos fusionados se validaron desde bases vacías.

## Resultado

```bash
./mvnw clean verify
```

**BUILD SUCCESS: 53 pruebas, 0 fallos, 0 errores y 0 omitidas.**

- Dominio: 19 pruebas.
- Aplicación: 13 pruebas.
- Infraestructura: 21 pruebas.
- Spotless: formato aprobado.

La cobertura incluye las 18 entidades JPA, 39 migraciones Flyway, validación de Hibernate, contratos HTTP, autenticación mediante `Principal`, autorización por rol y propiedad, hash BCrypt, claves compartidas y compuestas, instantáneas de tarifas y facturas, rollback transaccional, solapamiento inclusivo de fechas, confirmaciones concurrentes y unicidad concurrente del inventario.

## Alcance del resultado

La prueba confirma que la integración entre los contextos de backend de `developer/daniel` y las migraciones, datos demo y entorno PostgreSQL de `developer/conde` funciona en una base nueva. No valida todavía JWT, OpenAPI, una pasarela de pagos, impuestos avanzados, protección, evidencias, consentimientos ni despliegue productivo.
