# Auditoría del rediseño integral del frontend

## Alcance revisado

La auditoría contrasta el frontend React, el contrato REST, la seguridad de sesión, los cinco archivos gráficos de referencia y el prompt definitivo. El logo, el símbolo de marca y el favicon quedan congelados: el rediseño puede cambiar su posición o tamaño adaptable, pero no su geometría, composición ni colores internos.

## Estado verificable de rutas

| Ruta | Estado actual | Integración real | Trabajo requerido |
| --- | --- | --- | --- |
| `/` | Parcial | Consulta el catálogo público; usa datos demostrativos si la API no responde | Ajustar composición a las dos referencias de inicio, ubicación y mensajes de confianza |
| `/catalogo` | Parcial | Búsqueda y categoría locales sobre el catálogo recibido | Sincronizar filtros con URL, incorporar municipio persistente y eliminar orden por reseñas inexistentes |
| `/herramienta/:id` | Parcial | Consulta disponibilidad antes de cotizar | Galería, estados de disponibilidad y logística; retirar verificaciones, reseñas y proveedor ficticios |
| `/cotizacion` | Funcional parcial | Solicita cotización y crea reserva autenticada | Conservar cálculo autoritativo del backend y mejorar estados de error/carga |
| `/pago` | Simulación académica | Registra el pago simulado existente | No presentarlo como pasarela real; el sandbox externo sigue bloqueado |
| `/reserva-confirmada` | Funcional parcial | Muestra el identificador devuelto cuando existe | Diferenciar solicitud creada, confirmación y pago sin afirmar resultados inexistentes |
| `/cuenta` | Demostrativa | Solo comprueba que exista una sesión | Sustituir identidad, métricas y actividad ficticias por datos reales o estados vacíos honestos |
| `/panel/proveedor` | Demostrativa y no protegida en cliente | Sin consultas específicas | Proteger por rol e integrar publicaciones, unidades y solicitudes propias |
| `/panel/admin` | Demostrativa y no protegida en cliente | Sin consultas específicas | Proteger por rol e integrar recursos administrativos autorizados |
| `/nosotros` | Informativa básica | Contenido estático | Construir la narrativa y cobertura metropolitana de la referencia sin métricas inventadas |
| `/como-funciona` | Informativa básica | Contenido estático | Crear recorrido editorial coherente con el flujo vigente de solicitud y confirmación |
| `/terms-and-conditions` | Implementada | Documento público e imprimible | Conservar versión académica y mejorar coherencia visual |
| `/informacion/privacidad` | Placeholder declarado | Sin persistencia de consentimientos | Mantener el bloqueo visible hasta aprobar política y tratamiento |
| `/informacion/faq` | Informativa básica | Sin servicio externo | Ajustar contenido al comportamiento real |
| `/informacion/contacto` | Placeholder declarado | Sin canal de soporte configurado | No publicar datos ni promesas hasta configurar el operador |
| `/403`, rutas desconocidas | Implementadas | Solo presentación | Mantener mensajes accesibles y coherentes con el sistema visual |

## Datos y contratos que se conservan

- La API oficial continúa bajo `/api/v1`.
- El access token permanece únicamente en memoria; el refresh usa cookie `HttpOnly`, rotación y CSRF.
- La disponibilidad se comprueba en servidor y la cotización del backend es autoritativa.
- Las fechas de reserva son inclusivas.
- El flujo aprobado vigente sigue siendo solicitud, confirmación del proveedor y pago académico 50/50.
- Los tres módulos Maven y la dirección `infrastructure -> application -> domain` no cambian por el rediseño.

## Hallazgos prioritarios

1. La interfaz fija Bogotá en el header, buscador, catálogo, normalización de API y formularios de entrega. Debe migrarse a Bucaramanga, Floridablanca, Girón y Piedecuesta para contenido nuevo y demostrativo, sin reescribir silenciosamente direcciones persistidas.
2. Mi cuenta y ambos paneles contienen nombres, empresas, métricas, operaciones y estados inventados. Deben convertirse en vistas reales o vacías antes de considerarse operativos.
3. La ficha de herramienta afirma proveedor verificado, reseñas, disponibilidad y beneficios sin que todos esos campos provengan del contrato público.
4. El registro actual crea únicamente una cuenta natural mínima; faltan rol cliente/proveedor, campos colombianos, consentimientos y soporte de servidor para el contrato ampliado.
5. No existen favoritos ni notificaciones persistentes. Sus accesos no deben presentarse como funcionales hasta implementar modelo, API y autorización.
6. El frontend está concentrado en `App.jsx`; la separación debe ser incremental para no romper sesión, reservas ni manejo de errores.

