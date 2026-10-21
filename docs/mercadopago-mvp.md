# Mercado Pago Checkout Pro en pruebas

El proveedor de pagos vigente es Mercado Pago Checkout Pro en COP, limitado a cuentas y credenciales oficiales de prueba. La aplicación crea la preferencia en el backend, redirige al `init_point` validado y confirma el resultado únicamente después de consultarlo en la API de Mercado Pago.

## Configuración privada

Las credenciales se suministran exclusivamente como variables de entorno del backend. No existe endpoint ni pantalla para consultarlas o modificarlas:

```env
PAYMENT_PROVIDER=mercadopago
MERCADOPAGO_ACCESS_TOKEN=
MERCADOPAGO_COLLECTOR_ID=
MERCADOPAGO_WEBHOOK_SECRET=
PAYMENTS_PUBLIC_URL=https://tu-dominio.example
```

El adaptador consulta `/users/me` antes del primer checkout y rechaza cuentas que no tengan la marca `test_user` o cuyo identificador no coincida con `MERCADOPAGO_COLLECTOR_ID`. También rechaza pagos con `live_mode=true`, otra moneda, otro vendedor, referencia distinta o importe diferente al persistido.

`PAYMENTS_PUBLIC_URL` debe ser un origen HTTPS accesible desde Internet. El webhook que se configura en Mercado Pago es:

```text
https://tu-dominio.example/api/v1/payments/mercadopago/webhooks
```

La firma `x-signature` se valida con `MERCADOPAGO_WEBHOOK_SECRET`. El retorno del navegador nunca confirma por sí solo un pago. Las preferencias, búsquedas, consultas y reembolsos usan idempotencia y se concilian contra el estado persistido.

No se deben usar vendedores, compradores, tarjetas ni datos bancarios reales. La aceptación con credenciales auténticas de prueba y un webhook HTTPS sigue pendiente hasta que el operador complete el archivo privado `.env`; esas claves nunca se versionan.
