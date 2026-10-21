# Reporte de validación del MVP

## Aceptación crítica de API del 21 de octubre de 2026

La comprobación actual de Swagger, Insomnia y Apache Tomcat está en [pruebas del sistema](system-tests.md): 41 peticiones y 61 comprobaciones aprobadas mediante Inso CLI, persistencia consultada directamente en PostgreSQL aislado y dos pruebas focalizadas de OpenAPI. Esta verificación no sustituye ni amplía el alcance de las regresiones históricas descritas a continuación.

> Gestión de herramientas, imágenes y persistencia: [contratos y verificación actual](provider-persistence.md). Las evidencias anteriores conservan su revisión histórica.

> Integración actual de Conde: [cambios y validaciones pendientes](integration-review.md). Las capturas y recorridos previos son evidencia histórica y no acreditan automáticamente los cambios nuevos.

Validación del 20 de octubre de 2026 sobre la revisión `8e68b8b`, incluida la corrección de formularios y cierre de sesión, rama developer/daniel. Se conserva la integración de Conde hasta 981a16e. **Aplicación integrada y validada localmente con pasarela ficticia; publicación pública pendiente.** El cambio de alcance elimina la dependencia de una pasarela sandbox oficial.

| Comprobación | Resultado | Evidencia y límite |
| --- | --- | --- |
| Maven clean verify, JDK 21.0.12.1 | 105 pruebas, cero fallos, errores u omisiones; 24 suites | PostgreSQL/Testcontainers, capas, formato, V200→V305, instalación vacía, rollback y concurrencia |
| Frontend lint y build | Satisfactorios | Conserva diseño, animaciones y controles de accesibilidad |
| Navegador aislado | 88 correctas, dos omisiones previstas por dispositivo | Escritorio/móvil, axe, rutas, aprobación/rechazo ficticios y recuperación deshabilitada; usa fixtures API |
| Playwright CLI con frontend, backend y PostgreSQL reales | 91 comprobaciones correctas y 23 capturas | [Recorrido por roles](evidence/mvp/role-workflows.json): registro, activación/baja, inventario, pago, comprobante, entrega, devolución, cierre, reportes y permisos; sin mocks |
| production,showcase sin SMTP ni claves de pago | 18 comprobaciones correctas | [Perfil privado](evidence/mvp/secure-profile.json): cookies, revocación, roles, recuperación deshabilitada, OpenAPI y bloqueo de rutas internas antiguas |
| Apache Tomcat integrado | Apache Tomcat/10.1.55, JAR ejecutable, puerto interno 8080 y JDBC operativo | [Verificación del contenedor](evidence/mvp/tomcat-runtime.json); sin Tomcat externo ni WAR |
| Respaldo y persistencia | Conteos iguales en 26 tablas tras restauración y reinicio | [Respaldo con V305](evidence/mvp/backup-restore.json); incluye historial Flyway, sin transferencia externa |
| Datos y esquema | 25 tablas; 20 de negocio con mínimo 20 filas; 149 restricciones, 66 índices y 58 migraciones demo | [Auditoría](evidence/mvp/database-audit.json); excepciones técnicas justificadas |
| Compose, Caddy y shell | Configuración válida; publicación incompleta bloqueada | [Configuración](evidence/mvp/configuration.json); no abre servicios públicos |

## Gestión de proveedor y persistencia — 20 de octubre de 2026

Revisión de código `b3ac9f5`: **112 pruebas Java, 84 de navegador y 67 comprobaciones reales de Playwright CLI/PostgreSQL**, con dos omisiones de navegador previstas. Respaldo de base y fotos restaurado, hashes y conteos coincidentes, recreación de contenedores sin pérdida y aplicación habitual disponible en 5173/8080. Se verificó la regresión de carga del perfil. [Resultados detallados](provider-persistence.md) y [resumen verificable](evidence/provider-persistence/validation.json).

La configuración showcase se validó sintácticamente con valores temporales de validación: la plantilla exige secretos y referencias de imágenes externas. No constituye arranque público, configuración de DNS ni validación de Oracle o transferencia externa.

## Pagos y ciclo de alquiler