## Bloqueos de integración

- El pago directo solicitado no puede declararse implementado: no hay proveedor sandbox elegido, credenciales, contrato de webhook, política de expiración de retenciones ni cambio aprobado en `docs/product-scope.md`. Hasta resolverlo se conserva la simulación académica etiquetada.
- La política de privacidad, consentimientos versionados, impuestos definitivos y Plan Opcional de Protección por Daños siguen pendientes de decisión legal o comercial.
- Los paneles completos requieren endpoints orientados a cada actor y respuestas que identifiquen al usuario autenticado; el frontend no debe inferir autorización ni filtrar recursos ajenos como sustituto del servidor.
- Fotografías adicionales solo pueden incorporarse como material editorial o placeholders mientras no exista carga autorizada y almacenamiento seguro para publicaciones.

## Fases de implementación

1. Fundamentos compartidos, ubicación persistente y navegación accesible.
2. Inicio, catálogo y detalle con datos honestos y alta fidelidad visual.
3. Sobre nosotros, cómo funciona y cuenta con estados reales o vacíos.
4. Registro, paneles y rutas complementarias según las capacidades verificadas de la API.
5. Evolución de negocio y pago sandbox únicamente después de cerrar decisiones y credenciales.
6. Lint, compilación, pruebas Maven pertinentes y revisión responsive.

## Avance implementado

### Fases 2 y 3 — fundamentos y pantallas de referencia

- El municipio de servicio se limita a Bucaramanga, Floridablanca, Girón y Piedecuesta, se conserva en `localStorage` y se sincroniza con la URL del catálogo.
- El header incorpora selector territorial, estado activo, acceso y una ruta real para el proceso de proveedor.
- Inicio conserva el logo original y adopta el hero, buscador, cinco categorías, destacados etiquetados y cuatro pasos del flujo vigente.
- Catálogo sincroniza término, categoría y municipio con la URL; no ofrece orden por reseñas inexistentes.
- Detalle usa una composición de galería, información y reserva en tres áreas. Solo muestra reseñas, ubicación o verificación cuando el contrato las acredita; el placeholder declara que no es una fotografía del proveedor.
- Sobre nosotros incorpora una fotografía editorial genérica sin marcas, narrativa local, valores y los cuatro municipios sin estadísticas comerciales inventadas.
- Cómo funciona explica solicitud, confirmación del proveedor y pago académico 50/50 sin fingir el flujo sandbox futuro.
- Mi cuenta usa identidad de sesión y `GET /api/v1/reservations`; las secciones sin integración muestran un estado pendiente en vez de cifras o documentos falsos.
- Los paneles de proveedor y administración comprueban roles antes de renderizar y no presentan operaciones demostrativas como reales.
- El panel de proveedor consulta herramientas, categorías y unidades reales; permite crear herramientas sin fotografías falsas y registrar unidades físicas propias. El backend limita el listado de unidades al inventario del proveedor, mientras el administrador conserva la vista total.
- El panel administrativo calcula usuarios, unidades y categorías desde endpoints protegidos, sin métricas comerciales inventadas.
- Se eliminó el acceso demostrativo que fabricaba una sesión cuando fallaba la autenticación.
- El registro de una devolución antes de iniciar el alquiler responde como conflicto de estado (`409`) en lugar de ocultarlo como recurso inexistente (`404`), conforme al contrato de integración.

### Validación ejecutada

- `npm.cmd run lint`: correcto.
- `npm.cmd run build`: correcto.
- Vite sirvió inicio, catálogo, detalle, sobre nosotros, cómo funciona, cuenta, publicar, panel de proveedor, términos y ruta desconocida con respuesta HTTP 200 del fallback SPA.
- Revisión headless en Edge a 1600 × 1000 y 390 × 844; se corrigieron el ancho del buscador y el desbordamiento del hero móvil detectados en la primera pasada.
- `mvnw.cmd clean verify`: correcto; 74 pruebas superadas (`25 domain`, `16 application`, `33 infrastructure`), incluidas arquitectura, migraciones Flyway, persistencia, concurrencia y PostgreSQL real con Testcontainers.
