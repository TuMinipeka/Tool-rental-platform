# Cobertura de requisitos

| Requisito | Estado | Evidencia principal |
| --- | --- | --- |
| Java 21, Maven multimódulo | Implementado | POM raíz y wrapper |
| Arquitectura hexagonal y DDD | Implementado | ArchUnit y módulos Domain/Application/Infrastructure |
| PostgreSQL, JPA y Flyway | Implementado | 25 entidades, V1–V210 y `ddl-auto: validate` |
| Catálogo y equipos | Implementado | Contextos base y disponibilidad |
| Reserva concurrente | Implementado | `AtomicReservationManagement` y pruebas PostgreSQL |
| Sesiones y recuperación segura | Implementado | V201–V202, refresh opaco, SMTP y revocación |
| Cancelaciones e inasistencia | Implementado | V203–V204 y terminaciones |
| Entrega, extensión y mora | Implementado | V205–V208 y ajustes |
| Reembolsos | Simulación implementada | Contexto `refund`; falta proveedor financiero real |
| API estable | Implementado | `/api/v1`, ProblemDetail, OpenAPI filtrado |
| Frontend operativo actual | Integrado y probado | Historial, checkout interno, comprobante, reportes y ciclo de alquiler; alcance futuro en MVP |
| Publicación académica | Preparada, bloqueada | Compose seguro, respaldo restaurado localmente; faltan entorno público y dependencias externas |

La suite completa se ejecutó con JDK 21 y PostgreSQL/Testcontainers. Los comandos, resultados y límites se conservan en el reporte de validación.

## Evidencias del cierre local MVP

Consulta [validación](validation-report.md), [contratos por rol](integration-contracts.md) y [modelo relacional/físico](logical-physical-model.md). El sandbox interno firmado está probado; no acredita una pasarela oficial externa. La publicación pública y las dependencias externas permanecen pendientes en [MVP](mvp.md).
