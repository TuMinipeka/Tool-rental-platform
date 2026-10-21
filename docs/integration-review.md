# Integración de developer/conde y auditoría pendiente

## Alcance y revisión

Integración local realizada el 20 de octubre de 2026 en `developer/daniel`, sin publicar a GitHub ni modificar `develop`. La publicación requiere la luz verde expresa del responsable.

- Origen: `f762a5c973254661f842ea8ff177b2b11ae4ff5c`, punta de `developer/conde` confirmada mediante comparación remota de GitHub (idéntica, cero commits pendientes).
- Destino previo: `12e36b4`; base común: `b03b73c`.
- Se integraron 16 commits de Conde mediante merge con dos padres, sin conflictos de Git. Se conservaron las imágenes de carga, prompt de Veo, vídeo y eliminaciones de skills del commit de Daniel.
- El fetch por terminal falló por conectividad; la comprobación remota mediante el conector verificó la referencia integrada.

## Decisiones y funcionalidades integradas

| Área | Comportamiento recibido |
| --- | --- |
| Proveedores | Registro público con cuenta activa, rol SUPPLIER y sesión; deja de requerir activación administrativa inicial. |
| Pagos | Visa, Mastercard, PSE, Mercado Pago y PayPal con datos académicos fijos. Los nombres comerciales no implican cobros ni integración oficial. |
| Reservas | Persistencia antes del checkout; importe calculado por backend y selección de método en `/pago?reservationId=...`. |
| Cuenta | Edición de nombre/correo y cambio mediante contraseña actual; revocación de sesiones al cambiar contraseña. |
| Gestión | Paneles por rol para herramientas/unidades, proveedores y administradores; reportes con datos del servidor. |
| Catálogo | Ciudad de servicio persistida, filtros por municipio y restricción territorial de entrega/cotización. |
| Comprobantes | PDF académico con vista inline y descarga attachment; no constituye factura fiscal. |
| Recuperación | Eliminación del controlador y notificador SMTP; no hay recuperación por correo dentro del MVP. |

## Ajustes necesarios para integrar

| Antes | Después | Motivo |
| --- | --- | --- |
| Estado de carga actualizado sincrónicamente desde un efecto | Inicialización de carga y resultados de la petición asíncrona | Resolver lint sin alterar el diseño ni las animaciones. |
| Dependencias incompletas de efectos de cuenta/panel | Identidad primitiva y callback estable | Mantener los datos coordinados con el usuario y rol actuales. |
| Pruebas con HTML, etiquetas/rutas anteriores y conteos antiguos | Contratos PDF, nuevos paneles, metadatos de checkout y conteos vigentes | Verificar el comportamiento recibido sin revertir las decisiones de Conde. |

## Validación ejecutada

- `mvnw.cmd clean verify`: **104 pruebas, cero fallos, errores u omisiones** (27 Domain, 16 Application y 61 Infrastructure). Incluye PostgreSQL/Testcontainers, Flyway, JPA, arquitectura y formato. La diferencia frente a las 105 históricas corresponde a la retirada de `PasswordRecoveryNotifierTest` junto con el adaptador SMTP.
- Flyway desde V200 conserva datos/checksums y alcanza V306.1; instalación demo vacía alcanza V308. Hay 39 migraciones base y 65 con demo.
- `npm run lint`, `npm run build` y `docker compose config --quiet`: correctos.
- `npm run test:e2e`: **80 pruebas correctas y 2 omisiones previstas por dispositivo**, cero fallos en la ejecución completa final. Se cubren escritorio/móvil, accesibilidad automatizada, paneles, navegación al nuevo pago, formularios y revocación CSRF.
- Los tests frontend usan dobles HTTP para aislar contratos; no sustituyen recorridos nuevos con navegador, backend y base reales.
- Las capturas y las 91 comprobaciones anteriores permanecen como evidencia histórica, no aceptación de estas funcionalidades nuevas.

Resumen fechado y huellas de los logs: [evidencia de integración](evidence/conde-integration.json).

## Auditoría pendiente antes del despliegue

| Prioridad | Verificación | Criterio de cierre |
| --- | --- | --- |
| P0 | Actualización de bases demo existentes | Ensayar sobre copia de una base V305. Las nuevas V106.1/V109.1/V115.1 quedan por debajo de versiones aplicadas y pueden impedir validar/migrar. No usar repair, out-of-order ni borrado como solución implícita. |
| P0 | Cinco medios académicos con servicios reales | Probar aprobación, rechazo, reintentos, doble clic e idempotencia; una sola reserva/comprobante, importe del backend y ausencia de secretos financieros. |
| P0 | Registro directo y permisos | Validar SUPPLIER activo, imposibilidad de autoasignar ADMIN, propiedad de herramientas/unidades y accesos prohibidos entre roles y propietarios. |
| P0 | Cuenta y contraseña | Comprobar correo duplicado, contraseña incorrecta, cierre de todas las sesiones y actualización del perfil al volver a ingresar. |
| P1 | Paneles y catálogo territorial | CRUD por rol, bajas con historial, estados vacíos/errores y cotizaciones para los cuatro municipios y fuera de cobertura. |
| P1 | PDF | Abrir/renderizar el documento real, verificar importes históricos, acentos, paginación y aviso académico; probar inline y attachment con autorización. |
| P1 | Términos y manuales | Los datos NIT/dirección/teléfono/contacto académicos requieren identificación explícita o sustitución por datos autorizados antes de publicar. Revisar comandos de auditoría live que aún asumen activación del proveedor o comprobante HTML. |
| P0 público | Infraestructura | Oracle gratuito, DNS/HTTPS, ejecución ARM, accesos privados y restauración desde respaldo externo siguen sin acreditarse. |

No se actualizaron bases de trabajo, no se ejecutaron recorridos mutantes contra ellas y no se declara el MVP listo para publicación por el solo hecho de integrar las ramas.
