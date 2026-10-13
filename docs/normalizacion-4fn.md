# Normalización hasta cuarta forma normal

## Base del análisis

Se parte del [modelo conceptual](modelo-conceptual.md), especialmente sus supuestos. La normalización evalúa relaciones y dependencias, no pantallas ni diagramas por sí solos. `X → Y` es dependencia funcional; `X ↠ Y`, dependencia multivaluada (DMV). Una superclave determina toda una fila; una clave candidata es una superclave mínima.

Se aplicó la skill [database-normalization](../.agents/skills/database-normalization/SKILL.md), sus referencias de 1FN, 2FN, 3FN, BCNF y 4FN y su ejemplo MySQL de 4FN. La condición formal de 2FN se evalúa sobre **todas las claves candidatas**, no únicamente sobre la PK elegida. Agregar un ID artificial no elimina dependencias existentes. En BCNF y 4FN se exige que el determinante sea una **superclave**, no necesariamente una clave mínima.

## Relación inicial no normalizada (ilustrativa)

El enunciado no proporciona una tabla original. Esta estructura didáctica representa lo que sucedería al guardar una reserva y todo su contexto en una sola fila:

```text
FichaReserva(idReserva, fechas, estado, datosCliente,
  herramientas[{idHerramienta, nombre, categoria, nombreProveedor,
    tarifaActual, imagenes[URL], unidades[codigoInventario]}],
  unidadesReservadas[{idUnidad, tarifaAcordada}],
  pagos[{referencia, importe, estado}], factura{cabecera, lineas[...]})
```

Anomalías: no se puede registrar un equipo sin una reserva; corregir el proveedor requiere modificar múltiples fichas; eliminar la última reserva puede eliminar la única descripción del equipo. Además, imágenes y unidades multiplican combinaciones aunque no haya una relación entre ellas.

## 1FN: valores atómicos y eliminación de grupos repetidos

Cada fila identifica un hecho y cada atributo contiene un valor de su dominio. Se extraen líneas de reserva, roles, imágenes, pagos y líneas de factura. Fechas se expresan como inicio y fin; una dirección se divide según los componentes necesarios para su uso. No se usan `imagen1`, `imagen2`, listas de IDs ni roles separados por comas.

Una relación plana provisional sería:

```text
ReservaUnidad(idReserva, idUnidad, inicio, fin, idCliente, nombreCliente,
             idHerramienta, nombreHerramienta, tarifaAcordada)
Clave candidata: (idReserva, idUnidad)
```

Es atómica, pero repite la cabecera y el catálogo; todavía no cumple 2FN. Los conjuntos de pagos y de unidades se extraen por separado: no se genera una fila por cada combinación pago × unidad.

## 2FN: eliminar dependencias parciales

En la relación provisional:

```text
idReserva → inicio, fin, idCliente, nombreCliente
idUnidad → idHerramienta, nombreHerramienta
(idReserva, idUnidad) → tarifaAcordada
```

Los dos primeros determinantes son partes de la clave candidata. Se separan Reserva y UnidadEquipo, y queda `DetalleReserva(idReserva, idUnidad, tarifaDiariaAcordada)`. El precio acordado depende de la combinación y expresa un hecho histórico distinto de la tarifa actual del catálogo.

La proyección y reunión son sin pérdida: la intersección con Reserva es `idReserva`, clave de Reserva; con UnidadEquipo es `idUnidad`, clave de UnidadEquipo. Esas dependencias se comprueban localmente en las relaciones resultantes. Así, cambiar el período no requiere corregir todas las líneas y se registra inventario sin reservas.

## 3FN: eliminar dependencias transitivas

Las cabeceras provisionales aún presentan:

```text
idReserva → idCliente → nombreFacturacion, documentoCliente
idUnidad → idHerramienta → nombreHerramienta, idProveedor, idCategoria
idHerramienta → idProveedor → nombreComercial
idHerramienta → idCategoria → nombreCategoria
```

Se extraen Cliente, Herramienta, Proveedor y Categoría. Reserva conserva la referencia a Cliente; UnidadEquipo conserva la referencia a Herramienta; Herramienta conserva referencias a Proveedor y Categoría. Ninguna reserva almacena nombres vivos del catálogo o del cliente.

