# Rendimiento y accesibilidad del frontend

## Alcance y evidencia

Los resultados comparativos de este documento corresponden a la optimización guardada en `e8115ca`, anterior a la integración del rediseño de `developer/conde` (`0d5d720`). No describen el rendimiento del rediseño integrado. Sus nuevas rutas de acceso, paginación, estilos e imágenes requieren una nueva medición antes de afirmar mejoras sobre esa versión.

Auditoría realizada el 20 de octubre de 2026 sobre el build de producción. Los valores individuales, medianas, rangos, versiones y condiciones están en [los resultados verificables](quality/frontend-audit-results.json). Los informes Lighthouse HTML/JSON, capturas y trazas de pruebas se conservan localmente en `frontend/test-results/` y `frontend/playwright-report/`; no forman parte del bundle ni del contexto Docker.

Se comparan tres navegaciones con caché fría para cada escenario: inicio móvil, inicio de escritorio y catálogo móvil. Ambos estados utilizan Nginx, el mismo equipo, Chrome y configuración Lighthouse. El servidor de auditoría ocupa `localhost:4173` y reenvía `/api/` al backend local de `8080`. La API devolvió un catálogo vacío durante estas mediciones: se midió la muestra demostrativa que la aplicación ya ofrece en ese estado.

Las pruebas de navegador utilizan un catálogo determinista de 30 herramientas e interceptan la API. Así comprueban contratos y recorridos sin crear usuarios, reservas ni cobros en la base real. Esta verificación no sustituye pruebas integradas de autenticación, SMTP o pasarela.

No hay datos CrUX ni RUM de usuarios reales para localhost. LCP/CLS/TBT se presentan como laboratorio; TBT no equivale a INP. Las puntuaciones automatizadas de accesibilidad no acreditan conformidad WCAG por sí solas.

## Cambios aplicados

| Antes | Después | Motivo y conservación visual |
| --- | --- | --- |
| PNG de tamaño completo en todos los dispositivos | WebP sin pérdida y variantes de 480, 800, 960, 1280 píxeles y tamaño original, según corresponda | Selección por `srcset`/`sizes`; se conservan los 20 PNG como respaldo |
| Dos imágenes destacadas se descargaban antes de entrar en pantalla | Las imágenes de tarjetas usan carga diferida; la imagen principal mantiene prioridad alta | Se mantienen fotografías, tarjetas, carrusel y contenido al desplazarse |
| Pantallas legales, acceso, cuenta, paneles y checkout dentro de la carga inicial | Chunks por funcionalidad con `React.lazy` y `Suspense` | Mismas pantallas, estados y contratos; recuperación visible ante un fallo de descarga |
| Un mapa de SVG se reconstruía en cada icono/render | Geometría SVG estática compartida | No cambia el dibujo ni su tamaño |
| Cada evento de puntero leía y escribía estilos inmediatamente | Actualización agrupada mediante `requestAnimationFrame`, con cancelación al salir o desmontar | Mismos límites de paralaje y giro; se mantiene movimiento reducido |
| Recursos de texto sin gzip ni caché de larga duración | Gzip y caché inmutable para nombres con hash; HTML revalidado | Los recursos ausentes responden 404 en lugar de devolver HTML |
| Tarjetas con un rol incompatible, títulos desordenados y grises de bajo contraste | Enlaces nativos, jerarquía semántica y contraste ajustado dentro de la paleta | Sin cambios de disposición, tipografías ni dimensiones de componentes |
| Diálogo sin nombre accesible ni gestión completa del foco | Título asociado, fondo inerte, ciclo de Tab, Escape y devolución del foco | Se conservan login, registro, recuperación y animación de entrada |

La codificación WebP de tamaño original se verifica decodificando el resultado: deben coincidir el canal alfa y todos los píxeles visibles con el PNG fuente. Se ignora únicamente RGB de píxeles completamente transparentes. Las variantes pequeñas incluyen remuestreo, no compresión con pérdida. Mantener originales y variantes aumenta el almacenamiento de assets; la optimización busca reducir lo que descarga y decodifica cada navegador.

## Resultados comparables

La comparación local completó tres navegaciones por escenario en cada estado. Medianas de LCP: inicio móvil, 10,51 → 4,89 segundos; inicio de escritorio, 2,34 → 0,95 segundos; catálogo móvil, 6,12 → 1,98 segundos. La puntuación automatizada de accesibilidad pasó a 100 en los tres escenarios. Consulta el JSON enlazado para rangos, condiciones y auditorías pendientes.

