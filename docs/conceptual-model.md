# Modelo conceptual de alquiler de herramientas

## Alcance y supuestos

Fuente: [enunciado original](../ejercicioDemo.md). Se identifican entidades, atributos, relaciones y multiplicidades. El [modelo lógico 4FN](fourth-normal-form.md) y la [arquitectura](backend-architecture.md) explican su traducción a tablas y código. PostgreSQL sustituye MySQL por decisión autorizada.

1. Un usuario puede tener varios roles. Cliente y proveedor son perfiles opcionales compatibles; administrador es un rol, no otra copia de usuario.
2. Una herramienta representa la oferta de un proveedor y tiene una categoría principal. Una unidad representa el equipo físico con código de inventario único dentro de su proveedor.
3. Las imágenes describen la herramienta; no dependen de una unidad física. Ambos conjuntos son independientes.
4. Una reserva incluye una o más unidades distintas de un solo proveedor. Selecciones de varios proveedores requieren reservas separadas.
5. Todas las líneas comparten fechas inclusivas y moneda. Del 14 al 15 se cobran dos días. La confirmación congela fechas, líneas y tarifas.
6. Una reserva confirmada genera como máximo un alquiler: la reserva es la solicitud; el alquiler representa su ejecución.
7. Cada unidad alquilada tiene como máximo una entrega y una devolución en ese alquiler. Una devolución puede tener varios reportes de daño.
8. Una reserva puede registrar varios intentos o pagos parciales. No se almacenan datos de tarjeta.
9. Un alquiler tiene como máximo una factura; sus datos emitidos son instantáneas históricas.
10. Nombres, direcciones y tarifas no constituyen identidades estables.

Modificar estos supuestos exige revisar dependencias funcionales y multivaluadas. El modelo base no incluye protección, evidencias ni consentimientos futuros.

## Entidades y atributos

`?` indica atributo opcional. Se conservan nombres conceptuales en inglés para corresponder con el código; PostgreSQL utiliza `snake_case`.

| Entidad | Identidad | Atributos principales |
| --- | --- | --- |
| User | userId; único email | displayName, email, passwordHash, accountStatus, registeredAt |
| Role | roleId; único code | code (`ADMIN`, `SUPPLIER`, `CUSTOMER`), description |
| UserRole | user + role | Asignación de rol sin atributos adicionales |
| Customer | user | personType, documentType, documentNumber, billingName |
| Supplier | user | documentType, documentNumber, businessName |
| Category | categoryId; único name | name, description? |
| Tool | toolId | supplier, category, name, description, currentDailyRate, rateCurrency, publicationStatus |
| ToolImage | tool + URL | imageUrl |
| EquipmentUnit | equipmentUnitId; supplier + inventoryCode | tool, inventoryCode, serialNumber?, currentCondition, operationalStatus |
| Reservation | reservationId | customer, createdAt, startDate, returnDate, expiresAt?, reservationStatus, currency |
| ReservationItem | reservation + equipmentUnit | agreedDailyRate |
| Rental | reservation | startedAt, rentalStatus |
| UnitDelivery | reservation + equipmentUnit | scheduledAt, deliveredAt?, outboundCondition?, addressLine, city, region, country |
| UnitReturn | reservation + equipmentUnit | receivedAt, inboundCondition, notes? |
| DamageReport | damageReportId | devolución afectada, reportedAt, description, reportStatus, assessedCost? |
| Payment | paymentId; processor + externalReference | reservation, processor, externalReference, amount, paymentStatus, recordedAt, confirmedAt? |
| Invoice | invoiceId; único reservation; único number | rental, number, issuedAt, invoiceStatus, nombres y documentos históricos de emisor/destinatario |
| InvoiceItem | invoice + lineNumber | issuedDescription, quantity, issuedUnitPrice |


Los importes usan decimales exactos y moneda explícita. No hay conversión implícita de moneda.

## Relaciones y multiplicidades

