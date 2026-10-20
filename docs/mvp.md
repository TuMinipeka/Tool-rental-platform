# MVP — entrega de Daniel/Mayorga

## Estado de entrega

**Preparado para validación local; publicación pública bloqueada hasta cerrar el MVP.** La finalidad es una demo académica, con datos ficticios y pagos sandbox, no una operación comercial. La rama de integración es developer/daniel y conserva el diseño de Conde.

| Prioridad | Trabajo | Responsable | Estado / aceptación |
| --- | --- | --- | --- |
| P0 | Compose, PostgreSQL, backend, frontend y proxy | Daniel | Implementado; validar con los comandos de [despliegue](deployment.md) |
| P0 | HTTPS público en Oracle ARM | Daniel | Preparado; requiere cuenta/cupo, DNS y cierre funcional |
| P0 | Historial, herramientas, unidades y acciones vigentes de reserva | Daniel | Integración real; [evidencia HTTP/navegador](evidence/mvp/live-integration.json) |
| P0 | Orden, retención, expiración y confirmación automática por webhook | Conde | Pendiente; el flujo heredado aún permite confirmación del proveedor |
| P0 | Pasarela sandbox verificable y comprobante descargable | Conde | Pendiente; pagos registrados no prueban una pasarela |
| P0 | Reportes y permisos/CRUD pendientes | Conde | Pendiente; no fabricar reportes calculados en el navegador |
| P0 | Recuperación por correo y recorrido completo de restablecimiento | Conde + integración Daniel | Pendiente de entrega/validación; no se acredita SMTP público |
| P1 | 20 filas por entidad de negocio | Daniel | Migración demo V302; [matriz real](evidence/mvp/database-audit.json) |
| P1 | Modelo relacional, físico y 4FN | Daniel | [Modelo lógico](logical-physical-model.md), DDL exportado y normalización |
| P1 | Pruebas, Swagger, README y evidencias | Ambos | Registro actualizado en [validación](validation-report.md) |

## Contratos de integración

La API vigente es /api/v1. El frontend usa las consultas reales de reservas, herramientas, unidades, categorías, usuarios, alquileres, pagos y facturas. Las acciones vigentes son confirmation, rejection y cancellation; la cancelación conserva Idempotency-Key. La autorización y la disponibilidad las decide el backend.

Conde deberá entregar OpenAPI actualizado, ejemplos, permisos por rol/propietario, estados y errores estables, instrucciones sandbox, verificación de webhooks y pruebas. No se inventan rutas para sus contratos futuros. Daniel integrará esos contratos después de recibirlos y repetirá los recorridos reales.

## Cierre local de Daniel

- [ ] Compilación Maven y PostgreSQL/Testcontainers en la revisión final.
- [ ] Lint, build y suite de navegador aislada.
- [ ] Pruebas de navegador contra backend y PostgreSQL reales.
- [ ] Migración vacía, actualización, restricciones y concurrencia.
- [ ] Respaldo restaurado y persistencia tras reiniciar la composición aislada.
- [ ] Configuraciones Compose y proxy comprobadas.

Los resultados ejecutados, fecha y huella del código se conservan en evidence/mvp. Los datos SQL no sustituyen una prueba funcional.

## Puerta de publicación

[status.json](evidence/mvp/status.json) mantiene las condiciones externas en false hasta obtener evidencia: capacidad Oracle, DNS/HTTPS, flujo sandbox/factura/reportes, recuperación por correo, recorridos finales por rol, credenciales privadas provisionadas, respaldo externo y ejecución ARM. El script de publicación falla mientras quede una condición sin verificar. No se contratarán recursos de pago.

## Checklist funcional compartido

- [ ] Tres roles con acceso correcto y prohibiciones verificadas en la entrega final.
- [ ] Proveedor gestiona herramientas y unidades.
- [ ] Cliente reserva una unidad realmente disponible.
- [ ] Pago sandbox verificado confirma automáticamente la reserva.
- [ ] Entrega, devolución y comprobante descargable completan el recorrido.
- [ ] Paneles muestran reportes reales con permisos correctos.
- [ ] Recuperación funciona por el proveedor de correo configurado.
- [ ] Demo pública HTTPS desplegada y respaldada.

Conservar el [enunciado original](../ejercicioDemo.md). Las tareas pendientes de Conde no se reasignan silenciosamente a Daniel.
