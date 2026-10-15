# Reglas de trabajo del repositorio

Estas instrucciones aplican a todo el repositorio. Antes de modificar código, leer
`docs/guia-desarrollo.md` y `docs/arquitectura-backend.md`.

## Arquitectura obligatoria

- Conservar los módulos Maven `domain`, `application` e `infrastructure`.
- Mantener la dirección de dependencias `infrastructure -> application -> domain`.
- `domain` solo puede depender de Java. No introducir Spring, JPA, HTTP ni detalles de base de datos.
- `application` puede depender de `domain` y Java, nunca de `infrastructure` ni de frameworks.
- Los puertos pertenecen al dominio; sus adaptadores pertenecen a infraestructura.
- Organizar primero por contexto (`catalog`, `reservation` y los que se incorporen) y después por responsabilidad técnica.
- No crear un agregado, repositorio o caso de uso por cada tabla. Los límites los define el negocio.

## Ubicación y nombres

```text
domain/<contexto>/model/{aggregate,entity,valueobject}
domain/<contexto>/{event,port/repository}
application/<contexto>/{command,dto,usecase,exception}
infrastructure/<contexto>/adapters/in/rest/{controllers,dtos}
infrastructure/<contexto>/adapters/out/persistence/{entity,mapper,repository}
infrastructure/<contexto>/config
```

- Usar los sufijos `UseCase`, `Command`, `Response`, `Repository`, `Controller` y `Configuration` de forma consistente.
- Mantener el paquete raíz `com.toolrental` y los nombres del negocio en español. Los nombres técnicos establecidos (`usecase`, `repository`, `mapper`) se conservan.
- No añadir directorios, abstracciones o clases vacías salvo marcadores `.gitkeep` de una estructura ya acordada.

## Diseño y reglas de negocio

- Encapsular invariantes en agregados y value objects; no exponer setters genéricos ni colecciones mutables.
- Usar IDs tipados, `BigDecimal` mediante `Dinero`, `LocalDate` para períodos e `Instant` con un `Clock` inyectado para marcas de tiempo.
- Los casos de uso coordinan; no duplican reglas internas del agregado ni conocen HTTP/JPA.
- Entradas y salidas de aplicación son objetos inmutables. No devolver entidades JPA ni agregados mutables por la API.
- Toda operación que combine comprobar disponibilidad y guardar debe conservar un límite atómico. No separar esas acciones en llamadas vulnerables a concurrencia.
- Las fábricas `restaurar` reconstruyen persistencia sin generar nuevos eventos de dominio.
- El actor de una operación HTTP vendrá del principal autenticado; nunca se confiará en un ID de actor libre enviado en el cuerpo.

## Infraestructura y persistencia

- Mantener separadas las entidades JPA, los mappers y los repositorios adaptadores.
- Los cambios de esquema se realizan con migraciones Flyway versionadas; no usar generación destructiva de esquema.
- Reforzar en PostgreSQL las invariantes que correspondan: claves, unicidad, referencias y control de concurrencia.
- Los controladores deben ser delgados: validar el contrato HTTP, traducirlo a un comando, ejecutar un caso de uso y mapear la respuesta/error.
- Registrar dependencias con configuración por contexto e inyección por constructor.

## Calidad y documentación

- Por cada comportamiento nuevo, añadir pruebas en la capa que lo posee. Para persistencia o concurrencia, añadir pruebas de integración reales.
- Antes de terminar ejecutar `mvnw.cmd clean verify` en Windows o `./mvnw clean verify` en Linux/macOS.
- Aplicar formato con `mvnw.cmd spotless:apply` o `./mvnw spotless:apply` cuando sea necesario.
- Actualizar la documentación si cambia una regla, contrato, estructura, tecnología o estado real de implementación. No documentar como terminado algo que aún sea una plantilla.
- Conservar el formato de commits indicado en `README.md`.

