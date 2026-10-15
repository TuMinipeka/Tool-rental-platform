# Normalización hasta cuarta forma normal

## Base del análisis

El análisis parte del [modelo conceptual](conceptual-model.md), con PostgreSQL y periodos de reserva inclusivos. `X -> Y` indica dependencia funcional; `X ->> Y`, dependencia multivaluada. Una superclave determina toda la fila y una clave candidata es una superclave mínima.

La revisión original utilizó la skill `database-normalization`. Una relación cumple 4FN si toda dependencia multivaluada no trivial tiene una superclave como determinante. La conclusión depende de los supuestos del negocio; no se demuestra únicamente contando tablas.

## Datos sin normalizar

Un documento con usuario, roles, herramientas, imágenes, unidades, pagos y factura anidados mezcla hechos independientes. No permitiría registrar inventario sin reserva, repetiría datos de proveedor y podría borrar la última descripción de herramienta al borrar una operación.

## Primera forma normal

Cada columna contiene un valor y cada fila un hecho. Se separan roles, imágenes, líneas, pagos y líneas de factura. Inicio y devolución son columnas diferentes y la dirección se descompone en sus componentes. Aplanar todo en una relación de reserva/unidad produce valores atómicos, pero aún repite cabecera y catálogo.

## Segunda forma normal

Con clave `(reservation_id, equipment_unit_id)` aparecen dependencias parciales:

```text
reservation_id -> start_date, return_date, customer_id
equipment_unit_id -> tool_id
(reservation_id, equipment_unit_id) -> agreed_daily_rate
```

Se separan `reservations`, `equipment_units` y `reservation_items`. La tarifa acordada depende del par y es distinta de la tarifa actual del catálogo.

## Tercera forma normal

Se eliminan dependencias transitivas:

```text
reservation_id -> customer_id -> billing_name, document_number
equipment_unit_id -> tool_id -> name, supplier_id, category_id
tool_id -> supplier_id -> business_name
tool_id -> category_id -> category_name
```

Cliente, herramienta, proveedor y categoría tienen relaciones propias. Las reservas conservan referencias; no duplican nombres actuales. Las instantáneas de factura son atributos históricos del documento emitido, no dependencias del nombre vigente del perfil.

## Boyce-Codd

En toda dependencia funcional no trivial el determinante debe ser superclave. También se revisan correo, código de rol, documentos, número de factura y referencia del procesador. Añadir `tool_id` a `reservation_items` sería redundante: `equipment_unit_id -> tool_id`, pero la unidad sola no determina una línea de reserva. Por eso se conserva como referencia derivable desde la unidad.

## Cuarta forma normal

Una relación que mezclara herramienta, imagen y unidad generaría un producto cruzado:

```text
tool_id ->> image_url
tool_id ->> equipment_unit_id
```

El determinante no sería superclave de la relación combinada. Se descompone sin pérdida, bajo el supuesto de independencia, en:

```text
tools(tool_id, ...)
tool_images(tool_id, image_url)
equipment_units(equipment_unit_id, tool_id, inventory_code, ...)
```

También se separan roles de perfiles y líneas de reserva de pagos. Una asociación N:M no viola por sí misma 4FN: la tarifa acordada pertenece realmente al par reserva/unidad. Si una imagen pasa a describir una unidad específica, se necesita una relación distinta y revisar el supuesto.

## Esquema lógico resultante

PK: clave primaria; AK: clave candidata alternativa; FK: referencia; `?`: opcional. Tablas en inglés plural y `snake_case`; columnas en singular. El [historial Flyway](database-migrations.md) implementa estas 18 relaciones, sin confundir cantidad de tablas con cantidad de migraciones.

