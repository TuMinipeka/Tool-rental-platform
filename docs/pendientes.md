# Hoja de ruta

## Completado en backend crítico

- 25 contextos con Domain, Application, JPA y REST.
- Sesiones rotativas y revocación por familia/usuario.
- CORS con credenciales, CSRF, rate limit y `ProblemDetail`.
- API oficial `/api/v1` y alias heredado marcado como obsoleto.
- Cancelación, inasistencia, retracto administrativo y fallo del proveedor.
- Recogida, entrega colombiana con tarifa versionada, extensiones y mora.
- Reembolsos idempotentes registrados como pendientes y ajustes financieros pendientes.

## Pendientes antes de producción

1. Mantener la suite PostgreSQL/Testcontainers verificada en cada entrega; consultar el reporte de validación actual.
2. Configurar las credenciales privadas de prueba y validar Mercado Pago, webhook y comprobante contra PostgreSQL.
3. Integrar PayPal en el backend con credenciales de prueba, creación de órdenes, retorno y webhook verificado; hasta entonces la opción pública permanece inactiva.
4. Definir impuestos, facturación electrónica y retenciones con asesoría fiscal.
5. Mantener recuperación, restablecimiento y correo fuera de la API y de la interfaz pública.
6. Incorporar almacenamiento de evidencias, consentimiento, protección de datos y auditoría inmutable.
7. Añadir observabilidad, backups probados, recuperación, CI/CD y análisis de dependencias.
8. Cerrar las capacidades futuras de producto: protección, evidencias y aceptación legal persistente; los recorridos operativos y reportes actuales ya están conectados.

## Frontend

React consume `/api/v1` con access token en memoria, refresh HttpOnly y CSRF. Los paneles consultan datos y reportes persistidos; el flujo usa `PENDING_PAYMENT`, selección de medio en `/pago`, redirección de tarjeta a Mercado Pago, comprobante descargable, entrega y devolución. PayPal se muestra como opción inactiva hasta completar su contrato de backend. Consulta los [contratos](integration-contracts.md).

Siguen pendientes aceptación legal persistente, protección por daños y aceptación manual del checkout oficial de prueba. El frontend nunca decide permisos, importes ni credenciales.

## Entrega Daniel/Mayorga

La preparación de despliegue y las dependencias de Conde se siguen en [MVP](mvp.md). Los datos de negocio se amplían mediante V302; el perfil académico público usa V303 para bloquear credenciales conocidas. No hay publicación Oracle ni acreditación de pasarela por este trabajo.
