# Diagrama conceptual entidad-relación

El diagrama representa la base de 18 entidades descrita en el [modelo conceptual](../conceptual-model.md). Las extensiones de protección, evidencias, disputas, reembolsos, documentos legales y consentimientos quedan fuera de esta primera versión.

## Archivos

- [Escena editable de Excalidraw](conceptual-er-model.excalidraw): figuras, atributos, relaciones y conectores agrupados.
- [Vista vectorial](conceptual-er-model.svg): permite ampliar sin perder calidad.
- [Vista PNG](conceptual-er-model.png): previsualización rápida.
- [Registro de verificación](conceptual-er-model.audit.json): conteos y huella de la fuente.

![Diagrama conceptual ER](conceptual-er-model.svg)

## Notación

Los rectángulos representan entidades y enumeran sus atributos. Los rombos amarillos representan relaciones. `[ID]` identifica claves, `[unique]` identifica claves alternativas y `?` marca atributos opcionales. Es una representación conceptual con listas de atributos, no un esquema SQL físico.

Las multiplicidades se muestran junto a cada extremo: `1`, `0..1`, `0..N` o `1..N`. `UserRole` y `ReservationItem` permanecen como entidades asociativas. `Customer` y `Supplier` son perfiles opcionales compatibles: un mismo `User` puede tener ambos.

El diagrama contiene 18 entidades, 19 relaciones y 38 conectores. También registra reglas que no se deducen únicamente de la cardinalidad: un proveedor por reserva, fechas inclusivas, independencia entre imágenes y unidades físicas, y congelación de tarifas y valores facturados.

## Edición

Abre Excalidraw, elige **Open / Load from file** y selecciona `conceptual-er-model.excalidraw`. Conserva ese archivo como fuente editable y regenera las vistas SVG y PNG después de cada cambio.
