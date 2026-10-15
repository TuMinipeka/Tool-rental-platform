# Reporte de validación de migraciones e integración

Fecha: 15 de octubre de 2026. Rama: `developer/daniel`.

## Resultado reproducible

**BUILD SUCCESS: 59 pruebas, 0 fallos, 0 errores y 0 omitidas.**

| Módulo | Pruebas | Resultado |
| --- | ---: | --- |
| Domain | 19 | Aprobado |
| Application | 13 | Aprobado |
| Infrastructure | 27 | Aprobado |

```powershell
$env:JAVA_HOME = 'C:\Users\danie\scoop\apps\temurin21-jdk\current'
.\mvnw.cmd '-Dpostgres.test.url=jdbc:postgresql://127.0.0.1:55439/toolrental_validation' clean verify
```

Se utilizó JDK 21 y PostgreSQL 18.6 temporal, separado de la base de trabajo, con esquemas aislados por suite. La ruta predeterminada Testcontainers se conserva; esta ejecución no acredita su arranque Docker, cuyo motor no estaba operativo. Spotless pasó durante `verify`.

## Qué se comprobó

- Instalación vacía: 24 versiones estructurales y de roles, 18 tablas de negocio y validación Hibernate.
- Actualización V3 → V24: 21 versiones nuevas, conservación de un registro previo y checksums V1–V3, un solo trigger de inventario y segunda migración sin cambios.
- Demostración aislada: 42 versiones numeradas, con al menos cinco registros por cada tabla base.
- Correspondencia de columnas/claves JPA, claves compuestas, BCrypt y restauración de dominio.
- Fechas inclusivas, tarifas confirmadas, errores de negocio y transacciones con rollback.
- Confirmaciones concurrentes incompatibles, cancelación que libera disponibilidad y unicidad de inventario por proveedor.
- Contratos HTTP, 401 sin Principal y estado de conexión real.

Las siete pruebas de persistencia aportadas por Conde se conservan. Se añadió `FlywayUpgradeIntegrationTest` para comprobar la transición desde la historia ya aplicada, además de las suites existentes.

## Base de trabajo después de actualizar

Antes del cambio se generó un respaldo privado con `pg_dump -Fc`. El backend arrancó con el perfil `local` y aplicó V4–V24. Se compararon huellas privadas del contenido ordenado de las 18 tablas y los checksums previos de V1–V3: **sin diferencias**. No se borraron tablas ni se cargaron datos demo. No se ejecutó `repair`.

Resultado: 18 tablas de negocio más historial Flyway; versiones 1–24 exitosas; roles ADMIN, CUSTOMER y SUPPLIER conservados. Las conversiones de moneda concluyeron y Hibernate validó el esquema. El respaldo no se versionó y no se afirma un ensayo de restauración.

## Página web después de migrar

`GET /status/database` respondió 200 con `connected: true`; `/api/categories` sin autenticación respondió 401. El backend permanece disponible en [localhost:8080](http://localhost:8080).

Playwright volvió a comprobar el mensaje «Conexión a la base de datos exitosa.», actualización manual, escritorio 1440 × 1000 y móvil 390 × 844 sin desbordamiento. También verificó el estado de error con una respuesta 503 simulada en el navegador y la recuperación al restaurar la consulta real, sin excepciones JavaScript. No se interrumpió PostgreSQL para simular ese fallo.

## Documentación y alcance de la evidencia

Se verificaron enlaces relativos de README y guías, codificación UTF-8 y conservación del enunciado original. Las instrucciones de compilación, despliegue y migraciones están en español y enlazadas desde la [matriz de rúbrica](requirements-coverage.md).

Frontend: `npm ci`, `npm run lint` y `npm run build` fueron verificados durante el merge anterior; su código no se modificó en esta entrega. La configuración Compose se valida separadamente; no se presenta como un despliegue ejecutado. JWT, pasarela, frontend funcional y condiciones de producción siguen [pendientes](production-readiness.md).
