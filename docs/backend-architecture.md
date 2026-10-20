# Arquitectura del backend

## Límites y dependencias

El backend conserva tres módulos Maven. `domain` contiene agregados, objetos de valor, eventos y puertos; `application` coordina casos de uso; `infrastructure` implementa REST, Spring Security, JPA, SMTP y transacciones. ArchUnit verifica que Domain y Application no dependan de Spring, Hibernate ni HTTP.

La convención del proyecto asigna un contexto organizativo por tabla. No implica que cada tabla pueda modificar el negocio de forma aislada: reserva coordina detalles y disponibilidad; terminación coordina reserva, pagos y reembolsos; extensión coordina disponibilidad y ajustes; sesión coordina rotación y revocación.

## Contextos

El modelo base conserva los contextos `user`, `role`, `userrole`, `customer`, `supplier`, `category`, `tool`, `toolimage`, `equipmentunit`, `reservation`, `reservationitem`, `rental`, `unitdelivery`, `unitreturn`, `damagereport`, `payment`, `invoice` e `invoiceitem`.

Los siete contextos críticos añadidos son:

| Contexto | Responsabilidad |
| --- | --- |
| `usersession` | Familia, hash de refresh, rotación, vencimiento y revocación |
| `passwordreset` | Token opaco de recuperación, vencimiento y consumo único |
| `cancellationpolicy` | Versión congelada de umbrales y porcentajes |
| `reservationtermination` | Cancelación, inasistencia, retracto y fallo del proveedor |
| `deliveryrate` | Tarifa COP global y versionada |
| `reservationextension` | Solicitud y resolución de una nueva fecha efectiva |
| `refund` | Reembolso idempotente ligado al pago y a la terminación |

Cada uno de los 25 contextos tiene paquetes en los tres módulos y una configuración propia en Infrastructure. `passwordreset` conserva sus casos de uso y DTO de aplicación en `application.passwordreset`; las rutas de recuperación y sesión están en controladores de sus respectivos contextos. Las políticas, tarifas, extensiones y reembolsos se ensamblan también desde su propia configuración. Los controladores pueden compartir una ruta funcional sin compartir el paquete de implementación.

## Consistencia

- `AtomicReservationManagement` bloquea reserva y unidades en orden estable para crear la retención, confirmar el webhook y resolver extensiones.
- Un refresh se busca con bloqueo pesimista; la fila anterior y su sucesora se guardan en una transacción.
- El hash SHA-256 identifica secretos aleatorios de alta entropía. La base nunca guarda refresh ni token de recuperación en claro.
- El checkout registra `FULL_PAYMENT/PENDING`; el webhook oficial se verifica y consulta antes de confirmar pago, reserva y comprobante en una transacción. Las llamadas externas ocurren fuera de los bloqueos; la conciliación usa concesiones persistidas. El HMAC interno queda deshabilitado en el MVP público.
- La liquidación comercial usa pagos `CONFIRMED`, genera reembolsos con clave idempotente y crea un pago `ADJUSTMENT/PENDING` cuando queda saldo.
- `OperationalReportingPort` mantiene SQL y proyecciones de lectura en Infrastructure; Application aplica alcance de proveedor o administrador antes de consultarlo.
- JPA conserva referencias escalares entre contextos. Las claves compuestas viven solo en Infrastructure mediante `@EmbeddedId`.

## Seguridad HTTP

El JWT de acceso contiene `sub`, roles, propósito `access` y `sid`. La validación comprueba firma, emisor, expiración y que la sesión siga activa. La cookie de refresh usa `HttpOnly`, ruta `/api`, `SameSite=Lax` y `Secure` en producción. Refresh, logout y logout-all están protegidos por CSRF de doble envío.

CORS con credenciales acepta orígenes configurados. El filtro de autenticación limita intentos y devuelve 429. Las respuestas de error usan `ProblemDetail`; Spring Security aplica el mismo formato para 401 y 403.

## API versionada

Los controladores publican `/api/v1` y un alias `/api`. Un interceptor añade `Deprecation` y `Link` al alias. El customizador OpenAPI elimina rutas no versionadas de la especificación.

## Persistencia

Hay 25 entidades JPA para 25 tablas. Flyway conserva V1–V210 y añade V304 para checkout oficial; todas las ampliaciones son aditivas. Hibernate usa `ddl-auto: validate`. El perfil demo añade V101–V118 y V300–V301; no debe ejecutarse sobre producción.
