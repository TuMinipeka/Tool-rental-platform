# PROMPT DEFINITIVO — TOOLRENTAL
## Rediseño visual fiel a las referencias + funcionalidad integral

Actúa como **arquitecto de software full stack senior, diseñador UI/UX senior, director de arte digital y especialista en QA**. Trabaja directamente sobre el proyecto ToolRental que te adjunto. **Debes implementar y comprobar cambios reales en el repositorio; no limitarte a explicar, diseñar imágenes ni escribir una propuesta.**

Te adjunto el ZIP `Tool-rental-platform(1).zip` y cinco imágenes que son referencias visuales obligatorias:

1. `Landingpage1.png`: inicio y primer hero.
2. `Destacados.png`: versión del inicio con categorías y herramientas destacadas.
3. `DetalleHerramienta.png`: ficha de producto y alquiler.
4. `Sobrenosotros.png`: página institucional.
5. `MiCuenta.png`: área personal del cliente.

**No son las únicas pantallas del producto.** Debes aplicar el mismo sistema visual y el mismo nivel de calidad a **todas las rutas, estados, subpantallas, formularios, diálogos, dashboards y flujos**, aunque no tengan captura de referencia. Las imágenes guían el diseño; el código y los datos reales guían el comportamiento. No conviertas las capturas en fondos de pantalla ni en imágenes de una web fingida: construye los componentes verdaderos con React.

## 0. REGLA ABSOLUTA: NO TOCAR EL LOGO NI EL ICONO EXISTENTES

**Me gusta el logo/icono que ya tiene mi proyecto. Está aprobado y queda bloqueado.**

- NO crear, rediseñar, regenerar, sustituir, reinterpretar ni simplificar el logotipo, el símbolo, el isotipo, el favicon o el nombre gráfico ToolRental.
- NO modificar la geometría, colores internos, proporciones, letras ni composición del logotipo existente.
- NO crear un logo metálico 3D; **elimino expresamente esa instrucción del prompt anterior**.
- Conserva los recursos existentes y el componente `Brand` / `.brand-symbol` / favicon según estén implementados. Se permite **ubicar o dimensionar el conjunto en el header** si el diseño responsive lo exige, sin alterar su identidad ni código gráfico.
- Los iconos de interfaz, las ilustraciones de categorías y las imágenes de herramientas **sí** pueden diseñarse o mejorarse: la prohibición se refiere al **logo de la marca**.
- Si las capturas muestran una versión distinta del logo, **prevalece siempre el logo que ya está en el repositorio**.

## 1. INSPECCIÓN OBLIGATORIA DEL REPOSITORIO

Antes de escribir código, verifica la estructura y los contratos existentes. En la versión adjunta se identifican:

- Frontend: **React 19 + Vite 8 + JavaScript**, con `frontend/src/App.jsx`, `api.js`, `styles.css`, `premium.css`, `TermsAndConditionsPage.jsx` y el recurso `frontend/public/assets/hero-rotary-hammer-1200.png`.
- Backend: **Java 21 + Spring Boot**, organizado en módulos `domain`, `application`, `infrastructure`, con PostgreSQL, JPA y Flyway.
- API de referencia: `/api/v1`, documentación en `docs/rest-api.md` y, al ejecutar el servidor, `/v3/api-docs`.
- Sesión existente: access token en memoria, refresh token mediante cookie HttpOnly y protección CSRF. **No debilites este diseño.**
- La interfaz actual contiene datos demostrativos, referencias a Bogotá, pantallas de dashboard todavía ilustrativas y acciones incompletas. Debes distinguir funcionalidad real de presentación simulada.
- El flujo actual documentado de reservas exige confirmación del proveedor y pagos académicos divididos 50/50. **La experiencia objetivo solicitada más adelante representa un cambio funcional real, no un ajuste CSS.**

Revisa además `PRODUCT.md`, `DESIGN.md`, `docs/product-scope.md`, `docs/pendientes.md`, `docs/requirements-coverage.md`, migraciones, controladores, modelos, reglas de reserva y pruebas existentes. **No presupongas que una capacidad del documento está implementada.** El código vigente y los tests establecen qué funciona ahora; este prompt establece qué debe cambiar.

