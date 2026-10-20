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

## Auditoría de las tareas originales de Conde

Revisión: 20 de octubre de 2026. Referencia remota `developer/conde@981a16e`; integrada en `developer/daniel@59bb9c2`. Se contrastó este seguimiento con la sección «Tarea 2» del archivo original conservado en esa referencia de Git, los casos de uso y las pruebas. Las marcas de tarea completada del documento original no sustituyen la aceptación del alcance aprobado.

| Tarea original | Resultado verificado | Pendiente / límite |
| --- | --- | --- |
| 1. CRUD de clientes y proveedores | Implementados registro, consulta, actualización y baja lógica; se conserva el perfil y se revocan sesiones | Cierre backend de los perfiles verificado; no implica que toda operación futura de producto tenga pantalla |
| 2. Reservas | Retención atómica, vencimiento, disponibilidad, confirmación por evento interno, cancelación, entrega y devolución implementados | La confirmación desde un proveedor sandbox oficial depende de la tarea 3 |
| 3. Pagos y facturación | Checkout interno idempotente, HMAC, pago total, confirmación y comprobante HTML académico | **Parcial para el MVP aprobado:** falta proveedor sandbox oficial, checkout del proveedor, callback auténtico, rechazo/duplicados y conciliación verificados; la simulación interna no satisface el requisito confirmado por Daniel |
| 4. Reportes y estadísticas | Cinco endpoints con consultas persistidas, alcance de proveedor/administrador y 403 para cliente | profitability representa ingreso reservado, no utilidad neta; no hay costos contables en el modelo |
| 5. JWT y roles | Registro de proveedor suspendido, activación administrativa, restricciones, validaciones y revocación probados | Los accesos privados del entorno público se provisionan durante el despliegue; no reutilizar usuarios demo |
| 6. Documentación y pruebas backend | Conde entregó contratos y documentación en 981a16e; existen pruebas de onboarding, pago firmado, reportes y persistencia | La validación PostgreSQL que su documento dejaba pendiente fue cerrada por Daniel; faltan pruebas de la futura pasarela oficial y SMTP público |
| 7. Mejoras frontend e integración posterior | El documento original de Conde aún las marca pendientes; Daniel ya conectó los recorridos y reportes actuales preservando su diseño | No atribuir a Conde las integraciones posteriores de Daniel ni declarar terminadas mejoras adicionales sin criterios concretos de aceptación |

### Evidencias de implementación

- [CRUD de cliente](../application/src/main/java/com/toolrental/application/customer/usecase/ManageCustomerUseCase.java) y [proveedor](../application/src/main/java/com/toolrental/application/supplier/usecase/ManageSupplierUseCase.java): registro, actualización y baja lógica.
- [Checkout](../application/src/main/java/com/toolrental/application/payment/usecase/CreateSandboxCheckoutUseCase.java): crea una referencia local sandbox_; no invoca un adquirente externo. [Verificador](../infrastructure/src/main/java/com/toolrental/infrastructure/payment/adapters/in/rest/SandboxWebhookVerifier.java): HMAC propio con vigencia de cinco minutos.
- [Pruebas PostgreSQL/HTTP](../infrastructure/src/test/java/com/toolrental/infrastructure/PostgresIntegrationTest.java): onboarding y baja, firma inválida, confirmación, comprobante y alcance de reportes. La prueba PostgreSQL adicional conserva restricciones y concurrencia.
- [Recorrido real ya ejecutado](evidence/mvp/live-integration.json): 43 comprobaciones de integración sin mocks, incluyendo el sandbox interno y Mailpit. [Reporte de pruebas](validation-report.md): alcance, fecha y límites de las suites.

### Dictamen de despliegue

**No está completo el cierre público del MVP.** La pasarela oficial sigue siendo obligatoria por decisión expresa de Daniel. La recuperación está implementada, pero requiere configurar y demostrar recepción por SMTP público. Son dependencias de aceptación, no errores de compilación ni ausencia del flujo académico.

Además de esas tareas funcionales, Daniel debe cerrar capacidad Oracle gratuita, DNS/HTTPS, ejecución ARM, respaldo externo restaurado y accesos privados del destino. No se desplegará públicamente mientras falten estas evidencias. Protección, fiscalidad definitiva y otras capacidades futuras de product-scope no se declaran implementadas por haber completado el ciclo académico.

Revalidación de esta auditoría: `mvnw.cmd clean verify` con Java 21.0.12.1 y PostgreSQL/Testcontainers terminó el 20 de octubre a las 14:27:35 (America/Bogota): 83 pruebas, cero fallos, errores u omisiones; Spotless satisfactorio. Se conserva la huella del log privado en [evidencia consolidada](evidence/mvp/validation-summary.json). Los recorridos frontend se consultaron en su evidencia previa; no se presentan como repetidos en esta auditoría. La conexión JDBC local volvió a responder connected=true.
