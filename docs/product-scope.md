# Alcance del producto

> Alcance vigente del MVP: pagos académicos en COP con Visa, Mastercard, PSE, Mercado Pago y PayPal configurados con datos fijos no editables. El correo y la recuperación de contraseña no forman parte del contrato público. Consulta [MVP](mvp.md) para aceptación actual.

## Propósito

Tool Rental Platform es una simulación universitaria realista de un mercado colombiano para alquilar herramientas y equipos de construcción. El producto objetivo debe soportar el ciclo completo de alquiler y mantener el ambiente académico claramente separado de una operación comercial real.

La fase 0 define el contrato del producto. Una capacidad descrita aquí no está implementada hasta que el estado del repositorio lo indique expresamente.

Estado académico actual: el backend implementa registro directo de clientes y proveedores, retención atómica, vencimiento, proveedor académico persistente, conciliación idempotente, confirmación automática, comprobante descargable y reportes por rol. Los proveedores financieros externos, los impuestos definitivos y la facturación electrónica quedan fuera del MVP.

## Actores confirmados

La primera versión completa tiene tres roles:

| Rol | Responsabilidades |
| --- | --- |
| Customer | Explorar el catálogo, iniciar órdenes, elegir protección opcional, completar pagos en sandbox, recibir y devolver herramientas, revisar valoraciones de daños y consultar comprobantes académicos. |
| Supplier | Publicar herramientas, administrar unidades físicas y disponibilidad, registrar condiciones de entrega y devolución, reportar daños y revisar pagos de alquiler. |
| Administrator | Administrar usuarios y roles, moderar el catálogo, configurar parámetros comerciales, revisar disputas y evidencias de daños, y monitorear la plataforma. |

Supplier realiza las actividades de entrega, devolución y gestión de daños en el alcance inicial. No existe un rol separado de logística.

## Alcance funcional confirmado

El software debe ofrecer finalmente todas las siguientes capacidades funcionales e integradas:

1. Identidad, autenticación y autorización para los tres roles.
2. Catálogo, categorías, imágenes, unidades físicas, publicación, ciudad de servicio, filtrado por municipio y disponibilidad.
3. Orden pendiente de pago, retención temporal atómica de inventario, expiración y prevención de reservas superpuestas.
4. Activación del alquiler, programación y evidencia de entrega, registro de devolución y comparación de condiciones.
5. Protección opcional, evidencias de daños, valoración, notificación al cliente, disputas y cargos adicionales justificados.
6. Procesamiento mediante una pasarela sandbox oficial, webhooks verificados y reembolsos idempotentes.
7. Facturación simulada con identificación de la herramienta y su ciudad de servicio, precios e impuestos discriminados.
8. Administración, reportes operativos, auditoría y aceptación de documentos legales.
9. Interfaz React adaptable que consuma el contrato REST/OpenAPI publicado.

El registro público de proveedor solicita identidad, documento y razón social, crea la cuenta en estado activo y abre una sesión con rol `SUPPLIER`; no requiere aprobación administrativa previa.

## Decisiones tecnológicas

| Área | Decisión |
| --- | --- |
| Lenguaje backend | Java 21 |
| Framework backend | Spring Boot 3.5.16 |
| Servidor | Apache Tomcat embebido provisto por Spring Boot |
| Arquitectura backend | Arquitectura hexagonal y diseño dirigido por el dominio |
| Base de datos | PostgreSQL |
| Persistencia | Spring Data JPA, Hibernate y Flyway |
| API | API REST versionada y documentada con OpenAPI y Swagger UI |
| Frontend | React; Node.js se usa solo para sus herramientas |
| Marcado y estilos | HTML y CSS dentro de la aplicación React |

Node.js no es un segundo backend. Java y Spring Boot siguen siendo responsables de lógica de negocio, persistencia, seguridad y API HTTP.

## Reglas de reserva y pago