Entrega primero una auditoría **breve y concreta**, con el mapa de pantallas/rutas y los bloqueos de integración. Luego **continúa implementando**, sin detenerte tras la auditoría ni solicitar aprobación por cada cambio ordinario. Si encuentras una contradicción técnica importante que exige una decisión de negocio no especificada aquí, explica el bloqueo y no inventes reglas ni credenciales.

## 2. IDENTIDAD VISUAL: REPRODUCIR EL LENGUAJE DE LAS CINCO IMÁGENES

El objetivo es una fidelidad visual **muy alta**, no una interpretación genérica de “sitio moderno”. Reproduce proporciones, densidad, jerarquía, distribución de elementos, recortes fotográficos, contrastes, espacios, acabado de las tarjetas, botones, fondos y detalles decorativos. No necesitas copiar literalmente datos ficticios o marcas de agua.

**Paleta obligatoria:** amarillo eléctrico `#FFD600`, cyan `#00D9FF`, carbón `#101318`, blanco `#FFFFFF` y gris claro `#F4F6F8`. Utiliza negro azulado muy oscuro en las zonas cinematográficas, blanco/gris muy claro en las zonas comerciales, amarillo para acciones primarias y cyan para detalles técnicos e interacción. Mantén contraste accesible: no uses cyan o amarillo para texto pequeño sobre blanco sin verificar legibilidad.

**Lenguaje formal:** tipografía de titulares pesada y condensada visualmente, jerarquía potente, encabezados multilineales grandes, palabras con contorno blanco cuando corresponda, etiquetas superiores en mayúsculas con tracking y línea cyan, cuadrículas de planos técnicos, líneas finas, cruces de mira, contornos geométricos, capas con profundidad, brillos cyan contenidos, tarjetas blancas con bordes finos y sombras discretas, detalles industriales amarillos y composición editorial de alta gama.

Reutiliza preferentemente las familias actuales **Archivo Variable** (títulos) y **Public Sans Variable** (interfaz), ajustando pesos, espaciado y tamaños antes de instalar fuentes nuevas. Define tokens para colores, tipografía, radios (aprox. 10–16 px según componente), sombras, espacios, bordes, movimiento y breakpoints. Evita estilos duplicados y reglas CSS que se contradicen entre `styles.css` y `premium.css`.

**No debe parecer** un dashboard administrativo genérico, una plantilla Bootstrap, una web de tarjetas interminables ni una copia aproximada hecha solo con cambios de color.

## 3. HEADER Y NAVEGACIÓN GLOBAL

Reproduce el header horizontal oscuro y compacto de las referencias, con el **logo original intacto** a la izquierda, enlaces bien espaciados, ubicación y acciones a la derecha.

- Navegación pública: Catálogo, Cómo funciona, Sobre nosotros; agregar Inicio mediante el logo y demás accesos reales cuando corresponda.
- Ubicación inicial: **Bucaramanga, Santander**. Selector funcional limitado a Bucaramanga, Floridablanca, Girón y Piedecuesta. Persistir la selección durante la navegación; consultar o filtrar según las capacidades reales de la API.
- Visitante: Ingresar y botón amarillo **Publica tu herramienta** que abra el proceso válido de proveedor/registro.
- Autenticado: menú personal y accesos según rol, favoritos/notificaciones cuando existan, salir; evitar saturar el header.
- Indicador claro de sección activa, hover suave, foco visible, menús accesibles y header sticky sin cubrir contenido.
- En móvil: menú desplegable real, orden de tabulación correcto y cierre al navegar.
- Ningún enlace puede apuntar a una pantalla inexistente como si estuviera operativa.

## 4. INICIO — `Landingpage1.png` Y `Destacados.png`

Las dos imágenes son **complementarias**, no alternativas excluyentes: construye una sola página de inicio coherente que contenga todas sus mejores secciones.

**Hero:** fondo carbón/azul noche a todo el ancho, sección de gran impacto con distribución asimétrica. A la izquierda, titular enorme en blanco con una parte de contorno como **“Haz más. Compra menos.”** o el texto comercial de la referencia; subtítulo breve, CTA amarillo **Explorar equipos**, enlace secundario Cómo funciona y beneficios de confianza. A la derecha, la fotografía/imagen protagonista de una herramienta grande y bien recortada, con iluminación cyan/amarilla, rejillas técnicas y gráficos de calibración detrás, sin ensuciar su silueta. Priorizar el recurso de rotomartillo ya existente si ofrece calidad suficiente. No forzar imágenes de otra marca como publicación real.

