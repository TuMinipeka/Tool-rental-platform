# Ampliaciones de backend preparadas para la evaluación

## Propósito

Este documento registra tres ampliaciones realizadas exclusivamente en el backend para practicar cambios frecuentes en una evaluación de Spring Boot: agregar filtros y paginación, construir un reporte derivado y crear un nuevo contexto persistente. Las implementaciones conservan la arquitectura hexagonal del proyecto, la autenticación JWT, el alcance por roles, las migraciones Flyway y la documentación OpenAPI.

La cobertura general de la rúbrica permanece en [requirements-coverage.md](requirements-coverage.md); los contratos completos de la API están en [rest-api.md](rest-api.md).

## Resumen de lo implementado

| Posible requerimiento | Resultado | Evidencia principal |
| --- | --- | --- |
| Paginar y filtrar el catálogo | Endpoint público paginado con categoría, texto, ciudad, rango de precio y ordenamiento | `GET /api/v1/public/catalog/page` |
| Agregar un reporte operativo | Consulta de alquileres activos cuya devolución esperada ya venció | `GET /api/v1/reports/overdue-rentals` |
| Persistir una nueva decisión del usuario | Aceptación legal versionada, inmutable e idempotente | `POST /api/v1/legal-acceptances` |
| Consultar el nuevo historial | Historial propio ordenado desde el registro más reciente | `GET /api/v1/legal-acceptances/me` |

## 1. Catálogo paginado y filtrado

### Necesidad cubierta

El catálogo histórico devuelve una lista completa. La ampliación permite una interfaz paginada sin modificar el contrato existente, de modo que el frontend actual continúa funcionando.

### Contrato REST

```http
GET /api/v1/public/catalog/page?page=0&size=12&sort=dailyRate,desc&serviceCity=Bucaramanga&minDailyRate=10000&maxDailyRate=100000
```

Parámetros admitidos:

| Parámetro | Regla |
| --- | --- |
| `categoryId` | UUID opcional de la categoría |
| `query` | Texto opcional buscado en nombre y descripción |
| `serviceCity` | Ciudad opcional, sin distinguir mayúsculas |
| `minDailyRate` | Tarifa mínima no negativa |
| `maxDailyRate` | Tarifa máxima no negativa y mayor o igual a la mínima |
| `page` | Página desde cero; valor predeterminado `0` |
| `size` | Entre 1 y 100; valor predeterminado `12` |
| `sort` | `name,asc`, `name,desc`, `dailyRate,asc` o `dailyRate,desc` |

Respuesta abreviada:

```json
{
  "content": [
    {
      "toolId": "20000000-0000-0000-0000-000000000001",
      "name": "Taladro percutor",
      "dailyRate": 35000,
      "currency": "COP",
      "serviceCity": "Bucaramanga"
    }
  ],
  "page": 0,
  "size": 12,
  "totalElements": 33,
  "totalPages": 3
}
```

Una página negativa, un tamaño fuera del rango, un rango de precios inválido o un ordenamiento no permitido produce un error de negocio `409` mediante el manejador global de `ProblemDetail`.

### Ubicación por capa

- Application: [`BrowseCatalogUseCase`](../application/src/main/java/com/toolrental/application/tool/usecase/BrowseCatalogUseCase.java) valida filtros, ordena y construye la página.
- DTO de salida: [`CatalogPageResponse`](../application/src/main/java/com/toolrental/application/tool/dto/CatalogPageResponse.java).
- Infrastructure: [`PublicCatalogController`](../infrastructure/src/main/java/com/toolrental/infrastructure/tool/adapters/in/rest/controllers/PublicCatalogController.java) publica el nuevo recurso.

### Decisión de compatibilidad

`GET /api/v1/public/catalog` permanece intacto. La paginación se añadió en `/page` para que clientes existentes no cambien de una lista JSON a un objeto paginado inesperadamente.

## 2. Reporte de alquileres vencidos

### Necesidad cubierta