En cada extracción la intersección es la clave de la entidad extraída, por lo que su reunión es sin pérdida. Las dependencias listadas quedan preservadas en tablas individuales o por su composición. Las instantáneas de Factura son atributos del documento emitido: no dependen del nombre **actual** del usuario o proveedor y no se sincronizan con ellos.

## BCNF: todos los determinantes funcionales son superclaves

BCNF exige que para toda DF no trivial `X → Y`, X sea superclave. Se revisan también correo, códigos, documentos y referencias externas, no solo IDs.

Un caso a evitar es:

```text
DetalleAmpliado(idReserva, idUnidad, idHerramienta, tarifaAcordada)
idUnidad → idHerramienta
```

`idUnidad` no identifica una línea entre varias reservas. La herramienta debe consultarse desde UnidadEquipo. En el modelo final no se conserva esa columna redundante. Las claves alternativas válidas están enumeradas en el esquema; no se inventa unicidad para nombres de herramientas ni números de serie.

## 4FN: separar conjuntos multivaluados independientes

4FN exige que en toda DMV no trivial `X ↠ Y`, X sea superclave. Es no trivial si `Y` no está contenido en `X` y `X ∪ Y` no abarca todos los atributos de la relación. Toda DF implica una DMV, por lo que 4FN también implica BCNF.

Considérese la relación **hipotética**, sin otros atributos:

```text
CatalogoExpandido(idHerramienta, urlImagen, idUnidad)
Superclave: (idHerramienta, urlImagen, idUnidad)
idHerramienta ↠ urlImagen
idHerramienta ↠ idUnidad
```

En el negocio modelado también existe la DF `idUnidad → idHerramienta`, porque cada unidad pertenece a una sola herramienta. Por tanto, esta relación expandida incumple BCNF además de 4FN; no se presenta como un caso que ya cumple BCNF. El problema multivaluado sigue existiendo: imágenes y unidades son conjuntos independientes del catálogo y `idHerramienta` no es superclave de la relación expandida. La clave candidata mínima es `(urlImagen,idUnidad)` bajo estas reglas, pues idUnidad determina idHerramienta; la terna mostrada es una superclave no mínima.

Ejemplo de combinaciones que una estructura expandida obliga a mantener:

| Herramienta | Imagen | Unidad |
| --- | --- | --- |
| H1 | fotoA | U1 |
| H1 | fotoA | U2 |
| H1 | fotoB | U1 |
| H1 | fotoB | U2 |
| H1 | fotoC | U1 |
| H1 | fotoC | U2 |

Son seis filas para tres imágenes y dos unidades. Agregar U3 exigiría otras tres filas; eliminar combinaciones podría perder una imagen sin intención. Se descompone en:

```text
ImagenHerramienta(idHerramienta, urlImagen)
UnidadEquipo(idUnidad, idHerramienta, codigoInventario, ...)
```

La proyección `(idHerramienta, idUnidad)` está representada por UnidadEquipo. Bajo la independencia declarada, la reunión por `idHerramienta` reconstruye exactamente las combinaciones originales: la descomposición es sin pérdida por la DMV. Cada imagen y cada unidad se inserta por separado, incluso si el otro conjunto está vacío. La relación binaria ImagenHerramienta solo tiene DMV triviales; los atributos descriptivos de UnidadEquipo dependen de su clave.

También se mantienen separados UsuarioRol y los perfiles, y DetalleReserva y Pago: un pago no se asigna a una unidad concreta bajo estos supuestos. **Una relación N:M no es por sí sola una violación de 4FN.** No se descompone `(reserva, unidad, tarifaAcordada)` en conjuntos independientes: la tarifa pertenece a esa pareja. Tampoco se separan las asociaciones reales de daños y devoluciones.

## Esquema lógico resultante

PK = clave primaria; AK = clave candidata alternativa; FK = referencia. Los atributos no listados como opcionales en el conceptual se consideran obligatorios. Las claves alternativas requieren unicidad y ausencia de nulos. Las flechas entre tablas representan referencias; las DF relevantes de cada tabla se explican después.

