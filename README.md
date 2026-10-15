# Plataforma de alquiler de herramientas

Plataforma para alquilar herramientas y equipos de construcción, construida con Java 21, arquitectura hexagonal y diseño guiado por el dominio. Las reglas de negocio permanecen independientes de Spring, JPA, HTTP y los detalles de persistencia.

## Estado actual

Las 18 tablas base cuentan con contextos de dominio, aplicación e infraestructura. El backend ofrece operaciones REST para identidad, catálogo, reservas, alquileres, entregas, devoluciones, daños, pagos manuales y facturas con instantáneas históricas. PostgreSQL, Flyway, JPA y las operaciones críticas de reserva y facturación se validan con pruebas de integración y concurrencia.

También existe una base de frontend con React y JavaScript, todavía sin integrar con la API. Continúan pendientes JWT, OpenAPI/Swagger, la pasarela simulada de pagos, impuestos, protección, evidencias, consentimientos, reportes y los recorridos funcionales del frontend.

## Tecnologías

| Componente | Elección |
| --- | --- |
| Backend | Java 21 y Spring Boot 3.5.16 |
| Construcción | Maven multimódulo y Maven Wrapper 3.9.9 |
| Arquitectura | Hexagonal y DDD |
| Persistencia | PostgreSQL (Docker 17.11; validación local 18.6), Spring Data JPA, Hibernate y Flyway |
| API | REST con validación y control de acceso mediante `Principal`; JWT y OpenAPI pendientes |
| Pruebas | JUnit 5, Spring Boot Test, ArchUnit y Testcontainers |
| Formato | Spotless 2.43.0 con Google Java Format 1.27.0 |
| Frontend | React 19.3.1 con JavaScript y Vite 8.3.4 |

El [enunciado original](ejercicioDemo.md) solicitaba MySQL. PostgreSQL lo reemplaza por decisión explícita del proyecto.

## Modelo conceptual

![Modelo conceptual entidad-relación](docs/diagrams/conceptual-er-model.svg)

Consulta el [archivo editable de Excalidraw](docs/diagrams/conceptual-er-model.excalidraw), el [modelo conceptual](docs/conceptual-model.md) y la [cuarta forma normal](docs/fourth-normal-form.md).

## Arquitectura

```text
domain/          # Agregados, objetos de valor, eventos y puertos
application/     # Comandos, respuestas, excepciones y casos de uso
infrastructure/  # REST, JPA, configuración y migraciones Flyway
frontend/        # Aplicación React con JavaScript
docs/            # Modelo, decisiones y trabajo pendiente
```

La dirección de dependencias es `infrastructure -> application -> domain`. El dominio depende únicamente de Java y la aplicación solo conoce el dominio.

El proyecto usa un contexto por cada una de las 18 tablas existentes como convención organizativa. Esto no convierte automáticamente cada tabla en un agregado independiente ni permite escrituras que evadan las transacciones de reservas y facturas.

| Tabla | Contexto | Tipo de dominio |
| --- | --- | --- |
| `users` | `user` | `User` |
| `roles` | `role` | `Role` |
| `user_roles` | `userrole` | `UserRole` |
| `customers` | `customer` | `Customer` |
| `suppliers` | `supplier` | `Supplier` |
| `categories` | `category` | `Category` |
| `tools` | `tool` | `Tool` |
| `tool_images` | `toolimage` | `ToolImage` |
| `equipment_units` | `equipmentunit` | `EquipmentUnit` |
| `reservations` | `reservation` | `Reservation` |
| `reservation_items` | `reservationitem` | `ReservationItem` |
| `rentals` | `rental` | `Rental` |
| `unit_deliveries` | `unitdelivery` | `UnitDelivery` |
| `unit_returns` | `unitreturn` | `UnitReturn` |
| `damage_reports` | `damagereport` | `DamageReport` |
| `payments` | `payment` | `Payment` |
| `invoices` | `invoice` | `Invoice` |
| `invoice_items` | `invoiceitem` | `InvoiceItem` |

## Reglas implementadas destacadas

- Una unidad de equipo representa un activo físico; las imágenes pertenecen a la oferta de herramienta.
- Una reserva contiene unidades distintas de un único proveedor y usa una sola moneda.
- Las fechas son inclusivas y una fecha compartida produce conflicto para la misma unidad.
- Las solicitudes pendientes no ocupan inventario. La confirmación bloquea y vuelve a validar disponibilidad antes de congelar las tarifas.
- Las devoluciones requieren una entrega completada y el alquiler solo termina cuando regresan todas sus unidades.
- Los pagos actuales son registros administrativos; no ejecutan cobros ni reembolsos externos.
- Las facturas y sus líneas se crean atómicamente y conservan instantáneas históricas del cliente y proveedor.
- Las contraseñas nuevas se almacenan con BCrypt y nunca se exponen en respuestas.

