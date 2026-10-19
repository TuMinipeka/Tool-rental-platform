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
7. Construir los recorridos del frontend contra `/api/v1` y conservar access token solo en memoria.

## Frontend

El frontend permanece fuera de esta entrega. Debe consumir `GET /auth/csrf` antes de operaciones de cookie, usar `credentials: include`, enviar `X-XSRF-TOKEN`, reaccionar a 401/403/409/429 por `code` y nunca tomar decisiones de autorización por sí solo.