**Buscador:** banda blanca horizontal que se superpone elegantemente al límite inferior del hero; campo para herramienta, selector de municipio y botón Buscar amarillo. Sombra/profundidad con pequeño acento cyan; inputs y opciones totalmente interactivos. Buscar debe llevar al catálogo con filtros realmente aplicados.

**Categorías:** bloque de fondo claro, encabezado editorial alineado a la izquierda y **cinco tarjetas horizontales** como en las referencias: Demolición, Concreto, Corte, Limpieza y Energía. Cada una debe tener una **ilustración coherente de una herramienta**, número discreto, título, microdescripción y flecha cyan. Las ilustraciones de categorías NO son el logo de marca y sí pueden crearse. Si existen categorías adicionales en la base de datos, no borrarlas: simplemente destacar estas cinco en la portada y permitir explorar las demás en el catálogo.

**Equipos destacados:** bloque claro de productos, encabezado con enlace “Ver todos los equipos”, herramientas en tarjetas comerciales de fotografías grandes, nombre, especificaciones breves, precio COP/día, ubicación, disponibilidad y acción de favorito. Adaptar la cantidad de tarjetas al viewport. Incorporar paginación/carrusel solo si realmente funciona. **Jamás inventar reseñas, proveedores, estrellas, disponibilidad, precios o equipos para aparentar abundancia.** Un catálogo vacío debe verse profesional.

**Cómo funciona:** franja oscura con cuatro pasos bien compuestos: Busca, Reserva, Recibe y Haz más. Con iconografía, conectores y microanimación. Adaptar el texto a la lógica real cuando se complete el flujo.

Extiende la página con secciones útiles, no relleno: confianza, proveedores, cobertura local y CTA para publicar. Las secciones deben tener contenido auténtico o claramente identificado como demostrativo.

## 5. DETALLE DE HERRAMIENTA — `DetalleHerramienta.png`

Esta referencia es obligatoria para la ficha del producto. Utiliza un **layout de tres áreas en escritorio**: miniaturas verticales a la izquierda, fotografía principal grande al centro-izquierda y datos/especificaciones en el centro; a la derecha, tarjeta de reserva con borde/acento amarillo superior y sombra suave. Usa un layout responsive adecuado en tablet y móvil.

- Breadcrumbs navegables y correctos.
- Galería: fotografías verdaderas del proveedor, miniaturas seleccionables, imagen principal amplia, zoom/modal accesible y, si hay vídeos reales, tratamiento claro de vídeo. No simular vídeos con un botón de play vacío.
- Datos: categoría, nombre contundente en varias líneas, descripción, especificaciones, accesorios, ubicación, proveedor, verificación **solo si consta realmente**, y reseñas **solo si existen datos verificables**.
- Reserva: precio diario COP, estado real, selectores visuales de inicio y devolución, días facturables, subtotal, entrega cuando aplique, total estimado/final y CTA amarillo.
- Fechas: calendario de alta calidad, días deshabilitados según inventario, validaciones, accesibilidad y actualizaciones reales del total.
- Debajo de la sección principal: dos tarjetas seleccionables para **recogida en punto del proveedor** y **entrega a domicilio** (esta última solo si el proveedor la ofrece); beneficios operativos con iconos cyan; franja negra con especificaciones técnicas de la herramienta.
- El diseño de la captura es guía gráfica, **NO** fuente de reglas matemáticas. Por ejemplo, si las fechas 20–23 de octubre incluyen ambos días y el backend factura días inclusivos, corresponden **4 días**, aunque una imagen de ejemplo muestre 3. La UI debe seguir la regla comercial validada y probar los casos límite.
- Si falla la disponibilidad, impedir continuar y explicar claramente el motivo.

## 6. SOBRE NOSOTROS — `Sobrenosotros.png`

Recrea el hero dividido: texto a la izquierda sobre fondo oscuro y fotografía editorial de obra/trabajador a la derecha, con atmósfera cálida de atardecer, encuadre cinematográfico y trazos técnicos cyan. Titular del estilo **“Somos la plataforma que conecta herramientas con proyectos reales.”** con alternancia de texto lleno, contorno y cyan. CTA al catálogo y CTA para proveedor.

