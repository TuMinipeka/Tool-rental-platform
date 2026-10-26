# Solucionario razonado

Usa este documento para comparar decisiones, no para copiar nombres mecánicamente. Una solución correcta puede variar si mantiene las reglas, la dirección de dependencias y la evidencia.

## Cómo corregir un intento

Marca el primer punto donde tu flujo se apartó:

1. interpreté mal el actor o el estado;
2. elegí el agregado equivocado;
3. puse una regla en Controller o JPA;
4. olvidé autorización o atomicidad;
5. solo probé el caso feliz;
6. implementé bien, pero no produje evidencia.

Corregir el primer desvío suele eliminar varios errores posteriores.

## Solución 1: mantenimiento de una unidad

### Diseño esperado

- **Domain:** `EquipmentUnit` posee las transiciones. Reutiliza `sendToMaintenance()` y `makeOperational()` si sus precondiciones coinciden con el enunciado; ajusta allí una regla faltante.
- **Application:** un caso de uso carga la unidad y la herramienta, compara `tool.supplierId()` con el actor y ejecuta la transición.
- **Puerto:** reutiliza `EquipmentUnitRepository`; agrega una operación solo si el contrato actual no puede guardar el agregado.
- **Infrastructure:** dos endpoints delgados convierten UUID y actor tipado; la entidad y el mapper persisten `operational_status`.

Pseudocódigo de coordinación:

```java
public EquipmentUnitResponse sendToMaintenance(EquipmentUnitId id, SupplierId actor) {
  return work.inTransaction(() -> {
    var unit = units.findById(id).orElseThrow(...);
    var tool = tools.findById(unit.toolId()).orElseThrow(...);
    access.requireSupplierOwns(actor, tool.supplierId());
    unit.sendToMaintenance();
    units.save(unit);
    return EquipmentUnitResponse.from(unit);
  });
}
```

### Pruebas mínimas

- `OPERATIONAL → MAINTENANCE`;
- `MAINTENANCE → OPERATIONAL`;
- `RETIRED` rechaza ambas operaciones;
- proveedor ajeno no modifica el agregado;
- repositorio conserva el estado.

### Error frecuente

Comprobar el estado únicamente en el controlador permite que otro adaptador evada la regla.

## Solución 2: aprobar una extensión

### Diseño esperado

El repositorio ya contiene el patrón completo en `ManageReservationExtensionUseCase`. La extensión y la reserva son agregados distintos, pero la decisión modifica ambos y consulta ocupación. Por eso la coordinación debe ejecutarse dentro del límite atómico existente.

Orden correcto:

1. cargar y bloquear solicitud de extensión;
2. cargar y bloquear reserva y unidades;
3. validar proveedor propietario y activo;
4. exigir solicitud pendiente y reserva confirmada;
5. construir solo el periodo adicional;
6. comprobar cada unidad contra ocupaciones activas;
7. aplicar `reservation.extendTo(newDate)`;
8. aprobar la solicitud;
9. guardar ambos cambios o ninguno.

Para una reserva vigente hasta el 10 y una extensión hasta el 13, la ocupación adicional empieza el 11. Consultar otra vez desde la fecha original puede confundir a la propia reserva con un conflicto si no se la excluye correctamente.

### Pruebas mínimas

- extensión libre;
- fecha no creciente;
- conflicto exactamente en el nuevo último día;
- fallo en una de varias unidades;
- proveedor ajeno;
- dos aprobaciones concurrentes.

### Error frecuente

Guardar `effective_return_date` y luego validar disponibilidad rompe la atomicidad: otro proceso puede ocupar las fechas entre ambas acciones.

## Solución 3: cambiar la tarifa

### Diseño esperado

- El DTO HTTP recibe importe y moneda, pero construye `Money` antes de entrar al dominio.
- Application obtiene la herramienta, valida que el actor sea su proveedor y llama a una operación expresiva como `changeDailyRate`.
- El agregado valida importe y coherencia monetaria.
- El adaptador guarda `current_daily_rate` y `rate_currency`.
- No se actualiza `reservation_items.agreed_daily_rate`.

Pseudocódigo:

```java
public ToolResponse execute(ToolId id, SupplierId actor, Money newRate) {
  return work.inTransaction(() -> {
    var tool = tools.findById(id).orElseThrow(...);
    if (!tool.supplierId().equals(actor)) throw new AccessDeniedApplicationException();
    tool.changeDailyRate(newRate);
    tools.save(tool);
    return ToolResponse.from(tool);
  });
}
```

