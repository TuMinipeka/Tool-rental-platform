# Contrato REST

## Convenciones

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
| 400 | JSON, parámetros o token de recuperación inválidos |
| 401 | Credencial ausente, JWT inválido o sesión revocada |
| 403 | Rol o propiedad insuficiente, incluido CSRF |
| 404 | Recurso inexistente |
| 409 | Regla de negocio, disponibilidad, reutilización o idempotencia |
| 429 | Límite de autenticación excedido |

Los códigos específicos incluyen `AVAILABILITY_CONFLICT`, `IDEMPOTENCY_CONFLICT`, `REFRESH_TOKEN_REUSED`, `CSRF_VALIDATION_FAILED` y `RATE_LIMIT_EXCEEDED`.

## Autenticación y sesiones

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
| POST | `/api/v1/auth/password-recovery` | Público; respuesta 202 genérica |
| POST | `/api/v1/auth/password-reset` | Público; token opaco y nueva contraseña |

El access token dura 15 minutos y se conserva en memoria del cliente. El refresh dura 30 días y rota en cada renovación. La recuperación nunca devuelve el token: el puerto SMTP entrega el enlace; Mailpit lo captura en desarrollo.

El proveedor queda activo al completar el formulario público y puede entrar directamente a su panel. El administrador conserva `PATCH /api/v1/users/{id}/status` para gestionar estados. `DELETE /api/v1/customers/{id}` y `DELETE /api/v1/suppliers/{id}` conservan el perfil e historial, cambian el usuario compartido a `DISABLED` y revocan sus sesiones.

## Reservas y cotización

`POST /api/v1/quotes/reservations` acepta periodo, unidades y modalidad. Devuelve días inclusivos, subtotal, entrega, total, pagos estimados y versión de política. `POST /api/v1/reservations` agrega hora programada y, para `SUPPLIER_DELIVERY`, dirección colombiana. La creación bloquea y vuelve a comprobar las unidades en la misma transacción, responde `PENDING_PAYMENT` con `expiresAt` y evita retenciones superpuestas. Un proceso programado cambia retenciones vencidas a `EXPIRED`.

## Checkout sandbox y comprobante académico

- `POST /api/v1/payments/checkouts` exige rol `CUSTOMER`, cabecera `Idempotency-Key` y una reserva propia `PENDING_PAYMENT` no vencida. Registra el total como `FULL_PAYMENT/PENDING` sin recibir PAN ni CVV.
- `POST /api/v1/payments/sandbox/webhooks` es público exclusivamente para el sandbox interno y exige `X-Sandbox-Timestamp` y `X-Sandbox-Signature`. La firma es HMAC-SHA256 de `timestamp.eventId.externalReference.outcome`; se rechazan mensajes de más de cinco minutos.
- `APPROVED` confirma pago y reserva en una transacción y emite un comprobante académico `PAID`; `DECLINED` marca pago y reserva como fallidos y libera la retención.
- `GET /api/v1/invoices/{id}/document` devuelve HTML descargable y siempre muestra que es una simulación sin validez ante la DIAN.

El secreto `SANDBOX_WEBHOOK_SECRET` debe tener al menos 32 bytes y es obligatorio en producción. Este sandbox interno es demostrativo; no representa integración con un adquirente ni transferencia real de fondos.

## Reportes operativos

Todos requieren `from` y `to` en formato ISO `YYYY-MM-DD`:

| Método | Ruta | Resultado |
| --- | --- | --- |
| GET | `/api/v1/reports/income` | Pagos confirmados, reembolsos y neto por moneda |
| GET | `/api/v1/reports/most-rented-tools` | Reservas, días y valor reservado por herramienta |
| GET | `/api/v1/reports/frequent-customers` | Frecuencia e ingreso neto por cliente y moneda |
| GET | `/api/v1/reports/availability` | Unidades disponibles, reservadas, en mantenimiento y retiradas |
| GET | `/api/v1/reports/profitability` | Indicador de ingreso reservado por herramienta |

El proveedor queda limitado a sus herramientas. El administrador consulta toda la plataforma o filtra con `supplierId`; el cliente recibe 403. El indicador de rentabilidad usa ingreso reservado porque el modelo aún no registra costos operativos y, por tanto, no afirma un margen contable.

## Catálogo operativo por rol

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

## Contrato vigente del MVP: pasarela ficticia

La entrada actual de POST /api/v1/payments/checkouts es {"reservationId":"UUID"} con Idempotency-Key y Principal autenticado. No recibe método, PAN, CVV ni datos de PayPal. GET /api/v1/payments/checkouts/{id} informa el estado de conciliación. La decisión autenticada usa POST /api/v1/payments/fictional/checkouts/{id}/decision con outcome APPROVED o DECLINED. No hay proveedor ni webhook externo en el MVP. Los ejemplos anteriores de simulación/HMAC son históricos y están deshabilitados por defecto.

Recuperación y restablecimiento salen del MVP y devuelven 404 FEATURE_DISABLED; OpenAPI los omite. GET /api/v1/public/features comparte las capacidades con el navegador. Véase [pagos del MVP](fictional-payments.md) para configuración, estados y aceptación externa pendiente.