Después, construir una página auténtica y completa: esencia, misión, historia local, valores de confianza/colaboración/calidad/impacto local y cobertura de **Bucaramanga, Floridablanca, Girón y Piedecuesta** con tarjetas/fotografías apropiadas. Puedes usar fotografías editoriales con derechos de uso adecuados; jamás presentarlas como equipo real de un proveedor sin que lo sean.

La franja de cifras de la imagen es **solo referencia visual**: **no escribir +5.000 usuarios, +1.200 herramientas, +300 proveedores ni +8.000 proyectos si esos números no se obtienen realmente del sistema**. Usar métricas reales o sustituir la franja por beneficios comprobables.

## 7. MI CUENTA — `MiCuenta.png`

Transforma `/cuenta` en la experiencia personal clara y atractiva de la captura:

- Hero oscuro superior con tipografía grande, imagen de herramienta y mensaje **“Tu perfil, tus alquileres, todo en un solo lugar.”**
- Identidad del cliente autenticado mediante datos reales (nombre, correo y avatar solo si existe). **No usar nombres, correos ni estados de cuenta verificada inventados**.
- Tarjeta de perfil a la izquierda y cuadrícula de módulos reales: Mis reservas, Alquileres activos, Favoritos, Comprobantes/Facturas según naturaleza real, Notificaciones y Perfil y seguridad.
- Secciones internas completas: reservas con filtros por estado; detalle de alquiler; historial; estado de pagos; datos personales editables; cambio de contraseña; direcciones; favoritos y notificaciones.
- Cada tarjeta y botón debe navegar a una vista o abrir una interacción real, con estados vacío/carga/error/éxito; no debe quedar como una maqueta.
- CTA inferior de exploración con el mismo sistema visual de la imagen.

## 8. OTRAS PANTALLAS QUE DEBEN ALCANZAR LA MISMA CALIDAD

Las cinco referencias no limitan el alcance. Revisar y mejorar **todas las rutas existentes y los flujos necesarios**:

**Catálogo completo:** filtros por categoría, término, municipio, precio y disponibilidad cuando existan capacidades reales; orden, estado de búsqueda, resultados reales, paginación si aplica, vistas vacías y tarjetas consistentes. Los filtros de URL deben sincronizarse con la interfaz al navegar o compartir el enlace.

**Cómo funciona:** página pública propia con explicación paso a paso y componentes editoriales coherentes; vincular realmente cada CTA. **Publicar herramienta / Hazte proveedor:** introducción profesional y acceso correcto a registro y panel según autenticación.

**Login:** el diseño actual me gusta; **conservar su estructura y personalidad** salvo ajustes de consistencia, accesibilidad y validaciones. **Registro:** rediseño integral, con selección explícita Cliente / Proveedor, persona natural / empresa donde corresponda. Administrador **nunca** debe registrarse públicamente. Campo por campo: nombres, apellidos, tipo y número de documento, celular colombiano, correo, nacimiento (mayores de 18 años cuando legalmente corresponda), contraseña, confirmación, consentimientos; para empresas, razón social, NIT/dígito de verificación y responsable según lo requerido. **No enviar campos inexistentes a la API sin adaptar el backend**. Validar en cliente y servidor, aceptar tildes/ñ/apóstrofes/guiones adecuados, celular nacional de 10 dígitos que empieza por 3, documentos según tipo y contraseña mínima de **8 caracteres** según la decisión de producto solicitada, modificando la validación del backend si actualmente exige otra longitud. No debilitar hash, políticas de contraseñas comprometidas ni seguridad. No predeterminar ni permitir `ADMIN` desde el navegador.

**Proveedor:** dashboard independiente y restringido: resumen real, publicar herramienta, editar solo propias, múltiples fotografías, imagen principal, inventario físico, precios, disponibilidad, solicitudes o alquileres, ingresos, entregas, devoluciones, daños cuando existan, notificaciones y perfil. El botón amarillo “Publica tu herramienta” debe recorrer un camino funcional hasta esta experiencia. No permitir modificar equipos ajenos.

**Administrador:** dashboard independiente, con sidebar profesional, datos reales, usuarios, proveedores, herramientas, categorías, reservas/alquileres, pagos, comprobantes, reportes, seguridad y configuración según alcance. El administrador debe existir por aprovisionamiento seguro, no por formulario abierto; proteger rutas **y API**. No mostrar cifras inventadas.

