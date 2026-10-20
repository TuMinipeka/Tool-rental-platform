# MVP — cierre de Conde e integración de Daniel/Mayorga

## Decisiones vigentes

Demo pública académica; COP y Mercado Pago Checkout Pro con cuentas oficiales de prueba. **PayPal y correo quedan fuera de este MVP por decisión de Daniel.** La recuperación se conserva en código, pero está oculta y deshabilitada. No se ejecutan cobros reales ni se emiten documentos fiscales.

Rama de trabajo: developer/daniel. Se conserva el diseño de Conde y la arquitectura hexagonal de los tres módulos. El enunciado original no se modifica.

## Tareas de Conde

| Tarea | Implementación y aceptación |
| --- | --- |
| Clientes y proveedores | CRUD y baja lógica existentes; revocación de sesiones y conservación del historial cubiertas por regresión |
| Reservas | Retenciones, vencimiento, disponibilidad, confirmación, cancelación y ciclo operativo existentes; nueva aprobación oficial mantiene una transacción local |
| Pagos y comprobante | Adaptador oficial, checkout alojado, firma, consulta canónica, conciliación, idempotencia y reembolso tardío implementados; **aceptación real del proveedor pendiente de credenciales y callback HTTPS** |
| Reportes | Cinco consultas con datos persistidos y alcance por rol; profitability significa ingreso reservado, no margen neto |
| JWT y roles | Sesiones rotativas, CSRF, activación administrativa de proveedor y revocación existentes; conservar pruebas de accesos prohibidos |
| Documentación y pruebas | Contrato y procedimiento en [pagos del MVP](mercadopago-mvp.md); resultados actuales en [validación](validation-report.md) |
| Frontend | Checkout oficial y retorno conectado al estado del servidor, sin formulario académico ni PayPal; recuperación oculta y deshabilitada; se conserva el diseño |

La implementación no equivale a aceptación externa. El sandbox HMAC anterior solo conserva utilidad de regresión interna; no satisface el requisito oficial y está bloqueado por defecto.

## Responsabilidades de Daniel

| Prioridad | Entrega | Condición de cierre |
| --- | --- | --- |
| P0 | Compose aislado, frontend, backend y PostgreSQL | Repetir arranque, integración y evidencia de la revisión actual |
| P0 | Mercado Pago oficial | Titular proporciona configuración privada; validar compra aprobada/rechazada, callback auténtico y conciliación |
| P0 | Oracle gratuito, DNS y HTTPS | Cuenta y capacidad disponibles, dominio configurado, certificado válido y ejecución ARM probada |
| P0 | Respaldo externo | Recepción, retención de siete copias y restauración desde otro servidor |
| P0 | Evaluadores | Provisionar y rotar accesos privados; nunca publicar usuarios demo conocidos |
| P1 | Datos y modelos | Conservar matriz de 20 registros de negocio, excepciones técnicas, modelos y 4FN |

Correo público deja de ser una condición de publicación. Se sustituye por comprobar que la recuperación esté deshabilitada en backend, navegador y OpenAPI, sin exigir SMTP.

## Publicación condicionada

[Estado verificable](evidence/mvp/status.json) conserva bloqueado el lanzamiento mientras falten requisitos externos. No marcar una compra oficial como probada por pasar un doble HTTP. No abrir la demo general para resolver el callback; usar un entorno HTTPS de aceptación restringido.

Las evidencias anteriores de Mailpit y HMAC quedan como históricas, no como comportamiento público vigente. Consulta [despliegue](deployment.md), [contratos](integration-contracts.md) y [enunciado original](../ejercicioDemo.md).

## Validación de esta entrega

Código `0692c31`: Maven 100/100 sin omisiones; navegador 78 pruebas correctas y dos omisiones previstas por dispositivo; 21 comprobaciones contra vistas/API/PostgreSQL reales y 17 del perfil seguro sin SMTP. Respaldo restaurado y persistencia tras reinicio verificados con V304. La compra oficial sigue pendiente de las credenciales y el callback auténtico, no de Mailpit ni de un simulador. [Evidencias y límites](validation-report.md).
