# Caso guía: confirmar o rechazar una reserva

## Conclusión

El simulacro ya está representado en el backend, pero con nombres y límites más precisos que los del enunciado. No se debe crear un `ReservaService` paralelo ni otro enum en español. Se debe reconocer y extender el flujo existente.

| Enunciado académico | Implementación del proyecto |
| --- | --- |
| `Reserva` | Agregado `Reservation` |
| `EstadoReserva.PENDIENTE` | `ReservationStatus.PENDING` |
| `APROBADA` | `CONFIRMED` |
| `RECHAZADA` | `REJECTED` |
| `ReservaService.aprobarReserva` | `ConfirmReservationUseCase.execute` |
| `ReservaService.rechazarReserva` | `RejectReservationUseCase.execute` |
| `HerramientaNoDisponibleException` | `AvailabilityConflictApplicationException` |
| `POST /api/reservas/{id}/aprobar` | `POST /api/v1/reservations/{id}/confirmation` |
| `POST /api/reservas/{id}/rechazar` | `POST /api/v1/reservations/{id}/rejection` |

## Qué hace cada capa

### 1. Adaptador de entrada: REST y JWT

`ReservationController` recibe el UUID de la ruta. No recibe el proveedor en el cuerpo: `Actor.id(principal)` extrae el `sub` autenticado y lo convierte en `SupplierId`.

El controlador no decide si la reserva está pendiente, si la unidad está libre ni si pertenece al proveedor. Solo traduce HTTP y llama a un caso de uso.

### 2. Application: coordinación y autorización

`ConfirmReservationUseCase` abre la operación por `AtomicReservationManagement` y coordina estas validaciones:

1. la reserva pertenece al proveedor autenticado;
2. el proveedor continúa activo;
3. el cliente continúa activo;
4. cada unidad está operativa;
5. la herramienta continúa publicada;
6. proveedor y moneda no cambiaron;
7. no existe otra ocupación activa para las fechas inclusivas.

`RejectReservationUseCase` valida propiedad y actividad del proveedor, y después ordena al agregado rechazar.

Application sabe **qué necesita comprobar**, pero no conoce JPA, SQL ni el controlador.

### 3. Domain: transición válida

`Reservation.confirmWithRates` y `Reservation.reject` llaman a `requirePending`. Por eso una reserva confirmada, rechazada, cancelada o expirada no puede volver a decidirse.

La confirmación crea una copia con estado `CONFIRMED`, congela las tarifas vigentes y registra `ReservationConfirmed`. El rechazo cambia el estado a `REJECTED` y registra `ReservationRejected`.

Esta regla vive en el agregado porque debe cumplirse sin importar si la entrada futura es REST, una tarea programada o una prueba.

### 4. Puerto de salida: operación atómica

`AtomicReservationManagement` expresa un requisito de negocio y consistencia: cargar la reserva, consultar participantes, unidades y herramientas, validar ocupación y guardar el resultado en una sola transacción.

El puerto no menciona `JpaRepository`, `EntityManager`, anotaciones ni tablas.

### 5. Adaptador de persistencia

`ReservationPersistenceAdapter.execute`:

1. busca la reserva con bloqueo de escritura;
2. reconstruye el agregado;
3. bloquea unidades y herramientas en orden estable;
4. entrega un `Context` al caso de uso;
5. guarda únicamente si todas las reglas terminan correctamente.

`ReservationJpaRepository.existsActiveOverlap` considera ocupadas las reservas `CONFIRMED`, las revisiones de cancelación y las retenciones de pago vigentes. Las fechas son inclusivas: compartir el último día también es conflicto.

### 6. PostgreSQL

La tabla `reservations` persiste `reservation_status` y `confirmed_at`. Las restricciones `CHECK` impiden combinaciones incompatibles, por ejemplo una reserva confirmada sin fecha de confirmación.

`reservation_items` relaciona la reserva con unidades físicas concretas. La disponibilidad se calcula sobre `equipment_unit_id`, no sobre la herramienta genérica.

## Autenticación no es autorización

| Pregunta | Responsable | Fallo esperado |
| --- | --- | --- |
| ¿El token es auténtico y la sesión sigue activa? | Spring Security y validador JWT | `401` |
| ¿El usuario es un proveedor activo? | Caso de uso y `ParticipantsPort` | `403` |
| ¿La reserva pertenece a ese proveedor? | Caso de uso | `403` |
| ¿La reserva está pendiente? | Agregado | `409 BUSINESS_CONFLICT` |
| ¿La unidad sigue disponible? | Caso de uso dentro del puerto atómico | `409 AVAILABILITY_CONFLICT` |
| ¿La reserva existe? | Adaptador de persistencia | `404 RESOURCE_NOT_FOUND` |

