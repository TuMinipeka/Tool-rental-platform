# Compilación y pruebas

## Requisitos

JDK 21, acceso a las dependencias Maven y PostgreSQL de pruebas mediante Docker/Testcontainers o una instancia exclusiva. Maven Wrapper descarga Maven 3.9.9: no necesitas instalar Maven globalmente. El frontend requiere Node.js 24 y npm.

## Windows con PowerShell

```powershell
$env:JAVA_HOME = 'C:\Users\danie\scoop\apps\temurin21-jdk\current'
& "$env:JAVA_HOME/bin/java.exe" -version
.\mvnw.cmd -version
.\mvnw.cmd clean verify
```

Ajusta la ruta a tu JDK. `mvnw.cmd -version` debe indicar Java 21. En el equipo de desarrollo el `java` del PATH puede seguir en Java 17; para arrancar utiliza el ejecutable explícito de `JAVA_HOME`.

## Linux y macOS

Configura `JAVA_HOME` hacia JDK 21 y agrega su directorio `bin` al PATH:

```bash
./mvnw -version
./mvnw clean verify
java -jar infrastructure/target/infrastructure-0.1.0-SNAPSHOT.jar
```

El arranque requiere las variables de conexión descritas en [despliegue](deployment.md).

## PostgreSQL para las pruebas

Por defecto las suites arrancan PostgreSQL con Testcontainers; Docker debe tener el motor activo. No omiten silenciosamente la persistencia si Docker falla.

Como alternativa, crea previamente una base **exclusiva de pruebas** y usa:

```powershell
.\mvnw.cmd '-Dpostgres.test.url=jdbc:postgresql://127.0.0.1:55439/toolrental_validation' clean verify
```

La alternativa admite `postgres.test.username` y `postgres.test.password`; sus valores predeterminados son `postgres` y vacío para la instancia temporal de bucle local. No escribas contraseñas reales en comandos compartidos ni uses la base de trabajo: las suites crean esquemas, insertan y alteran datos de prueba.

`verify` incluye dominio, aplicación, arquitectura, persistencia, REST, rollback y concurrencia; Spotless comprueba el formato. Produce `infrastructure/target/infrastructure-0.1.0-SNAPSHOT.jar`. Los informes XML quedan en `*/target/surefire-reports/`.

## Formato y diagnóstico

Para la aceptación crítica HTTP con Insomnia, JWT, pagos ficticios y alquiler completo, usar [pruebas del sistema](system-tests.md). La colección se ejecuta con `node scripts/verify-system-tests.mjs` contra la composición aislada en `18080`; no requiere ejecutar toda la regresión Maven. El alcance y los resultados quedan separados del histórico de `clean verify`.

```powershell
.\mvnw.cmd spotless:apply
.\mvnw.cmd spotless:check
```

Una diferencia `\r\n` frente a `\n` indica formato, no sintaxis Java. Los `.java` usan LF y los `.cmd` CRLF según `.gitattributes`. Tras reubicar paquetes ejecuta `clean verify` para eliminar clases antiguas. Si Maven funciona pero el explorador Java no, recarga Maven o usa **Java: Clean Java Language Server Workspace**. El antiguo `catalog` ahora está en `category`, `tool`, `toolimage` y `equipmentunit`.

## Frontend

```bash
cd frontend
npm ci
npm run lint
npm run build
npm run test:e2e
npm run dev
```

`npm ci` respeta el lockfile; `build` produce `frontend/dist`. Vite sirve desarrollo en 5173. La base consume `/api/v1`, mantiene el access token en memoria y usa la cookie refresh con CSRF; los paneles consumen los contratos disponibles del backend; la cobertura y los límites están en la verificación por roles.

Las pruebas E2E requieren Chrome y utilizan respuestas API de prueba sin mutar la base. El procedimiento de Lighthouse, axe, capturas, memoria y entrega Nginx está en [rendimiento y accesibilidad](frontend-quality.md). Sus informes se generan fuera de `dist` y del contexto Docker.

## Evidencia y límites

Consulta el [reporte de validación](validation-report.md). Empaquetar con `-DskipTests` no acredita persistencia, concurrencia ni preparación de producción.

## Recorridos contra la aplicación real

Con la composición aislada activa, ejecuta desde la raíz `node frontend/scripts/verify-role-workflows.mjs`. Usa Playwright CLI, crea datos académicos y registra capturas verificables. Consulta [la matriz por roles y contratos](browser-verification.md) antes de ejecutarlo. Estas pruebas se complementan con la suite de frontend aislada; no son intercambiables.