La rúbrica exige reportes y control del historial de alquileres. Este reporte identifica reservas con alquiler activo cuya `effective_return_date` es anterior a la fecha consultada.

### Contrato REST

```http
GET /api/v1/reports/overdue-rentals?asOf=2026-10-23
Authorization: Bearer <access-token>
```

El administrador puede limitar el resultado a un proveedor:

```http
GET /api/v1/reports/overdue-rentals?asOf=2026-10-23&supplierId=<uuid>
```

Respuesta de ejemplo:

```json
[
  {
    "reservationId": "a55b7587-1af3-44d2-948c-7c2a9258a112",
    "customerId": "9f831daa-f191-4088-b56f-c92001982710",
    "customerName": "Cliente Demo",
    "expectedReturnDate": "2026-10-20",
    "overdueDays": 3,
    "rentalStatus": "ACTIVE",
    "unitCount": 1
  }
]
```

### Autorización

- `ADMIN`: consulta toda la plataforma o utiliza `supplierId`.
- `SUPPLIER`: el caso de uso fuerza el identificador del actor autenticado; no puede consultar herramientas ajenas.
- `CUSTOMER`: recibe `403 Forbidden`.

El controlador no acepta un `actorId` enviado por el navegador. La identidad procede del `Principal` generado a partir del JWT.

### Ubicación por capa

- Puerto de dominio: [`OperationalReportingPort`](../domain/src/main/java/com/toolrental/domain/payment/port/OperationalReportingPort.java).
- Caso de uso: [`OperationalReportUseCase`](../application/src/main/java/com/toolrental/application/payment/usecase/OperationalReportUseCase.java).
- DTO: [`OverdueRentalReportResponse`](../application/src/main/java/com/toolrental/application/payment/dto/OverdueRentalReportResponse.java).
- Adaptador PostgreSQL: [`OperationalReportingPersistenceAdapter`](../infrastructure/src/main/java/com/toolrental/infrastructure/payment/adapters/out/persistence/OperationalReportingPersistenceAdapter.java).
- Controlador: [`OperationalReportController`](../infrastructure/src/main/java/com/toolrental/infrastructure/payment/adapters/in/rest/controllers/OperationalReportController.java).

La consulta agrupa las unidades por reserva y calcula los días vencidos en PostgreSQL usando la fecha `asOf` suministrada.

## 3. Aceptaciones legales versionadas

### Necesidad cubierta

Mostrar una página de términos no demuestra que el usuario haya aceptado una versión concreta. El nuevo contexto conserva una evidencia persistente con usuario, tipo de documento, versión, propósito y fecha.

### Registrar una aceptación

```http
POST /api/v1/legal-acceptances
Authorization: Bearer <access-token>
Content-Type: application/json

{
  "documentType": "TERMS_AND_CONDITIONS",
  "documentVersion": "2026-10-20",
  "purpose": "CUSTOMER_REGISTRATION"
}
```

Respuesta:

```json
{
  "id": "dd645011-ebbd-4817-a165-4f30f80d2676",
  "userId": "9f831daa-f191-4088-b56f-c92001982710",
  "documentType": "TERMS_AND_CONDITIONS",
  "documentVersion": "2026-10-20",
  "purpose": "CUSTOMER_REGISTRATION",
  "acceptedAt": "2026-10-23T14:30:00Z"
}
```

Tipos admitidos:

- `TERMS_AND_CONDITIONS`
- `PRIVACY_NOTICE`

El propósito se normaliza a mayúsculas y solo admite letras, números y guion bajo. Enviar nuevamente la misma combinación de usuario, documento, versión y propósito devuelve la aceptación existente, sin duplicar filas.

### Consultar el historial propio

```http
GET /api/v1/legal-acceptances/me
Authorization: Bearer <access-token>
```

La respuesta contiene únicamente las aceptaciones del usuario autenticado.

### Arquitectura del contexto