La configuración HTTP exige autenticación para cualquier ruta no pública. La autorización fina se realiza con identidad y propiedad dentro del caso de uso; no depende de un `supplierId` enviado por el cliente.

## Pruebas que demuestran el comportamiento

### Domain

- una reserva nueva inicia en `PENDING`;
- confirmar produce `CONFIRMED` y un evento;
- rechazar produce `REJECTED` y un evento;
- una reserva terminal no puede confirmarse o rechazarse otra vez.

### Application

- el proveedor propietario puede confirmar;
- otro proveedor recibe acceso denegado;
- una ocupación mantiene la segunda reserva en `PENDING`;
- una falla en la segunda unidad no confirma parcialmente la primera;
- tarifas y moneda se vuelven a validar.

### Infrastructure

- dos confirmaciones concurrentes sobre la misma unidad producen un solo éxito;
- PostgreSQL conserva una única reserva confirmada para el periodo;
- los endpoints devuelven JSON y códigos HTTP coherentes.

Comandos focalizados:

```powershell
.\mvnw.cmd -pl domain -Dtest=ReservationTest test
.\mvnw.cmd -pl application -Dtest=CatalogAndReservationsTest test
.\mvnw.cmd -pl infrastructure -am -Dtest=PostgreSqlPersistenceIntegrationTest test
```

## Prueba manual con Insomnia

### Preparación

1. Inicia sesión como cliente y crea una reserva.
2. Copia `id` de la respuesta.
3. Inicia sesión como el proveedor dueño de las unidades.
4. Configura `Authorization: Bearer <accessToken>`.

### Confirmar

```http
POST http://localhost:8080/api/v1/reservations/<reservationId>/confirmation
Authorization: Bearer <supplierAccessToken>
```

No requiere cuerpo. La respuesta esperada es `200` con `status: "CONFIRMED"`.

### Rechazar

Crea otra reserva pendiente y ejecuta:

```http
POST http://localhost:8080/api/v1/reservations/<reservationId>/rejection
Authorization: Bearer <supplierAccessToken>
```

La respuesta esperada es `200` con `status: "REJECTED"`.

### Casos negativos mínimos

- repite la confirmación: `409`;
- confirma con otro proveedor: `403`;
- usa un UUID inexistente: `404`;
- confirma dos reservas superpuestas sobre la misma unidad: la segunda devuelve `409 AVAILABILITY_CONFLICT`.

## Evidencia en PostgreSQL

```sql
SELECT
    reservation_id,
    reservation_status,
    confirmed_at,
    start_date,
    return_date
FROM reservations
WHERE reservation_id = '<reservationId>';
```

Para revisar la unidad asociada:

```sql
SELECT reservation_id, equipment_unit_id, agreed_daily_rate
FROM reservation_items
WHERE reservation_id = '<reservationId>';
```

## Guion de sustentación de dos minutos

> El proveedor envía una petición autenticada al endpoint de confirmación o rechazo. El controlador obtiene su identidad del JWT y llama a un caso de uso. El caso de uso valida que sea el proveedor propietario y activo. Para confirmar, también vuelve a consultar la unidad, la herramienta, la tarifa y la disponibilidad. La transición de estado pertenece al agregado Reservation, que solo permite decidir reservas pendientes. Todo ocurre mediante AtomicReservationManagement, cuyo adaptador JPA bloquea la reserva y las unidades dentro de una transacción. Si una regla falla no se guarda ningún cambio. Finalmente, PostgreSQL conserva el estado y refuerza las combinaciones válidas con restricciones CHECK. Lo demuestro con pruebas de dominio, aplicación, concurrencia, una llamada en Insomnia y una consulta SQL.

## Preguntas de recuperación activa

Sin mirar el documento, responde:

1. ¿Por qué `CONFIRMED` es el equivalente correcto de `APROBADA`?
2. ¿Por qué `requirePending` está en Domain y no en el controlador?
3. ¿Qué carrera ocurriría si disponibilidad y guardado fueran dos transacciones?
4. ¿Qué diferencia hay entre una herramienta y una unidad física?
5. ¿Qué código decide entre `401`, `403`, `404` y `409`?