```text
Usuario(idUsuario PK, correo AK, nombreVisible, hashClave, estadoCuenta, fechaRegistro)
Rol(idRol PK, codigo AK, descripcion)
UsuarioRol(idUsuario PK/FK→Usuario, idRol PK/FK→Rol)
Cliente(idUsuario PK/FK→Usuario, tipoPersona, tipoDocumento, numeroDocumento,
        nombreFacturacion; AK(tipoDocumento, numeroDocumento))
Proveedor(idUsuario PK/FK→Usuario, tipoDocumento, numeroDocumento, nombreComercial;
          AK(tipoDocumento, numeroDocumento))
Categoria(idCategoria PK, nombre AK, descripcion?)
Herramienta(idHerramienta PK, idProveedor FK→Proveedor, idCategoria FK→Categoria,
            nombre, descripcion, tarifaDiariaActual, monedaTarifa, estadoPublicacion)
ImagenHerramienta(idHerramienta PK/FK→Herramienta, urlImagen PK)
UnidadEquipo(idUnidad PK, idHerramienta FK→Herramienta, codigoInventario,
             numeroSerie?, condicionActual, estadoOperativo)
Reserva(idReserva PK, idCliente FK→Cliente, creadaEn, inicio, fin, venceEn?,
        estadoReserva, moneda)
DetalleReserva(idReserva PK/FK→Reserva, idUnidad PK/FK→UnidadEquipo,
               tarifaDiariaAcordada)
Alquiler(idReserva PK/FK→Reserva, iniciadoEn, estadoAlquiler)
EntregaUnidad(idReserva PK, idUnidad PK, programadaEn, realizadaEn?,
              condicionSalida?, lineaDireccion, ciudad, region, pais;
              FK(idReserva,idUnidad)→DetalleReserva; FK(idReserva)→Alquiler)
DevolucionUnidad(idReserva PK, idUnidad PK, recibidaEn, condicionEntrada,
                 observaciones?; FK(idReserva,idUnidad)→EntregaUnidad)
ReporteDano(idReporte PK, idReserva, idUnidad, registradoEn, descripcion,
            estadoReporte, costoEvaluado?;
            FK(idReserva,idUnidad)→DevolucionUnidad)
Pago(idPago PK, idReserva FK→Reserva, procesador, referenciaExterna,
     importe, estadoPago, registradoEn, confirmadoEn?;
     AK(procesador,referenciaExterna))
Factura(idFactura PK, idReserva AK/FK→Alquiler, numero AK, emitidaEn,
        estadoFactura, nombreEmisorEmitido, documentoEmisorEmitido,
        nombreReceptorEmitido, documentoReceptorEmitido)
DetalleFactura(idFactura PK/FK→Factura, numeroLinea PK, descripcionEmitida,
               cantidad, precioUnitarioEmitido)
```

La unicidad `(proveedor, codigoInventario)` de UnidadEquipo es una regla **entre tablas**, porque proveedor se deriva de Herramienta. No es una AK local de UnidadEquipo ni se declara un `UNIQUE` ficticio sin columna. Debe implementarse mediante validación transaccional serializada por proveedor o rediseñarse al crear el DDL. No se añade idProveedor a UnidadEquipo, pues introduciría `idHerramienta → idProveedor` con determinante que no es superclave.

## Auditoría de dependencias por relación

En cada fila, `clave → resto` resume todas las DF de los atributos descriptivos bajo los supuestos declarados. Las referencias a otras tablas no determinan atributos adicionales almacenados localmente, salvo cuando ellas mismas son claves alternativas.