- Customer selecciona equipo, fechas y logística; el backend vuelve a validar disponibilidad y crea una orden pendiente de pago con retención temporal atómica. La interfaz abre el paso `/pago` únicamente después de recibir el identificador persistido y la misma reserva queda consultable en `/cuenta`.
- La orden tiene un vencimiento explícito. El pago rechazado, cancelado o vencido libera la retención de inventario de manera idempotente.
- La decisión académica registrada por el backend convierte automáticamente la orden en reserva confirmada, sin aprobación manual posterior del Supplier.
- El checkout sandbox cobra el total aprobado y mostrado. Daños, extensiones u otros ajustes posteriores se discriminan como operaciones separadas.
- Extensiones, daños, pérdidas u otros ajustes autorizados son cargos separados y discriminados; nunca se ocultan en el saldo restante.
- El paso de pago permite escoger Visa, Mastercard, PSE, Mercado Pago o PayPal y siempre muestra el total persistido de la reserva. Los datos visibles son fijos, pertenecen al ambiente de prueba y no se pueden editar.
- La plataforma solo guarda el medio, la red, los últimos cuatro dígitos y el vencimiento cuando aplica. Nunca persiste el PAN completo, CVV o contraseña de una cuenta.
- Los estados distinguen como mínimo transacciones pendientes, aprobadas, rechazadas, canceladas y reembolsadas.
- Cada pago y reembolso debe ser idempotente y auditable.

## Precios y reglas tributarias colombianas

- Los valores monetarios se expresan en pesos colombianos (`COP`) salvo que se añada explícitamente otra moneda.
- La referencia general vigente de IVA es 19%. Es una tarifa legal, no un porcentaje comercial configurable.
- El cálculo no debe asumir que toda la tarifa diaria siempre es la base gravable. El artículo 1.3.1.7.1 del Decreto 1625 de 2016 establece una base especial para arrendamiento de bienes corporales muebles. La fórmula de producción debe validarse con un contador o abogado tributario colombiano antes de facturar realmente.
- Deben registrarse o congelarse la tarifa de IVA, método de base gravable, resultado de redondeo y versión de la regla legal usada, para que cambios posteriores no alteren históricos.
- Antes de aceptar, el checkout muestra alquiler, entrega, protección opcional, descuentos, base gravable, IVA, ajustes y total final.
- Los precios deben ser claros y no revelar cargos obligatorios únicamente después de confirmar.
- El proyecto actual solo emite documentos académicos con la leyenda `Documento académico - no válido como factura ante la DIAN` hasta contar con integración real y configuración del contribuyente.

## Protección opcional por daños

La funcionalidad se llama **Plan Opcional de Protección por Daños**. No debe comercializarse como seguro, póliza ni cobertura emitida por aseguradora. La actividad aseguradora en Colombia requiere una entidad o intermediario autorizado.

- La selección es opcional, está desactivada por defecto y exige una acción explícita del cliente.
- Precio, deducible, límites, eventos cubiertos, exclusiones y vigencia se muestran antes de aceptar.
- Precio y deducible son parámetros comerciales. La ley colombiana no establece un porcentaje universal para la protección de herramientas; el sistema no presenta un valor configurado como tarifa legal o general del mercado.
- Los parámetros se versionan. La reserva conserva términos y valores exactos aceptados.
- El plan no elimina el derecho a inspeccionar evidencias ni disputar una valoración.
- Fraude, daño intencional, uso prohibido, transferencia no autorizada, pérdida no reportada y otras exclusiones aprobadas solo quedan fuera si se informaron claramente antes de aceptar.

## Daños, pérdida y cargos adicionales

1. Entrega y devolución usan listas de condición con fecha y evidencia fotográfica por unidad.
2. Supplier crea un reporte que describe la diferencia entre condición de salida y entrada.
3. La valoración identifica si la unidad es reparable y adjunta cotización o evidencia del valor de reposición.
4. Sin protección, puede cobrarse el costo documentado de reparación, limitado por el valor documentado de reposición. La misma pérdida no se cobra dos veces.
5. Con protección, el cliente paga el deducible u otra participación aceptada para daños cubiertos, sujeto al límite y exclusiones.
6. La pérdida total o unidad irreparable usa la instantánea de valor acordada, no un valor sin soporte elegido después.
7. El cliente recibe evidencia y cálculo discriminado antes de un cargo adicional y tiene oportunidad documentada de disputarlo.
8. Administrator resuelve disputas escaladas. Cada decisión y ajuste es auditable.

