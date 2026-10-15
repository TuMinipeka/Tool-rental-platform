# Reporte de validación del merge y los contextos base

Fecha: 15 de octubre de 2026.

## Entorno y resultado

JDK 21, Spring Boot 3.5.16 y PostgreSQL 18.6 en una instancia temporal aislada, separada de la base de trabajo. Las dos suites PostgreSQL crean esquemas distintos. La ruta predeterminada con Testcontainers se conserva; en esta ejecución se usó una URL explícita porque Docker Desktop no tiene un motor operativo.

```powershell
.\mvnw.cmd '-Dpostgres.test.url=jdbc:postgresql://127.0.0.1:55439/toolrental_validation' clean verify
```

**BUILD SUCCESS: 58 pruebas, 0 fallos, 0 errores y 0 omitidas.**

- Dominio: 19 pruebas.
- Aplicación: 13 pruebas.
- Infraestructura: 26 pruebas, incluidas las siete pruebas PostgreSQL aportadas por Conde.
- Spotless: aprobado.
- Frontend integrado desde Conde: `npm ci`, `npm run lint` y `npm run build` correctos.
- `docker compose config --quiet`: correcto; no acredita el arranque de los contenedores.

## Compatibilidad de datos

Las migraciones V1–V3 se conservan byte por byte respecto a `developer/daniel` antes del merge. La propuesta de reemplazo V1–V21 de Conde se conserva como referencia fuera de las ubicaciones de Flyway. No se ejecutó `repair`, no se alteró el historial y no se recreó la base de trabajo.

La suite base valida tres migraciones sobre una base vacía. La suite demo aplica esas tres más las 18 migraciones demo, verifica 21 versiones, datos por tabla, rollback y concurrencia. El perfil `local` no carga ejemplos; `demo` requiere activación explícita.

## Página de conexión

Las pruebas verifican la consulta real, cierre de recursos JDBC, HTTP 200 con conexión, HTTP 503 sin conexión y ausencia de detalles de credenciales. La prueba integrada confirma el acceso a la página y mantiene HTTP 401 para las rutas de negocio sin `Principal`.

La comprobación del proceso final y del navegador se registra en [el reporte de integración](branch-merge-report.md).

## Comprobación final del navegador y de la base de trabajo

El backend fusionado inició con el perfil `local` y la configuración privada original. La base conserva 18 tablas de dominio, V1–V3 y los roles iniciales. El endpoint de estado respondió 200 con conexión verificada; las rutas de negocio conservaron su respuesta 401 sin autenticación.

Playwright confirmó la página en escritorio y móvil, el botón de actualización, ausencia de desbordamiento horizontal, estado de error ante una respuesta 503 simulada y recuperación al restaurar la consulta real. No se desconectó PostgreSQL para simular ese fallo.
