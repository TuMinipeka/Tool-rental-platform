# Plataforma de alquiler de herramientas

Backend para alquilar herramientas y equipos de construcción, construido con Java 21, arquitectura hexagonal y diseño guiado por el dominio. Las reglas de negocio se mantienen independientes de Spring, JPA, HTTP y los detalles de seguridad.

## Estado actual

Están implementados la estructura Maven multimódulo, los agregados de catálogo y reservas, los casos de uso, los puertos del dominio, los eventos, la persistencia JPA, PostgreSQL, Flyway y los adaptadores actuales. La base de datos local es reproducible y cuenta con pruebas reales de migraciones, mapeos, rollback y concurrencia.

Continúan pendientes las decisiones comerciales de la Fase 0, las ampliaciones correspondientes del esquema, las escrituras de identidad, REST/OpenAPI, autorización JWT, los adaptadores restantes, reportes, despliegue y el frontend React. La aplicación todavía no ofrece un servidor HTTP. Consulta la [hoja de ruta](docs/pendientes.md) y el [trabajo pendiente de base de datos](docs/pendientes-base-datos.md).

## Tecnologías

| Componente | Elección |
| --- | --- |
| Java | 21 |
| Spring Boot | 3.5.16 |
| Construcción | Maven multimódulo; Maven Wrapper 3.9.9 |
| Pruebas | JUnit 5, Spring Boot Test, ArchUnit y Testcontainers |
| Formato | Spotless 2.43.0 con Google Java Format 1.27.0 |
| Base de datos | PostgreSQL 17.11, Spring Data JPA y Flyway |
| API | REST con OpenAPI y Swagger UI; confirmada, no implementada |
| Frontend | React con herramientas de Node.js; confirmado, no creado |

El [enunciado original](ejercicioDemo.md) solicitaba MySQL. PostgreSQL lo reemplaza por decisión explícita del proyecto.

## Arquitectura

```text
domain/          # Agregados, entidades, objetos de valor, eventos y puertos
application/     # Comandos, respuestas, excepciones y casos de uso
infrastructure/  # Spring Boot, adaptadores JPA y migraciones Flyway
docs/            # Modelo, arquitectura, decisiones y trabajo pendiente
pom.xml          # Reactor Maven y versiones centralizadas
```

Las dependencias apuntan hacia el dominio:

```text
infrastructure -> application -> domain
```

El código se organiza primero por los contextos de negocio `catalog` y `reservation`, y después por responsabilidad técnica. Una tabla relacional no se convierte automáticamente en un agregado DDD.

Los identificadores de código están en inglés. Los tipos Java usan `PascalCase`, sus miembros usan `camelCase`, los paquetes usan minúsculas y PostgreSQL usa tablas plurales y columnas singulares en `snake_case`.

Consulta la [arquitectura del backend](docs/backend-architecture.md) y la [guía de desarrollo](docs/development-guide.md).

## Reglas de negocio implementadas

- Los agregados actuales son `Category`, `Tool`, `EquipmentUnit` y `Reservation`.
- Las imágenes pertenecen a una oferta de herramienta; cada unidad de equipo representa un activo físico.
- Una reserva contiene unidades distintas de un solo proveedor y usa una única moneda.
- Las fechas inicial y final son inclusivas. Compartir cualquier fecha produce un conflicto para la misma unidad.
- Las solicitudes pendientes no bloquean unidades. La confirmación vuelve a validar actor, perfiles activos, publicación, estado operativo, moneda y disponibilidad dentro de una frontera atómica.
- La tarifa diaria vigente se congela en los elementos de la reserva al confirmarla.
- El rechazo solo acepta reservas pendientes. La cancelación acepta reservas pendientes o confirmadas.
- Los identificadores de actor todavía son entradas de aplicación; la autenticación no está implementada.

## Requisitos locales

- JDK 21.
- Docker con Docker Compose para PostgreSQL y las pruebas de integración.
- Node.js se necesitará únicamente para el frontend cuando se cree.

