# Guía de desarrollo y criterios de construcción

## Propósito

Esta guía es la fuente de verdad para extender Tool Rental Platform sin deformar su arquitectura. La estructura actual no es solo una organización de carpetas: expresa qué capa puede tomar decisiones, en qué dirección se conocen los componentes y dónde se protege cada regla del negocio.

La regla central es:

```text
infrastructure -> application -> domain
```

El dominio permanece en el centro y no conoce tecnologías externas. Aplicación coordina el negocio a través de puertos. Infraestructura conecta HTTP, Spring, PostgreSQL u otros servicios con esos contratos.

## Estructura que se debe conservar

```text
domain/src/main/java/com/toolrental/domain/
  common/
  <contexto>/
    model/aggregate/
    model/entity/          # Solo entidades internas de un agregado
    model/valueobject/
    event/
    port/repository/

application/src/main/java/com/toolrental/application/
  common/exception/
  <contexto>/
    command/
    dto/
    exception/
    usecase/

infrastructure/src/main/java/com/toolrental/infrastructure/
  common/
  <contexto>/
    adapters/in/rest/controllers/
    adapters/in/rest/dtos/
    adapters/out/persistence/entity/
    adapters/out/persistence/mapper/
    adapters/out/persistence/repository/
    config/
```

Los contextos actuales son `catalog` y `reservation`. Un contexto nuevo se crea porque existe una capacidad coherente del negocio, no porque aparezca una tabla. Tampoco se crean paquetes vacíos “por si acaso”; la excepción son los `.gitkeep` de adaptadores ya planificados.

## Responsabilidad de cada capa

### Domain

Contiene el lenguaje y las reglas del negocio:

- agregados que protegen invariantes y transiciones válidas;
- entidades que solo tienen sentido dentro de su agregado;
- value objects inmutables e IDs tipados;
- eventos que describen hechos ya ocurridos;
- puertos que expresan lo que el negocio necesita del exterior;
- excepciones de reglas de negocio.

No se permiten anotaciones o dependencias de Spring, JPA, JSON, HTTP o PostgreSQL. El dominio no debe saber cómo se almacena ni cómo se expone un objeto. Las colecciones se copian y se entregan como valores inmutables; no se agregan setters genéricos.

### Application

Contiene los casos de uso. Su trabajo es cargar información mediante puertos, comprobar autorización y referencias, invocar comportamiento del dominio y coordinar una unidad atómica de trabajo.

- `command`: entrada inmutable de una operación que modifica estado;
- `dto`: respuesta inmutable y estable para el exterior;
- `usecase`: orquestación de una intención del usuario;
- `exception`: fallos de aplicación, como una referencia inexistente.

Un caso de uso no conoce controladores, entidades JPA ni repositorios Spring Data. Tampoco devuelve agregados para que otra capa los modifique.

### Infrastructure

Contiene todos los detalles reemplazables:

- controladores y DTO del contrato HTTP;
- entidades JPA y mappers;
- implementaciones de los puertos;
- configuración de Spring y ensamblado de dependencias;
- integración con base de datos o servicios externos.

Los controladores deben ser delgados. Un mapper de persistencia usa las fábricas `restaurar` y no debe volver a producir eventos de creación al leer datos existentes.

## Convenciones de código

| Elemento | Convención | Ejemplo |
| --- | --- | --- |
| Paquete base | `com.toolrental.<modulo>.<contexto>` | `com.toolrental.application.catalog` |
| Caso de uso | verbo de intención + `UseCase` | `ConfirmarReservaUseCase` |
| Comando | verbo de intención + `Command` | `SolicitarReservaCommand` |
| Respuesta | concepto + `Response` | `ReservaResponse` |
| Puerto de datos | concepto + `Repository` | `HerramientaRepository` |
| Adaptador HTTP | recurso + `Controller` | `ReservaController` |
| Configuración | contexto + `Configuration` | `ReservaBeanConfiguration` |