La pasarela guarda APPROVED o DECLINED en PostgreSQL y recupera decisiones interrumpidas. No solicita tarjeta, no necesita cuentas externas y no mueve dinero. El importe y el vencimiento proceden del backend. La decisión exige cliente propietario autenticado; proveedor y tercero no pueden aprobarla. La repetición es idempotente y una decisión contradictoria devuelve 409.

Las cinco pruebas nuevas con PostgreSQL real comprueban aprobación repetida con un solo comprobante, permisos, rechazo y vencimiento, recuperación tardía con reembolso ficticio y aprobaciones concurrentes. Se conservan las pruebas de transacción local, disponibilidad y rollback si falla la factura. El adaptador opcional de Mercado Pago conserva pruebas aisladas como regresión de código inactivo; no constituye el pago del MVP.

El recorrido del navegador real comprueba una reserva de un día por 25.000 COP, conflicto por unidades retenidas, aprobación académica, comprobante único descargable, entrega por el proveedor, inicio del alquiler, devolución y cierre COMPLETED. Comprueba también que el rechazo libere inventario, que los reportes consulten datos persistidos y que el cliente no acceda a administración.

## Evidencias y reproducción

La [evidencia consolidada](evidence/mvp/validation-summary.json) identifica revisión, huella de fuentes, imágenes locales, suites y SHA-256 de los logs privados. Las evidencias individuales conservan la huella y la fecha de su ejecución; algunas se generaron antes de los commits locales. Logs, tokens, contraseñas y volcados no se versionan.

Las capturas del recorrido actual están incrustadas en el [README](../README.md#funcionamiento-verificado-con-playwright-cli); la [matriz por roles](browser-verification.md) distingue interfaz y REST. El recorrido anterior de 19 comprobaciones se conserva como evidencia de una ejecución previa, no como el conteo actual.

Ejecuta `mvnw.cmd clean verify` con JDK 21 y Docker; en frontend, `npm run lint`, `npm run build` y `npm run test:e2e`. Prepara la composición privada mediante [despliegue](deployment.md). Con ella activa ejecuta:

```powershell
node frontend/scripts/verify-role-workflows.mjs
node scripts/verify-showcase.mjs
node scripts/verify-tomcat.mjs
node scripts/verify-backup.mjs
node scripts/audit-database.mjs
node scripts/verify-configuration.mjs
```

El verificador de respaldo reinicia exclusivamente PostgreSQL y backend de tool-rental-mvp; no ejecutar recorridos de navegador simultáneamente. La aplicación aislada queda en http://localhost:15173 y el backend en http://localhost:18080. La composición de trabajo anterior permanece separada.

Las evidencias [HMAC/Mailpit](evidence/mvp/live-integration.json) y [preparación del proveedor externo](evidence/mvp/hosted-integration.json) son históricas; no representan el comportamiento vigente. No usar sus runners para acreditar la publicación actual.

## Hallazgos corregidos

Se retiene el elemento de los formularios antes de esperar la respuesta del servidor, evitando errores al limpiarlos después de guardar. Las operaciones de sesión obtienen un CSRF vigente: el cierre real ahora exige 204 y el acceso anterior devuelve 401. Las regresiones cubren escritorio/móvil después del ingreso y de la renovación. No se deshabilitaron CSRF ni el límite de autenticación; el recorrido respeta sus ventanas.

## Pendientes del destino público

La simulación de pagos está aceptada localmente conforme al alcance autorizado; no se exige una compra externa. PayPal y correo están aplazados, recuperación permanece oculta y deshabilitada, y SMTP no bloquea el arranque.

Faltan capacidad gratuita Oracle, DNS/HTTPS, ejecución ARM, restauración desde respaldo externo y accesos privados del destino. Después deberán repetirse allí los recorridos por rol. Los [manifiestos ARM anteriores](evidence/mvp/arm-manifests.json) acreditan imágenes base, no una VM ejecutándose. [MVP](mvp.md), [estado](evidence/mvp/status.json) y [despliegue](deployment.md) mantienen bloqueada la publicación hasta contar con evidencia del entorno público.