| Relación | Claves candidatas / determinantes no triviales | Revisión de DMV y resultado |
| --- | --- | --- |
| Usuario | idUsuario; correo → resto | Un solo hecho de cuenta; sin conjuntos independientes internos. 4FN. |
| Rol | idRol; codigo → resto | Un hecho por rol. 4FN. |
| UsuarioRol | (idUsuario,idRol); sin DF no trivial | Relación binaria; DMV triviales. 4FN. |
| Cliente | idUsuario; (tipoDocumento,numeroDocumento) → resto | Un perfil por cuenta, un documento bajo el supuesto. 4FN. |
| Proveedor | idUsuario; (tipoDocumento,numeroDocumento) → resto | Un perfil por cuenta. 4FN. |
| Categoria | idCategoria; nombre → resto | Un hecho por categoría. 4FN. |
| Herramienta | idHerramienta → resto | Una categoría principal, una tarifa actual; imágenes y unidades externas. 4FN. |
| ImagenHerramienta | (idHerramienta,urlImagen); sin DF no trivial | Relación binaria; DMV triviales. 4FN. |
| UnidadEquipo | idUnidad → resto | Una pertenencia y un estado actual por unidad. 4FN; unicidad de inventario entre tablas pendiente de implementación. |
| Reserva | idReserva → resto | Un intervalo y cliente; pagos y líneas externos. 4FN. |
| DetalleReserva | (idReserva,idUnidad) → tarifaDiariaAcordada | La tarifa depende de la pareja; no hay DMV independiente adicional. 4FN. |
| Alquiler | idReserva → resto | Un alquiler por reserva. 4FN. |
| EntregaUnidad | (idReserva,idUnidad) → resto | Dirección y condición pertenecen a la entrega concreta. 4FN. |
| DevolucionUnidad | (idReserva,idUnidad) → resto | Un hecho de recepción; daños externos. 4FN. |
| ReporteDano | idReporte → resto | Cada reporte es un hecho completo de una devolución. 4FN. |
| Pago | idPago; (procesador,referenciaExterna) → resto | Un intento/operación por referencia; sin unidades de pago independientes. 4FN. |
| Factura | idFactura; idReserva; numero → resto | Un documento por alquiler; líneas externas. Las instantáneas no dependen de un documento de persona vivo. 4FN. |
| DetalleFactura | (idFactura,numeroLinea) → resto | Descripción, cantidad y precio forman un concepto facturado concreto. 4FN. |

Resultado: 18 relaciones en 4FN **respecto de las DF/DMV y supuestos identificados**. No es una certificación sobre reglas desconocidas ni una validación física en MySQL. Una PK artificial, un diagrama o una prueba SQL no demuestran por sí solos 4FN; la evidencia es el análisis de dependencias.

## Restricciones que la normalización no resuelve

| Restricción | Validación posterior propuesta |
| --- | --- |
| PK, AK, referencias, importes positivos, inicio anterior a fin | Restricciones declarativas del DDL cuando sean expresables localmente. |
| Mismo proveedor en todas las líneas; código de inventario único por proveedor | Consulta y escritura en una transacción con bloqueo del proveedor, o rediseño justificado. |
| No solapamiento y no entregar una unidad que sigue fuera | Bloquear la unidad, verificar reservas/alquileres y confirmar en la misma transacción; comprobar carreras concurrentes. |
| Perfil coherente con rol; mínimos de líneas al enviar/emitir | Servicios de dominio y transacciones; PK/FK no garantizan estos mínimos. |
| Crear alquiler solo desde reserva confirmada; devolución tras entrega realizada | Reglas de estado y validaciones transaccionales. |
| Precio histórico, moneda y factura inmutables | Política de escritura del dominio; restricciones/permisos de persistencia según implementación. |
| Transiciones e idempotencia de confirmación de pagos | Reglas del dominio y clave única del procesador/referencia; definir reintentos del adaptador. |

## Comprobaciones didácticas

1. Insertar una herramienta sin reservas no debe requerir un cliente: Herramienta y Reserva son independientes.
2. Cambiar el nombre de una categoría modifica una fila; no altera líneas ni facturas emitidas.
3. Agregar una imagen no crea filas de unidades; agregar una unidad no duplica imágenes.
4. Dos reservas de la misma unidad pueden acordar tarifas diferentes: la tarifa depende de la pareja.
5. Dos pagos parciales no duplican líneas de reserva ni se asignan arbitrariamente a equipos.
6. Si una foto identifica una unidad específica, el supuesto de independencia deja de aplicar: se requiere otra relación, no producir todas las combinaciones.

Estas comprobaciones son una revisión del diseño; no se han ejecutado contra una base de datos. El siguiente paso físico deberá especificar tipos, restricciones, índices, migraciones y casos de concurrencia.
