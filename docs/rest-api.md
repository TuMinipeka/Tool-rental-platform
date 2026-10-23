# Contrato REST

> Gestión de herramientas, imágenes y persistencia: [contratos y verificación actual](provider-persistence.md). Las evidencias anteriores conservan su revisión histórica.

## Convenciones

Swagger incluye ejemplos JSON e instrucciones de autorización. Consulta [pruebas del sistema](system-tests.md) para importar la colección de Insomnia, reproducir los flujos y consultar sus resultados ejecutados.

La API oficial usa `/api/v1`. El alias `/api` es temporal y responde con `Deprecation: true` y `Link` hacia la ruta sucesora. OpenAPI muestra solo `/api/v1`.

Las rutas privadas requieren `Authorization: Bearer <access-token>`. La identidad procede del `Principal`; ningún `actorId` de entrada la sustituye. Las mutaciones idempotentes reciben `Idempotency-Key`.

Los errores tienen `Content-Type: application/problem+json`:

```json
{
  "type": "about:blank",
  "title": "Conflict",
  "status": 409,
  "detail": "Equipment unit is unavailable for the requested period",
  "instance": "/api/v1/reservation-extensions/...",
  "code": "AVAILABILITY_CONFLICT",
  "traceId": "..."
}
```

| Estado | Uso |
| --- | --- |
| 400 | JSON o parámetros inválidos |
| 401 | Credencial ausente, JWT inválido o sesión revocada |
| 403 | Rol o propiedad insuficiente, incluido CSRF |
| 404 | Recurso inexistente |
| 409 | Regla de negocio, disponibilidad, reutilización o idempotencia |
| 429 | Límite de autenticación excedido |

Los códigos específicos incluyen `AVAILABILITY_CONFLICT`, `IDEMPOTENCY_CONFLICT`, `REFRESH_TOKEN_REUSED`, `CSRF_VALIDATION_FAILED` y `RATE_LIMIT_EXCEEDED`.

## Autenticación y sesiones

El login limita el correo a 320 caracteres, valida su formato y rechaza controles invisibles o caracteres incompatibles. La contraseña se conserva exactamente y admite símbolos válidos; se rechazan nulos y más de 72 bytes UTF-8 por el BCrypt existente. Entradas inválidas reciben 400; cuenta inexistente, no activa o contraseña incorrecta comparten una respuesta 401 genérica. Los alias de login comparten el contador de intentos. [Cambios y pruebas de seguridad](header-integration.md).

| Método | Ruta | Acceso |
| --- | --- | --- |
| POST | `/api/v1/auth/register` | Público; devuelve access JSON y refresh cookie |
| POST | `/api/v1/auth/supplier-registration` | Público; crea proveedor `ACTIVE`, devuelve access JSON y refresh cookie |
| POST | `/api/v1/auth/login` | Público; devuelve access JSON y refresh cookie |
| GET | `/api/v1/auth/csrf` | Público; inicializa cookie CSRF |
| POST | `/api/v1/auth/refresh` | Refresh cookie + `X-XSRF-TOKEN` |
| POST | `/api/v1/auth/logout` | Refresh cookie + `X-XSRF-TOKEN` |
| POST | `/api/v1/auth/logout-all` | Bearer + `X-XSRF-TOKEN` |
| GET | `/api/v1/auth/sessions` | Bearer |
| DELETE | `/api/v1/auth/sessions/{id}` | Bearer |

El access token dura 15 minutos y se conserva en memoria del cliente. El refresh dura 30 días y rota en cada renovación. La API pública no ofrece recuperación ni restablecimiento de contraseña sin sesión; un usuario autenticado puede cambiar su propia contraseña con `PATCH /api/v1/users/me/password`, suministrando la actual, y el cambio revoca todas sus sesiones.

`GET /api/v1/users/me` consulta el perfil autenticado y `PATCH /api/v1/users/me/profile` actualiza nombre y correo sin aceptar un identificador de actor en el cuerpo.

El proveedor queda activo al completar el formulario público y puede entrar directamente a su panel. El administrador conserva `PATCH /api/v1/users/{id}/status` para gestionar estados. `DELETE /api/v1/customers/{id}` y `DELETE /api/v1/suppliers/{id}` conservan el perfil e historial, cambian el usuario compartido a `DISABLED` y revocan sus sesiones.

## Reservas y cotización

`POST /api/v1/quotes/reservations` acepta periodo, unidades y modalidad. Devuelve días inclusivos, subtotal, entrega, total, pagos estimados y versión de política. `POST /api/v1/reservations` agrega hora programada y, para `SUPPLIER_DELIVERY`, dirección colombiana. La creación bloquea y vuelve a comprobar las unidades en la misma transacción, responde `PENDING_PAYMENT` con `expiresAt` y evita retenciones superpuestas. Un proceso programado cambia retenciones vencidas a `EXPIRED`.

## Pago académico y comprobante

- `POST /api/v1/payments/checkouts` exige rol `CUSTOMER`, cabecera `Idempotency-Key`, un medio permitido y una reserva propia `PENDING_PAYMENT` no vencida. Registra el total como `FULL_PAYMENT/PENDING` sin aceptar un importe del navegador.
- `POST /api/v1/payments/fictional/checkouts/{id}/decision` conserva `APPROVED` o `DECLINED` para el cliente propietario y procesa la decisión de forma idempotente.
- Una aprobación confirma pago y reserva dentro del límite transaccional y emite un comprobante. Un rechazo libera la retención; una aprobación tardía inicia el reembolso idempotente.
- `GET /api/v1/invoices/{id}/document?disposition=inline|attachment` devuelve el PDF para vista previa o descarga sin presentarlo como factura válida ante la DIAN.