El JavaScript inicial pasó de 314.589 a 274.365 bytes; la suma de todos los chunks aumentó de 314.589 a 326.450 bytes. La memoria JavaScript no disminuyó: las mediciones no justifican afirmar una reducción del heap ni del consumo total del navegador. Estos resultados describen este build y este entorno, y deben repetirse después de cambios posteriores.

## Pruebas y comprobaciones

La suite Playwright cubre 13 rutas en móvil/escritorio, búsqueda y municipio, filtros vacíos, menú móvil, carrusel, fechas inclusivas, logística, login, registro, recuperación, sesiones en memoria, paneles por rol y creación de una solicitud sin pago anticipado. También fuerza fallos de imagen y de descarga de una ruta para comprobar la recuperación.

Las pruebas de accesibilidad combinan axe con comprobaciones de teclado/foco, landmarks, nombres de controles, orden de títulos, contraste y ausencia de desbordamiento horizontal. Las capturas comparables conservan disposición y dimensiones; las animaciones solo se estabilizan para capturar imágenes, nunca para calcular Lighthouse. Las pruebas de movimiento ejecutan el paralaje normal y la preferencia de movimiento reducido.

Para capturas completas se desplaza la página de forma inmediata y se completan también las imágenes fuera de pantalla del carrusel, en ambos estados. Este ajuste afecta únicamente la captura visual; Lighthouse y la observación de memoria conservan la política normal de carga de la aplicación. La comparación falla automáticamente si cambian las dimensiones de las capturas o la geometría medida más de medio píxel; las diferencias de color/imagen requieren revisión visual.

La memoria JavaScript se observa con CDP y GC explícito, en tres contextos nuevos por viewport. Cada contexto realiza tres rondas de 20 viajes inicio/catálogo. El informe separa heap JavaScript, nodos retenidos y una estimación RGBA (`ancho × alto × 4`) de los archivos de imagen elegidos. Esa estimación no representa memoria total de GPU ni del proceso de Chrome.

## Reproducción en Windows

Requisitos: Node.js 24, npm, Chrome instalado, Docker operativo y backend disponible en `8080`. La primera vez, crea la imagen base desde la raíz con `docker compose build frontend`. Los comandos siguientes se ejecutan en PowerShell:

```powershell
cd frontend
npm ci
$env:VITE_API_BASE_URL = ''
npm run build
npm run audit:serve
npm run audit:delivery
npm run test:e2e
npm run audit:lighthouse -- candidate
npm run audit:browser -- candidate --check
npm run audit:runtime -- candidate
npm run audit:bundle -- candidate
```

Para comparar dos estados medidos:

```powershell
npm run audit:visual -- baseline candidate
npm run audit:report -- baseline candidate
```

No ejecutes pruebas, builds u otros benchmarks simultáneamente con Lighthouse. Antes de medir otro estado, conserva el informe anterior y cambia únicamente el código/entrega que deseas evaluar. `AUDIT_URL` permite utilizar otra URL de prueba. `AUDIT_RESUME=true` reutiliza navegaciones completas del mismo perfil; usa etiquetas nuevas para una medición independiente. No reutilices informes entre versiones de código.

`audit:serve` crea exclusivamente `tool-rental-frontend-audit` en el puerto 4173, valida Nginx y no recrea PostgreSQL ni el backend. Al terminar: `docker stop tool-rental-frontend-audit`. Playwright puede arrancar Vite Preview automáticamente si ese servidor no existe; ese modo sirve para pruebas funcionales, no para verificar gzip/caché ni comparar estas cifras de producción.

Para regenerar los assets después de modificar una imagen original:

```powershell
npm run images:optimize
npm run build
```

Versiona las variantes y `src/generated/responsiveImages.js` juntos. Sus nombres contienen hash del contenido para permitir caché inmutable. No elimines los PNG de respaldo ni cambies calidad/resolución sin revisar la comparación visual.

## Límites y seguimiento

- Repetir los recorridos con datos, permisos y latencias reales antes de publicar en producción.
- Comprobar Safari, Firefox, dispositivos físicos y lector de pantalla; la automatización actual utiliza Chrome.
- Verificar comportamiento con red lenta, sesión vencida y navegación entre versiones desplegadas; el límite de errores evita que falle toda la aplicación cuando no carga una pantalla.
- Incorporar RUM en producción para observar LCP, INP y CLS por dispositivo; los resultados locales no permiten afirmar que los usuarios cumplan Core Web Vitals.
- El trabajo no modifica reglas comerciales, endpoints ni seguridad del backend. No se añaden cobros reales.
