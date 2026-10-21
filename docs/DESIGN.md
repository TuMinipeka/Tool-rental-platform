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

`frontend/index.html` incluye en línea una pantalla de carga con SVG y CSS, sin imágenes ni video, para que se pinte antes que el bundle de React. Sobre fondo `#101318`, un martillo minimalista golpea tres veces un clavo terco; un rayo amarillo y cian lo parte en dos y las mitades se vuelven a unir con rebote.

- Primera visita de la sesión del navegador: permanece al menos un ciclo completo (2,6 s) y luego se desvanece en 320 ms.
- Visitas posteriores en la misma sesión: solo aparece si la carga tarda más de 450 ms y se retira al renderizar la aplicación.
- Movimiento reducido: se muestra estática y se retira en cuanto la aplicación renderiza.
- `src/lib/bootSplash.js` la libera; `tests/boot-splash.spec.js` verifica los tres comportamientos.

Las fotografías deben tener permiso de uso y evitar datos personales identificables. Conserva las [condiciones de imágenes](tool-image-requirements.md). Los [resultados previos de calidad](frontend-quality.md) identifican la versión medida y no describen automáticamente este MVP.
