<div align="center">

![Tool Rental Platform](docs/assets/readme-banner.svg)

**Backend de alquiler de herramientas con arquitectura hexagonal, DDD y seguridad orientada a producción.**

[Inicio](#inicio-rápido) · [Arquitectura](#arquitectura) · [Seguridad](#seguridad) · [Negocio](#reglas-críticas-de-negocio) · [API](#api-rest) · [Documentación](#documentación)

</div>

---

## Entrega MVP de Daniel/Mayorga

**Estado: integrado, ejecutándose y validado localmente; publicación pública pendiente.** Accede a la composición aislada en http://localhost:15173 después de seguir [despliegue](docs/deployment.md). No hay una demo pública acreditada todavía. El cierre compartido y las dependencias de Conde están en [el MVP](docs/mvp.md).

[Despliegue reproducible](docs/deployment.md) · [Contratos por rol](docs/integration-contracts.md) · [Modelo relacional y físico](docs/logical-physical-model.md) · [Evidencias](docs/validation-report.md)


## Funcionamiento verificado con Playwright CLI

Recorridos ejecutados contra **React, Spring Boot/Tomcat y PostgreSQL reales**, en la composición aislada de validación. Las cuentas, herramientas, documentos e importes mostrados son datos académicos. El pago es ficticio: no se introducen tarjetas ni se mueve dinero.

| Perfil | Acciones verificadas |
| --- | --- |
| Cliente | Registro, consulta y búsqueda del catálogo, cotización, reserva, aprobación/rechazo del pago, cancelación, descarga de comprobante, renovación y cierre de sesión |
| Proveedor | Registro sujeto a activación, publicación y unidades propias, entrega, alquiler, devolución y cierre; cinco reportes con alcance autorizado |
| Administrador | Consulta operativa, activación y desactivación de proveedores; revocación inmediata del acceso al desactivar |
| Contratos REST adicionales | Extensiones, daños y valoración, retracto en revisión, reembolsos registrados, políticas, tarifas y consultas administrativas |

La [matriz de verificación](docs/browser-verification.md) distingue interfaz, REST y pruebas automatizadas; incluye límites y pasos para repetir el recorrido. Los resultados y las huellas de las imágenes están en [la evidencia JSON](docs/evidence/mvp/role-workflows.json).

### Cliente: reserva y pago académico

**Cotización de un día con recogida por el cliente.** El servidor valida disponibilidad y calcula el importe final.

![Cotización del cliente con fechas y tarifa de 25.000 COP](docs/evidence/mvp/screenshots/qa-customer-quote.png)

**Pasarela ficticia.** El cliente propietario puede simular aprobación o rechazo; otros usuarios reciben acceso denegado.

![Pasarela ficticia con importe persistido y opciones de aprobación o rechazo](docs/evidence/mvp/screenshots/qa-fictional-checkout.png)

**Aprobación persistida.** Repetir la decisión conserva un solo comprobante; una decisión contradictoria devuelve conflicto.

![Pago ficticio aprobado y reserva confirmada por el backend](docs/evidence/mvp/screenshots/qa-payment-approved.png)

### Proveedor: operación y reportes

**Alquiler completado.** Se programó y completó la entrega, se inició el alquiler y se registró la devolución antes de cerrarlo.

![Reserva del proveedor con entrega completada, devolución registrada y alquiler cerrado](docs/evidence/mvp/screenshots/qa-supplier-completed-rental.png)

**Reporte consultado en el backend.** El ingreso reservado no se presenta como margen contable.

![Reporte de ingresos por moneda con datos reales de la base aislada](docs/evidence/mvp/screenshots/qa-supplier-report.png)

### Administrador: permisos y activación

**Proveedor activado por administración.** El registro inicial queda suspendido; la baja posterior revoca su sesión.

![Cuenta de proveedor activada mediante el panel administrativo](docs/evidence/mvp/screenshots/qa-admin-supplier-activation.png)

<details>
<summary>Más evidencias: catálogo, inventario, rechazo, comprobante y acceso restringido</summary>

**Búsqueda de una publicación persistida.**

![Catálogo filtrado por la herramienta creada durante la prueba](docs/evidence/mvp/screenshots/qa-catalog-search.png)

**Herramienta publicada por el proveedor y registro de su unidad física.**

![Publicación creada desde el formulario del proveedor](docs/evidence/mvp/screenshots/qa-supplier-publication.png)

![Formulario de unidad física después de guardar correctamente](docs/evidence/mvp/screenshots/qa-supplier-equipment.png)

**Rechazo del pago ficticio.** La prueba comprobó después que la unidad podía reservarse nuevamente.

![Resultado rechazado del pago ficticio](docs/evidence/mvp/screenshots/qa-payment-declined.png)

**Comprobante descargado.** Documento académico, sin validez fiscal ante la DIAN.

![Contenido del comprobante generado por el backend y descargado por el cliente](docs/evidence/mvp/screenshots/qa-academic-invoice.png)

**Cliente sin autorización administrativa.**

![Acceso al panel administrativo denegado para un cliente](docs/evidence/mvp/screenshots/qa-customer-admin-denied.png)

**Catálogo en móvil.**

![Catálogo filtrado en una pantalla móvil sin desbordamiento horizontal](docs/evidence/mvp/screenshots/qa-mobile-catalog.png)

**Catálogo público completo y cuenta nueva sin reservas.**

![Catálogo público conectado al backend](docs/evidence/mvp/screenshots/qa-public-catalog.png)

![Cuenta de cliente recién registrada y sin reservas](docs/evidence/mvp/screenshots/qa-customer-empty-account.png)

**Reserva y comprobante en la cuenta del cliente.**

![Reserva pendiente de pago con importe registrado en el servidor](docs/evidence/mvp/screenshots/qa-customer-reservation.png)

![Listado de comprobantes académicos con descarga autorizada](docs/evidence/mvp/screenshots/qa-customer-invoice.png)

![Reserva cancelada desde la cuenta del cliente](docs/evidence/mvp/screenshots/qa-customer-cancellation.png)

**Publicación y solicitud de incorporación del proveedor.**

![Formulario del proveedor después de guardar una herramienta](docs/evidence/mvp/screenshots/qa-supplier-inventory.png)

![Solicitud de proveedor recibida y pendiente de activación](docs/evidence/mvp/screenshots/qa-supplier-registration.png)

**Administración y rechazo de acceso.**

![Usuarios consultados en el panel administrativo](docs/evidence/mvp/screenshots/qa-admin-dashboard.png)

![Cuenta de proveedor desactivada y sin acceso operativo](docs/evidence/mvp/screenshots/qa-admin-account-disabled.png)

![Ingreso rechazado al introducir una contraseña incorrecta](docs/evidence/mvp/screenshots/qa-login-error.png)

</details>

## Estado actual

El proyecto usa Java 21, Spring Boot 3.5.16, PostgreSQL y Flyway. Los tres módulos Maven conservan las dependencias hacia dentro: `infrastructure → application → domain`. El modelo contiene **25 tablas y 25 contextos organizativos**; las transacciones coordinan las reglas que atraviesan reservas, pagos, sesiones, devoluciones y reembolsos.

| Área | Implementado | Fuera de este alcance |
| --- | --- | --- |
| Identidad | BCrypt, JWT de acceso, sesiones rotativas, revocación y CSRF; recuperación deshabilitada en el MVP | MFA y proveedores de identidad externos |
| Reservas | Retención atómica con vencimiento, confirmación automática, cancelación, entrega, devolución, extensión y mora | Optimización logística avanzada |
| Dinero | Pasarela ficticia COP, aprobación/rechazo, comprobante académico y reembolso ficticio idempotente | Cobros reales, PayPal e impuestos definitivos |
| Reportes | Ingresos, demanda, clientes, disponibilidad y rentabilidad con alcance por rol | Margen contable hasta modelar costos operativos |
| Entrega | Recogida gratuita o entrega del proveedor en Colombia | Tarifas geográficas y transportadoras externas |
| Persistencia | JPA, PostgreSQL, Flyway hasta V305 y datos demo separados | Operación administrada de respaldos y alta disponibilidad |
| API | `/api/v1`, OpenAPI, `ProblemDetail` y alias temporal `/api` | Eliminación futura del alias heredado |

El [enunciado original](ejercicioDemo.md) se conserva sin cambios. PostgreSQL reemplaza a MySQL por decisión autorizada.

## Inicio rápido

Requisitos: JDK 21, PostgreSQL y Docker cuando se quiera ejecutar la suite Testcontainers.

```powershell
$env:JAVA_HOME = 'C:\ruta\al\jdk-21'
.\mvnw.cmd -version
.\mvnw.cmd clean verify
docker compose config --quiet
docker compose up --build
```

Compose levanta PostgreSQL, backend y frontend. Mailpit se conserva como herramienta de desarrollo para una entrega futura; correo y recuperación están deshabilitados en el MVP. El backend queda en `http://localhost:8080`; la página raíz ejecuta una consulta JDBC real y muestra si la conexión fue exitosa.

### Entorno demostrativo

El Compose local activa los perfiles `local,demo` y carga, sobre una base nueva, un catálogo aislado de 30 herramientas publicadas, 20 herramientas históricas sin publicar y tres cuentas listas para iniciar sesión. No debe activarse sobre una base productiva.

```powershell
Copy-Item .env.example .env
docker compose up --build -d
```

| Rol | Correo | Contraseña | Acceso esperado |
| --- | --- | --- | --- |
| Cliente | `cliente@toolrental.local` | `ToolRental2026!` | Catálogo, cotización, reservas y cuenta |
| Proveedor | `proveedor@toolrental.local` | `ToolRental2026!` | Panel y publicaciones propias |
| Administrador | `admin@toolrental.local` | `ToolRental2026!` | Panel y recursos administrativos |

Estas credenciales existen únicamente en las migraciones `db/demo`; no son usuarios predeterminados de producción. En otro computador, al clonar el repositorio, copiar `.env.example` como `.env` y ejecutar el comando anterior, Flyway crea las tres cuentas automáticamente. La carga está pensada para una base o volumen nuevo; no actives `demo` posteriormente sobre una base que ya haya avanzado hasta V305 sin esas migraciones.

Para ejecutar sin Compose:

```powershell
$env:SPRING_PROFILES_ACTIVE = 'production'
$env:DB_URL = 'jdbc:postgresql://localhost:5432/tool_rental'
$env:DB_USERNAME = 'tool_rental'
$env:DB_PASSWORD = '<secret>'
$env:JWT_SECRET = '<mínimo-32-bytes>'
$env:PAYMENT_PROVIDER = 'fictional'
$env:CORS_ALLOWED_ORIGINS = 'https://app.example'
.\mvnw.cmd -pl infrastructure -am spring-boot:run
```

No actives los perfiles `local,demo` sobre datos reales. Las credenciales se suministran mediante variables o un gestor de secretos.

## Apache Tomcat integrado

El backend se empaqueta como JAR ejecutable. `java -jar infrastructure/target/infrastructure-0.1.0-SNAPSHOT.jar` inicia Spring Boot y su Apache Tomcat integrado en el puerto 8080. El Dockerfile ejecuta ese mismo comando con Java 21; no se instala otro Tomcat ni se usa un WAR. Caddy recibe HTTPS y dirige la API al Tomcat privado. Consulta [el procedimiento de despliegue](docs/deployment.md#apache-tomcat-integrado).

## Arquitectura

```mermaid
flowchart LR
    REST[REST / Infrastructure] --> APP[Use cases / Application]
    APP --> DOMAIN[Aggregates and ports / Domain]
    JPA[JPA adapters / Infrastructure] -. implements .-> DOMAIN
    JPA --> PG[(PostgreSQL)]
    SMTP[SMTP / Mailpit · aplazado] -. implements notification port .-> DOMAIN
```

Los módulos Domain y Application no importan Spring, Hibernate ni HTTP. Infrastructure contiene controladores, seguridad, entidades JPA, mappers, repositorios Spring Data y configuración.

<details>
<summary><strong>Ver los 25 contextos y tablas</strong></summary>

| Contexto | Tabla | Contexto | Tabla |
| --- | --- | --- | --- |
| `user` | `users` | `role` | `roles` |
| `userrole` | `user_roles` | `customer` | `customers` |
| `supplier` | `suppliers` | `category` | `categories` |
| `tool` | `tools` | `toolimage` | `tool_images` |
| `equipmentunit` | `equipment_units` | `reservation` | `reservations` |
| `reservationitem` | `reservation_items` | `rental` | `rentals` |
| `unitdelivery` | `unit_deliveries` | `unitreturn` | `unit_returns` |
| `damagereport` | `damage_reports` | `payment` | `payments` |
| `invoice` | `invoices` | `invoiceitem` | `invoice_items` |
| `usersession` | `user_sessions` | `passwordreset` | `password_reset_tokens` |
| `cancellationpolicy` | `cancellation_policies` | `reservationtermination` | `reservation_terminations` |
| `deliveryrate` | `delivery_rates` | `reservationextension` | `reservation_extensions` |
| `refund` | `refunds` |  |  |

</details>

Consulta [arquitectura](docs/backend-architecture.md) y [mapa de paquetes](docs/source-package-map.md).

## Seguridad

- JWT de acceso con `sid` y vigencia predeterminada de 15 minutos. React debe conservarlo únicamente en memoria.
- Refresh token opaco de 256 bits, guardado como hash, con vigencia de 30 días y rotación atómica.
- La reutilización fuera del margen de 10 segundos compromete y revoca toda la familia.
- El refresh vive en cookie `HttpOnly`, ruta `/api`, `SameSite=Lax` y `Secure` en producción.
- Renovación y cierres de sesión exigen cookie CSRF y cabecera `X-XSRF-TOKEN`.
- Cambio de contraseña, cambio de roles y desactivación revocan sesiones activas.
- Recuperación y restablecimiento quedan deshabilitados y ocultos en el MVP; el código opaco de un uso se conserva para una entrega futura. No se exige SMTP.
- CORS con credenciales acepta únicamente los orígenes configurados.
- Los errores usan `application/problem+json` con `code`, `status`, `detail`, `instance` y `traceId`.

## Reglas críticas de negocio

- Una reserva pendiente se cancela sin cargo.
- Una reserva confirmada cancelada con 48 horas exactas o más recibe reembolso completo; por debajo del límite se cobra 25 %.
- La inasistencia puede declararse dos horas después de la hora programada y cobra 50 %.
- El retracto queda en revisión administrativa. Aprobación: cancelación y reembolso completo. Rechazo: vuelve al estado confirmado.
- Un alquiler ya iniciado bloquea cancelación comercial, inasistencia y retracto.
- Los reembolsos se calculan a partir de pagos confirmados y se registran **pendientes**, con idempotencia; no se marcan como transferidos sin pasarela externa. Si el cargo supera lo pagado se crea un ajuste pendiente.
- `CUSTOMER_PICKUP` cuesta cero. `SUPPLIER_DELIVERY` usa una tarifa COP global, versionada, y una dirección en Colombia.
- Las extensiones conservan la fecha original, actualizan la fecha efectiva, comprueban disponibilidad y usan tarifas congeladas.
- La mora se calcula por día calendario iniciado en la zona `America/Bogota` y se registra como ajuste.

## API REST

El contrato oficial empieza por `/api/v1`. El alias `/api` permanece temporalmente y responde con `Deprecation: true` y un enlace a la ruta sucesora. Swagger solo publica rutas versionadas.

Rutas principales:

- `GET /api/v1/auth/csrf`, `POST /auth/refresh`, `/logout` y `/logout-all`.
- `GET /api/v1/auth/sessions` y `DELETE /auth/sessions/{id}`.
- `GET /api/v1/public/features`: capacidades vigentes; recuperación deshabilitada.
- `POST /api/v1/auth/supplier-registration` y activación administrativa del usuario.
- `POST /api/v1/payments/checkouts`, checkout ficticio y decisión autenticada y descarga del comprobante académico.
- `GET /api/v1/reports/{income|most-rented-tools|frequent-customers|availability|profitability}`.
- `POST /api/v1/reservation-terminations/reservations/{id}/cancellation`, `/no-show`, `/withdrawal`, `/external-review` y `/supplier-failure`.
- `POST /api/v1/reservation-extensions/reservations/{id}` y resolución por aprobación o rechazo.
- `GET|POST /api/v1/commercial-configuration/delivery-rates` y `/cancellation-policies` para administración.
- `GET /api/v1/refunds/payments/{id}` y consulta normal de pagos para ajustes.

La especificación está en `http://localhost:8080/v3/api-docs` y Swagger UI en `http://localhost:8080/swagger-ui.html`. Consulta [contratos REST](docs/rest-api.md).

## Migraciones y validación

El historial previo hasta V200 se conserva sin reescritura. Las migraciones aditivas V201–V210 agregan sesiones, recuperación, políticas, terminaciones, tarifa de entrega, extensiones, reembolsos, campos operativos y retenciones/pagos completos. `db/demo/V300–V302` carga políticas, cuentas, catálogo y escenarios históricos académicos. V303 se reserva a showcase y desactiva credenciales conocidas. V304 añade la orquestación del checkout en payments; V305 incorpora decisiones y reembolsos ficticios persistidos.

Consulta [pagos del MVP](docs/fictional-payments.md): simulación explícita, retorno, permisos y persistencia sin claves externas. PayPal y correo están aplazados por decisión de alcance.

Hibernate usa `ddl-auto: validate`; Flyway tiene `clean` deshabilitado. Nunca edites una migración aplicada. Revisa [migraciones](docs/database-migrations.md), [compilación](docs/build-and-test.md) y el [reporte de validación](docs/validation-report.md).

## Modelo conceptual

La ilustración conservada es anterior a V210: la factura actualmente pertenece a la reserva y puede preceder al alquiler. Consulta el [modelo relacional vigente y el DDL físico](docs/logical-physical-model.md) para las claves actuales.

![Modelo conceptual de 25 contextos](docs/diagrams/conceptual-er-model.svg)

[Abrir SVG](docs/diagrams/conceptual-er-model.svg) · [Editar en Excalidraw](docs/diagrams/conceptual-er-model.excalidraw) · [Modelo explicado](docs/conceptual-model.md) · [Normalización 4FN](docs/fourth-normal-form.md)

## Documentación

| Tema | Documento |
| --- | --- |
| Arquitectura y paquetes | [backend-architecture.md](docs/backend-architecture.md) · [source-package-map.md](docs/source-package-map.md) · [Guía de desarrollo](docs/development-guide.md) |
| API y seguridad | [rest-api.md](docs/rest-api.md) · [production-readiness.md](docs/production-readiness.md) |
| Modelo y datos | [conceptual-model.md](docs/conceptual-model.md) · [fourth-normal-form.md](docs/fourth-normal-form.md) · [data-sources.md](docs/data-sources.md) |
| Alcance y cobertura | [product-scope.md](docs/product-scope.md) · [requirements-coverage.md](docs/requirements-coverage.md) · [pendientes.md](docs/pendientes.md) |
| Operación | [deployment.md](docs/deployment.md) · [build-and-test.md](docs/build-and-test.md) · [database-migrations.md](docs/database-migrations.md) |
| Rendimiento y accesibilidad del frontend | [Auditoría, mediciones y pruebas](docs/frontend-quality.md) |
| Condiciones | [terms-and-conditions.md](docs/terms-and-conditions.md) |

Los nombres de código y commits se mantienen en inglés. La documentación para el equipo se conserva en español.