## Límite de autenticación

Todas las rutas `/api/**` requieren un `Principal` autenticado cuyo nombre sea el UUID del usuario. JWT y el inicio de sesión aún no están implementados, por lo que una petición HTTP anónima recibe `401`. Los casos de uso validan usuario activo, roles, propiedad y participación; ningún `actorId` enviado por el cliente sustituye la identidad autenticada.

Consulta [API REST](docs/rest-api.md) para ver las rutas y contratos disponibles.

## Entorno local con Docker

Requisitos: JDK 21, Docker con Docker Compose y Node.js 24 para trabajar directamente con el frontend.

Crea el archivo local de variables y cambia `POSTGRES_PASSWORD` y `DB_PASSWORD` por el mismo valor:

```bash
cp .env.example .env
docker compose --env-file .env up --build
```

En PowerShell usa `Copy-Item .env.example .env`. Para iniciar únicamente PostgreSQL:

```bash
docker compose --env-file .env up -d postgres
```

El backend se publica en el puerto `8080`. El frontend se construye con Node 24.22.0, se sirve con Nginx y queda disponible en `http://localhost:5173`. El volumen `postgres_data` conserva la base entre reinicios.

Para trabajar solo en el frontend:

```bash
cd frontend
npm install
npm run dev
```

## Compilar y verificar

Linux/macOS:

```bash
./mvnw clean verify
java -jar infrastructure/target/infrastructure-0.1.0-SNAPSHOT.jar
```

Windows:

```powershell
.\mvnw.cmd clean verify
java -jar infrastructure\target\infrastructure-0.1.0-SNAPSHOT.jar
```

`verify` ejecuta las pruebas y valida el formato Java. Con Docker, Testcontainers crea PostgreSQL 17.11 y verifica migraciones, mapeos, contratos HTTP, rollback y concurrencia. Aplica el formato cuando sea necesario con `./mvnw spotless:apply` o `.\mvnw.cmd spotless:apply`.

El perfil `local` conecta de forma predeterminada con `jdbc:postgresql://localhost:5432/tool_rental`. `DB_URL`, `DB_USERNAME` y `DB_PASSWORD` permiten reemplazar esos valores; el perfil `production` los exige.

Flyway crea las 18 tablas mediante migraciones individuales, aplica las migraciones de compatibilidad y roles, y carga 18 migraciones de demostración únicamente con el perfil `local`. Cada tabla recibe al menos cinco filas de demostración. Hibernate usa `ddl-auto: validate` y nunca genera el esquema.

Las tarifas demo y sus referencias públicas colombianas se registran en [Fuentes de datos](docs/data-sources.md).

## Documentación

1. [Modelo conceptual](docs/conceptual-model.md)
2. [Arquitectura del backend](docs/backend-architecture.md)
3. [Guía de desarrollo](docs/development-guide.md)
4. [API REST](docs/rest-api.md)
5. [Reporte de validación](docs/validation-report.md)
6. [Trabajo pendiente de base de datos](docs/pendientes-base-datos.md)
7. [Hoja de ruta](docs/pendientes.md)
8. [Alcance del producto](docs/product-scope.md)
9. [Borrador de términos y condiciones](docs/terms-and-conditions.md)

## Próxima entrega

La base técnica de datos, los contextos de backend y la API REST del modelo actual están implementados. La prioridad es cerrar las reglas comerciales de la Fase 0, implementar JWT y publicar el contrato OpenAPI antes de integrar los recorridos del frontend.

## Convención de commits

Todos los mensajes se escriben en inglés con Conventional Commits, un código Gitmoji y un resumen concatenado en `CamelCase`:

```text
<type>: :<gitmoji>: <CamelCaseSummary>
```

Ejemplo: `feat: :card_file_box: AddImplementationMigrations`.

## Estado de la conexión

Con el backend iniciado, abre [http://localhost:8080](http://localhost:8080). La página verifica la base mediante una consulta real a `/status/database`, muestra la hora de comprobación y permite volver a consultar. Un fallo devuelve HTTP 503 y nunca se presenta como conexión exitosa. Las rutas de negocio `/api/**` mantienen su requisito de autenticación.

- [Comparación y reporte del merge de developer/conde](docs/branch-merge-report.md)
- [Mapa de paquetes y archivos reubicados](docs/source-package-map.md)

Para pruebas sin Docker usa una base exclusiva y `mvnw.cmd '-Dpostgres.test.url=jdbc:postgresql://127.0.0.1:55439/toolrental_validation' clean verify`. Las pruebas crean esquemas separados; no utilices la base de trabajo como base de pruebas.
