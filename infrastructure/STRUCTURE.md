# Alineación con SpringBootDataJPA-DDD

Referencia inspeccionada: [TuMinipeka/SpringBootDataJPA-DDD, rama main](https://github.com/TuMinipeka/SpringBootDataJPA-DDD). Esta revisión modifica exclusivamente los módulos `application`, `domain` e `infrastructure`; conserva los cuatro agregados y las reglas existentes. REST/JPA son únicamente una plantilla por indicación del usuario.

## Correspondencias

| Referencia | Proyecto de renta | Decisión |
| --- | --- | --- |
| `application/common/exception/ApplicationException` | Mismo paquete relativo y clase abstracta | Constructores protegidos para mensaje y mensaje/causa. |
| `application/country/exception/CountryNotFoundApplicationException` | `application/catalog/exception/*NotFoundApplicationException` y `application/reservation/exception/*NotFoundApplicationException` | Excepciones por recurso, con identidad tipada y causa opcional. Se elimina RecursoNoEncontradoException. |
| `application/<context>/{command,dto,usecase}` | `application/{catalog,reservation}/{command,dto,usecase}` | Se mantienen los contextos actuales; no se introduce un contexto por tabla ni CRUD adicional. |
| `domain/common/event/DomainEvent` | Mismo paquete relativo e interfaz | Se mantiene `Instant` y `Clock` inyectado en vez de volver al tiempo local implícito. Método común `occurredOn()`. |
| `domain/common/model/AggregateRoot` | Mismo paquete relativo y clase abstracta | Registro protegido, consulta inmutable y limpieza de eventos. Los cuatro agregados la extienden. |
| `domain/<context>/event` | `domain/catalog/event` y `domain/reservation/event` | Eventos concretos de registro y transiciones de reserva, con validación de identidad y fecha. |
| Fábricas `register`/`restore` | Fábricas `registrar`/`restaurar` en los cuatro agregados | Restaurar conserva identidad/estado y no registra eventos. |
| `domain/<context>/model/{aggregate,valueobject}` y `port/repository` | Misma organización relativa | DetalleReserva conserva `model/entity` porque es entidad interna y no una raíz independiente. |
| `infrastructure/<context>/config/*BeanConfiguration` | `catalog/config/CatalogBeanConfiguration` y `reservation/config/ReservaBeanConfiguration` | Ensamblan casos de uso por constructor cuando sus puertos están registrados; ClockConfiguration ofrece un reloj sustituible. |
| `infrastructure/<context>/adapters/in/rest/{controllers,dtos}` | Mismos paquetes relativos en catálogo y reservas | Solo `.gitkeep`; no hay controladores ni DTO HTTP. |
| `infrastructure/<context>/adapters/out/persistence/{entity,mapper,repository}` | Mismos paquetes relativos en catálogo y reservas | Solo `.gitkeep`; no hay entidades JPA, mappers ni Spring Data. |

## Jerarquías de errores y diferencias intencionales

`ApplicationException` reproduce el patrón de la referencia. Las excepciones concretas de aplicación no se colocan en `common`: corresponden al recurso de catálogo o reservas. Cliente y proveedor siguen siendo referencias tipadas; sus excepciones viven en la funcionalidad que las utiliza, sin implementar módulos de identidad.

En dominio, `DomainException` agrupa las reglas de negocio y `ReglaNegocioException` la extiende. En infraestructura, `InfrastructureException` y `PersistenceException` reservan la clasificación de fallos técnicos y conservan su causa. Estas dos jerarquías son una adaptación explícita: la referencia inspeccionada no define esas bases, y no se atribuyen a ella. No hay manejadores HTTP ni traducción de fallos de una base de datos todavía.

La estructura conserva eventos y excepciones como paquetes hermanos de `common`; no se anidan excepciones dentro de `common.event`. No se copia código clínico, credenciales ni versiones de dependencias del repositorio de referencia.

## Configuración y alcance operativo

Las configuraciones por funcionalidad usan `@Bean` y dependen de los puertos del dominio. Sus condiciones no crean repositorios ficticios: sin adaptadores solo se configura el reloj, por lo que el arranque sigue funcionando sin base de datos. Las pruebas registran los puertos antes de evaluar las configuraciones y verifican los ocho casos de uso ensamblados. Al implementar los adaptadores, su configuración deberá registrarse antes de estas configuraciones condicionales; la prueba de ensamblado deberá ampliarse a los adaptadores reales.

La creación de categorías, herramientas y unidades, y las transiciones de reserva reciben `Clock` explícito. El puerto atómico de registro de unidades también recibe el reloj. No se publica ningún evento externamente: `domainEvents()` es una colección de eventos pendientes, y `clearDomainEvents()` permite limpiarla. `Reserva.copia()` aísla el estado y no vuelve a emitir eventos anteriores; `confirmacion()` conserva el hecho histórico aunque se limpien los eventos pendientes.

Las carpetas vacías REST y JPA se preservan con `.gitkeep` porque Git no registra directorios vacíos. No incluyen código de implementación ni dependencias nuevas. El futuro adaptador de persistencia se ubicará directamente en `adapters/out/persistence`, junto a los subpaquetes preparados.

## Verificación

Desde la raíz, con Java 21: `mvnw.cmd clean verify` (Windows) o `./mvnw clean verify` (Linux/macOS). Las pruebas cubren las reglas previas, jerarquías por capa, identidad/causa en excepciones, herencia de agregados, contratos de eventos, restauración sin repetición de eventos, arranque sin DataSource y ensamblado de casos de uso con puertos de prueba. No validan REST ni persistencia JPA, que siguen sin implementar.

Resultado verificado: BUILD SUCCESS, 36 pruebas, cero fallos y cero errores. Se comprobó además que todos los cambios pertenecen a los tres módulos y que la plantilla REST/JPA contiene únicamente diez marcadores `.gitkeep` vacíos.
