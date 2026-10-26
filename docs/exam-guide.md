# Guía del examen: aprobación y rechazo de reservas

## Objetivo

Esta guía sirve para desarrollar, probar y presentar una funcionalidad en la que un proveedor
acepta o rechaza una solicitud de alquiler pendiente. No contiene la solución del examen ni pide
copiar código: organiza el trabajo y señala las piezas equivalentes que ya existen en este
repositorio para estudiar el patrón arquitectónico.

## Antes de comenzar

1. Confirma que estás trabajando en la rama `examen/conde`:

   ```bash
   git branch --show-current
   ```

2. Lee `docs/development-guide.md` y `docs/backend-architecture.md`.
3. Revisa el estado del repositorio con `git status --short` y no incluyas cambios ajenos.
4. Abre el proyecto desde la raíz, donde están `pom.xml`, `domain`, `application` e
   `infrastructure`.
5. Anota el contrato exacto pedido por el profesor: nombres de estados, rutas, códigos HTTP,
   respuesta JSON, permisos y casos de error. No cambies esos nombres por intuición.

## Traducción del ejemplo a la arquitectura del proyecto

El ejemplo del profesor usa una entidad y un `ReservaService`. Este proyecto aplica arquitectura
hexagonal, por lo que las responsabilidades se distribuyen así:

| Necesidad | Lugar correcto |
| --- | --- |
| Estado y transiciones válidas | Agregado y objetos de valor de `domain/reservation` |
| Coordinación, autorización y disponibilidad | Caso de uso de `application/reservation` |
| Transacción atómica | Puerto de dominio y adaptador de persistencia |
| Endpoint HTTP | Controlador de `infrastructure/reservation` |
| Persistencia del estado | Entidad, mapper y repositorio JPA de Infrastructure |
| Restricciones de PostgreSQL | Nueva migración versionada de Flyway |
| Presentación al proveedor | Flujo correspondiente en `frontend` |

No crees un servicio genérico que mezcle HTTP, JPA y negocio. Tampoco expongas la entidad JPA en
la respuesta REST.

## Lo que ya existe y debes estudiar

Antes de escribir código, recorre estas referencias en este orden:

1. `domain/.../reservation/model/valueobject/ReservationStatus.java`: vocabulario de estados.
2. `domain/.../reservation/model/aggregate/Reservation.java`: creación en `PENDING`, validación de
   estado, confirmación y rechazo.
3. `application/.../reservation/usecase/ConfirmReservationUseCase.java`: autorización del
   proveedor, comprobación de cliente, unidades, publicación, precio y disponibilidad.
4. `application/.../reservation/usecase/RejectReservationUseCase.java`: autorización y transición
   de rechazo.
5. `domain/.../reservation/port/repository/AtomicReservationManagement.java`: contrato que mantiene
   el bloqueo, la verificación y el guardado dentro de una sola transacción.
6. `infrastructure/.../reservation/adapters/out/persistence/ReservationPersistenceAdapter.java`:
   implementación transaccional y bloqueos.
7. `infrastructure/.../reservation/adapters/in/rest/controllers/ReservationController.java`:
   adaptación del `Principal`, UUID y respuesta.
8. `infrastructure/.../reservation/config/ReservationBeanConfiguration.java`: creación de los casos
   de uso mediante inyección por constructor.

En `develop` ya existen operaciones equivalentes bajo los nombres `confirmation` y `rejection`.
Úsalas para comprender el recorrido, pero entrega el contrato exacto que se evalúe; no dupliques
casos de uso o rutas si el examen no exige un contrato distinto.

> Atención: el proyecto actual también usa `PENDING_PAYMENT` y confirma una reserva después del
> pago ficticio. No reemplaces ese flujo por el ejemplo simplificado del profesor. Si el examen
> exige aprobación manual de una reserva pagada, primero debe definirse desde qué estado parte y
> cómo convive con la confirmación del pago.

## Paso a paso de implementación

### 1. Dibujar la transición de estados

Escribe primero una tabla pequeña con el estado inicial, acción, estado final y actor. Para el
ejemplo sería:

| Estado inicial | Acción | Estado final | Actor |
| --- | --- | --- | --- |
| `PENDING` | Aprobar | `CONFIRMED` o el nombre exigido | Proveedor propietario |
| `PENDING` | Rechazar | `REJECTED` | Proveedor propietario |

Define también qué debe pasar si la reserva no existe, no pertenece al proveedor, el proveedor está
inactivo, el estado ya es terminal o la unidad ya no está disponible.

### 2. Ajustar el dominio solo si hace falta

- Comprueba si `ReservationStatus` ya contiene los estados solicitados.
- Comprueba que la fábrica de solicitud establezca el estado inicial requerido.
- La transición debe ser una operación explícita del agregado y validar el estado actual.
- Usa el `Clock` inyectado para eventos o marcas de tiempo.
- Conserva `restore` únicamente para reconstrucción desde persistencia, sin emitir eventos.
- Añade pruebas unitarias del dominio para transición exitosa y transición inválida.

No agregues setters como `setStatus`. El agregado debe impedir que una reserva aprobada o rechazada
vuelva a procesarse.

### 3. Implementar el caso de uso de aprobación

El caso de uso debe recibir identificadores tipados, no `Long` o `UUID` sin significado durante todo
el recorrido. Su secuencia esperada es:

1. Abrir la operación mediante `AtomicReservationManagement`.
2. Cargar y bloquear la reserva y sus unidades dentro de la misma transacción.
3. Obtener el proveedor actor desde el usuario autenticado.
4. Verificar que la reserva pertenece a ese proveedor y que su cuenta está activa.
5. Verificar el estado inicial mediante el agregado.
6. Comprobar que cliente, herramienta y unidad siguen habilitados.
7. Volver a comprobar solapamientos de disponibilidad para el periodo inclusivo.
8. Ejecutar la transición del agregado.
9. Guardar y devolver un `ReservationResponse` inmutable.

La comprobación de disponibilidad y el cambio de estado deben compartir transacción y bloqueos. Una
consulta previa seguida de un guardado independiente permitiría que dos solicitudes aprobaran la
misma unidad concurrentemente.

### 4. Implementar el caso de uso de rechazo

Sigue el mismo límite atómico y de autorización:

1. Buscar y bloquear la reserva.
2. Comprobar que el actor es el proveedor propietario y está activo.
3. Exigir que la reserva esté pendiente.
4. Ejecutar la transición a rechazo en el agregado.
5. Guardar y responder con el DTO de aplicación.

El rechazo no necesita comprobar disponibilidad, pero sí debe competir bajo el mismo bloqueo que la
aprobación para impedir resultados contradictorios.

### 5. Exponer los endpoints

En `ReservationController` agrega o adapta únicamente las rutas pedidas. Mantén el controlador
delgado:

- recibe el `Principal` y el identificador de ruta;
- construye los identificadores tipados;
- invoca un solo caso de uso;
- devuelve su respuesta.

El identificador del proveedor nunca debe recibirse en el cuerpo. Sale de `Actor.id(principal)`. La
autorización no se resuelve solo ocultando un botón en el frontend: el caso de uso debe comprobar la
propiedad de la reserva.

El proyecto publica `/api/v1` y conserva `/api` como alias obsoleto. Para la entrega, usa
preferentemente las rutas versionadas que correspondan al contrato aprobado.

### 6. Revisar persistencia y migraciones

- Verifica que `ReservationJpaEntity` y `ReservationPersistenceMapper` conserven el estado.
- Revisa las restricciones `ck_reservations_status` y `ck_reservations_confirmation`.
- Si agregas un estado, crea una migración Flyway nueva; nunca edites una migración aplicada.
- Asegura que una confirmación guarde también los campos exigidos por las restricciones, por ejemplo
  la fecha de confirmación.
- Prueba el flujo contra PostgreSQL real, no solamente con dobles en memoria.

### 7. Añadir el recorrido de interfaz si forma parte de la entrega

Una funcionalidad visible no se considera completa en este repositorio sin su recorrido frontend.
El panel del proveedor debería listar únicamente solicitudes propias, mostrar el estado, ofrecer
acciones válidas y actualizar el resultado persistido. Deshabilita las acciones durante la petición,
muestra errores del backend y evita dobles envíos.

