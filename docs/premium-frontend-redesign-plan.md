# Plan de rediseño premium del frontend

## Objetivo

Transformar el frontend existente en una experiencia de comercio electrónico industrial, tecnológica y memorable, conservando íntegramente los contratos HTTP, la autenticación, las reservas, los pagos simulados y las reglas de negocio existentes.

La dirección visual combinará una estética industrial futurista con acabados de producto digital de alta gama. La identidad utilizará amarillo eléctrico `#FFD600`, cyan `#00D9FF`, blanco `#FFFFFF`, negro profundo `#101318` y gris suave `#F4F6F8`.

## Estado de partida

- React 19 y Vite 8 sin biblioteca de componentes ni animación.
- Aplicación concentrada principalmente en `frontend/src/App.jsx` y `frontend/src/styles.css`.
- Integración activa con `/api/v1`, access token en memoria, refresh mediante cookie y protección CSRF.
- Flujos existentes de catálogo, detalle, cotización, autenticación, creación de solicitudes, pago simulado, recuperación y términos.
- Composición Docker operativa para frontend, backend, PostgreSQL y Mailpit.
- La rama de trabajo es `developer/conde` y no se publicará ningún cambio sin autorización expresa.

## Límites obligatorios

- No modificar reglas de negocio, esquema de base de datos ni contratos de API.
- No sustituir datos reales por información ficticia ni presentar funciones inexistentes.
- No mover responsabilidades del backend al navegador.
- No almacenar credenciales o tokens persistentes en el frontend.
- Mantener accesibilidad, navegación por teclado y semántica HTML.
- Respetar `prefers-reduced-motion` y eliminar efectos dependientes del cursor en dispositivos táctiles.
- No instalar Framer Motion, GSAP u otra dependencia pesada salvo que exista una necesidad demostrable que CSS o WAAPI no puedan resolver.

## Estrategia de implementación

### 1. Fundamentos visuales y arquitectura de interfaz

- Convertir colores, superficies, sombras, radios, espaciado, tipografía, curvas y tiempos en tokens CSS.
- Separar responsabilidades visuales reutilizables cuando reduzca la complejidad del archivo principal, sin crear abstracciones vacías.
- Construir primitivas de interacción para botones, enlaces, tarjetas, chips, estados de carga, revelados y transiciones de ruta.
- Mantener las animaciones de interfaz frecuentes por debajo de 300 ms y reservar movimientos largos para momentos editoriales poco frecuentes.

### 2. Navegación y transición entre vistas

- Rediseñar la barra principal con estados activos claros, contraste estable y menú móvil accesible.
- Añadir una transición breve entre rutas que no bloquee la interacción ni produzca pantallas blancas.
- Incorporar feedback de pulsación, foco visible y estados de carga en acciones importantes.

### 3. Inicio cinematográfico

- Crear un hero asimétrico con tipografía expresiva, profundidad por capas y fotografía protagonista.
- Aplicar reveal de texto, spotlight y parallax sutil únicamente en escritorio con puntero fino.
- Incluir una llamada a la acción evidente y un indicador de exploración no intrusivo.
- Diseñar secciones editoriales con storytelling, composición alternada y revelados escalonados.

### 4. Catálogo y detalle de herramienta

- Evitar una cuadrícula uniforme mediante jerarquías de tarjeta, variaciones controladas de composición y mejores recortes de imagen.
- Incorporar filtros, chips, estados vacíos, skeletons y reorganización visual sin alterar la consulta real.
- Añadir tilt 3D y sombras reactivas de baja amplitud solo en dispositivos compatibles.
- Reforzar precio, ubicación, disponibilidad y acción principal en la página de detalle.

### 5. Flujos transaccionales

- Unificar cotización, logística, autenticación, solicitud, confirmación y pago simulado bajo el mismo sistema visual.
- Conservar la secuencia de negocio: solicitud, confirmación del proveedor y pago posterior.
- Diseñar errores, avisos, estados deshabilitados y confirmaciones con jerarquía clara y sin animaciones decorativas repetitivas.
- Mantener términos y condiciones legibles, imprimibles y separados de las composiciones promocionales.

