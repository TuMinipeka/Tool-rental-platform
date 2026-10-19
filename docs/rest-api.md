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

## Reservas y cotización

`POST /api/v1/quotes/reservations` acepta periodo, unidades y modalidad. Devuelve días inclusivos, subtotal, entrega, total, pagos estimados y versión de política. `POST /api/v1/reservations` agrega hora programada y, para `SUPPLIER_DELIVERY`, dirección colombiana. La respuesta conserva modalidad, desglose, política, fecha efectiva y acciones permitidas.

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