El Maven Wrapper descarga Maven, pero no instala Java.

## Entorno local con Docker

Primero crea el archivo local de variables. `.env` contiene valores propios del equipo y no se versiona; `.env.example` sí se mantiene en Git.

Linux/macOS:

```bash
cp .env.example .env
docker compose --env-file .env up --build
```

PowerShell:

```powershell
Copy-Item .env.example .env
docker compose --env-file .env up --build
```

Cambia `POSTGRES_PASSWORD` y `DB_PASSWORD` por el mismo valor antes de levantar el entorno. Para iniciar únicamente PostgreSQL:

```bash
docker compose --env-file .env up -d postgres
```

El volumen `postgres_data` conserva la base entre reinicios. `docker compose down` detiene los servicios sin eliminar los datos; agregar `--volumes` también elimina la base local.

El contenedor del backend usa una construcción por etapas con JDK 21 y una imagen de ejecución JRE 21 sin usuario privilegiado. Actualmente ejecuta Flyway y arranca la aplicación sin publicar un puerto HTTP, porque la API aún no existe.

## Compilar y verificar

Windows:

```powershell
.\mvnw.cmd clean verify
java -jar infrastructure\target\infrastructure-0.1.0-SNAPSHOT.jar
```

Linux/macOS:

```bash
./mvnw clean verify
java -jar infrastructure/target/infrastructure-0.1.0-SNAPSHOT.jar
```

`verify` ejecuta las pruebas y valida el formato Java. Con Docker disponible, Testcontainers crea PostgreSQL 17.11 y verifica las migraciones, JPA, repositorios, rollback y concurrencia. Aplica el formato cuando sea necesario con:

```bash
./mvnw spotless:apply
```

El perfil predeterminado `local` conecta con `jdbc:postgresql://localhost:5432/tool_rental`. Sus valores pueden reemplazarse mediante `DB_URL`, `DB_USERNAME` y `DB_PASSWORD`. El perfil `production` exige esas tres variables.

Flyway crea 18 tablas mediante migraciones ordenadas y aplica una migración adicional de compatibilidad con JPA. El perfil `local` también carga 18 migraciones de demostración con al menos cinco filas por tabla; `production` nunca incluye esos datos. Hibernate usa `ddl-auto: validate` y no crea ni modifica el esquema.

Las tarifas demo y sus referencias públicas colombianas se registran en [Fuentes de datos](docs/data-sources.md).

## Documentación

1. [Enunciado original](ejercicioDemo.md)
2. [Modelo conceptual](docs/conceptual-model.md)
3. [Cuarta forma normal](docs/fourth-normal-form.md)
4. [Arquitectura del backend](docs/backend-architecture.md)
5. [Guía de desarrollo](docs/development-guide.md)
6. [Estructura de infraestructura](infrastructure/STRUCTURE.md)
7. [Trabajo pendiente de base de datos](docs/pendientes-base-datos.md)
8. [Hoja de ruta](docs/pendientes.md)
9. [Alcance del producto](docs/product-scope.md)
10. [Borrador de términos y condiciones](docs/terms-and-conditions.md)

`AGENTS.md` registra reglas para el trabajo automatizado. ArchUnit protege la dirección de dependencias y las jerarquías principales; Spotless protege el formato Java.

## Próxima entrega

La base técnica de la Fase 1 está terminada para el modelo actual. La siguiente prioridad es cerrar las reglas comerciales de la Fase 0 que permiten ampliar el esquema sin inventar valores. REST/OpenAPI y JWT siguen antes de integrar el frontend con el backend.

## Convención de commits

Todos los mensajes se escriben en inglés con Conventional Commits, un código Gitmoji y un resumen concatenado en estilo `CamelCase`:

```text
<type>: :<gitmoji>: <CamelCaseSummary>
```

Ejemplos:

```text
feat: :card_file_box: AddImplementationMigrations
fix: :bug: PreventOverlappingReservations
docs: :memo: DocumentDatabaseConventions
refactor: :recycle: RenameCatalogTypes
```
