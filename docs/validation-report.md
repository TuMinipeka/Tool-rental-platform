# Reporte de validación

Fecha de actualización: 2026-10-19.

## Resultado verificado

- Java 21 y compilación de los tres módulos.
- Pruebas de Domain y Application exitosas.
- Spotless aplicado; arquitectura hexagonal y mapeo de 25 entidades JPA comprobados mediante pruebas sin base de datos.
- Configuración Compose válida mediante `docker compose config --quiet`.
- El perfil PostgreSQL local respondió en una comprobación anterior de conectividad; no se ejecutaron las migraciones nuevas contra él.
- Límites exactos de 48 horas, dos horas, precisión monetaria y margen de reutilización cubiertos por pruebas de dominio.
- OpenAPI configurado para filtrar `/api/v1`; alias `/api` configurado con cabeceras de obsolescencia. Falta comprobar ambos contratos mediante HTTP con PostgreSQL activo.

## Validación pendiente por entorno

`mvnw.cmd clean verify` terminó con error en 20 pruebas de infraestructura dependientes de Testcontainers: Docker Desktop no expuso el motor. Dominio (25 pruebas), Application (14 pruebas), compilación de Infrastructure y sus pruebas sin PostgreSQL pasaron. Se intentó previamente la suite Flyway sobre PostgreSQL local, pero el usuario de aplicación recibió SQLSTATE `42501` al crear un esquema aislado. No se modificó el esquema de trabajo.

Para completar la evidencia ejecuta `mvnw.cmd clean verify` con Docker activo o concede `CREATE` sobre una base exclusiva al usuario de pruebas. Deben pasar actualización V200→V208, instalación vacía, `ddl-auto: validate`, concurrencia e idempotencia. Las 25 tablas y los contratos HTTP no están aún validados contra PostgreSQL.

## Límites

La implementación de reembolso y pagos continúa siendo académica; no ejecuta transferencias reales. SMTP necesita credenciales y TLS de un proveedor en producción. La respuesta de estado JDBC acredita conectividad en ese momento, no respaldo, recuperación, alertas ni alta disponibilidad.
