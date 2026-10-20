# Referencia histórica: adaptador Mercado Pago

**Fuera del MVP vigente.** Daniel eligió una [pasarela ficticia](fictional-payments.md), sin credenciales externas ni cobros reales, para acelerar el despliegue. No completar ni cargar .env.mercadopago.local para publicar esta demo. El archivo privado se conserva intacto, ignorado por Git.

El código del adaptador y sus pruebas se conservan como referencia técnica y para preservar el historial aplicado de V304. Solo se ensambla al seleccionar explícitamente payments.provider=mercadopago en un entorno de desarrollo; production usa fictional. Las pruebas con servidor HTTP aislado no acreditan ninguna compra oficial.

La publicación ya no depende de una cuenta de Mercado Pago ni de un callback auténtico. [MVP](mvp.md), [despliegue](deployment.md) y [validación](validation-report.md) describen el alcance actual. Una integración financiera real requiere una entrega y aceptación separadas.