**Otras:** cotización, checkout, resultados de pago, detalle de operación, restablecimiento de contraseña, ajustes, favoritos, notificaciones, estados 403/404/500, contacto, FAQ, privacidad y Términos y condiciones. Reutilizar patrones visuales de la marca sin dar a formularios administrativos el tamaño exagerado de los héroes comerciales. Mantener impresión legible de los documentos legales.

## 9. RESERVA, DISPONIBILIDAD Y PAGOS — CAMBIO FUNCIONAL EXPLÍCITO

**Objetivo solicitado:** experiencia de alquiler directa. Una vez seleccionado el equipo y las fechas, se verifica el inventario, se informa el total, se elige entrega/recogida, se crea una orden pendiente, se reserva temporalmente la disponibilidad, el cliente completa un pago **en sandbox oficial** y, tras una confirmación auténtica del proveedor de pagos recibida por backend, el alquiler se confirma automáticamente **sin aprobación manual posterior del proveedor**.

**ATENCIÓN:** el sistema adjunto documenta otro flujo: solicitud → aprobación del proveedor → pago simulado 50/50. Esta sección requiere revisar y cambiar casos de uso, estados, reglas del dominio, persistencia y endpoints. **No simplemente cambies etiquetas de botones ni simules que el pago fue aprobado.** Conserva historial y compatibilidad de las operaciones existentes mediante cambios y migraciones cuidadosamente delimitados.

Diseñar e implementar, en orden:

1. Consulta verificable de inventario por unidad y fechas; validación obligatoria en servidor.
2. Regla de días facturables y precio calculados y comprobados desde backend; precio del navegador jamás es autoritativo.
3. Modalidades disponibles del proveedor y tarifa de entrega informada antes de aceptar.
4. Órdenes pendientes de pago con vencimiento definido y retención temporal atómica de inventario para evitar dobles alquileres.
5. Checkout real de pruebas, retorno al sitio y **webhooks oficiales verificados**, idempotencia y manejo de reintentos, cancelación, error, expiración y liberación de retenciones.
6. Conversión segura a alquiler confirmado solo cuando el backend reciba y valide una confirmación real del pago en sandbox.
7. Avisos reales a cliente/proveedor y pantallas de seguimiento con estados persistidos.
8. Pruebas de concurrencia, operaciones duplicadas, pago rechazado y retorno interrumpido.

**Tarjeta y PayPal:** usar exclusivamente SDK y campos alojados/tokenizados del proveedor seleccionado. Nunca recoger ni guardar PAN completo, CVV ni contraseña PayPal en nuestros propios formularios; nunca incluirlos en logs. Si aún no hay credenciales, preparar adaptadores/configuración, documentar variables necesarias y marcar la integración como **PENDIENTE DE CONFIGURACIÓN/PRUEBA**, sin fingir cobros exitosos. Distinguir de manera honesta entorno académico/sandbox de pago real.

**No imponer depósito monetario obligatorio en esta versión.** No eliminar sin análisis históricos, reglas documentadas u otros registros de seguridad relacionados. Diferenciar depósitos de los posibles servicios opcionales y de cobros por daño. No inventar seguros, impuestos, garantías ni coberturas. Los comprobantes PDF internos deben señalar su naturaleza académica si no existe facturación DIAN real, y nunca declarar pagada una orden no confirmada.

## 10. UBICACIÓN, FOTOS Y CONTENIDO CONFIABLE

La zona operativa inicial será exclusivamente **Bucaramanga, Floridablanca, Girón y Piedecuesta, Santander**. Eliminar Bogotá de textos, selects y demostraciones operativas; revisar específicamente los datos de ejemplo en `App.jsx`, `Header`, `SearchBar`, `CatalogPage` y `QuotePage`, así como cualquier dato/backend pertinente. No cambies domicilios reales registrados silenciosamente: corrige datos de ejemplo y configura la validación territorial de nuevas operaciones.

Las fotografías de herramientas publicadas deben provenir de proveedores autorizados, con carga, validación de tipo/tamaño, vistas previas, orden, borrado autorizado, imagen principal y almacenamiento seguro. La foto del hero es material editorial y puede ser distinta a una publicación; no reutilizarla como si fuera la foto real de cada herramienta. Crear placeholders de calidad cuando no haya fotografías.

