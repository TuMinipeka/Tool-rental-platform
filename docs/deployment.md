# Despliegue y operación

## Alcance

El proyecto ofrece un JAR ejecutable con Tomcat embebido y una composición Docker de PostgreSQL, backend y frontend Nginx. No genera un WAR para Tomcat externo. Docker cubre la alternativa de contenedores de la rúbrica; la ejecución de contenedores requiere validar su motor en el entorno destino.

La entrega sigue en desarrollo. Revisa los [bloqueos de producción](production-readiness.md) antes de exponerla públicamente.

## Variables de conexión

| Variable | Uso |
| --- | --- |
| `SPRING_PROFILES_ACTIVE` | `production` exige credenciales externas; `local` permite configuración local |
| `DB_URL` | URL JDBC PostgreSQL; dentro de Compose utiliza el servicio `postgres` |
| `DB_USERNAME`, `DB_PASSWORD` | Credenciales externas, nunca dentro del repositorio o una imagen publicada |
| `DB_MAX_POOL_SIZE`, `DB_MIN_IDLE` | Tamaño máximo y conexiones ociosas mínimas del pool |
| `DB_CONNECTION_TIMEOUT_MS` | Espera máxima de Hikari para obtener conexión |

Los valores compartidos del pool son 10/2; `local` usa 2/0 salvo sobrescritura. Dimensiona el total entre réplicas y clientes de administración según el límite del rol PostgreSQL.

## Ejecutar el JAR

Completa [compilación y pruebas](build-and-test.md). Define `SPRING_PROFILES_ACTIVE=production`, `DB_URL`, `DB_USERNAME` y `DB_PASSWORD` mediante el entorno del proceso o tu gestor de secretos.

```powershell
& "$env:JAVA_HOME/bin/java.exe" -jar infrastructure/target/infrastructure-0.1.0-SNAPSHOT.jar
```

Linux/macOS, con Java 21 seleccionado:

```bash
java -jar infrastructure/target/infrastructure-0.1.0-SNAPSHOT.jar
```

Flyway aplica migraciones pendientes antes de que Hibernate valide. Un fallo debe impedir el arranque; no actives `ddl-auto: update`, `baseline-on-migrate` ni `repair` para ocultarlo. Para producción separa el rol migrador del rol de ejecución con privilegios limitados; actualmente se utiliza el mismo DataSource.

## Ejecutar con Docker Compose

```powershell
Copy-Item .env.example .env
# Edita .env y reemplaza las contraseñas de ejemplo antes de continuar.
docker compose --env-file .env config --quiet
docker compose --env-file .env up --build -d
docker compose ps
docker compose logs --tail 100 backend
```

Compose usa `POSTGRES_PASSWORD` para el servidor y para `DB_PASSWORD` del backend. Para ejecutar el JAR fuera de Compose define también `DB_PASSWORD`. `.env` no se versiona. Spring Boot fuera de Compose no carga automáticamente ese archivo.

| Servicio | Dirección local predeterminada |
| --- | --- |
| Backend / estado | http://localhost:8080 |
| Frontend Nginx | http://localhost:5173 |
| PostgreSQL | localhost:5432 |

Si ya utilizas PostgreSQL en 5432, cambia `POSTGRES_PORT`. La conexión interna sigue usando `postgres:5432`. El volumen `postgres_data` conserva los datos. `docker compose down` detiene los servicios; evita eliminar volúmenes con información que debas conservar.

La imagen backend ejecuta Java 21 con un usuario sin privilegios. La etapa de empaquetado omite tests: verifica antes la revisión con `clean verify`. Las imágenes actuales son una base de desarrollo; fija versiones y digest revisados al publicar. No se acredita un arranque Docker cuando su motor no está operativo.

## Demostración aislada

En una base vacía de demostración, activa `local,demo` y proporciona sus variables de conexión. Flyway ejecuta 24 versiones estructurales y 18 de datos, con al menos cinco registros por tabla. No habilites `demo` en producción o en la base de trabajo: incluye cuentas y operaciones ficticias. No se ofrece un inicio de sesión demo.

## Verificaciones posteriores al arranque

```powershell
Invoke-RestMethod http://localhost:8080/status/database
```

Debe responder `connected: true`. La página raíz presenta el resultado de una consulta real; si falla responde 503. Verifica también Flyway, Hibernate y 401 en `/api/categories` sin autenticación. El estado de conexión no sustituye métricas, alertas ni pruebas de negocio.

## Actualización, respaldo y recuperación

Antes de publicar: respalda con `pg_dump -Fc` usando credenciales externas; prueba la restauración en un destino aislado; registra el artefacto e historial Flyway; programa una ventana de cambio. [Migraciones](database-migrations.md) explica los bloqueos de moneda y la compatibilidad con V3.

Ante un fallo, conserva logs privados y revisa la transacción. No borres historial ni restaures sobre la base activa automáticamente. Una recuperación requiere detener escrituras y un procedimiento ensayado; retroceder el JAR no revierte el esquema. El respaldo privado generado durante esta entrega no acredita un ensayo completo de recuperación.

Consulta los [criterios de salida a producción](production-readiness.md).