La tarifa actual pertenece a `tools`; la tarifa acordada pertenece a cada línea de reserva. Esta duplicación controlada preserva historia y facturación reproducible.

### Pruebas mínimas

- propietario actualiza;
- actor ajeno recibe `403`;
- importe negativo y moneda inválida fallan;
- una reserva pendiente toma la tarifa vigente al confirmarse;
- una reserva confirmada conserva su tarifa anterior.

### Error frecuente

Usar `double` introduce errores binarios y perder la moneda elimina una invariante del modelo.

## Solución 4: reservas pendientes antiguas

### Diseño esperado

Sigue el patrón de `OperationalReportUseCase` y `OperationalReportingPort`:

1. Domain declara una proyección de lectura y una operación del puerto.
2. Application determina el alcance efectivo según los roles del actor.
3. Infrastructure ejecuta SQL y mapea filas a la proyección.
4. El controlador acepta `asOf` y un filtro opcional, pero nunca permite que un proveedor amplíe su alcance.

Regla de alcance:

```text
ADMIN + sin supplierId       → toda la plataforma
ADMIN + supplierId           → proveedor solicitado
SUPPLIER                     → siempre el propio actor
CUSTOMER u otro rol          → 403
```

Conviene definir el borde como `created_at < asOf - 24 hours`: exactamente 24 horas todavía no es “más de 24 horas”. La prueba debe fijar el `Clock` para no depender del reloj real.

### Pruebas mínimas

- proveedor A no ve filas de B;
- administrador ve A y B;
- filtro administrativo devuelve solo B;
- cliente recibe `403`;
- 23:59:59, 24:00:00 y 24:00:01 alrededor del corte.

### Error frecuente

Aplicar `supplierId` directamente desde el query parameter permite que un proveedor consulte información ajena.

## Solución 5: herramientas favoritas

### Diseño esperado

Usa el contexto `legalacceptance` como referencia estructural, no como código para copiar.

Árbol mínimo:

```text
domain/toolfavorite/
  model/aggregate/ToolFavorite.java
  model/valueobject/ToolFavoriteId.java
  port/repository/ToolFavoriteRepository.java
application/toolfavorite/
  dto/ToolFavoriteResponse.java
  usecase/ManageToolFavoriteUseCase.java
infrastructure/toolfavorite/
  adapters/in/rest/controllers/ToolFavoriteController.java
  adapters/out/persistence/{entity,mapper,repository}/...
  config/ToolFavoriteBeanConfiguration.java
```

Migración propuesta:

```sql
CREATE TABLE tool_favorites (
    tool_favorite_id uuid PRIMARY KEY,
    customer_id uuid NOT NULL REFERENCES customers (user_id) ON DELETE CASCADE,
    tool_id uuid NOT NULL REFERENCES tools (tool_id) ON DELETE CASCADE,
    created_at timestamptz NOT NULL,
    CONSTRAINT uq_tool_favorites_customer_tool UNIQUE (customer_id, tool_id)
);

CREATE INDEX ix_tool_favorites_customer_created
    ON tool_favorites (customer_id, created_at DESC);
```

Idempotencia de creación:

```text
buscar por customerId + toolId
  ├─ existe → devolver la existente
  └─ no existe → crear con Clock, guardar y devolver
```

Para concurrencia real, la restricción única es la última defensa. El adaptador debe convertir la carrera de duplicado en lectura de la fila existente o en un conflicto controlado, según el contrato documentado.

### Pruebas mínimas

- agregado registra actor, herramienta y `Instant`;
- dos peticiones secuenciales devuelven una fila;
- prueba PostgreSQL demuestra la restricción única;
- historial solo contiene favoritas del actor;
- herramienta inexistente y perfil no cliente fallan;
- eliminación repetida conserva el contrato idempotente elegido.

### Error frecuente

Confiar solo en `existsBy...` no evita duplicados cuando dos transacciones consultan al mismo tiempo.

## Plantilla universal para una solución

### 1. Traducir

```text
Actor:
Acción:
Agregado dueño:
Estado inicial:
Estado final:
Datos externos:
Errores:
Evidencia:
```

### 2. Implementar de adentro hacia afuera

```text
Domain rule
  → Application orchestration
    → Domain port
      → Infrastructure adapter
        → REST and persistence
```

### 3. Probar por riesgo

```text
regla de estado
propiedad/autorización
no encontrado
rollback o atomicidad
persistencia real
contrato HTTP
```

### 4. Sustentar

Usa cuatro frases:

1. “La intención del cambio es…”
2. “La regla pertenece a … porque…”
3. “La petición recorre … y se guarda de forma…”
4. “Lo demuestro con…”

