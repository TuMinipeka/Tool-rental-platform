# Integración por rol y contratos

La API oficial usa /api/v1 y mantiene temporalmente el alias /api. El navegador guarda access tokens en memoria, refresh en cookie HttpOnly y usa CSRF. El backend decide importes, permisos y transiciones.

| Operación | Contrato y aceptación |
| --- | --- |
| Capacidades | GET /public/features; recuperación deshabilitada en el MVP |
| Catálogo | /public/catalog; sin catálogo ficticio de respaldo en publicación |
| Registro y sesión | /auth/register, /auth/supplier-registration, login, refresh y logout; el proveedor queda activo y autenticado al registrarse |
| Historial | /reservations, /rentals, /payments e /invoices; filtrados por actor |
| Catálogo del proveedor | /tools, /equipment-units y /categories; permisos y propiedad en servidor |
| Reserva | POST /reservations; retención PENDING_PAYMENT y fechas inclusivas |
| Pago ficticio | POST /payments/checkouts con reserva e Idempotency-Key; GET /payments/checkouts/{id} para retorno y conciliación |
| Decisión académica | POST /payments/fictional/checkouts/{id}/decision; APPROVED o DECLINED, cliente propietario autenticado |
| Comprobante | GET /invoices/{id}/document; descarga autenticada, académica y sin validez fiscal |
| Cancelación | POST /reservations/{id}/cancellation; clave idempotente e importes del servidor |
| Entrega y alquiler | /unit-deliveries, /rentals y /unit-returns; proveedor propietario y orden operativo vigente |
| Usuarios | /users; administrador activa o desactiva y revoca sesiones |
| Reportes | /reports/{income,most-rented-tools,frequent-customers,availability,profitability}; fechas y alcance por rol |

Todas las rutas son relativas a /api/v1. [La pasarela ficticia](fictional-payments.md) documenta campos, configuración y aceptación académica. Un retorno del navegador nunca confirma un pago.

Recuperación, restablecimiento, simulaciones heredadas, webhook HMAC y cambios administrativos de pagos no están disponibles en el MVP público. Devuelven FEATURE_DISABLED. Las pruebas de regresión antiguas habilitan expresamente esas funciones; no son instrucciones de publicación.

Los reportes representan datos persistidos. profitability es ingreso reservado, sin cálculo de utilidad neta. El comprobante puede preceder al alquiler y sus datos históricos no se editan libremente.
