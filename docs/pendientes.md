# Hoja de ruta

## Completado en backend y frontend

- 26 contextos con Domain, Application, JPA y REST en arquitectura hexagonal.
- Sesiones rotativas y revocacion por familia/usuario con almacenamiento seguro de tokens.
- CORS con credenciales, CSRF, rate limit de autenticacion y ProblemDetail estandar.
- API oficial /api/v1 con alias /api marcado como obsoleto mediante cabeceras HTTP.
- Cancelacion, inasistencia, retracto administrativo y fallo del proveedor.
- Recogida, entrega colombiana con tarifa versionada, extensiones y mora.
- Reembolsos idempotentes registrados y ajustes financieros trazables.
- Catalogo territorial con ciudad de servicio persistida (Bucaramanga, Floridablanca, Giron, Piedecuesta) e indice de busqueda (V306, V307, V308).
- Pasarela academica con Hosted Checkout y cinco medios fijos (Visa, Mastercard, PSE, Mercado Pago, PayPal) respaldados por V306.1.
- Generacion y descarga de comprobantes academicos en PDF estructurado mediante OpenPDF en /api/invoices/{id}/document.
- Retiro definitivo de controladores y notificadores SMTP de recuperacion de contrasena en la interfaz publica.
- Gestion de cuenta con actualizacion de perfil, cambio de contrasena y revocacion de sesiones activas.
- Dashboards y reportes por rol en frontend (BusinessDashboardPage y AccountWorkspacePage) con aislamiento estricto entre cliente, proveedor y administrador.
- Pagina de terminos y condiciones, más aceptación versionada persistente e idempotente en backend; queda conectar el formulario del frontend al nuevo endpoint.

## Pendientes antes de una salida a produccion comercial

1. Despliegue en infraestructura de nube con dominio publico propio, terminacion TLS/HTTPS y configuracion de DNS.
2. Definicion formal de credenciales de produccion para pasarela de pagos (Mercado Pago u homonimo) y webhook seguro validado mediante firma digital.
3. Definicion del regimen tributario colombiano, retenciones e integracion de facturacion electronica DIAN con soporte contable calificado.
4. Almacenamiento persistente de evidencias graficas de entrega, consentimientos de tratamiento de datos personales y auditoria inmutable.
5. Sistema centralizado de observabilidad (metricas, trazas OTLP y logs estructurados), respaldos automatizados comprobados y pipeline de CI/CD continuo.
6. Cobertura de seguro y polizas de proteccion por danos materiales o perdida de equipos.

## Frontend

React v1.2 consume /api/v1 con access token en memoria, refresh token en cookie HttpOnly y proteccion CSRF. Las vistas operativas consumen datos persistidos en PostgreSQL sin inventar estados ni suplantar permisos de servidor. Los recorridos de reserva, seleccion de medio fijo, emision de comprobante PDF y seguimiento de inventario se encuentran completamente operativos.

## Entrega e integracion

La sincronizacion con los cambios de developer/conde se encuentra integrada en developer/daniel con suite de pruebas completa en verde (104 pruebas backend sin errores y build de frontend exitoso). No hay publicaciones a Oracle ni a pasarelas reales en este entorno.
