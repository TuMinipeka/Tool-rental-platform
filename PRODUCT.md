# Product

<!-- impeccable:product-schema 1 -->

## Platform

web

## Users

El usuario principal es el cliente en Colombia que necesita una herramienta o equipo por un periodo corto sin comprarlo. Su trabajo principal es encontrar una opción adecuada, comprobar disponibilidad, entender el costo, reservarla y seguir su alquiler hasta la devolución.

El proveedor publica herramientas propias y administra exclusivamente su catálogo e inventario. Puede crear herramientas, retirar su publicación, eliminarlas cuando las reglas históricas lo permitan y marcar unidades sin existencias o fuera de servicio. El proveedor no administra la plataforma ni recursos de otros proveedores.

El administrador representa al propietario u operador de Tool Rental. Supervisa usuarios, roles, categorías, pagos simulados, facturas y la operación general de la plataforma.

## Product Purpose

Tool Rental permite encontrar, reservar y seguir herramientas locales con precios base, disponibilidad y estados claros. El producto conecta clientes con proveedores y conserva trazabilidad desde la solicitud hasta la entrega, devolución, daños, pagos y documentos.

El éxito significa que un cliente pueda completar el recorrido principal sin necesitar asistencia: inicio, catálogo, detalle, acceso, cotización, reserva y pago simulado.

## Positioning

La plataforma organiza el alquiler alrededor de unidades físicas y fechas concretas. Vuelve a comprobar disponibilidad al confirmar una reserva, conserva tarifas históricas y permite que cada participante siga el estado real de la operación.

## Operating Context

- La operación inicial está orientada a Colombia y usa pesos colombianos.
- El cliente explora, cotiza, reserva, paga de forma simulada y consulta reservas, alquileres y facturas.
- El proveedor publica herramientas, administra imágenes y unidades físicas, atiende solicitudes y registra entregas o devoluciones.
- El administrador supervisa identidad, catálogo, categorías y registros financieros académicos.
- Los alquileres tienen fechas inclusivas y relacionan unidades físicas específicas.

## Capabilities and Constraints

- El frontend utiliza React 19, JavaScript y Vite, separado del reactor Maven del backend.
- El backend expone REST con JWT Bearer, PostgreSQL, OpenAPI y Swagger UI.
- El pago vigente es una simulación académica dividida 50/50. Nunca solicita ni almacena número completo de tarjeta o CVV; usa únicamente método, red, vencimiento, últimos cuatro dígitos e idempotencia.
- La recuperación de contraseña devuelve actualmente un token académico; el envío por correo está pendiente.
- La renovación y revocación persistente de JWT están pendientes.
- Continúan abiertas las políticas de cancelación, retrasos, extensiones, entrega, protección, privacidad definitiva, impuestos y pasarela externa.
- Las acciones destructivas deben respetar el historial: retirar una publicación o una unidad es preferible a eliminar registros que ya participan en operaciones.

## Brand Commitments

- Nombre comercial confirmado: Tool Rental.
- La identidad debe emplear una armonía triádica con funciones semánticas claras, no color decorativo indiscriminado.
- La experiencia debe resultar profesional, directa y apropiada para herramientas, inventario y trabajo físico.
- Las interfaces operativas priorizan claridad, eficiencia, heurísticas de usabilidad, accesibilidad y estados comprensibles.
- La documentación humana permanece en español; los identificadores de código se mantienen en inglés.

## Evidence on Hand

- Alcance aprobado: `docs/product-scope.md`.
- Hoja de ruta: `docs/pendientes.md`.
- Contrato actual de API: `docs/rest-api.md` y `/v3/api-docs` durante la ejecución.
- Arquitectura: `docs/backend-architecture.md`.
- Borrador legal: `docs/terms-and-conditions.md`.
- Cinco referencias visuales se conservan en `plantillas/`.
- El frontend actual contiene una primera experiencia navegable con datos demostrativos. No existen todavía fotografías de producto aprobadas, testimonios, métricas comerciales verificadas ni identidad gráfica definitiva.

## Product Principles

1. Mostrar disponibilidad, precio y estado sin ambigüedad.
2. Permitir que cada rol actúe únicamente sobre los recursos que le corresponden.
3. Conservar la historia de reservas, alquileres, pagos y facturas.
4. Diferenciar claramente las simulaciones académicas de operaciones comerciales reales.
5. No inventar políticas, valores comerciales o afirmaciones legales pendientes.

## Accessibility & Inclusion

La experiencia debe funcionar con teclado, mantener foco visible, respetar movimiento reducido, conservar contraste suficiente y adaptarse desde pantallas móviles hasta escritorio. Los estados no pueden depender exclusivamente del color.
