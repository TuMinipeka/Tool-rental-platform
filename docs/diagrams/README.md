# Diagrama conceptual entidad-relación

El diagrama representa las 25 entidades descritas en el [modelo conceptual](../conceptual-model.md): el modelo base y siete ampliaciones críticas de seguridad y operación.

## Archivos

- [Escena editable de Excalidraw](conceptual-er-model.excalidraw).
- [Vista vectorial](conceptual-er-model.svg).
- [Vista PNG](conceptual-er-model.png).
- [Auditoría](conceptual-er-model.audit.json) con conteos y huella de la fuente.

![Diagrama conceptual ER](conceptual-er-model.svg)

## Notación

Los rectángulos representan entidades. `[ID]` identifica la identidad conceptual, `[unique]` una clave alternativa y `?` un dato opcional. Las multiplicidades usan `1`, `0..1`, `0..N` y `1..N`.

La primera sección conserva el modelo base. El panel de ampliación conecta usuario con sesiones y recuperación, reserva con política, tarifa, terminación y extensión, y pagos con reembolsos. Es un modelo conceptual; tipos SQL, índices y restricciones físicas se documentan en las migraciones.

Para editar, abre el archivo `.excalidraw` y vuelve a exportar SVG y PNG. Actualiza también la huella SHA-256 del modelo y los conteos de la auditoría.
