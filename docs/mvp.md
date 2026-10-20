# MVP — cierre de Conde e integración de Daniel/Mayorga

## Decisiones vigentes

Demo pública académica; COP y una pasarela ficticia integrada, sin proveedores ni cobros externos. **PayPal y correo quedan fuera de este MVP por decisión de Daniel.** La recuperación se conserva en código, pero está oculta y deshabilitada. No se ejecutan cobros reales ni se emiten documentos fiscales.

Rama de trabajo: developer/daniel. Se conserva el diseño de Conde y la arquitectura hexagonal de los tres módulos. El enunciado original no se modifica.

## Tareas de Conde

| Tarea | Implementación y aceptación |
| --- | --- |
| Clientes y proveedores | CRUD y baja lógica existentes; revocación de sesiones y conservación del historial cubiertas por regresión |
| Reservas | Retenciones, vencimiento, disponibilidad, confirmación, cancelación y ciclo operativo existentes; la decisión académica mantiene una transacción local |
| Pagos y comprobante | Pasarela ficticia persistente, decisión autenticada, idempotencia, confirmación y comprobante académico; aceptación mediante recorrido real contra PostgreSQL |
| Reportes | Cinco consultas con datos persistidos y alcance por rol; profitability significa ingreso reservado, no margen neto |
| JWT y roles | Sesiones rotativas, CSRF, registro directo de proveedor y revocación existentes; conservar pruebas de accesos prohibidos |
| Documentación y pruebas | Contrato y procedimiento en [pagos del MVP](fictional-payments.md); resultados actuales en [validación](validation-report.md) |
| Frontend | Pasarela ficticia y retorno conectado al estado del servidor, sin formulario académico ni PayPal; recuperación oculta y deshabilitada; se conserva el diseño |

Se retira expresamente la exigencia anterior de pasarela sandbox oficial. El HMAC antiguo queda como regresión interna bloqueada; el MVP usa una decisión de prueba autenticada y explícita.

## Responsabilidades de Daniel

| Prioridad | Entrega | Condición de cierre |
| --- | --- | --- |
| P0 | Compose aislado, frontend, backend y PostgreSQL | Repetir arranque, integración y evidencia de la revisión actual |
| P0 | Pasarela ficticia | Aprobar/rechazar, emitir un solo comprobante y completar alquiler contra PostgreSQL real, sin claves externas |
| P0 | Oracle gratuito, DNS y HTTPS | Cuenta y capacidad disponibles, dominio configurado, certificado válido y ejecución ARM probada |
| P0 | Respaldo externo | Recepción, retención de siete copias y restauración desde otro servidor |
| P0 | Evaluadores | Provisionar y rotar accesos privados; nunca publicar usuarios demo conocidos |
| P1 | Datos y modelos | Conservar matriz de 20 registros de negocio, excepciones técnicas, modelos y 4FN |

Correo público deja de ser una condición de publicación. Se sustituye por comprobar que la recuperación esté deshabilitada en backend, navegador y OpenAPI, sin exigir SMTP.

## Publicación condicionada

[Estado verificable](evidence/mvp/status.json) conserva bloqueado el lanzamiento mientras falten requisitos externos. El flujo de pago se acredita como simulación, nunca como cobro real. La publicación conserva controles de sesiones, roles y HTTPS.

Las evidencias anteriores de Mailpit y HMAC quedan como históricas, no como comportamiento público vigente. Consulta [despliegue](deployment.md), [contratos](integration-contracts.md) y [enunciado original](../ejercicioDemo.md).

## Apache Tomcat

Se confirma el uso de **Apache Tomcat integrado en Spring Boot**, desplegado como JAR ejecutable dentro de Docker con Java 21. No se requiere instalar Catalina por separado ni convertir el proyecto a WAR. [Procedimiento y explicación](deployment.md#apache-tomcat-integrado).

## Evidencia vigente

Las pruebas y capturas de esta revisión se registran en [validación](validation-report.md). Las referencias anteriores a Mercado Pago, credenciales de vendedor y aceptación de callback ya no bloquean este MVP. Continúan pendientes los requisitos del servidor público que aún no se hayan verificado.

Validación local concluida: **105 pruebas Java, 88 de navegador y dos omisiones previstas por dispositivo; 91 comprobaciones del recorrido real con Playwright CLI y 18 del perfil seguro**. Se verificaron Tomcat 10.1.55, JDBC, V305 y restauración local. El destino público conserva los bloqueos externos del checklist.

La [verificación por roles](browser-verification.md) documenta acciones de cliente, proveedor y administrador, contratos REST y limitaciones. El [README](../README.md#funcionamiento-verificado-con-playwright-cli) contiene capturas del recorrido. Se corrigieron el reinicio de formularios del proveedor y el token CSRF desactualizado al cerrar sesión.