### 6. Movimiento y rendimiento

- Animar principalmente `transform`, `opacity` y `clip-path` cuando sea apropiado.
- Usar transiciones CSS para estados interrumpibles, `IntersectionObserver` para revelados y WAAPI para respuestas dinámicas puntuales.
- Evitar `transition: all`, animaciones desde `scale(0)` y cambios de layout costosos.
- Aplicar stagger corto, entre 30 y 80 ms, sin impedir el uso inmediato de los controles.
- Limitar listeners de puntero a regiones específicas y actualizaciones mediante `requestAnimationFrame`.
- Cargar imágenes de forma diferida fuera del primer pantallazo y preservar dimensiones para evitar saltos de contenido.

### 7. Responsive y accesibilidad

- Validar escritorio, tableta y móvil con puntos de quiebre basados en el contenido.
- Sustituir hover, spotlight, tilt y parallax por estados táctiles simples en móvil.
- Mantener áreas táctiles suficientes, orden de foco lógico, contraste AA y etiquetas accesibles.
- Reducir movimiento conservando únicamente transiciones de opacidad y color cuando el sistema lo solicite.

## Orden de ejecución y puntos de continuidad

Cada fase debe dejar el proyecto compilable para que el trabajo pueda retomarse sin reconstruir el contexto:

1. Auditoría visual y mapa de componentes/rutas.
2. Tokens, utilidades de movimiento y estructura compartida.
3. Navegación, hero y página principal.
4. Catálogo y detalle.
5. Cotización, autenticación, reserva, pago, éxito y páginas informativas.
6. Responsive, accesibilidad y optimización.
7. Revisión crítica final, correcciones y validación automatizada.

## Criterios de aceptación

- Todas las rutas existentes continúan accesibles y conservan su comportamiento.
- Las llamadas reales a `/api/v1` mantienen payloads, seguridad y manejo de sesión.
- El primer pantallazo presenta una identidad claramente diferenciada y no parece una plantilla.
- Catálogo, detalle y flujos de reserva comparten un lenguaje visual coherente.
- Las interacciones de escritorio tienen equivalentes táctiles comprensibles.
- `prefers-reduced-motion` elimina desplazamientos, parallax y tilt.
- No existen errores de lint, build ni formato.
- La composición Docker abre frontend, backend y Mailpit correctamente.
- Se revisan estados vacío, carga, error, foco, hover, active y disabled.

## Validación prevista

```powershell
cd frontend
npm.cmd run lint
npm.cmd run build

cd ..
.\mvnw.cmd clean verify
docker compose --env-file .env.example up --build -d
docker compose --env-file .env.example ps
```

Además de las comprobaciones automatizadas, se realizará una revisión manual de las rutas principales en tamaños de escritorio, tableta y móvil, con movimiento normal y reducido.

## Estrategia de Git

- Registrar este plan con `docs: :memo: AddPremiumFrontendRedesignPlan`.
- Registrar la implementación terminada con un mensaje en inglés siguiendo `<type>: :<gitmoji>: <CamelCaseSummary>`.
- No ejecutar `git push` hasta recibir una orden expresa del propietario de la rama.
- No mezclar correcciones ajenas al rediseño, salvo defectos que impidan ejecutar o validar el frontend; cualquier excepción debe quedar documentada.

## Evaluación de capacidad

El rediseño es viable con el presupuesto restante si se ejecuta como una intervención enfocada sobre el frontend actual, se reutilizan las imágenes y datos disponibles y se prioriza CSS/WAAPI sobre dependencias nuevas. El alcance permite completar una versión integral y validada; la revisión artística iterativa posterior puede continuar en ajustes menores sin comprometer los flujos funcionales.

Los principales riesgos son el tamaño monolítico de `App.jsx`, la densidad actual de `styles.css` y el tiempo de revisión visual entre varios tamaños de pantalla. Los puntos de continuidad anteriores reducen el riesgo de perder contexto si la sesión se interrumpe.
