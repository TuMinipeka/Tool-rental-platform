# Verificación funcional con Playwright CLI

> Gestión de herramientas, imágenes y persistencia: [contratos y verificación actual](provider-persistence.md). Las evidencias anteriores conservan su revisión histórica.

## Entorno y alcance

Ejecutada el 20 de octubre de 2026 en la composición privada tool-rental-mvp, http://localhost:15173, con frontend React, backend Spring Boot/Tomcat y PostgreSQL reales. No se interceptan respuestas ni se usa el catálogo demo como sustituto de una API caída. Los datos de cada ejecución son ficticios, exclusivos de esta base y llevan un identificador único.

Se aplica la skill instalada [playwright-cli](https://skills.sh/microsoft/playwright-cli/playwright-cli) mediante la CLI incluida en Playwright 1.64.1. No se instaló una segunda copia global. El runner usa `open`, `resize`, `run-code` y `close`; las capturas se obtienen de páginas y elementos del navegador de esa sesión.

## Resultados

La [evidencia del recorrido](evidence/mvp/role-workflows.json) registra 91 comprobaciones correctas, revisión de origen, huella de fuentes, fecha y SHA-256/dimensiones de cada captura. Las imágenes se muestran en el [README](../README.md#funcionamiento-verificado-con-playwright-cli). La suite aislada terminó con 88 pruebas correctas y dos omisiones previstas por dispositivo; Maven clean verify terminó con 105 pruebas correctas, sin errores u omisiones.

Una captura documenta el estado visible; las comprobaciones de respuestas y consultas posteriores acreditan persistencia, permisos e idempotencia. No se infiere una transferencia financiera a partir de una imagen de aprobación.

## Matriz de casos verificados

| Área | Interfaz real | REST real y comprobaciones |
| --- | --- | --- |
| Visitante | Catálogo, búsqueda por nombre y presentación móvil | Administración sin sesión devuelve 401 |
| Cliente | Alta, cuenta inicialmente vacía, ingreso, cotización, reserva y cancelación | Solo obtiene CUSTOMER; fechas inclusivas; unidad retenida no admite superposición ni duplicados |
| Proveedor | Registro directo, publicación y registro de una unidad física | Pendiente de repetir manualmente tras el cambio: el contrato crea la cuenta activa y abre la sesión; el panel limita el inventario al proveedor |
| Pago ficticio | Aprobación y rechazo desde la pantalla del cliente | Propietario autorizado; proveedor/otro cliente denegados; aprobación repetida no duplica factura; decisión contradictoria devuelve 409 |
| Comprobante | Descarga desde la cuenta y contenido devuelto por el backend | Un solo documento académico, marcado sin validez ante DIAN |
| Entrega y alquiler | Programación, entrega completa, inicio, devolución y cierre | Estado COMPLETED persistido; devolución puntual con cero días y cero importe de mora |
| Extensiones | Sin pantalla específica; no se simula una interfaz inexistente | Cliente solicita, proveedor aprueba, cliente no puede aprobar; conserva fecha original y actualiza efectiva |
| Daños | Sin pantalla específica de creación en este recorrido | Proveedor registra después de la devolución y realiza valoración explícita |
| Reportes | Ingresos, herramientas más alquiladas, clientes frecuentes, disponibilidad e ingreso reservado | Todos consultan el backend con alcance de proveedor; ingreso reservado no equivale a beneficio contable |
| Cancelación y retracto | Cancelación de reserva sin pagar | Anticipación superior a 48 horas sin cargo; reembolso sobre pago confirmado; repetición idempotente; retracto sin penalidad durante revisión y rechazo administrativo restaura confirmación |
| Inasistencia y fallos | Sin pantalla específica en este recorrido | No permite declarar inasistencia antes de la gracia; fallo del proveedor exige una entrega fallida registrada |
| Administración | Activación y desactivación de proveedor; acceso de cliente denegado | Consultas de usuarios, roles, asociaciones, catálogo, reservas, detalles, alquileres, entregas, devoluciones, daños, pagos, facturas, líneas, terminaciones, políticas y tarifas |
| Sesiones | Restauración al navegar/recargar y logout | Lista autenticada; token fuera de web storage; baja administrativa y logout revocan acceso |
| Funciones excluidas | Recuperación oculta; contraseña incorrecta muestra error | Recuperación y simulación antigua deshabilitadas; OpenAPI publica solo /api/v1 y omite recuperación |

Los 25 contextos quedan representados mediante recorridos y contratos: los 18 originales por registro/consulta y ciclo operativo; usersession mediante renovación/listado/revocación; passwordreset mediante su exclusión; cancellationpolicy, deliveryrate, reservationtermination, reservationextension y refund mediante sus contratos vigentes. Esto no significa probar cada combinación de cada endpoint.

## Defectos encontrados y corregidos

Los formularios de herramienta y unidad leían `event.currentTarget` después de un await. React ya no garantiza ese valor, por lo que el registro podía guardarse y después mostrar un error o conservar el formulario. Ahora se captura el elemento antes de la petición y solo se limpia tras éxito. Una regresión de navegador verifica ambos formularios en escritorio y móvil; el recorrido real confirma la persistencia y ausencia del error.

El logout reutilizaba un token CSRF almacenado en memoria que podía quedar desactualizado tras autenticar o renovar la sesión. El servidor respondía 403 y la interfaz limpiaba su estado, mientras el token de acceso continuaba activo. Ahora las operaciones protegidas obtienen un token CSRF vigente; las regresiones de escritorio/móvil prueban logout después del ingreso y de una renovación. El recorrido real exige respuesta 204 y verifica después que el acceso anterior reciba 401. No se desactiva CSRF.

Se corrigieron textos de inicio, preguntas frecuentes y retorno que todavía describían el sandbox anterior. No se alteraron diseño, animaciones, reglas financieras ni contratos de backend.

## Reproducción

Con Docker/Compose y Chrome disponibles, desde la raíz:

```powershell
node scripts/prepare-validation.mjs
docker compose --env-file .env.validation -f compose.validation.yml up -d --build
node frontend/scripts/verify-role-workflows.mjs
```

El runner espera una ventana de 61 segundos al iniciar y otra entre grupos de roles para respetar el límite compartido de autenticación; no desactiva ni eleva el límite del backend. Por ello tarda aproximadamente dos o tres minutos. Usa exclusivamente localhost:15173, crea su propia sesión de navegador y cierra solo esa sesión. Escribe registros académicos en la composición aislada: no lo dirijas a la base de trabajo ni al destino público. Conserva las cuentas demo locales; el despliegue público utiliza otros accesos privados.

Para operar manualmente con la CLI, desde frontend:

```powershell
npx --no-install playwright cli -s=manual-qa open http://localhost:15173 --browser=chrome
npx --no-install playwright cli -s=manual-qa snapshot
npx --no-install playwright cli -s=manual-qa close
```

El código reutilizable del recorrido está en [verify-role-workflows.cli.js](../frontend/scripts/verify-role-workflows.cli.js). El wrapper [verify-role-workflows.mjs](../frontend/scripts/verify-role-workflows.mjs) registra metadatos y rechaza un resultado incompleto. No se guardan cookies, tokens, trazas de autenticación ni contraseñas privadas en la evidencia publicada.

## Límites de aceptación

La automatización no demuestra todas las combinaciones de entrada ni sustituye una evaluación del instructor. La mora por límites de calendario, reglas exactas de política y concurrencia se complementan con las pruebas Java/PostgreSQL; no se adelanta el reloj de producción para fotografiarlas. La auditoría no incorpora carga binaria de imágenes ni pantallas completas de edición/baja para cada recurso. La edición de perfil continúa sin contrato público dedicado. Tampoco incorpora recuperación por correo, pasarela real ni facturación fiscal.

La publicación Oracle, HTTPS y respaldo externo conservan sus verificaciones pendientes. Consulta [validación](validation-report.md), [MVP](mvp.md), [pagos ficticios](fictional-payments.md) y [despliegue](deployment.md).