No presentar como hechos comprobados reseñas, calificaciones, estadísticas, verificación de proveedores, certificados, direcciones, promesas de “pago seguro”, disponibilidad, testimonios o descuentos sin datos y funcionalidades que lo sustenten. Durante desarrollo puede existir un modo demostración **etiquetado**, pero no debe mezclarse silenciosamente con información de producción.

## 11. MOVIMIENTO, RESPONSIVE Y ACCESIBILIDAD

Conservar y mejorar las animaciones que ya existen. Usar movimientos de entrada cinematográficos controlados, revelados tipográficos, parallax moderado, brillo cyan discreto, profundidad de herramienta y hover/tilt sutil en tarjetas. Priorizar **CSS, transform/opacity y Web Animations API**; valorar librerías solo si justifican una necesidad real. No instalar paquetes por moda.

- Apuntar a fluidez aproximada de 60 FPS sin bloquear interacciones.
- Respetar `prefers-reduced-motion`; no aplicar efectos de cursor/tilt en dispositivos táctiles.
- No animar altura/posición de layout innecesariamente ni generar saltos por imágenes.
- Mantener accesibilidad con teclado, foco visible, etiquetas, lectura de errores y áreas táctiles suficientes.
- Comprobar 360, 390, 768, 1024, 1366 y 1600 px, además de estados reales de contenido largo o vacío.
- En móvil reorganizar el hero, buscador, categorías, galería, tarjeta de reserva y paneles; no reducir toda la captura de escritorio a escala microscópica.
- No debe existir scroll horizontal accidental ni botones escondidos o cortados.

## 12. REGLAS DE IMPLEMENTACIÓN Y SEGURIDAD

- **Modificar el proyecto existente**, no crear un proyecto paralelo, landing independiente ni nuevo frontend desconectado del backend.
- No tocar el logo ni el favicon. No borrar características correctas para simplificar el trabajo.
- Mantener los módulos de backend `domain`, `application` e `infrastructure`; no reemplazar la arquitectura por mocks, Node.js ni un backend distinto.
- Conservar `/api/v1` y sus contratos cuando sea posible; cualquier evolución necesaria debe implementarse en ambos lados, documentarse y probarse.
- Cuidar JWT, cookie HttpOnly, CSRF, permisos por rol, autorización por propietario, validaciones de servidor e idempotencia.
- No agregar usuarios, herramientas, órdenes, cifras ni comprobantes falsos a las tablas operativas para “llenar” las capturas.
- Separar componentes del monolito `App.jsx` cuando aporte claridad y menor riesgo; no hacer una reescritura global injustificada.
- Todas las acciones deben tener comportamiento real. Si un servicio externo aún no está conectado, debe informarse con claridad y no crear una interacción falsa.
- Proteger datos personales y secretos. Utilizar `.env.example` sin credenciales, mantener las variables reales fuera del repositorio.
- No ejecutar `git push`, despliegues externos, pagos reales, borrados de datos ni acciones irreversibles sin autorización.

## 13. SECUENCIA OBLIGATORIA DE TRABAJO

**Fase 1 — Auditoría verificable.** Identificar rutas, componentes, API, inventario, registros, roles, imágenes, estado de pagos, pendientes y riesgos. Documentar una lista de rutas existentes + rutas necesarias, con estado real/demostrativo/inexistente. Comprobar los cinco archivos gráficos. Hacer respaldo/commit de punto de partida sin publicar remotamente cuando corresponda.

**Fase 2 — Fundamentos visuales.** Congelar identidad del logo; implementar tokens, contenedores, tipografía, botones, navegación, inputs, mensajes, iconos de interfaz y responsive reutilizables. Evitar romper pantallas mientras cambia CSS.

**Fase 3 — Fidelidad visual.** Realizar y comparar capturas reales de Inicio, Destacados, Detalle, Sobre nosotros y Mi cuenta con las cinco imágenes. Corregir iterativamente composición, espaciados, tipografía, fotos, tamaños, fondos y jerarquía. No conformarse con que “se parece”.

