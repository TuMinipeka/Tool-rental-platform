# Pagos del MVP: Mercado Pago Checkout Pro

## Alcance aprobado

Demo académica, COP y cuentas oficiales de prueba de Mercado Pago Colombia. PayPal y recuperación por correo se aplazan expresamente. El comprobante HTML sigue siendo académico, sin validez fiscal. Implementar el adaptador y pasar pruebas aisladas no acredita una compra real en el sandbox oficial.

## Configuración privada

Ubicación preparada en la raíz: `.env.mercadopago.local` (ignorada por Git). La plantilla pública es `.env.mercadopago.example`. El archivo privado ya existe con campos vacíos; no contiene credenciales inventadas. En Linux aplica `chmod 600 .env.mercadopago.local` y configura sus valores desde la cuenta oficial.

La publicación y los respaldos leen `.env.showcase` y después `.env.mercadopago.local`. Para validar localmente usa `docker compose --env-file .env.validation --env-file .env.mercadopago.local -f compose.validation.yml up -d --build`. No ejecutes `docker compose config` sin `--quiet` ni imprimas el entorno del contenedor: puede mostrar secretos. En showcase, PAYMENTS_PUBLIC_URL se deriva de PUBLIC_HOST para mantener un solo origen.


| Variable | Contenido |
| --- | --- |
| MERCADOPAGO_ACCESS_TOKEN | Access token de la aplicación del vendedor de prueba; nunca una variable VITE_* |
| MERCADOPAGO_COLLECTOR_ID | Identificador del vendedor de prueba |
| MERCADOPAGO_WEBHOOK_SECRET | Secreto oficial de firma de notificaciones, mínimo 32 caracteres |
| PAYMENTS_PUBLIC_URL | Origen HTTPS sin barra final, accesible para el proveedor |

El prefijo APP_USR por sí solo no distingue cuentas reales de prueba. Antes de operar, el adaptador consulta /users/me y exige que coincidan el identificador y la etiqueta test_user. Las notificaciones también exigen live_mode=false, receptor correcto, referencia local, COP e importe exacto.

Configura la notificación de pagos en /api/v1/payments/mercadopago/webhooks. La firma usa x-signature, x-request-id y data.id de la URL. La aplicación consulta después el pago por su identificador: el cuerpo de la notificación y la URL de retorno no autorizan una confirmación.

## Contrato y consistencia

- POST /api/v1/payments/checkouts: Principal de cliente, Idempotency-Key y JSON con reservationId. El importe y el vencimiento se obtienen de la reserva. Devuelve paymentId, reservationId, paymentStatus, checkoutUrl y expiresAt.
- GET /api/v1/payments/checkouts/{id}: consulta autorizada del estado de conciliación; no expone credenciales.
- POST /api/v1/payments/mercadopago/webhooks: callback firmado, sin sesión de navegador; devuelve 200 tras procesarlo. Las fallas temporales no se convierten en éxito.
- /payment-return?paymentId=...: requiere sesión y consulta el servidor. Un estado aprobado en los parámetros de retorno carece de autoridad.

Los estados del checkout son CREATING, READY, REVIEW, DONE, REJECTED, REFUND_PENDING y REFUNDED. Los estados financieros existentes de Payment permanecen separados.

El intento se guarda antes de contactar al proveedor. Una concesión temporal en PostgreSQL evita trabajo concurrente; los bloqueos de reserva y unidades solo cubren las transacciones locales. Una restricción única mantiene un intento oficial por reserva. Ante una creación ambigua, se busca la preferencia existente y nunca se repite a ciegas la creación. Si no se encuentra de forma inequívoca, el intento queda en REVIEW.

La conciliación programada revisa hasta 20 intentos por lote, con espera entre intentos y un máximo automático de 100 operaciones por fila. Agotado el límite, el operador debe inspeccionar el estado local y el proveedor; no borrar filas ni crear cobros manuales. Los duplicados no repiten factura ni confirmación.

Una aprobación válida confirma reserva, pago y comprobante en la misma transacción. Si venció la retención, se canceló o dejó de ser posible confirmar, registra el dinero recibido y solicita un reembolso sandbox con clave estable. Nunca reactiva inventario vencido. Los reembolsos comerciales pendientes existentes siguen siendo registros de liquidación: no deben anunciarse como transferidos sin confirmación externa.

## Funciones retiradas del MVP

GET /api/v1/public/features es la fuente de capacidades del navegador. passwordRecovery=false oculta el enlace y desactiva la pantalla de restablecimiento. Ambas rutas REST devuelven 404 con code=FEATURE_DISABLED y no se anuncian en OpenAPI. Se conserva el código y sus pruebas mediante activación explícita solo en pruebas.

El perfil production deshabilita recuperación, entrega SMTP y pagos manuales/internos. No exige SMTP. Los endpoints de simulación, checkout interno y webhook HMAC devuelven FEATURE_DISABLED; el alta manual y los cambios manuales de estado de pagos también quedan bloqueados. No habilitar estas rutas para superar una prueba de aceptación del proveedor.

## Aceptación del proveedor, aún pendiente

1. El titular crea las cuentas oficiales de comprador y vendedor de prueba para Colombia y configura las variables privadas.
2. Configura una URL HTTPS de validación y la notificación; conserva cerrada la publicación general del MVP.
3. Ejecuta compras aprobadas y rechazadas con los datos oficiales de prueba; comprueba referencia, receptor, COP y ausencia de operaciones reales.
4. Verifica notificaciones auténticas, duplicados, conciliación, factura única y reembolso tardío.
5. Guarda fecha, revisión y evidencia sin tarjetas, tokens ni secretos. Solo entonces marca cumplida la aceptación externa.

No se han facilitado credenciales del proveedor ni una URL HTTPS de notificación. La aplicación debe mostrar un error temporal honesto si se intenta pagar sin configuración.

## Fuentes oficiales

- [Checkout Pro y preferencias](https://www.mercadopago.com.co/developers/es/docs/checkout-pro/overview).
- [Webhooks y verificación de firma](https://www.mercadopago.com.co/developers/es/docs/checkout-bricks/additional-content/your-integrations/notifications/webhooks).
- [Compras de prueba](https://www.mercadopago.com.co/developers/en/docs/checkout-pro-preferences/integration-test/test-purchases).
- [Reembolsos e idempotencia](https://www.mercadopago.com.co/developers/en/reference/online-payments/checkout-api-payments/create-refund/post).
