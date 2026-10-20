# Pasarela ficticia del MVP

## Decisión vigente

Daniel autoriza reemplazar Mercado Pago por una pasarela completamente ficticia para publicar la demo académica. No hay cobros, bancos, cuentas de proveedor, claves externas ni webhooks de terceros. PayPal, correo y recuperación permanecen aplazados. Esto sustituye el requisito anterior de aceptación sandbox oficial para este MVP.

## Recorrido

1. El cliente solicita una reserva y el servidor retiene sus unidades hasta el vencimiento.
2. En Mi cuenta selecciona **Pagar en pasarela ficticia**. El importe COP, la reserva y el vencimiento provienen de PostgreSQL.
3. La pantalla `/checkout/{paymentId}` identifica la simulación y ofrece **Simular aprobación** o **Simular rechazo**. No solicita PAN, CVV ni contraseñas bancarias.
4. El backend exige sesión, rol Customer y propiedad. Guarda la decisión académica antes de procesarla; no acepta importes ni actores suministrados por el navegador.
5. Aprobación: confirma pago y reserva y emite un comprobante académico, todo en una transacción. Rechazo: falla el pago y libera la retención. Una decisión repetida es idempotente; una decisión contradictoria devuelve 409.
6. El retorno consulta el estado del servidor. Los parámetros de la URL no aprueban nada. Una decisión recibida después de vencer la reserva se rechaza; si una aprobación persistida quedó sin procesar y vence antes de recuperarse, el backend registra un reembolso también ficticio, sin restaurar inventario.

## API

| Contrato | Uso |
| --- | --- |
| GET /api/v1/public/features | paymentProvider=FICTIONAL_GATEWAY, hostedCheckout=true; recuperación deshabilitada |
| POST /api/v1/payments/checkouts | Principal, Idempotency-Key y reservationId; crea o recupera un intento por reserva |
| GET /api/v1/payments/checkouts/{id} | Consulta autorizada: amount, currency, processor, paymentStatus, checkoutUrl y expiresAt |
| POST /api/v1/payments/fictional/checkouts/{id}/decision | JSON outcome=APPROVED o DECLINED; solo cliente propietario |

Los estados del checkout son CREATING, READY, REVIEW, DONE, REJECTED, REFUND_PENDING y REFUNDED. Los registros financieros siempre deben leerse como académicos. Los reembolsos comerciales PENDING no acreditan una transferencia bancaria.

## Arquitectura y persistencia

El puerto PaymentGateway sigue en Domain. Application coordina casos de uso, permisos y transacciones; FictionalPaymentGateway implementa el adaptador persistente en Infrastructure. No hay resultados guardados exclusivamente en memoria. La conciliación programada recupera decisiones persistidas tras una interrupción.

V305 amplía payments con fictional_outcome y fictional_refunded, conservando 25 tablas/contextos y las migraciones aplicadas. La unicidad del intento abarca los procesadores actuales e históricos. El adaptador externo de Mercado Pago conserva su código como opción inactiva para una entrega futura; ninguna ruta del MVP lo invoca y no se cargan sus credenciales privadas en Compose.

## Ejecución y pruebas

El proveedor predeterminado y el perfil production usan fictional. No es necesario completar .env.mercadopago.local; ese archivo privado se conserva intacto y no lo leen los scripts de publicación.

En la raíz: `docker compose --env-file .env.validation -f compose.validation.yml up -d --build`. Después ejecuta `node frontend/scripts/verify-fictional-mvp.mjs`, `node scripts/verify-showcase.mjs` y `node scripts/verify-tomcat.mjs`. El primer recorrido escribe datos ficticios exclusivamente en la composición local aislada; no ejecutarlo contra una base de trabajo o pública.

Consulta [MVP](mvp.md), [Tomcat y publicación](deployment.md) y [validación](validation-report.md). La pasarela ficticia elimina la dependencia externa de pagos, pero no acredita que Oracle, HTTPS o un respaldo externo ya estén funcionando.