| Capa | Componentes |
| --- | --- |
| Domain | Agregado `LegalAcceptance`, identificador y puerto de repositorio |
| Application | Comando, DTO y `ManageLegalAcceptanceUseCase` |
| Infrastructure | Controlador REST, entidad JPA, mapper, repositorio Spring Data, adaptador y configuración de beans |

El contexto se encuentra en los paquetes `legalacceptance` de los tres módulos. El agregado no depende de Spring ni de JPA.

### Modelo físico

[`V309__create_legal_acceptances_table.sql`](../infrastructure/src/main/resources/db/migration/V309__create_legal_acceptances_table.sql) crea `legal_acceptances` con:

- clave primaria UUID;
- clave foránea hacia `users`;
- restricciones para documento y propósito;
- clave única `(user_id, document_type, document_version, purpose)`;
- índice por usuario y fecha de aceptación descendente.

La ampliación eleva el modelo a 26 contextos, 26 entidades JPA y 26 tablas. Flyway alcanza V309, con 40 migraciones base y 66 al incluir el perfil demo.

## Seguridad y manejo de errores

Las rutas de aceptaciones legales y reportes permanecen protegidas por Spring Security. El catálogo paginado es público porque solo expone herramientas publicadas. Las validaciones utilizan las excepciones de negocio existentes y el manejador global conserva respuestas `application/problem+json`.

Ejemplo abreviado de error:

```json
{
  "title": "Conflict",
  "status": 409,
  "detail": "Daily rate range is invalid",
  "code": "BUSINESS_CONFLICT",
  "instance": "/api/v1/public/catalog/page"
}
```

## Evidencia de pruebas

La validación ejecutada el 22 de octubre de 2026 produjo:

| Módulo | Pruebas aprobadas |
| --- | ---: |
| Domain | 29 |
| Application | 17 |
| Infrastructure | 75 |
| **Total** | **121** |

Las pruebas nuevas comprueban:

- normalización y reglas del agregado legal;
- respuesta y validaciones de la paginación;
- idempotencia de la aceptación y una sola fila persistida;
- cálculo de tres días de mora;
- aislamiento del reporte por rol;
- correspondencia de 26 entidades con 26 tablas;
- instalación limpia y actualización Flyway desde V200 hasta V309.

Prueba integrada principal: [`PostgresIntegrationTest`](../infrastructure/src/test/java/com/toolrental/infrastructure/PostgresIntegrationTest.java).

Comando reproducible:

```powershell
.\mvnw.cmd -pl infrastructure -am verify
```

## Demostración con Docker, Tomcat y Swagger

```powershell
docker compose up --build -d
docker compose ps
docker compose logs --tail 50 backend
```

Servicios locales:

- frontend: `http://localhost:5173`;
- backend sobre Apache Tomcat integrado: `http://localhost:8080`;
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`;
- especificación OpenAPI: `http://localhost:8080/v3/api-docs`.

En los logs debe aparecer `Apache Tomcat/10.1.55`, la aplicación de V309 y `Tomcat started on port 8080`.

## Orden recomendado para explicarlo

1. Mostrar que el cambio entra por un controlador REST pequeño.
2. Explicar que la regla y autorización están en Application o Domain, no en el controlador.
3. Señalar el puerto que evita acoplar el caso de uso a JPA o JDBC.
4. Mostrar el adaptador de persistencia y la migración Flyway.
5. Ejecutar la prueba integrada y abrir Swagger.
6. Probar un caso válido y uno inválido o sin permisos.

## Límites conocidos

- La paginación del catálogo se realiza en Application después de leer el catálogo publicado. Es adecuada para la carga académica actual; una evolución con millones de herramientas debe llevar filtros, conteo y `LIMIT/OFFSET` al puerto de persistencia.
- El backend ya persiste aceptaciones legales, pero el formulario del frontend todavía debe conectarse al nuevo `POST`.
- El reporte considera alquileres con estado `ACTIVE`; no reemplaza un proceso programado de notificaciones o cobro automático.
- Los medios de pago y comprobantes son académicos; no representan integración financiera ni factura electrónica DIAN.
