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

- No impone un tiempo mínimo. El contenido aparece a los 300 ms para no parpadear en cargas rápidas.
- La cinta avanza rápido al principio y luego cada vez más lento durante 10 s: una carga lenta sigue desenrollando la cinta sin detenerse. La lectura es decorativa y no representa un porcentaje de progreso.
- Al renderizar la aplicación, `src/lib/bootSplash.js` recoge la cinta de golpe en 260 ms y desvanece la pantalla en 220 ms.
- Movimiento reducido: la cinta se muestra estática y la pantalla solo se desvanece.

Las fotografías deben tener permiso de uso y evitar datos personales identificables. Conserva las [condiciones de imágenes](tool-image-requirements.md). Los [resultados previos de calidad](frontend-quality.md) identifican la versión medida y no describen automáticamente este MVP.
