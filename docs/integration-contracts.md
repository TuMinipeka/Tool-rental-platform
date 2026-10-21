# Integración por rol y contratos

La API oficial usa /api/v1 y mantiene temporalmente el alias /api. El navegador guarda access tokens en memoria, refresh en cookie HttpOnly y usa CSRF. El backend decide importes, permisos y transiciones.

| Operación | Contrato y aceptación |
| --- | --- |
| Capacidades | GET /public/features; disponibilidad del checkout y proveedor activo |
| Catálogo | /public/catalog; sin catálogo ficticio de respaldo en publicación |
| Registro y sesión | /auth/register, /auth/supplier-registration, login, refresh y logout; el proveedor queda activo y autenticado al registrarse |
| Historial | /reservations, /rentals, /payments e /invoices; filtrados por actor |
| Catálogo del proveedor | /tools, /equipment-units y /categories; permisos y propiedad en servidor |
| Reserva | POST /reservations; retención PENDING_PAYMENT y fechas inclusivas. Solo una respuesta persistida habilita la redirección automática a `/pago?reservationId={id}` y su consulta posterior en `/cuenta` |
| Pago académico | POST /payments/checkouts con reserva, medio fijo e Idempotency-Key; GET /payments/checkouts/{id} para retorno y conciliación |
| Decisión de prueba | POST /payments/fictional/checkouts/{id}/decision; aprobación o rechazo persistente del cliente propietario |
| Comprobante | GET /invoices/{id}/document; descarga autenticada, académica y sin validez fiscal |
| Cancelación | POST /reservations/{id}/cancellation; clave idempotente e importes del servidor |
| Entrega y alquiler | /unit-deliveries, /rentals y /unit-returns; proveedor propietario y orden operativo vigente |
| Usuarios | /users; administrador activa o desactiva y revoca sesiones |
| Reportes | /reports/{income,most-rented-tools,frequent-customers,availability,profitability}; fechas y alcance por rol |

Todas las rutas son relativas a /api/v1. [Pagos académicos](fictional-payments.md) documenta campos, configuración y restricciones del ambiente de prueba. Un retorno del navegador nunca confirma un pago.

Recuperación y restablecimiento de contraseña no forman parte de la API pública. Los endpoints internos heredados y los cambios administrativos de pagos permanecen bloqueados.

Los reportes representan datos persistidos. profitability es ingreso reservado, sin cálculo de utilidad neta. El comprobante puede preceder al alquiler y sus datos históricos no se editan libremente.
