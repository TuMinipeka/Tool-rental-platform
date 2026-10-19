# Cobertura de requisitos

| Requisito | Estado | Evidencia principal |
| --- | --- | --- |
| Java 21, Maven multimódulo | Implementado | POM raíz y wrapper |
| Arquitectura hexagonal y DDD | Implementado | ArchUnit y módulos Domain/Application/Infrastructure |
| PostgreSQL, JPA y Flyway | Implementado | 25 entidades, V1–V208 y `ddl-auto: validate` |
| Catálogo y equipos | Implementado | Contextos base y disponibilidad |
| Reserva concurrente | Implementado | `AtomicReservationManagement` y pruebas PostgreSQL |
| Sesiones y recuperación segura | Implementado | V201–V202, refresh opaco, SMTP y revocación |
| Cancelaciones e inasistencia | Implementado | V203–V204 y terminaciones |
| Entrega, extensión y mora | Implementado | V205–V208 y ajustes |
| Reembolsos | Simulación implementada | Contexto `refund`; falta proveedor financiero real |
| API estable | Implementado | `/api/v1`, ProblemDetail, OpenAPI filtrado |
| Frontend completo | Pendiente | Base React existente; recorridos aún no implementados |
| Producción operada | Pendiente | Faltan CI/CD, observabilidad y recuperación ensayada |

La suite sin PostgreSQL pasa Domain, Application, arquitectura, mapeo y formato. La evidencia completa de migraciones y concurrencia requiere Docker activo o un usuario dueño de una base de pruebas con permiso `CREATE SCHEMA`.