No añadas esta parte si el examen limita explícitamente la entrega a REST; documenta entonces ese
límite en vez de afirmar que el producto quedó completo.

## Pruebas mínimas recomendadas

### Dominio

- una reserva nueva queda en el estado inicial;
- una reserva pendiente puede aprobarse;
- una reserva pendiente puede rechazarse;
- una reserva no pendiente no puede aprobarse ni rechazarse otra vez;
- la transición correcta emite el evento esperado, si el contrato lo exige.

### Aplicación

- el proveedor propietario activo puede actuar;
- otro proveedor recibe acceso denegado;
- un proveedor inactivo recibe acceso denegado;
- una reserva inexistente produce el error correspondiente;
- una unidad no operativa, una herramienta retirada o un solapamiento impiden aprobar;
- un fallo no deja cambios parciales.

### Infraestructura e integración

- el endpoint exige autenticación y rol adecuado;
- una aprobación válida responde el código acordado y persiste el nuevo estado;
- un rechazo válido persiste el nuevo estado;
- repetir una decisión devuelve conflicto o el error definido;
- dos aprobaciones concurrentes sobre la misma unidad no pueden dejar ambas reservas confirmadas.

Ejecuta primero las pruebas enfocadas de reserva. Usa `./mvnw clean verify` únicamente si el alcance
final requiere una validación amplia.

## Preparación de las evidencias

### 1. Obtener una reserva apta

1. Inicia sesión como cliente.
2. Crea una solicitud para una unidad operativa y fechas disponibles.
3. Conserva el ID devuelto.
4. Confirma mediante GET o SQL que el estado inicial es el requerido por el examen.
5. Inicia sesión como el proveedor dueño de esa herramienta y conserva su token.

No reutilices la misma reserva para aprobación y rechazo: cada transición deja un estado terminal.
Crea dos reservas diferentes.

### 2. Evidencia de aprobación en Postman

Configura una petición `POST` al endpoint versionado solicitado, añade
`Authorization: Bearer <token-del-proveedor>` y no envíes un proveedor en el cuerpo. La captura debe
mostrar al mismo tiempo:

- método y URL completa;
- cabecera de autenticación oculta o censurada;
- código HTTP exitoso;
- ID de la reserva;
- estado final en el JSON.

No publiques el JWT completo en la captura o en el repositorio.

### 3. Evidencia en PostgreSQL

Conecta DBeaver o pgAdmin a la base usada realmente por la aplicación y ejecuta una consulta de
solo lectura como esta, sustituyendo el UUID:

```sql
SELECT reservation_id, reservation_status, confirmed_at
FROM reservations
WHERE reservation_id = '<reservation-id>';
```

La captura debe mostrar la conexión/base correcta, el ID y el estado persistido. En el entorno local
documentado, PostgreSQL se expone en `localhost:5433`; confirma los valores reales de `.env` antes de
conectarte.

### 4. Evidencia de rechazo en Postman

Repite el procedimiento con una segunda reserva pendiente y el endpoint de rechazo. La respuesta y
la consulta posterior deben conservar el mismo ID y mostrar `REJECTED` o el valor exacto exigido.

### 5. Evidencia de errores

Aunque el profesor pida solo casos exitosos, conserva pruebas de al menos estos dos casos:

- intentar decidir de nuevo una reserva ya procesada;
- intentar aprobar como un proveedor distinto.

Estas pruebas demuestran que la regla está protegida y no es únicamente un cambio de columna.

## Ejecución local

### Arranque normal

Desde la raíz del repositorio, prepara una sola vez la configuración local:

```bash
cp .env.example .env
```

Abre `.env`, conserva la configuración local del proyecto y define una contraseña de PostgreSQL.
No publiques ese archivo ni pegues su contenido en capturas.

Después valida y levanta la composición:

```bash
docker compose config --quiet
docker compose up --build -d --wait
```

Comprueba el estado de los contenedores:

```bash
docker compose ps -a
```

Verifica la conexión real del backend con PostgreSQL:

```bash
curl --fail http://localhost:8080/status/database
```

La respuesta esperada contiene `"connected": true`. Verifica también que la interfaz responda:

```bash
curl --head --fail http://localhost:5173/
```

