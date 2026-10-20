# Hoja de ruta

## Completado en backend crítico

- 25 contextos con Domain, Application, JPA y REST.
- Sesiones rotativas, revocación por familia/usuario y recuperación opaca por SMTP.
- CORS con credenciales, CSRF, rate limit y `ProblemDetail`.
- API oficial `/api/v1` y alias heredado marcado como obsoleto.
- Cancelación, inasistencia, retracto administrativo y fallo del proveedor.
- Recogida, entrega colombiana con tarifa versionada, extensiones y mora.
- Reembolsos idempotentes registrados como pendientes y ajustes financieros pendientes.
- Orden `PENDING_PAYMENT` con retención atómica, vencimiento programado y confirmación automática.
- Checkout académico idempotente, webhook HMAC, comprobante descargable y reportes operativos por rol.
- Incorporación de proveedores con revisión administrativa y bajas lógicas con revocación de sesiones.

## Pendientes antes de producción

1. Mantener la suite completa PostgreSQL/Testcontainers en CI con Docker disponible.
2. Sustituir el sandbox académico interno por la pasarela sandbox oficial seleccionada, conservando idempotencia y verificación de webhooks.
3. Definir impuestos, facturación electrónica y retenciones con asesoría fiscal.
4. Configurar SMTP productivo, reputación de dominio, plantillas y telemetría de entrega.
5. Incorporar almacenamiento de evidencias, consentimiento, protección de datos y auditoría inmutable.
6. Añadir observabilidad, backups probados, recuperación, CI/CD y análisis de dependencias.
7. Completar los paneles y recorridos operativos del frontend para clientes, proveedores y administradores.

## Frontend

La base React ya consume la API oficial `/api/v1`, conserva el access token solo en memoria, restaura la sesión mediante la cookie refresh, obtiene CSRF antes de renovar o cerrar sesión y usa `credentials: include`. El recorrido visual heredado todavía espera confirmación del proveedor y usa el pago simulado anterior; debe conectarse al checkout, retención y reportes ya disponibles en backend. También existe la ruta pública e imprimible `/terms-and-conditions`.

Siguen pendientes los paneles funcionales contra datos reales, el registro persistente de aceptaciones legales, la selección del Plan Opcional de Protección por Daños y el tratamiento específico de 403, 409 y 429 por `code`. El frontend nunca debe tomar decisiones de autorización por sí solo.
