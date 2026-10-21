# MVP — cierre de Conde e integración de Daniel/Mayorga

## Decisiones vigentes

Demo pública académica en COP con Visa, Mastercard, PSE, Mercado Pago y PayPal configurados con datos fijos no editables. **El correo, la recuperación de contraseña y los cobros reales quedan fuera del MVP.**

Rama de trabajo: developer/daniel. Se conserva el diseño de Conde y la arquitectura hexagonal de los tres módulos. El enunciado original no se modifica.

## Tareas de Conde

| Tarea | Implementación y aceptación |
| --- | --- |
| Clientes y proveedores | CRUD y baja lógica existentes; revocación de sesiones y conservación del historial cubiertas por regresión |
| Reservas | Retenciones, vencimiento, disponibilidad, confirmación, cancelación y ciclo operativo existentes; la decisión académica mantiene una transacción local |
| Pagos y comprobante | Cinco medios fijos, conciliación, idempotencia, confirmación y comprobante persistidos; pendiente de revisión manual del nuevo formulario |
| Reportes | Cinco consultas con datos persistidos y alcance por rol; profitability significa ingreso reservado, no margen neto |
| JWT y roles | Sesiones rotativas, CSRF, registro directo de proveedor y revocación existentes; conservar pruebas de accesos prohibidos |
| Documentación y pruebas | Contrato y procedimiento en [Mercado Pago](mercadopago-mvp.md); resultados históricos en [validación](validation-report.md) |
| Frontend | Redirección a Checkout Pro y retorno conectado al estado del servidor; sin recuperación de contraseña ni editor de credenciales |

La integración acepta exclusivamente vendedores y pagos de prueba. El retorno del navegador no confirma pagos: el backend consulta al proveedor y valida la firma del webhook.

## Responsabilidades de Daniel

| Prioridad | Entrega | Condición de cierre |
| --- | --- | --- |
| P0 | Compose aislado, frontend, backend y PostgreSQL | Repetir arranque, integración y evidencia de la revisión actual |
| P0 | Revisión del pago fijo | Comprobar manualmente los cinco medios, la confirmación y su aparición en `/cuenta` |
| P0 | Oracle gratuito, DNS y HTTPS | Cuenta y capacidad disponibles, dominio configurado, certificado válido y ejecución ARM probada |
| P0 | Respaldo externo | Recepción, retención de siete copias y restauración desde otro servidor |
| P0 | Evaluadores | Provisionar y rotar accesos privados; nunca publicar usuarios demo conocidos |
| P1 | Datos y modelos | Conservar matriz de 20 registros de negocio, excepciones técnicas, modelos y 4FN |

Correo y recuperación de contraseña no forman parte del contrato público ni requieren SMTP.

## Publicación condicionada

[Estado verificable](evidence/mvp/status.json) conserva bloqueado el lanzamiento mientras falten requisitos externos. Los pagos permanecen en ambiente académico y la publicación conserva controles de sesiones, roles y HTTPS.

Las evidencias anteriores de Mailpit y la pasarela interna quedan como históricas, no como comportamiento público vigente. Consulta [despliegue](deployment.md), [contratos](integration-contracts.md) y [enunciado original](../ejercicioDemo.md).

## Apache Tomcat

Se confirma el uso de **Apache Tomcat integrado en Spring Boot**, desplegado como JAR ejecutable dentro de Docker con Java 21. No se requiere instalar Catalina por separado ni convertir el proyecto a WAR. [Procedimiento y explicación](deployment.md#apache-tomcat-integrado).

## Evidencia vigente

Las pruebas y capturas anteriores se registran en [validación](validation-report.md). Los cinco medios fijos y la migración V306 quedan pendientes de revisión manual; no se ejecutaron pruebas para este cambio por solicitud del responsable.

Validación local concluida: **105 pruebas Java, 88 de navegador y dos omisiones previstas por dispositivo; 91 comprobaciones del recorrido real con Playwright CLI y 18 del perfil seguro**. Se verificaron Tomcat 10.1.55, JDBC, V305 y restauración local. El destino público conserva los bloqueos externos del checklist.

La [verificación por roles](browser-verification.md) documenta acciones de cliente, proveedor y administrador, contratos REST y limitaciones. El [README](../README.md#funcionamiento-verificado-con-playwright-cli) contiene capturas del recorrido. Se corrigieron el reinicio de formularios del proveedor y el token CSRF desactualizado al cerrar sesión.
