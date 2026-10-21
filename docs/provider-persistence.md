# Gestión del proveedor y persistencia verificada

## Resultado y límites

La creación de herramientas ya respondía HTTP 201 en la reproducción inicial con servicios reales. Faltaban la gestión visual de imágenes, la republicación directa y una carga independiente del inventario: un fallo de reportes podía ocultar la administración. Se añadieron estas capacidades y se verificaron operaciones de los tres roles.

| Antes | Después | Motivo |
| --- | --- | --- |
| Fotos siempre vacías en el formulario | Archivos y URLs con vista previa y retirada | Completar las publicaciones sin depender de recursos precargados. |
| Retirar sin acción directa inversa | Publicar y retirar con respuesta persistida | Evitar estados aparentes mantenidos solo en React. |
| Reportes e inventario fallan juntos | Cargas independientes y reintento | Mantener disponibles las herramientas aunque falle un reporte. |
| Una carga tardía puede sobrescribir el perfil editado | Edición habilitada después de cargar los datos del servidor | Impedir que se guarde accidentalmente el valor anterior. |
| Creación administrativa en varias peticiones | Cuenta, rol y perfil en una transacción | Evitar cuentas incompletas si falla una restricción SQL. |

La arquitectura conserva Java 21, tres módulos, 25 contextos y 25 tablas. No se modificaron migraciones aplicadas ni se crearon tablas para los archivos.

## Uso del panel

1. Ingresa como proveedor y abre **Dashboard → Herramientas**.
2. Completa categoría, nombre, descripción, municipio, tarifa y visibilidad; pulsa **Crear herramienta**.
3. Se abre la sección de imágenes de la herramienta guardada. Puedes subir JPEG/PNG o añadir una URL HTTP(S).
4. Cada herramienta ofrece **Editar**, **Imágenes** y **Publicar/Retirar**. Retirar conserva sus datos e historial.
5. Usa **Unidad física** para registrar existencias. La lista inferior permite editar serial, condición y estado operativo. Una unidad retirada definitivamente no admite reactivación.

Publicar una herramienta no inventa existencias. La disponibilidad depende de sus unidades operativas y de las reservas; una publicación sin unidades no puede reservarse.

## Base estable y DBeaver

La aplicación habitual utiliza `tool_rental_demo_conde_mvp` en PostgreSQL Docker. La configuración privada queda en `.env`, ignorado por Git, con `POSTGRES_PORT=5433`, la base acordada y perfiles `local,demo`. El PostgreSQL local del puerto 5432 permanece separado.

En DBeaver utiliza host `localhost`, puerto `5433`, base `tool_rental_demo_conde_mvp` y el usuario/contraseña privados de `.env`. No conectes al puerto interno del contenedor desde Windows.

```powershell
docker compose up --build -d
Invoke-RestMethod http://localhost:8080/status/database
```

La web queda en http://localhost:5173. Se desactiva el reemplazo silencioso del catálogo por datos demo cuando falle la API.

Ejemplo de comprobación SQL, sin consultar contraseñas ni tokens:

```sql
SELECT tool_id, supplier_id, name, publication_status, service_city FROM tools ORDER BY name;
SELECT tool_id, image_url FROM tool_images;
SELECT equipment_unit_id, tool_id, inventory_code, operational_status FROM equipment_units;
```

Las pruebas mutantes se ejecutan en `mvp_supplier_20261020`, una base aislada del proyecto `tool-rental-mvp`, accesible mediante http://localhost:15173. Los registros QA de esa base no se copiaron a la base estable.

## Imágenes y almacenamiento

Usa fotografías propias o con permiso de uso, con la herramienta completa y sin datos personales identificables. Estas condiciones consolidadas sustituyen la guía anterior de entrega de fotografías del catálogo.

- Máximo 8 imágenes por herramienta. Archivos JPEG/PNG de hasta 5 MiB, máximo 4096 × 4096 píxeles.
- Validación por contenido real, reescritura sin metadatos, sin cambiar dimensiones. JPEG utiliza calidad máxima del codificador.
- Los enlaces externos admiten HTTP(S), sin credenciales en la URL. El backend no los descarga ni los copia.
- PostgreSQL guarda las asociaciones en `tool_images`; los bytes de archivos propios viven en el volumen `tool_images` (validación: `mvp_tool_images`).
- Las referencias propias son relativas y estables: `/api/v1/public/tool-images/files/{uuid}.png|jpg`. El frontend resuelve el origen de la API, tanto directo como detrás del proxy.
- Los archivos son fotografías públicas de catálogo; no deben contener documentos privados. La modificación exige sesión activa y propietario o administrador.
- Los nombres originales no se usan como rutas. Se rechazan formatos ajenos, rutas arbitrarias y enlaces con esquemas ejecutables.
- Si falla la transacción de subida, se elimina el archivo recién generado. Un recolector reintenta limpiar archivos no asociados y temporales con más de 24 horas; no elimina URLs externas.

