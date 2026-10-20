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

1. Mantener la suite PostgreSQL/Testcontainers verificada en cada entrega; consultar el reporte de validación actual.
2. Validar el adaptador Mercado Pago Checkout Pro con cuentas oficiales de prueba, callback HTTPS auténtico y conciliación; no confundir pruebas aisladas con aceptación externa.
3. Definir impuestos, facturación electrónica y retenciones con asesoría fiscal.
4. Fuera del MVP: recuperación por SMTP, reputación del dominio, plantillas y telemetría; mantener la función oculta y deshabilitada.
5. Incorporar almacenamiento de evidencias, consentimiento, protección de datos y auditoría inmutable.
6. Añadir observabilidad, backups probados, recuperación, CI/CD y análisis de dependencias.
7. Cerrar las capacidades futuras de producto: protección, evidencias y aceptación legal persistente; los recorridos operativos y reportes actuales ya están conectados.

## Frontend

React consume `/api/v1` con access token en memoria, refresh HttpOnly y CSRF. Los paneles consultan datos y reportes reales; el flujo actual usa PENDING_PAYMENT, checkout interno firmado, comprobante descargable, entrega y devolución. La recuperación se probó con Mailpit privado. Consulta los [contratos](integration-contracts.md).

Siguen pendientes aceptación legal persistente, protección por daños, pasarela oficial y correo público. El frontend nunca decide permisos ni importes finales.

## Entrega Daniel/Mayorga

La preparación de despliegue y las dependencias de Conde se siguen en [MVP](mvp.md). Los datos de negocio se amplían mediante V302; el perfil académico público usa V303 para bloquear credenciales conocidas. No hay publicación Oracle ni acreditación de pasarela por este trabajo.
