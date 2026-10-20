# MVP — entrega de Daniel/Mayorga

## Estado verificable

**Validado localmente; publicación pública bloqueada.** Demo académica con datos ficticios, sin cargos externos. Rama `developer/daniel`; entrega de Conde integrada hasta `981a16e`. El diseño existente se conserva.

| Prioridad | Trabajo | Responsable | Estado y evidencia |
| --- | --- | --- | --- |
| P0 | Compose, base exclusiva, backend, frontend y proxy | Daniel | Ejecutándose en localhost:15173; [configuración](evidence/mvp/configuration.json) |
| P0 | Oracle ARM, DuckDNS y HTTPS público | Daniel | Preparado; faltan cuenta/capacidad, dominio y ensayo ARM |
| P0 | Retención atómica, expiración y confirmación por webhook | Conde | Integrado; Maven/PostgreSQL y [recorrido real](evidence/mvp/live-integration.json) |
| P0 | Checkout y comprobante descargable | Conde + integración Daniel | Sandbox **interno HMAC**, pago total y comprobante académico verificados; proveedor oficial externo pendiente |
| P0 | Reportes y permisos | Conde + integración Daniel | Cinco reportes del backend, restricciones por rol y proveedor; alta de proveedor suspendido y activación administrativa verificadas |
| P0 | Correo y restablecimiento | Conde + integración Daniel | SMTP privado Mailpit y pantalla /password-reset verificados; SMTP público pendiente |
| P0 | Respaldo, persistencia y restauración | Daniel | [Restauración local](evidence/mvp/backup-restore.json) satisfactoria; destino externo pendiente |
| P1 | 20 filas por entidad de negocio | Daniel | V302 y [matriz real](evidence/mvp/database-audit.json); excepciones técnicas justificadas |
| P1 | Modelos y normalización | Daniel | [Relacional y DDL físico](logical-physical-model.md); conceptual y 4FN conservados |
| P1 | Documentación y pruebas | Ambos | [Reporte de validación](validation-report.md) |

## Cierre local de Daniel

- [x] Maven clean verify con JDK 21 y PostgreSQL/Testcontainers.
- [x] Instalación vacía, actualización desde V200, rollback y concurrencia.
- [x] Lint, build y pruebas de navegador aisladas en escritorio y móvil.
- [x] Recorrido real de tres roles sin mocks: catálogo, unidad, reserva, checkout interno, webhook, factura, entrega, devolución y cierre.
- [x] Permisos, suspensión/activación de proveedor, renovación y revocación.
- [x] Recuperación por Mailpit, token de un uso y revocación de sesión tras cambiar contraseña.
- [x] Restauración y persistencia tras reinicio; configuración pública validada sin abrir servicios públicos.
- [x] Modelos, matriz de datos y evidencias sin secretos.

Las pruebas automáticas crean datos ficticios en una base exclusiva. No prueban cobros bancarios, envío público de correo ni ejecución en Oracle.

## Dependencias y aceptación pública

| Dependencia | Responsable | Criterio pendiente |
| --- | --- | --- |
| Pasarela oficial sandbox | Conde / decisión de producto | Proveedor configurado, callback auténtico, rechazo, duplicados y conciliación; el HMAC interno no sustituye este requisito vigente |
| SMTP público | Conde + Daniel | Remitente verificado, TLS y recepción real en buzón de evaluación |
| Oracle y DNS | Daniel / titular de cuenta | Capacidad gratuita disponible, DNS y HTTPS válidos, ensayo en ARM sin recursos de pago |
| Copia externa | Daniel / titular del destino | Recepción y restauración desde otro servidor, siete copias y alertas |
| Accesos públicos | Operador | Provisionar y rotar evaluadores privados en la base pública; los usuarios demo conocidos quedan desactivados |

[status.json](evidence/mvp/status.json) distingue validación local de aceptación pública. El script de publicación y Caddy permanecen bloqueados. Los indicadores públicos solo se habilitan con evidencia del entorno destino; no por compilar. El procedimiento está en [despliegue](deployment.md).

Los contratos consumidos están en [integración](integration-contracts.md). La autoridad de importes, permisos, disponibilidad y transiciones pertenece al backend. Se conserva el [enunciado original](../ejercicioDemo.md).
