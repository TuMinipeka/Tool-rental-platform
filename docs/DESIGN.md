# Reglas visuales vigentes

El diseño de Conde es la referencia de interfaz. Esta guía consolida reglas aún válidas de los documentos de rediseño retirados; no es un encargo de rediseño nuevo.

- Conservar el logo y el símbolo aprobados, su geometría y proporciones.
- Mantener amarillo eléctrico, cyan, fondos industriales oscuros y superficies claras, con ajustes de contraste accesibles.
- Mantener tipografías Archivo y Public Sans, composición adaptable, imágenes y animaciones existentes.
- No sustituir componentes por capturas ni presentar favoritos, métricas, pagos o notificaciones inexistentes como funcionales.
- Preservar teclado, foco visible, semántica de enlaces, jerarquía de títulos y movimiento reducido.
- Usar las rutas unificadas de acceso y registro del diseño integrado.
- Toda optimización necesita evidencia comparable; una integración no acredita por sí sola mejoras de rendimiento.

## Pantalla de carga inicial

`frontend/index.html` incluye en línea una pantalla de carga con SVG y CSS, sin imágenes ni video, para que se pinte antes que el bundle de React. Sobre fondo `#101318`, una cinta métrica amarilla sale de su carcasa mientras la lectura en centímetros aumenta junto al texto «Tomando medidas».

- En cada carga completa del sitio aparece de inmediato y permanece al menos 4 s. Mientras tanto, React carga y renderiza la interfaz en segundo plano.
- La cinta llega cerca del 90 % de su recorrido a los 4 s y después avanza cada vez más lento hasta los 12 s: una carga lenta sigue desenrollando la cinta sin detenerse. La lectura es decorativa y no representa un porcentaje de progreso.
- Al cumplirse el mínimo y con la aplicación renderizada, `src/lib/bootSplash.js` recoge la cinta de golpe en 260 ms. Luego la pantalla se disuelve en 420 ms mientras la interfaz principal sube 12 px y aparece con un fundido de 520 ms (clase temporal `app-entering`).
- Movimiento reducido: la cinta se muestra estática, se respeta el mínimo de 4 s y la interfaz solo aparece con un fundido.
- `tests/boot-splash.spec.js` verifica el mínimo, el renderizado en segundo plano, la transición y el movimiento reducido. La suite general elimina la pantalla al cargar para que no tape las interacciones.

Las fotografías deben tener permiso de uso y evitar datos personales identificables. Conserva las [condiciones de imágenes](tool-image-requirements.md). Los [resultados previos de calidad](frontend-quality.md) identifican la versión medida y no describen automáticamente este MVP.
