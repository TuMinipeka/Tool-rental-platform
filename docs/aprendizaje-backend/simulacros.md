# Cinco simulacros de backend

No abras el [solucionario](soluciones.md) hasta terminar tu intento o consumir 120 minutos. En cada ejercicio escribe primero una tabla con actor, estado inicial, resultado, errores y evidencia.

## Simulacro 1: transición de estado

### Enunciado

Permitir que un proveedor marque una unidad propia como enviada a mantenimiento y luego la reactive. Solo una unidad `OPERATIONAL` puede pasar a `MAINTENANCE`; solo una unidad `MAINTENANCE` puede volver a `OPERATIONAL`. Una unidad `RETIRED` es terminal.

Endpoints hipotéticos:

```http
POST /api/v1/equipment-units/{id}/maintenance
POST /api/v1/equipment-units/{id}/reactivation
```

### Distribución del tiempo

- 0–20: localizar agregado, estado, caso de uso y controlador semejantes.
- 20–55: implementar reglas y caso de uso.
- 55–80: añadir endpoints y ensamblaje.
- 80–105: pruebas.
- 105–120: Insomnia, SQL y revisión.

### Mapa que debes descubrir

- agregado y enum del contexto `equipmentunit`;
- puerto que guarda unidades;
- caso de uso que ya cambia su condición;
- controlador y configuración del contexto;
- entidad y mapper JPA.

### Casos exigidos

| Caso | Resultado |
| --- | --- |
| Proveedor propietario + `OPERATIONAL` | `MAINTENANCE` |
| Reactivar `MAINTENANCE` | `OPERATIONAL` |
| Cambiar unidad de otro proveedor | `403` |
| Enviar `RETIRED` a mantenimiento | `409` |
| UUID inexistente | `404` |

### Pistas progresivas

1. Busca `sendToMaintenance` antes de crear un método nuevo.
2. La propiedad se comprueba mediante la herramienta asociada.
3. El controlador no debe recibir `supplierId`.

### Insomnia y SQL

Envía ambos `POST` sin cuerpo con JWT de proveedor. Verifica:

```sql
SELECT equipment_unit_id, operational_status
FROM equipment_units
WHERE equipment_unit_id = '<id>';
```

### Rúbrica y evidencia

- 35 % reglas del agregado;
- 25 % autorización y coordinación;
- 20 % REST y errores;
- 20 % pruebas, SQL y commit.

Explica en dos minutos por qué `RETIRED` no debe reactivarse y por qué el actor sale del JWT.

---

## Simulacro 2: validación cruzada de disponibilidad

### Enunciado

Permitir que un proveedor apruebe una solicitud de extensión de una reserva confirmada. La nueva fecha debe ser posterior a la devolución efectiva y ninguna unidad puede estar ocupada durante los días adicionales.

Endpoint hipotético:

```http
POST /api/v1/reservation-extensions/{extensionId}/approval
```

### Distribución del tiempo

- 0–25: rastrear reserva, extensión y consulta de ocupación.
- 25–65: regla, coordinación y límite transaccional.
- 65–90: persistencia y endpoint.
- 90–110: pruebas de bordes y concurrencia.
- 110–120: evidencia.

### Mapa que debes descubrir

- agregado `ReservationExtension` y estado de solicitud;
- `Reservation.extendTo`;
- `ManageReservationExtensionUseCase`;
- `AtomicReservationManagement.Context.isOccupied`;
- controlador de extensiones.

### Casos exigidos

| Caso | Resultado |
| --- | --- |
| Fecha posterior y unidades libres | extensión aprobada |
| Fecha igual o anterior | `409` |
| Solapamiento en un solo día | `409 AVAILABILITY_CONFLICT` |
| Proveedor ajeno | `403` |
| Extensión inexistente | `404` |

### Pistas progresivas

1. Las fechas del proyecto son inclusivas.
2. La extensión y la reserva deben cambiar dentro de la misma unidad de trabajo.
3. Un fallo en una unidad no puede ampliar parcialmente la reserva.

### Insomnia y SQL

```http
POST /api/v1/reservation-extensions/<extensionId>/approval
Authorization: Bearer <supplierAccessToken>
```

```sql
SELECT reservation_id, return_date, effective_return_date, reservation_status
FROM reservations
WHERE reservation_id = '<reservationId>';
```

### Rúbrica y evidencia

- 30 % disponibilidad inclusiva;
- 30 % atomicidad;
- 20 % autorización;
- 20 % pruebas y evidencia.

Explica qué pasaría si primero guardaras la nueva fecha y después comprobaras disponibilidad.

---

## Simulacro 3: autorización por propiedad

### Enunciado

Agregar una operación para cambiar la tarifa diaria de una herramienta publicada. Solo el proveedor propietario puede hacerlo. La tarifa debe ser no negativa, conservar COP y afectar reservas futuras, no tarifas ya congeladas en reservas confirmadas.

Endpoint hipotético:

```http
PATCH /api/v1/tools/{id}/daily-rate
Content-Type: application/json

{"amount": 45000, "currency": "COP"}
```

### Distribución del tiempo

- 0–20: localizar actor, herramienta, dinero y tarifa congelada.
- 20–60: comando, caso de uso y regla de dominio.
- 60–85: endpoint, DTO y validación.
- 85–110: pruebas de propiedad e historial.
- 110–120: evidencia.

### Casos exigidos

