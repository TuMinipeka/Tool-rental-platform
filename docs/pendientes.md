# Hoja de ruta

## Completado en backend crítico

- 25 contextos con Domain, Application, JPA y REST.
- Sesiones rotativas, revocación por familia/usuario y recuperación opaca por SMTP.
- CORS con credenciales, CSRF, rate limit y `ProblemDetail`.
- API oficial `/api/v1` y alias heredado marcado como obsoleto.
- Cancelación, inasistencia, retracto administrativo y fallo del proveedor.
- Recogida, entrega colombiana con tarifa versionada, extensiones y mora.
- Reembolsos idempotentes registrados como pendientes y ajustes financieros pendientes.

## Pendientes antes de producción

1. Ejecutar la suite completa PostgreSQL/Testcontainers con Docker disponible.
2. Integrar pasarela de pagos y reembolsos real con webhooks firmados.
3. Definir impuestos, facturación electrónica y retenciones con asesoría fiscal.
4. Configurar SMTP productivo, reputación de dominio, plantillas y telemetría de entrega.
5. Incorporar almacenamiento de evidencias, consentimiento, protección de datos y auditoría inmutable.
6. Añadir observabilidad, backups probados, recuperación, CI/CD y análisis de dependencias.
7. Completar los paneles y recorridos operativos del frontend para clientes, proveedores y administradores.

## Frontend

La base React ya consume la API oficial `/api/v1`, conserva el access token solo en memoria, restaura la sesión mediante la cookie refresh, obtiene CSRF antes de renovar o cerrar sesión y usa `credentials: include`. El recorrido de solicitud ya captura recogida o entrega, hora programada y dirección colombiana, y espera la confirmación del proveedor antes de habilitar el pago simulado. También existe la ruta pública e imprimible `/terms-and-conditions`.

Siguen pendientes los paneles funcionales contra datos reales, el registro persistente de aceptaciones legales, la selección del Plan Opcional de Protección por Daños y el tratamiento específico de 403, 409 y 429 por `code`. El frontend nunca debe tomar decisiones de autorización por sí solo.