| Relación | Multiplicidad |
| --- | --- |
| User — Role | N:M mediante UserRole; cada asociación tiene un usuario y un rol |
| User — Customer / Supplier | Usuario 0..1 de cada perfil; cada perfil tiene exactamente un usuario |
| Supplier — Tool | Proveedor 0..N herramientas; herramienta exactamente un proveedor |
| Category — Tool | Categoría 0..N herramientas; herramienta exactamente una categoría |
| Tool — ToolImage / EquipmentUnit | Herramienta 0..N de cada conjunto; cada elemento pertenece a una herramienta |
| Customer — Reservation | Cliente 0..N reservas; reserva exactamente un cliente |
| Reservation — EquipmentUnit | N:M mediante ReservationItem; reserva 1..N líneas |
| Reservation — Rental | Reserva 0..1 alquiler; alquiler exactamente una reserva |
| ReservationItem — UnitDelivery | Línea 0..1 entrega; entrega exactamente una línea y un alquiler |
| UnitDelivery — UnitReturn | Entrega 0..1 devolución; devolución exige una entrega completada |
| UnitReturn — DamageReport | Devolución 0..N reportes; reporte exactamente una devolución |
| Reservation — Payment | Reserva 0..N pagos; pago exactamente una reserva |
| Rental — Invoice | Alquiler 0..1 factura; factura exactamente un alquiler |
| Invoice — InvoiceItem | Factura emitida 1..N líneas; línea exactamente una factura |

`UserRole` explicita la asociación N:M. No debe almacenarse una lista de roles en una columna ni duplicarse el usuario por rol. `ReservationItem` añade a la asociación una tarifa acordada propia del par reserva/unidad.

## Diagrama editable

[Excalidraw](diagrams/conceptual-er-model.excalidraw) · [SVG ampliable](diagrams/conceptual-er-model.svg) · [PNG](diagrams/conceptual-er-model.png) · [Guía de notación](diagrams/README.md)

```mermaid
erDiagram
    USER ||--o{ USER_ROLE : has
    ROLE ||--o{ USER_ROLE : assigned
    USER ||--o| CUSTOMER : profile
    USER ||--o| SUPPLIER : profile
    SUPPLIER ||--o{ TOOL : offers
    CATEGORY ||--o{ TOOL : classifies
    TOOL ||--o{ TOOL_IMAGE : shows
    TOOL ||--o{ EQUIPMENT_UNIT : owns
    CUSTOMER ||--o{ RESERVATION : requests
    RESERVATION ||--|{ RESERVATION_ITEM : contains
    EQUIPMENT_UNIT ||--o{ RESERVATION_ITEM : reserves
    RESERVATION ||--o| RENTAL : creates
    RENTAL ||--o{ UNIT_DELIVERY : executes
    RESERVATION_ITEM ||--o| UNIT_DELIVERY : schedules
    UNIT_DELIVERY ||--o| UNIT_RETURN : receives
    UNIT_RETURN ||--o{ DAMAGE_REPORT : records
    RESERVATION ||--o{ PAYMENT : receives
    RENTAL ||--o| INVOICE : invoices
    INVOICE ||--|{ INVOICE_ITEM : details
```


## Reglas adicionales a las cardinalidades

- La devolución nunca es anterior al inicio; los importes son no negativos y un pago es positivo.
- Una unidad no puede estar en dos reservas confirmadas con fechas coincidentes, incluidos los extremos.
- Las solicitudes pendientes no bloquean inventario. El proveedor confirma antes de gestionar el pago.
- La disponibilidad es derivada, no un booleano global. La implementación actual valida solapamientos confirmados y estado operativo; la caducidad automática y obligaciones de alquiler no devuelto requieren revisión adicional antes de producción.
- Las unidades de una reserva pertenecen al mismo proveedor. Cambiar titularidad durante obligaciones vigentes exige un modelo histórico explícito.
- Los datos del alquiler derivan de su reserva confirmada. Tarifas y documentos emitidos preservan historia.
- Los registros con historia se conservan o desactivan según su operación; no hay eliminación física indiscriminada.
- Ingresos cobrados e importes facturados son métricas diferentes; el reporte comercial sigue pendiente.
- Los totales derivados no se guardan como duplicados en el modelo inicial.

## Correspondencia con DDD y estado

El proyecto organiza una tabla por contexto por decisión expresa, pero las transacciones de reserva y factura siguen coordinadas. Sus líneas no pueden eludir invariantes mediante escritura independiente. Las 18 tablas tienen mapeo JPA y operaciones del backend; JWT, pasarela, informes y recorridos de frontend siguen pendientes. Consulta [arquitectura](backend-architecture.md), [README](../README.md#modelo-conceptual) y [cobertura](requirements-coverage.md).