## Protección del consumidor y comercio electrónico

La interfaz y la API deben permitir, y no contradecir, las siguientes protecciones:

- identificar proveedor y operador con datos de contacto;
- mostrar características esenciales, disponibilidad, precio completo, medios de pago, condiciones de entrega y vigencia antes del checkout;
- permitir revisar y corregir un resumen final antes de aceptar;
- conservar evidencia de aceptación de términos y versión del plan;
- ofrecer canales legalmente aplicables de retracto, cancelación, reembolso, reclamación y reversión;
- incluir enlace visible a la autoridad colombiana de protección al consumidor;
- obtener autorización previa, informada y consultable para datos personales, salvo excepción legal;
- minimizar datos personales y de pago y ofrecer canales para ejercer derechos del titular.

## Contrato de la página de Términos y Condiciones

La aplicación React debe implementar una ruta pública `/terms-and-conditions` con el texto aprobado en [Términos y Condiciones](terms-and-conditions.md).

La página debe:

- ser accesible sin autenticación;
- mostrar versión, publicación, operador, contacto y condición de simulación;
- enlazarse desde pie global, registro, checkout, confirmación y selección de protección;
- ofrecer enlace visible a la Superintendencia de Industria y Comercio;
- ser legible e imprimible en móvil y escritorio;
- nunca usar casilla de aceptación preseleccionada;
- registrar versión aceptada, usuario, fecha, finalidad y transacción cuando corresponda;
- separar la aceptación de términos del mercadeo opcional y la protección opcional.

La ruta pública e imprimible ya forma parte de la base React y se enlaza desde el pie, registro, solicitud, pago y confirmación. El registro persistente de la versión aceptada continúa pendiente y no debe simularse solo en el cliente.

## Referencias legales y límite de lanzamiento

El diseño se basa en fuentes oficiales:

- [Ley 1480 de 2011: Estatuto del Consumidor](https://www.funcionpublica.gov.co/eva/gestornormativo/norma.php?i=44306), especialmente venta a distancia, retracto, comercio electrónico y reversión.
- [Decreto 587 de 2016: procedimiento de reversión de pagos](https://www1.funcionpublica.gov.co/eva/gestornormativo/norma.php?i=69037).
- [Ley 1581 de 2012: protección de datos personales](https://sedeelectronica.sic.gov.co/transparencia/normativa/ley-estatutaria-1581-de-2012).
- [Ley 527 de 1999: comercio electrónico y mensajes de datos](https://www.funcionpublica.gov.co/eva/gestornormativo/norma.php?i=4276).
- [Decreto 1625 de 2016: base gravable del IVA en arrendamiento de bienes muebles](https://normograma.dian.gov.co/dian/compilacion/docs/pdf/decreto_1625_2016.pdf).
- [Superintendencia Financiera: aseguradoras e intermediarios autorizados](https://www.superfinanciera.gov.co/publicaciones/15491/industrias-supervisadasindustria-aseguradorainformacion-general-aseguradoras-e-intermediarios-de-seguros-15491/).

Estos documentos orientan una simulación académica y no sustituyen asesoría legal, tributaria, contable ni de seguridad. Un lanzamiento comercial real queda bloqueado hasta que profesionales colombianos calificados revisen identidad del operador, cancelaciones, parámetros de protección, tratamiento tributario, facturación, privacidad y texto legal final.

## Decisión de alcance del MVP — 20 de octubre de 2026

Se selecciona el proveedor académico persistente en COP con Visa, Mastercard, PSE, Mercado Pago y PayPal. La interfaz muestra datos fijos no editables y el backend conserva el medio, el resultado y los metadatos mínimos permitidos. El correo, la recuperación y el restablecimiento de contraseña quedan fuera del contrato vigente. Consulta [MVP](mvp.md) y [contrato de pagos](fictional-payments.md).
