# Comparación e integración de developer/conde con developer/daniel

> [!NOTE]
> Este reporte describe el primer merge. La decisión de archivar migraciones se corrigió después: los 21 scripts de Conde ahora son ejecutables como V4–V24. Consulta [migraciones](database-migrations.md) y el [reporte vigente](validation-report.md).

Fecha: 15 de octubre de 2026. Rama destino: `developer/daniel`.

## Referencias comparadas

- Daniel antes del merge: `3dd463730db0d85e323a4cdd00be18ffc4c0395f`.
- Conde integrado: `ac149a1db7a95d3cc2bb4f313e063927fb7a0915`.
- Ancestro común: `7ed83c1036c74f7f40ff1a7e6c23895d7e33d733`.
- Commit de merge: `b69f5da5578ef013bfadac77cab4be8447b433ec`. Conserva ambos padres y no reescribe historial.
- Antes de integrar: **1 commit exclusivo de Daniel y 16 exclusivos de Conde**. El aporte de Conde respecto al ancestro común afectaba **74 archivos**.

## Comparación

| Área | developer/daniel antes del merge | developer/conde | Resultado integrado |
| --- | --- | --- | --- |
| Dominio y aplicación | 18 contextos y reglas de reserva/facturación | Ya incorporaba esos contextos desde Daniel | Se conserva la lógica existente; no había diferencias nuevas en esos módulos |
| Organización | Un contexto por tabla y mapa navegable de paquetes | Código de los 18 contextos, pero instrucciones arquitectónicas antiguas | Se mantienen los 18 contextos y el mapa; instrucciones corregidas |
| Base de trabajo | PostgreSQL 18.6 local, con V1–V3 ya aplicadas | Historial inicial reemplazado por V1–V21 | Se conservan exactamente V1–V3; la propuesta de Conde queda como referencia no ejecutable |
| Datos de ejemplo | Sin carga automática en la base de trabajo | 18 migraciones demo activadas con `local` | Perfil adicional `demo` explícito; la base local habitual no recibe ejemplos |
| Persistencia | Pruebas de mapeo, HTTP, reservas y concurrencia | Siete pruebas adicionales de restricciones, demo, rollback e inventario/reservas concurrentes | Se ejecutan ambas suites, con esquemas aislados y soporte de URL PostgreSQL explícita |
| Entorno Docker | Sin Compose ni Dockerfiles | Backend, PostgreSQL, frontend, ejemplos de variables y volumen persistente | Incorporado; configuración validada, contenedores no ejecutados en esta sesión |
| Frontend | Sin proyecto React | Base React + JavaScript + Vite y contenedor Nginx | Incorporada; lint y build aprobados. Los recorridos de negocio no están conectados a la API |
| Documentación | README y mapa de paquetes | README en español, fuentes de datos y hoja de ruta ampliada | README integrado, diagrama conceptual y enlaces preservados |
| Estado de conexión en 8080 | API anónima respondía 401, sin página inicial | Sin comprobación web de conexión | Página pública de estado con consulta real, actualización y estados de fallo |

## Resoluciones del merge

El conflicto textual de Git estaba en README. Se integró su presentación en español y se mantuvieron el diagrama conceptual y el mapa de paquetes de Daniel.

La incompatibilidad más importante era Flyway: sustituir V1–V3 por archivos que crean tablas diferentes, o renombrarlos, rompería la validación de la base existente. Se conservaron los archivos aplicados byte por byte, sin `repair`, sin cambio de checksums y sin recrear tablas. La propuesta completa de Conde se archivó en [la referencia de migraciones](database/conde-migration-reference/README.md), fuera de las ubicaciones ejecutables.

La carga demo ahora exige `local,demo` sobre una base destinada a demostraciones. La suite demo valida tres versiones estructurales más 18 versiones de datos, no las 39 de la propuesta original. Se mantuvo la organización de 18 contextos solicitada por el usuario, aunque la versión entrante de AGENTS contenía una instrucción antigua incompatible.

Se conservó la configuración privada de conexión durante el merge. Sus credenciales se restauraron localmente y no se incluyeron en el commit de integración. El pool local admite como máximo dos conexiones y no mantiene conexiones ociosas mínimas.

## Validación

**Backend: 58 pruebas aprobadas, 0 fallos, 0 errores y 0 omitidas.**

```powershell
.\mvnw.cmd '-Dpostgres.test.url=jdbc:postgresql://127.0.0.1:55439/toolrental_validation' clean verify
```

Las pruebas usaron PostgreSQL 18.6 temporal y esquemas separados de los datos de trabajo. La suite predeterminada conserva Testcontainers, pero su arranque con Docker no fue ejecutado aquí. Se validaron también Spotless, `npm ci`, `npm run lint`, `npm run build` y `docker compose config --quiet`.

En la base de trabajo se comprobó después del merge:

- 18 tablas de dominio, además de la tabla de historial Flyway.
- Versiones aplicadas: 1, 2 y 3.
- Roles iniciales: ADMIN, CUSTOMER y SUPPLIER.
- Arranque de Spring Boot, validación de Hibernate y Tomcat en 8080.
- `GET /status/database`: HTTP 200 con `connected: true`.
- `GET /api/categories` sin Principal: HTTP 401.

## Interfaz solicitada

Abre [http://localhost:8080](http://localhost:8080). La página muestra **«Conexión a la base de datos exitosa.»** únicamente después de una consulta JDBC real. Permite volver a comprobar y se actualiza cada 30 segundos mientras está visible. Los errores SQL y las credenciales no se devuelven al navegador.

Playwright verificó:

- Estado real conectado y comprobación manual.
- Escritorio 1440 × 1000 y móvil 390 × 844, sin desbordamiento horizontal.
- Respuesta 503 simulada únicamente en el navegador: se muestra el estado sin conexión.
- Restauración de la respuesta real: vuelve el estado conectado.
- Ninguna excepción JavaScript durante el recorrido.

La página de estado se sirve desde Spring Boot en 8080. La base React de Conde sigue siendo una aplicación separada; JWT, OpenAPI y los recorridos completos de negocio permanecen pendientes.
