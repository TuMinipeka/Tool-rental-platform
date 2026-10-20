# Cobertura de requisitos

| Requisito | Estado | Evidencia principal |
| --- | --- | --- |
| Java 21, Maven multimódulo | Implementado | POM raíz y wrapper |
| Arquitectura hexagonal y DDD | Implementado | ArchUnit y módulos Domain/Application/Infrastructure |
| PostgreSQL, JPA y Flyway | Implementado | 25 entidades, hasta V305 y `ddl-auto: validate` |
| Catálogo y equipos | Implementado | Contextos base y disponibilidad |
| Reserva concurrente | Implementado | `AtomicReservationManagement` y pruebas PostgreSQL |
| Sesiones | Implementado | Refresh opaco, rotación y revocación vigentes; no hay recuperación pública de contraseña |
| Cancelaciones e inasistencia | Implementado | V203–V204 y terminaciones |
| Entrega, extensión y mora | Implementado | V205–V208 y ajustes |
| Reembolsos | Integrado con proveedor de prueba | Contexto `refund` y adaptador de Mercado Pago; falta aceptación con credenciales privadas |
| API estable | Implementado | `/api/v1`, ProblemDetail, OpenAPI filtrado |
| Frontend operativo actual | Integrado; cambio de pagos pendiente de prueba manual | Registro por rol, historial, Checkout Pro, comprobante, reportes y ciclo de alquiler |
| Publicación académica | Preparada, bloqueada | Compose seguro, respaldo restaurado localmente; faltan entorno público y dependencias externas |

La suite completa se ejecutó con JDK 21 y PostgreSQL/Testcontainers. Los comandos, resultados y límites se conservan en el reporte de validación.

## Evidencias del cierre local MVP

Consulta [validación histórica](validation-report.md), [contratos por rol](integration-contracts.md) y [modelo relacional/físico](logical-physical-model.md). Mercado Pago usa únicamente credenciales oficiales de prueba y su aceptación externa permanece pendiente en [MVP](mvp.md).

Decisión vigente del MVP: Mercado Pago Checkout Pro en COP con vendedor y comprador de prueba; PayPal, correo y recuperación no están disponibles. [Contrato y límites](mercadopago-mvp.md).

La verificación por roles distingue operaciones realizadas desde formularios y contratos REST. Las imágenes del [README](../README.md#funcionamiento-verificado-con-playwright-cli) proceden del navegador real; no acreditan una función que aún no tiene interfaz ni una transferencia bancaria.