**Fase 4 — Extensión a TODAS las pantallas.** Catálogo, cómo funciona, registro, proveedor, administrador, formularios, favoritos, notificaciones, checkout, estados, términos y los demás apartados detectados en el repositorio. Navegación completa y operaciones reales.

**Fase 5 — Lógica pendiente.** Registro y roles, CRUD del proveedor, protección de dashboards, disponibilidad, reservas, pagos sandbox, correcciones de negocio, entrega, historial, comprobantes y notificaciones. Hacer las migraciones y tests necesarios sin alterar datos históricos indebidamente.

**Fase 6 — Calidad.** Ejecutar `npm run lint` y `npm run build` en frontend; ejecutar los tests Maven pertinentes y, cuando el entorno lo permita, pruebas de integración PostgreSQL/Testcontainers y levantamiento Docker. Comprobar al menos un flujo por rol, controles de acceso, formularios, disponibilidad, precio por día, cancelación/pago fallido, ausencia de doble reserva, PDF y responsive. Revisar consola, errores HTTP, red, enlaces y capturas. No afirmar pruebas que no ejecutaste.

Priorizar que las primeras fases dejen **una versión ejecutable, visualmente fiel y sin regresiones**, sin sacrificar funciones importantes por efectos decorativos. Si el entorno impide terminar un módulo por credenciales o servicios externos, dejar un punto de integración real, documentar exactamente lo pendiente y continuar con las mejoras independientes.

## 14. CRITERIOS DE ACEPTACIÓN

El trabajo solo se considera completo cuando:

- [ ] El **logo/icono/favicon original está intacto**.
- [ ] Las cinco referencias están reproducidas con alta fidelidad en composición y diseño, con variaciones razonables por resolución y contenido real.
- [ ] Inicio reúne hero, buscador, cinco categorías, destacados y cómo funciona.
- [ ] Detalle tiene galería, información, fechas, disponibilidad, precios y entrega funcionales.
- [ ] Sobre nosotros tiene narrativa visual, misión, valores y cobertura local sin cifras inventadas.
- [ ] Mi cuenta muestra datos del usuario auténtico y cada módulo funciona.
- [ ] Todas las demás ventanas, modales y dashboards comparten el sistema visual y no parecen de otro proyecto.
- [ ] No hay referencias operativas erróneas a Bogotá; los cuatro municipios autorizados funcionan.
- [ ] Cliente, proveedor y administrador tienen permisos separados validados desde backend.
- [ ] El proveedor puede administrar sus propias herramientas e imágenes; el administrador gestiona recursos autorizados.
- [ ] El registro y las validaciones colombianas funcionan en frontend/backend.
- [ ] La reserva y el inventario no permiten solapamientos incompatibles.
- [ ] El flujo de pago solo confirma operaciones verificadas; distingue demo/sandbox de dinero real.
- [ ] No existen botones decorativos que prometan funciones inexistentes.
- [ ] No se muestran estadísticas, reseñas ni estados falsos.
- [ ] Los documentos internos no se presentan indebidamente como facturas DIAN.
- [ ] El sitio es usable en escritorio, tableta, móvil y con movimiento reducido.
- [ ] Lint, build y tests pertinentes están ejecutados y reportados honestamente.
- [ ] No se ha roto una función existente ni alterado el logo.

## 15. FORMATO DE ENTREGA OBLIGATORIO

Cuando termines cada fase, informa de forma compacta:

1. Funcionalidades **realmente implementadas**.
2. Pantallas y rutas terminadas, y cuáles siguen pendientes.
3. Archivos principales modificados y migraciones creadas.
4. Pruebas **ejecutadas**, con resultados y fallos.
5. Bloqueos reales (credenciales sandbox, infraestructura, decisiones legales/comerciales).
6. Capturas comparativas de las cinco pantallas de referencia **si el entorno permite tomar screenshots de la aplicación ejecutándose**.

**NO QUIERO:** un nuevo logo, un frontend de plantilla, una maqueta estática, botones de mentira, datos inventados, un backend sustituido por mocks ni un informe optimista que declare terminado lo que no funciona.

**SÍ QUIERO:** conservar la identidad aprobada, lograr que ToolRental se vea tan cuidado como las cinco imágenes, extender ese lenguaje a todas las ventanas y completar progresivamente un marketplace real y seguro. **Diseño fiel + funcionalidades comprobables + respeto al proyecto existente.**