```text
users(
  user_id PK, email AK, display_name, password_hash,
  account_status, registered_at
)

roles(
  role_id PK, code AK, description
)

user_roles(
  user_id PK/FK -> users,
  role_id PK/FK -> roles
)

customers(
  user_id PK/FK -> users,
  person_type, document_type, document_number, billing_name,
  AK(document_type, document_number)
)

suppliers(
  user_id PK/FK -> users,
  document_type, document_number, business_name,
  AK(document_type, document_number)
)

categories(
  category_id PK, name AK, description?
)

tools(
  tool_id PK,
  supplier_id FK -> suppliers.user_id,
  category_id FK -> categories.category_id,
  name, description, current_daily_rate, rate_currency,
  publication_status
)

tool_images(
  tool_id PK/FK -> tools,
  image_url PK
)

equipment_units(
  equipment_unit_id PK,
  tool_id FK -> tools,
  inventory_code, serial_number?, current_condition,
  operational_status
)

reservations(
  reservation_id PK,
  customer_id FK -> customers.user_id,
  created_at, start_date, return_date, expires_at?,
  reservation_status, currency
)

reservation_items(
  reservation_id PK/FK -> reservations,
  equipment_unit_id PK/FK -> equipment_units,
  agreed_daily_rate
)

rentals(
  reservation_id PK/FK -> reservations,
  started_at, rental_status
)

unit_deliveries(
  reservation_id PK,
  equipment_unit_id PK,
  scheduled_at, delivered_at?, outbound_condition?,
  address_line, city, region, country,
  FK(reservation_id, equipment_unit_id) -> reservation_items,
  FK(reservation_id) -> rentals
)

unit_returns(
  reservation_id PK,
  equipment_unit_id PK,
  received_at, inbound_condition, notes?,
  FK(reservation_id, equipment_unit_id) -> unit_deliveries
)

damage_reports(
  damage_report_id PK,
  reservation_id, equipment_unit_id,
  reported_at, description, report_status, assessed_cost?,
  FK(reservation_id, equipment_unit_id) -> unit_returns
)

payments(
  payment_id PK,
  reservation_id FK -> reservations,
  processor, external_reference, amount,
  payment_status, recorded_at, confirmed_at?,
  AK(processor, external_reference)
)

invoices(
  invoice_id PK,
  reservation_id AK/FK -> rentals,
  number AK, issued_at, invoice_status,
  issued_supplier_name, issued_supplier_document,
  issued_customer_name, issued_customer_document
)

invoice_items(
  invoice_id PK/FK -> invoices,
  line_number PK,
  issued_description, quantity, issued_unit_price
)
```

## Auditoría de dependencias

| Relación | Claves candidatas / determinante | Resultado 4FN |
| --- | --- | --- |
| `users` | `user_id`; `email -> demás atributos` | Un hecho de cuenta; 4FN. |
| `roles` | `role_id`; `code -> demás atributos` | Un hecho de rol; 4FN. |
| `user_roles` | `(user_id, role_id)` | Relación binaria; dependencias multivaluadas triviales. |
| `customers` | `user_id`; `(document_type, document_number) -> demás atributos` | Un perfil de cliente; 4FN. |
| `suppliers` | `user_id`; `(document_type, document_number) -> demás atributos` | Un perfil de proveedor; 4FN. |
| `categories` | `category_id`; `name -> demás atributos` | Un hecho de categoría; 4FN. |
| `tools` | `tool_id -> demás atributos` | Imágenes y unidades separadas; 4FN. |
| `tool_images` | `(tool_id, image_url)` | Relación binaria; dependencias multivaluadas triviales. |
| `equipment_units` | `equipment_unit_id -> demás atributos` | Un hecho de unidad y estado; 4FN. |
| `reservations` | `reservation_id -> demás atributos` | Líneas y pagos separados; 4FN. |
| `reservation_items` | `(reservation_id, equipment_unit_id) -> agreed_daily_rate` | Tarifa propia del par; 4FN. |
| `rentals` | `reservation_id -> demás atributos` | Como máximo un alquiler por reserva; 4FN. |
| `unit_deliveries` | `(reservation_id, equipment_unit_id) -> demás atributos` | Un hecho de entrega; 4FN. |
| `unit_returns` | `(reservation_id, equipment_unit_id) -> demás atributos` | Reportes separados; 4FN. |
| `damage_reports` | `damage_report_id -> demás atributos` | Un hecho de reporte; 4FN. |
| `payments` | `payment_id`; `(processor, external_reference) -> demás atributos` | Un intento de pago; 4FN. |
| `invoices` | `invoice_id`; `reservation_id`; `number -> demás atributos` | Documento histórico inmutable; 4FN. |
| `invoice_items` | `(invoice_id, line_number) -> demás atributos` | Una línea emitida; 4FN. |


Las 18 relaciones cumplen 4FN bajo las dependencias documentadas. Las pruebas PostgreSQL validan restricciones y mapeos físicos; no sustituyen la revisión de supuestos de normalización.

## Reglas que requieren algo más que normalización

| Regla | Mecanismo actual / límite |
| --- | --- |
| Claves, referencias, importes y orden de fechas | Restricciones SQL y dominio |
| Inventario único por proveedor | Trigger y bloqueo asesor; no unicidad global falsa |
| Un proveedor por reserva | Coordinación y comprobación de referencias |
| Solapamientos confirmados | Bloqueos ordenados y comprobación/guardado atómicos |
| Roles, perfiles y propiedad | Aplicación; autenticación JWT pendiente |
| Devolución posterior a entrega | Transiciones y validación operativa |
| Tarifas y facturas históricas | Política de escritura y transacciones |
| Referencia única de pago | Restricción `(processor, external_reference)`; reintentos externos pendientes |

El código de inventario depende del proveedor a través de herramienta; no debe imponerse un `UNIQUE(inventory_code)` global. No se añaden totales duplicados ni conjuntos multivaluados en una misma columna.