| Caso | Resultado |
| --- | --- |
| Propietario y valor válido | tarifa actualizada |
| Proveedor diferente | `403` |
| Importe negativo | `409` o validación coherente |
| Moneda diferente | `409` |
| Herramienta inexistente | `404` |
| Reserva confirmada previa | conserva tarifa acordada |

### Pistas progresivas

1. Compara `tool.supplierId()` con el actor.
2. Usa `Money`; no uses `double`.
3. La reserva almacena `agreed_daily_rate` en sus líneas.

### Insomnia y SQL

```sql
SELECT tool_id, current_daily_rate, rate_currency
FROM tools
WHERE tool_id = '<toolId>';

SELECT reservation_id, agreed_daily_rate
FROM reservation_items
WHERE equipment_unit_id = '<equipmentUnitId>';
```

### Rúbrica y evidencia

- 30 % propiedad y JWT;
- 25 % modelo monetario;
- 25 % preservación histórica;
- 20 % pruebas y contrato REST.

Explica por qué una tarifa nueva no debe reescribir una reserva confirmada.

---

## Simulacro 4: reporte con alcance por rol

### Enunciado

Crear un reporte de reservas pendientes con más de 24 horas. Un proveedor solo ve las suyas; un administrador puede ver toda la plataforma o filtrar por proveedor; un cliente recibe `403`.

Endpoint hipotético:

```http
GET /api/v1/reports/stale-pending-reservations?asOf=<instant>&supplierId=<uuid-opcional>
```

### Distribución del tiempo

- 0–25: localizar reportes, roles y proyecciones existentes.
- 25–60: definir puerto, proyección y SQL.
- 60–85: aplicar alcance en Application y exponer REST.
- 85–110: pruebas por rol y borde temporal.
- 110–120: evidencia.

### Casos exigidos

| Caso | Resultado |
| --- | --- |
| Proveedor | solo reservas propias |
| Administrador sin filtro | todas las pendientes antiguas |
| Administrador con filtro | solo proveedor solicitado |
| Cliente | `403` |
| Reserva con exactamente 24 horas | documentar y probar el borde elegido |

### Pistas progresivas

1. El SQL pertenece al adaptador de reportes, no al controlador.
2. Application decide el alcance efectivo del actor.
3. Inyecta `Clock`; no disperses `Instant.now()`.

### Insomnia y SQL

Prueba el mismo `GET` con JWT de proveedor, administrador y cliente. Contrasta con:

```sql
SELECT r.reservation_id, r.created_at, r.reservation_status, t.supplier_id
FROM reservations r
JOIN reservation_items ri ON ri.reservation_id = r.reservation_id
JOIN equipment_units eu ON eu.equipment_unit_id = ri.equipment_unit_id
JOIN tools t ON t.tool_id = eu.tool_id
WHERE r.reservation_status = 'PENDING'
  AND r.created_at < CAST('<asOf>' AS timestamptz) - INTERVAL '24 hours';
```

### Rúbrica y evidencia

- 30 % SQL y proyección;
- 30 % aislamiento por rol;
- 20 % contrato REST;
- 20 % pruebas y evidencia.

Explica por qué el controlador no debe confiar en `supplierId` para limitar a un proveedor.

---

## Simulacro 5: nueva persistencia

### Enunciado

Permitir que un cliente marque herramientas como favoritas y consulte sus favoritas. La combinación cliente-herramienta debe ser única y registrar la fecha. Repetir la misma solicitud debe ser idempotente.

Endpoints hipotéticos:

```http
POST /api/v1/tool-favorites/{toolId}
GET  /api/v1/tool-favorites/me
DELETE /api/v1/tool-favorites/{toolId}
```

### Distribución del tiempo

- 0–20: diseñar agregado, identidad y restricción única.
- 20–55: Domain y Application.
- 55–90: migración, entidad JPA, mapper y adaptador.
- 90–108: controlador y configuración.
- 108–120: pruebas y evidencia.

### Casos exigidos

| Caso | Resultado |
| --- | --- |
| Cliente agrega favorita | fila creada |
| Repite la petición | misma evidencia, sin duplicado |
| Herramienta inexistente | `404` |
| Proveedor o administrador | `403` si el alcance exige cliente |
| Elimina favorita inexistente | comportamiento idempotente documentado |

### Pistas progresivas

1. Usa la aceptación legal existente como patrón de contexto nuevo e idempotente.
2. La restricción única también debe existir en PostgreSQL.
3. `userId` sale del `Principal`; no del JSON.

### Insomnia y SQL

```sql
SELECT customer_id, tool_id, created_at
FROM tool_favorites
WHERE customer_id = '<customerId>'
ORDER BY created_at DESC;
```

### Rúbrica y evidencia

- 25 % arquitectura completa;
- 25 % modelo físico y migración;
- 20 % idempotencia;
- 15 % autorización;
- 15 % pruebas y Swagger.

Explica el recorrido completo desde el `POST` hasta la restricción única.

## Checklist común de entrega

- [ ] Caso feliz en Insomnia con JSON visible.
- [ ] Caso sin permiso.
- [ ] Caso de regla inválida.
- [ ] Consulta SQL que demuestra el cambio.
- [ ] Pruebas focalizadas en verde.
- [ ] Swagger muestra el contrato nuevo.
- [ ] `git diff` no contiene cambios accidentales.
- [ ] Commit con `<type>: :<gitmoji>: <UpperCamelCaseSummary>`.

