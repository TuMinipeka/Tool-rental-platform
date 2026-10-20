# Integración por rol y contratos

Entrega de Conde integrada hasta `981a16e`; API oficial `/api/v1`. El navegador conserva access tokens en memoria, cookies refresh HttpOnly y CSRF. Nunca recibe el secreto del webhook.

| Vista / operación | Contrato | Autoridad y aceptación |
| --- | --- | --- |
| Catálogo y disponibilidad | GET /public/catalog y disponibilidad por herramienta | Backend; build público sin catálogo ficticio de respaldo |
| Registro cliente y proveedor | POST /auth/register y /auth/supplier-registration | Cliente activo; proveedor SUSPENDED hasta activación administrativa; sin token al registrar proveedor |
| Sesión y recuperación | /auth/login, refresh, logout, password-recovery y password-reset | Cookies/CSRF y respuesta genérica; correo probado con Mailpit privado |
| Reservas e historial | GET /reservations, /rentals, /payments y /invoices | Filtrado por actor; carga, vacío y error explícitos |
| Herramientas y unidades | /tools, /equipment-units y /categories | Propiedad y permisos en servidor |
| Reserva nueva | POST /reservations | PENDING_PAYMENT con vencimiento y retención atómica; total inclusivo del servidor |
| Checkout | POST /payments/checkouts | Idempotency-Key estable; FULL_PAYMENT; registra referencia del sandbox interno, sin cobro externo |
| Verificación académica | POST /payments/sandbox/webhooks | Operador privado; HMAC, timestamp y duplicados; jamás firmado en React |
| Comprobante | GET /invoices/{id}/document | Descarga HTML autenticada con aviso de simulación no válida ante DIAN |
| Cancelación | POST /reservations/{id}/cancellation | Idempotency-Key estable por intento; importes y transición del servidor |
| Entrega y alquiler | /unit-deliveries, /rentals y /unit-returns | Proveedor propietario; programación, entrega, inicio, devolución y cierre según estado |
| Usuarios | GET /users; PATCH /users/{id}/status | Administrador; activar suspendido/desactivar activo, revocación inmediata |
| Reportes | GET /reports/{income,most-rented-tools,frequent-customers,availability,profitability} | From/to obligatorios; proveedor limitado a su alcance, administrador global, cliente 403 |

Las rutas de la tabla son relativas a `/api/v1`. OpenAPI es la referencia exacta de cuerpos, parámetros y errores. La confirmación/rechazo manual se conserva solo para solicitudes heredadas PENDING, no para nuevas órdenes pendientes de pago.

El reporte profitability muestra ingreso reservado, **no margen contable**: aún no se registran costos. Las facturas se generan atómicamente al aprobar el pago interno y pueden preceder al alquiler. Los documentos históricos no se editan en el navegador.

## Verificación del sandbox interno

Crear el checkout desde la cuenta del cliente. Copiar su referencia académica y, exclusivamente en el entorno local aislado, configurar SANDBOX_EXTERNAL_REFERENCE en una terminal privada y ejecutar `node scripts/send-sandbox-event.mjs`. SANDBOX_OUTCOME admite APPROVED o DECLINED. El script lee el secreto de .env.validation, genera timestamp/firma y no imprime secretos ni simula un proveedor externo. El pago aprobado confirma la reserva y emite un único comprobante.

La prueba `node frontend/scripts/verify-live-mvp.mjs` ejecuta este contrato contra PostgreSQL, verifica firma inválida, duplicados, descarga, reportes, permisos, ciclo operativo, correo privado y revocación. Los estados de error no se convierten en éxitos locales.

## Pendiente de aceptación pública

Pasarela sandbox oficial, SMTP público, HTTPS y pruebas en Oracle ARM mantienen sus criterios en [MVP](mvp.md). El frontend no inventa endpoints para reemplazar esas dependencias. Los reportes y el correo privado ya entregados por Conde no se clasifican como ausentes.