Se usa Java 21. Se prefiere inmutabilidad, composición, clases `final` cuando no están diseñadas para herencia, records para datos y dependencias explícitas por constructor. El formato Java lo define Google Java Format mediante Spotless; no se ajusta manualmente un estilo distinto.

Los nombres del negocio permanecen en español. Los nombres técnicos que ya forman parte de la estructura (`usecase`, `repository`, `mapper`, `dto`) se mantienen para no mezclar convenciones.

## Decisiones que requieren cuidado especial

### Consistencia transaccional y concurrencia

Consultar disponibilidad y guardar una confirmación forman una sola operación. `GestionReservaAtomica` existe para evitar que dos solicitudes confirmen la misma unidad al mismo tiempo. El adaptador PostgreSQL deberá bloquear o versionar de manera explícita y mantener un orden estable de bloqueo. Una prueba con repositorios en memoria no sustituye una prueba de concurrencia real.

### Dinero, fechas y tiempo

- Los importes se representan con `Dinero`, `BigDecimal` y moneda explícita; no se usa `double`.
- Una reserva usa `LocalDate` y extremos inclusivos. Cualquier día compartido significa solapamiento.
- Las marcas técnicas usan `Instant` y un `Clock` inyectado para que las pruebas sean deterministas.
- No se introduce conversión de moneda, zona horaria, impuestos o redondeo implícito sin una decisión de negocio documentada.

### Persistencia

Las entidades JPA no son el modelo de dominio. Deben vivir en infraestructura y transformarse mediante mappers. El esquema cambia mediante Flyway y se valida al arrancar; las restricciones de unicidad y referencia importantes también deben existir físicamente en PostgreSQL.

### Seguridad

Las comprobaciones de propietario y rol siguen perteneciendo al caso de uso. Cuando exista JWT, el controlador/adaptador obtendrá la identidad del principal autenticado. Un campo `actorId` enviado libremente por el cliente no constituye autenticación.

### Eventos

Los eventos de dominio registran hechos del agregado. Actualmente no existe publicación externa ni Outbox. Si se incorpora, debe garantizarse que guardar el agregado y registrar el evento para publicación formen una unidad consistente.

## Flujo para implementar una funcionalidad

1. Describir la intención y las reglas en términos del negocio.
2. Identificar el agregado responsable; evitar lógica repartida entre controladores y repositorios.
3. Implementar o ajustar value objects, comportamiento y eventos en `domain`.
4. Definir en el dominio solo los puertos externos realmente necesarios.
5. Crear el comando, la respuesta, las excepciones y el caso de uso en `application`.
6. Implementar adaptadores en `infrastructure` sin filtrar tipos JPA/HTTP hacia adentro.
7. Añadir pruebas unitarias de reglas, pruebas de aplicación y, cuando aplique, pruebas de integración y concurrencia.
8. Actualizar documentación y ejecutar la verificación completa.

## Lista de comprobación antes de terminar

- [ ] La nueva clase está en el módulo, contexto y paquete correctos.
- [ ] Las dependencias siguen apuntando hacia el dominio.
- [ ] Las reglas están en el agregado/value object o se coordinan en el caso de uso apropiado.
- [ ] No se expusieron colecciones mutables, entidades JPA ni detalles HTTP.
- [ ] Dinero, fechas, reloj, autorización y concurrencia se trataron explícitamente cuando aplican.
- [ ] Hay pruebas del comportamiento nuevo y de sus errores importantes.
- [ ] La documentación refleja el estado real, incluidos los pendientes.
- [ ] `mvnw clean verify` termina correctamente.

## Protección automática

`ArchitectureTest` verifica la dirección de dependencias y las jerarquías principales ya definidas. Spotless comprueba el formato Java durante `verify`. Estas barreras detectan desviaciones comunes, pero no reemplazan la revisión de los límites del negocio ni de la consistencia transaccional.

