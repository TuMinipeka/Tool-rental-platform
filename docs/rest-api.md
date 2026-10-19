# API REST

Las rutas privadas empiezan por `/api` y requieren un JWT Bearer cuyo `sub` es el UUID del usuario. Ningún campo `actorId` ni encabezado personalizado sustituye la identidad autenticada. `/api/auth/**` y `/api/public/**` son públicos, excepto `/api/auth/logout`, que exige un token válido.

Los listados administrativos requieren el rol `ADMIN`. Los usuarios activos pueden consultar categorías y herramientas publicadas; el catálogo público expone exclusivamente ofertas publicadas. Los proveedores también pueden ver sus propias ofertas sin publicar. Los recursos individuales validan propiedad o participación. La administración de roles requiere `ADMIN`, y las operaciones sobre herramientas requieren al proveedor propietario.

Las contraseñas solo se reciben al crear una cuenta, deben tener entre 12 caracteres y 72 bytes UTF-8 y se almacenan con BCrypt. Ninguna respuesta expone el hash.

Swagger UI está disponible en `/swagger-ui.html` y el documento OpenAPI en `/v3/api-docs`. El botón `Authorize` acepta el JWT emitido por login o registro.

## Rutas

| Recurso | Operaciones |
| --- | --- |
| `/auth` | POST `/register`, `/login`, `/password-recovery`, `/password-reset` y `/logout` |
| `/public/catalog` | Listado GET, GET `/{id}` y disponibilidad GET `/{id}/availability` |
| `/categories` | POST, listado GET y GET/PUT/DELETE `/{id}`; una categoría referenciada no se elimina |
| `/tools` | POST, listado GET, GET/PUT `/{id}` y DELETE `/{id}`, que retira la publicación |
| `/equipment-units` | POST, listado GET y GET/PUT `/{id}`; el retiro es terminal |
| `/reservations` | POST, listado del actor GET, GET `/{id}` y POST `/{id}/confirmation`, `/rejection`, `/cancellation` |
| `/quotes/reservations` | POST para subtotal, días facturables y división 50/50 sin inventar IVA |
| `/users` | POST, listado GET, GET `/{id}` y PATCH `/{id}/status` |
| `/roles` | POST, listado GET y GET/PUT `/{id}` |
| `/user-roles` | POST, listado GET y GET/DELETE `/entry?userId=...&roleId=...` |
| `/customers`, `/suppliers` | POST, listado GET y GET/PUT `/{userId}` |
| `/tool-images` | POST, listado GET y GET/DELETE `/entry?toolId=...&imageUrl=...` |
| `/reservation-items` | Listado GET y GET `/entry?reservationId=...&equipmentUnitId=...` |
| `/rentals` | POST con `reservationId`, listado GET, GET `/{reservationId}` y PATCH de estado |
| `/unit-deliveries` | POST, listado GET, GET de `/entry` y PATCH `/entry/completion` |
| `/unit-returns` | POST, listado GET y GET de `/entry` |
| `/damage-reports` | POST, listado GET, GET `/{id}`, PATCH de valoración y PATCH de estado |
| `/payments` | POST manual administrativo, POST `/simulations`, listado por participación GET, GET `/{id}` y PATCH administrativo de estado |
| `/invoices` | POST con líneas, listado GET, GET `/{id}` y PATCH de estado |
| `/invoice-items` | Listado GET y GET `/entry?invoiceId=...&lineNumber=...` |

Las claves compuestas usan parámetros de consulta para evitar rutas ambiguas, especialmente con las URL de imágenes. Estos parámetros identifican el recurso, nunca al actor autenticado.

## Autenticación y recuperación

El registro público crea una cuenta y un perfil `CUSTOMER`, asigna el rol y devuelve un JWT de acceso. Login usa correo y contraseña. El token dura 15 minutos por defecto; `JWT_ACCESS_TTL` puede cambiar la duración. No hay token de renovación porque su contrato continúa pendiente.

Logout es stateless: responde `204` y el cliente elimina el JWT. No existe todavía una lista persistente de revocación, por lo que el token conserva validez hasta expirar. La recuperación emite un token firmado de demostración ligado al hash actual; cambiar la contraseña lo invalida. Mientras no exista un proveedor de correo, el entorno académico devuelve ese token en la respuesta. Esto no es un flujo apto para producción.

## Ejemplos de entrada

La creación de una reserva obtiene el cliente desde el `Principal`:

```json
{"startDate":"2026-10-20","returnDate":"2026-10-21","equipmentUnitIds":["00000000-0000-0000-0000-000000000001"]}
```

La cotización usa las mismas fechas y unidades. Calcula días inclusivos, subtotal de alquiler y dos mitades. `taxCalculationStatus` permanece en `PENDING_LEGAL_VALIDATION`; no representa un total fiscal.

El pago simulado recibe `paymentStage` (`FIRST_HALF` o `FINAL_HALF`), `paymentMethod` (`CREDIT_CARD`, `DEBIT_CARD` o `PAYPAL`), una clave de idempotencia y `simulationOutcome` (`APPROVE` o `DECLINE`). Para tarjeta solo recibe red, últimos cuatro dígitos y expiración; nunca número completo ni CVV. El segundo 50 % exige primer pago confirmado y alquiler completado.

Una operación de estado acepta `{"status":"COMPLETED"}`. Las transiciones admitidas son:

- usuario: `ACTIVE -> SUSPENDED/DISABLED` y `SUSPENDED -> ACTIVE/DISABLED`;
- alquiler: `ACTIVE -> COMPLETED/CANCELLED`;
- pago: `PENDING -> CONFIRMED/FAILED` y `CONFIRMED -> REFUNDED`;
- daño: `OPEN -> ASSESSED/CLOSED` y `ASSESSED -> CLOSED`;
- factura: `ISSUED -> PAID/VOIDED`.

La valoración de daños usa `{"cost":5}`. La finalización de una entrega usa `{"condition":"GOOD"}` y registra la hora del servidor.

La creación de una factura incluye `reservationId`, `number` y una lista no vacía `lines`, cuyos elementos contienen `description`, `quantity` y `unitPrice`. Las instantáneas de emisor y destinatario provienen de sus perfiles. La operación guarda encabezado y líneas en una sola transacción; todavía no implementa numeración fiscal ni impuestos. Los pagos son una simulación académica, no una pasarela externa.

## Respuestas y errores

- POST exitoso: `201`.
- GET, PUT o PATCH exitoso: `200`.
- DELETE admitido: `204`.
- Contrato inválido: `400`.
- Falta de identidad: `401`.
- Acceso denegado: `403`.
- Recurso inexistente: `404`.
- Conflicto de regla, unicidad o referencia: `409`.
- Operación histórica no permitida: `405`.

Los mensajes internos de base de datos no se exponen. Las respuestas de aplicación son inmutables. Los recursos originales de catálogo y reserva conservan identificadores tipados con forma `{"value":"uuid"}`; los demás contextos exponen claves con nombre, como `userId` o `invoiceId`. Una futura versión de la API puede unificar esta representación.