El proveedor académico no requiere claves financieras externas. Los datos fijos se definen en la interfaz, no se pueden modificar y el backend persiste únicamente metadatos mínimos del medio elegido.

## Reportes operativos

Todos requieren `from` y `to` en formato ISO `YYYY-MM-DD`:

| Método | Ruta | Resultado |
| --- | --- | --- |
| GET | `/api/v1/reports/income` | Pagos confirmados, reembolsos y neto por moneda |
| GET | `/api/v1/reports/most-rented-tools` | Reservas, días y valor reservado por herramienta |
| GET | `/api/v1/reports/frequent-customers` | Frecuencia e ingreso neto por cliente y moneda |
| GET | `/api/v1/reports/availability` | Unidades disponibles, reservadas, en mantenimiento y retiradas |
| GET | `/api/v1/reports/profitability` | Indicador de ingreso reservado por herramienta |
| GET | `/api/v1/reports/overdue-rentals` | Alquileres activos vencidos a la fecha `asOf` |

El proveedor queda limitado a sus herramientas. El administrador consulta toda la plataforma o filtra con `supplierId`; el cliente recibe 403. El indicador de rentabilidad usa ingreso reservado porque el modelo aún no registra costos operativos y, por tanto, no afirma un margen contable.

El reporte de mora recibe `asOf=YYYY-MM-DD` en lugar del intervalo `from/to` y devuelve reserva, cliente, devolución esperada, días vencidos, estado y cantidad de unidades.

## Catálogo operativo por rol

- `GET /api/v1/public/catalog/page` expone el catálogo público paginado sin cambiar el contrato histórico de `/api/v1/public/catalog`. Acepta `categoryId`, `query`, `serviceCity`, `minDailyRate`, `maxDailyRate`, `page`, `size` (máximo 100) y `sort=name|dailyRate,asc|desc`.
- `GET /api/v1/tools` devuelve todas las herramientas al administrador. Para un proveedor autenticado devuelve las publicaciones visibles y sus propios borradores; el frontend del panel selecciona las herramientas cuyo `supplierId` coincide con el actor.
- `POST /api/v1/tools` exige rol `SUPPLIER` y crea la herramienta para el actor autenticado. Las actualizaciones y el retiro validan propiedad o administración en el caso de uso.
- `GET /api/v1/equipment-units` devuelve todas las unidades al administrador y únicamente las unidades pertenecientes a herramientas propias al proveedor. La selección se aplica en Application mediante los puertos de unidades y herramientas; no depende de un filtro del navegador.
- `POST /api/v1/equipment-units` exige rol `SUPPLIER` y valida que la herramienta pertenezca al actor.

## Terminaciones

Todas las operaciones directas requieren `Idempotency-Key`:

- `POST /api/v1/reservation-terminations/reservations/{id}/cancellation`
- `POST /api/v1/reservation-terminations/reservations/{id}/no-show`
- `POST /api/v1/reservation-terminations/reservations/{id}/supplier-failure`
- `POST /api/v1/reservation-terminations/reservations/{id}/withdrawal`
- `POST /api/v1/reservation-terminations/reservations/{id}/external-review`
- `POST /api/v1/reservation-terminations/{id}/approval`
- `POST /api/v1/reservation-terminations/{id}/rejection`
- `GET /api/v1/reservation-terminations`

## Extensiones, entrega y devolución

- `POST /api/v1/reservation-extensions/reservations/{id}` solicita una fecha con idempotencia.
- `POST /api/v1/reservation-extensions/{id}/approval|rejection` resuelve como proveedor.
- `GET /api/v1/reservation-extensions/reservations/{id}` consulta el historial.
- `PATCH /api/v1/unit-deliveries/entry/failure` registra causa del cliente, proveedor o externa.
- La programación y entrega de todas las unidades preceden al inicio del alquiler. Una falla del proveedor permite liquidación completa antes de iniciarlo; una causa externa abre revisión administrativa.
- Las devoluciones incluyen `lateDays` y `lateFee` calculados con zona `America/Bogota`.

## Configuración y finanzas

- `GET|POST /api/v1/commercial-configuration/delivery-rates` administra la tarifa COP global.
- `GET|POST /api/v1/commercial-configuration/cancellation-policies` versiona la política.
- `GET /api/v1/refunds/payments/{id}` consulta reembolsos.
- Los endpoints existentes de pagos muestran ajustes `PENDING` de terminación, extensión o mora.

## Aceptaciones legales

- `POST /api/v1/legal-acceptances` registra para el usuario autenticado una aceptación de `TERMS_AND_CONDITIONS` o `PRIVACY_NOTICE`, con versión y propósito. Repetir la misma combinación es idempotente y devuelve el registro existente.
- `GET /api/v1/legal-acceptances/me` devuelve el historial inmutable del usuario autenticado, ordenado desde la aceptación más reciente.

## Contrato vigente del MVP: medios fijos de prueba

La entrada de `POST /api/v1/payments/checkouts` incluye `reservationId`, `paymentMethod` y, solo para Visa o Mastercard, red, últimos cuatro dígitos y vencimiento. Requiere `Idempotency-Key` y `Principal` autenticado; no recibe el importe, PAN completo, CVV ni contraseñas. `GET /api/v1/payments/checkouts/{id}` informa el estado de conciliación.

Recuperación y restablecimiento no tienen rutas públicas y OpenAPI no los anuncia. `GET /api/v1/public/features` comparte con el navegador la disponibilidad del checkout. Véase [pagos académicos](fictional-payments.md) para configuración, estados y límites.
