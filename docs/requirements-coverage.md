# Cobertura de requisitos

| Requisito | Estado | Evidencia principal |
| --- | --- | --- |
| Java 21, Maven multimódulo | Implementado | POM raíz y wrapper |
| Arquitectura hexagonal y DDD | Implementado | ArchUnit y módulos Domain/Application/Infrastructure |
| PostgreSQL, JPA y Flyway | Implementado | 26 entidades, hasta V309 y `ddl-auto: validate` |
| Catálogo y equipos | Implementado | Contextos base y disponibilidad |
| Reserva concurrente | Implementado | `AtomicReservationManagement` y pruebas PostgreSQL |
| Sesiones | Implementado | Refresh opaco, rotación y revocación vigentes; no hay recuperación pública de contraseña |
| Cancelaciones e inasistencia | Implementado | V203–V204 y terminaciones |
| Entrega, extensión y mora | Implementado | V205–V208 y ajustes |
| Reembolsos | Integrado con proveedor académico | Contexto `refund`, conciliación y decisión persistente |
| API estable | Implementado | `/api/v1`, ProblemDetail, OpenAPI filtrado |
| Aceptación legal versionada | Implementado | V309 y contexto `legalacceptance`; POST idempotente y consulta del usuario |
| Frontend operativo actual | Integrado; cambio de pagos pendiente de revisión manual | Registro por rol, historial, cinco medios fijos, comprobante, reportes y ciclo de alquiler |
| Publicación académica | Preparada, bloqueada | Compose seguro, respaldo restaurado localmente; faltan entorno público y dependencias externas |

La suite completa se ejecutó con JDK 21 y PostgreSQL/Testcontainers. Los comandos, resultados y límites se conservan en el reporte de validación.

## Evidencias del cierre local MVP

Consulta [validación histórica](validation-report.md), [contratos por rol](integration-contracts.md) y [modelo relacional/físico](logical-physical-model.md). El proveedor académico conserva pagos, decisiones y comprobantes en PostgreSQL; la revisión de los cinco medios permanece pendiente en [MVP](mvp.md).

Decisión vigente del MVP: Visa, Mastercard, PSE, Mercado Pago y PayPal en COP con datos fijos no editables; el correo y la recuperación no están disponibles. [Contrato y límites](fictional-payments.md).

La verificación por roles distingue operaciones realizadas desde formularios y contratos REST. Las imágenes del [README](../README.md#funcionamiento-verificado-con-playwright-cli) proceden del navegador real; no acreditan una función que aún no tiene interfaz ni una transferencia bancaria.