## Contratos añadidos

| Método y ruta `/api/v1` | Entrada / resultado |
| --- | --- |
| `PATCH /tools/{id}/publication` | `{ "published": true|false }`; devuelve la herramienta guardada. |
| `GET /tools/{id}/images` | Imágenes del recurso propio, también accesibles al administrador. |
| `POST /tools/{id}/images` | Multipart con campo `file`; devuelve `toolId` e `imageUrl`. |
| `GET /public/tool-images/files/{filename}` | Bytes con tipo JPEG/PNG y `nosniff`. |
| `POST /suppliers/accounts` | Alta administrativa atómica de usuario, rol SUPPLIER y perfil. |
| `POST /users/administrators` | Alta administrativa atómica de usuario y rol ADMIN. |

Se conservan `POST /tool-images`, `DELETE /tool-images/entry`, edición de herramientas/unidades y alias legado `/api`. La asociación manual de una referencia propia está prohibida: debe originarse en la subida de esa herramienta.

Los errores mantienen `ProblemDetail`: 400 entrada inválida, 401 sin sesión, 403 sin permiso, 404 ausente, 409 conflicto, 413 archivo demasiado grande y 503 almacenamiento no disponible.

## Evidencias y reproducción

La validación automatizada incluye **112 pruebas Java** (27 Domain, 16 Application y 69 Infrastructure), PostgreSQL/Testcontainers y formato. El frontend pasa lint, build y **84 pruebas de navegador**, con dos omisiones previstas por dispositivo. [Resumen y huellas de los reportes](evidence/provider-persistence/validation.json).

El [resultado Playwright CLI](evidence/provider-persistence/site-persistence.json) registra **67 comprobaciones** con React, Spring Boot y PostgreSQL reales, sin interceptar la API. El snapshot SQL verifica el estado final de las operaciones; las respuestas HTTP y las lecturas posteriores comprueban las transiciones intermedias. No se presenta cada transición como una consulta SQL independiente.

| Rol | Recorridos comprobados | Tablas principales |
| --- | --- | --- |
| Proveedor | Crear, editar, retirar, republicar; subir/retirar fotos y URLs; registrar y actualizar unidades | `tools`, `tool_images`, `equipment_units` |
| Cliente | Registro, perfil, contraseña, reserva, cinco pagos ficticios, comprobante, cancelación y solicitud de extensión | `users`, `customers`, `user_roles`, `user_sessions`, `reservations`, `reservation_items`, `payments`, `invoices`, `invoice_items`, `reservation_terminations`, `reservation_extensions` |
| Proveedor operativo | Entrega, alquiler, devolución, cierre, aprobación REST de extensión y cinco consultas de reportes | `unit_deliveries`, `rentals`, `unit_returns`, `reservation_extensions` |
| Administrador | Crear/editar/desactivar proveedores y administradores, con sus roles | `users`, `suppliers`, `user_roles` |

La aprobación de extensión se comprobó mediante REST porque la interfaz vigente no ofrece ese control; no se presenta como recorrido visual. Los reportes son consultas, no escrituras. La prueba no añade funcionalidades ajenas a los formularios vigentes ni certifica escenarios comerciales no recorridos.

```powershell
node frontend/scripts/verify-site-persistence.mjs
node scripts/verify-persistence-backup.mjs
```

El segundo comando utiliza exclusivamente `.env.validation`. Restaura PostgreSQL en otra base, extrae los archivos en otro volumen y compara conteos y hashes. Después recrea la composición aislada conservando sus volúmenes y verifica JDBC, catálogo y archivos mediante HTTP. Consulta [el resultado del respaldo](evidence/provider-persistence/backup-restore.json).

Los archivos privados del respaldo quedan bajo `backups/`, ignorado por Git. En publicación, `scripts/backup.sh` toma conjuntamente base y fotos durante una breve parada del backend, reanuda el servicio y copia el paquete al destino externo antes de aplicar retención. La transferencia externa y Oracle siguen pendientes; la restauración validada aquí es local.

## Capturas

![Gestión de fotografías de una herramienta propia](evidence/provider-persistence/supplier-images.png)

![Comprobante académico PDF consultado por el cliente](evidence/provider-persistence/customer-invoice.png)

![Herramienta persistida visible en el catálogo móvil](evidence/provider-persistence/mobile-persisted-catalog.png)