La web queda en `http://localhost:5173`, el backend en `http://localhost:8080`, PostgreSQL en el
puerto configurado por `POSTGRES_PORT` y Swagger en
`http://localhost:8080/swagger-ui/index.html`.

### Procedimiento usado en este equipo

En esta sesión el puerto `8080` ya estaba ocupado por otro proceso. Para no detener una aplicación
ajena, se usó `18080` para el backend y `5433` para PostgreSQL. Primero se comprobó qué puertos y
contenedores estaban activos:

```bash
ss -ltnp '( sport = :8080 or sport = :5173 or sport = :5433 )'
docker --context rootless ps
```

El intento de construcción completa fue:

```bash
POSTGRES_PORT=5433 BACKEND_PORT=18080 \
VITE_API_BASE_URL=http://localhost:18080 \
docker --context rootless compose up --build -d --wait
```

La imagen nueva del backend no pudo construirse debido a un problema que ya estaba en `develop`:
la prueba `FileSystemToolImageStorageTest` referencia una clase que no está en el árbol versionado.
Como el alcance de esta tarea era únicamente documental, no se modificó el código para ocultar o
corregir ese fallo.

Se reconstruyó solamente el frontend para que utilizara el puerto alternativo:

```bash
POSTGRES_PORT=5433 BACKEND_PORT=18080 \
VITE_API_BASE_URL=http://localhost:18080 \
docker --context rootless compose build frontend
```

Después se recrearon los servicios usando la imagen local disponible del backend, sin intentar
compilarla otra vez:

```bash
POSTGRES_PORT=5433 BACKEND_PORT=18080 \
VITE_API_BASE_URL=http://localhost:18080 \
docker --context rootless compose up -d --no-build \
  --force-recreate backend frontend --wait
```

Finalmente se verificaron los servicios:

```bash
POSTGRES_PORT=5433 BACKEND_PORT=18080 \
VITE_API_BASE_URL=http://localhost:18080 \
docker --context rootless compose ps -a

curl --fail http://localhost:18080/status/database
curl --head --fail http://localhost:5173/
```

El resultado fue PostgreSQL, backend y frontend en estado `healthy`, el backend respondió
`"connected": true` y el frontend respondió HTTP `200`. En esta configuración las direcciones son:

- frontend: `http://localhost:5173`;
- backend: `http://localhost:18080`;
- Swagger: `http://localhost:18080/swagger-ui/index.html`;
- PostgreSQL: `localhost:5433`.

La variable `POSTGRES_PASSWORD` también debe estar definida mediante `.env` o en la sesión de la
terminal antes de ejecutar Compose. Se omitió deliberadamente de los ejemplos para no guardar
secretos en el repositorio.

### Detener y volver a iniciar

Para detener los servicios conservando los datos:

```bash
docker compose stop
```

Para volver a iniciarlos con la misma configuración:

```bash
docker compose start
```

No ejecutes `docker compose down -v`, porque elimina los volúmenes con datos e imágenes.

## Lista de comprobación antes del commit

- [ ] La rama activa es `examen/conde`.
- [ ] Solo están incluidos los archivos propios del examen.
- [ ] No se modificaron migraciones Flyway ya aplicadas.
- [ ] El proveedor se obtiene del principal autenticado.
- [ ] Solo el proveedor propietario puede aprobar o rechazar.
- [ ] La disponibilidad y la aprobación son atómicas.
- [ ] Las transiciones inválidas dejan la base sin cambios parciales.
- [ ] Las pruebas enfocadas pasan con PostgreSQL real cuando corresponde.
- [ ] El frontend servido fue reconstruido si se modificó.
- [ ] Las capturas no muestran contraseñas, JWT ni otros secretos.
- [ ] `git diff --check` no reporta errores de formato.

Revisa los cambios con `git diff` antes de confirmar. El mensaje debe estar en inglés y cumplir
`<type>: :<gitmoji>: <CamelCaseSummary>`; un ejemplo válido para una implementación terminada sería:

```text
feat: :white_check_mark: AddSupplierReservationDecision
```

No hagas el commit hasta que el código funcional, las pruebas y las evidencias correspondan al mismo
estado del repositorio.
