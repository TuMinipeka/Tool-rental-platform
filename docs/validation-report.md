# Reporte de validación del MVP

Validación del 20 de octubre de 2026 sobre el código de `0692c31`, rama developer/daniel. Se conserva la integración de Conde hasta 981a16e. **Aplicación validada localmente; aceptación oficial del proveedor y publicación pública pendientes.**

| Comprobación | Resultado | Evidencia y límite |
| --- | --- | --- |
| Maven clean verify, JDK 21.0.12.1 | 100 pruebas, cero fallos, errores u omisiones; 23 suites | PostgreSQL/Testcontainers, capas, formato, V200→V304, instalación vacía, rollback y concurrencia |
| Frontend lint y build | Satisfactorios | Conserva diseño, animaciones y controles de accesibilidad |
| Navegador aislado | 78 correctas, dos omisiones según dispositivo | Escritorio/móvil, axe, rutas, checkout alojado, retorno no confiable y recuperación deshabilitada; usa fixtures API |
| Frontend, backend y PostgreSQL reales | 21 comprobaciones correctas | [Vistas y datos por rol](evidence/mvp/hosted-integration.json), sin mocks; no acredita compra externa |
| production,showcase sin SMTP | 17 comprobaciones correctas | [Perfil privado](evidence/mvp/secure-profile.json): cookies, revocación, recuperación deshabilitada, OpenAPI y rechazo de simulaciones |
| Respaldo y persistencia | Conteos iguales en 26 tablas tras restauración y reinicio | [Respaldo nuevo con V304](evidence/mvp/backup-restore.json); incluye historial Flyway, sin transferencia externa |
| Datos y esquema | 25 tablas; 20 de negocio con mínimo 20 filas; 148 restricciones, 66 índices y 57 migraciones demo | [Auditoría actualizada](evidence/mvp/database-audit.json); excepciones técnicas justificadas |
| Compose, Caddy y shell | Configuración válida; publicación incompleta bloqueada | [Configuración](evidence/mvp/configuration.json); no abre servicios públicos |

## Qué se verificó en pagos

Las pruebas PostgreSQL comprueban un intento por reserva ante concurrencia, firma inválida, aprobación duplicada, importe/moneda/receptor/ambiente incorrectos, rechazo que libera inventario, recuperación de preferencias ambiguas, reembolso tardío idempotente y reintento tras falla del reembolso. Si falla la factura, pago y reserva revierten juntos. Se conservan las regresiones de CRUD, autorización, reportes y ciclo de alquiler.

El adaptador HTTP se prueba contra un servidor aislado; verifica cuenta de prueba, cuerpo del checkout, consulta del pago, recuperación de preferencia, reembolso y errores sin secretos. **Estas pruebas no sustituyen una transacción auténtica con Mercado Pago.** El navegador real confirma autenticación y lectura de datos para cliente, proveedor y administrador, además de los contratos deshabilitados del MVP.

## Evidencias y reproducción

La [evidencia consolidada](evidence/mvp/validation-summary.json) identifica revisión, huella de fuentes, imágenes locales, suites y SHA-256 de los logs privados. Logs, tokens, contraseñas y volcados no se versionan. Las capturas actuales están en [cliente](evidence/mvp/screenshots/hosted-customer.png), [proveedor](evidence/mvp/screenshots/hosted-supplier.png), [administrador](evidence/mvp/screenshots/hosted-admin.png) y [recuperación deshabilitada](evidence/mvp/screenshots/recovery-disabled.png).

Ejecuta `mvnw.cmd clean verify` con JDK 21 y Docker; en frontend, `npm run lint`, `npm run build` y `npm run test:e2e`. Con la composición privada activa ejecuta `node frontend/scripts/verify-hosted-mvp.mjs`, `node scripts/verify-showcase.mjs`, `node scripts/verify-backup.mjs`, `node scripts/audit-database.mjs` y `node scripts/verify-configuration.mjs`. El verificador de respaldo reinicia exclusivamente PostgreSQL y backend de tool-rental-mvp; no ejecutar recorridos de navegador simultáneamente.

El [recorrido anterior de 43 comprobaciones](evidence/mvp/live-integration.json) es histórico: validó HMAC interno y Mailpit antes de retirar correo y seleccionar proveedor oficial. No representa la aceptación vigente. Su runner exige ahora una activación explícita de regresión y no debe usarse para publicar.

## Lo que falta demostrar

Credenciales oficiales del vendedor de prueba, callback HTTPS auténtico, compra aprobada/rechazada y conciliación externa. La ubicación privada está preparada en .env.mercadopago.local y está ignorada por Git. No se inventaron credenciales. PayPal y correo están aplazados por decisión del alcance; recuperación permanece deshabilitada y SMTP no bloquea el arranque.

También faltan capacidad gratuita Oracle, DNS/HTTPS, ejecución ARM, restauración desde respaldo externo y accesos privados del destino. Los [manifiestos ARM anteriores](evidence/mvp/arm-manifests.json) acreditan imágenes base, no una VM ejecutándose. [MVP](mvp.md) y [despliegue](deployment.md) mantienen bloqueada la publicación hasta contar con evidencia real.
