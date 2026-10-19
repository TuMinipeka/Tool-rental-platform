# Arquitectura del backend

## Dependencias y organización

El reactor Maven conserva Java 21, Spring Boot 3.5.16 y los módulos `domain`, `application` e `infrastructure`, siguiendo [SpringBootDataJPA-DDD](https://github.com/TuMinipeka/SpringBootDataJPA-DDD).

```text
infrastructure → application → domain
```

Por decisión del proyecto hay 18 contextos organizados por tabla. Esta convención no establece que toda tabla sea un límite de negocio independiente. `common` reúne contratos compartidos y no constituye una tabla adicional. El [mapa de paquetes](source-package-map.md) permite navegar cada contexto.

```text
domain/<context>/{event,model/aggregate,model/entity,model/valueobject,port/repository}
application/<context>/{command,dto,exception,usecase}
infrastructure/<context>/adapters/in/rest/{controllers,dtos}
infrastructure/<context>/adapters/out/persistence/{entity,mapper,repository}
infrastructure/<context>/config
```

Solo se crean clases con responsabilidad real. Los adaptadores de puertos están directamente en `adapters/out/persistence`; `repository` contiene las interfaces Spring Data.

## Dominio y aplicación

Los agregados heredan `AggregateRoot`; los eventos implementan `DomainEvent`. Las factorías `restore` reconstruyen estado sin emitir eventos de creación. Los comandos y respuestas son inmutables. Los identificadores tipados mantienen claves UUID, numéricas, compartidas y compuestas. `Money` usa `BigDecimal` y moneda explícita; los tiempos técnicos provienen de un `Clock` inyectado.

`ApplicationException` es abstracta y tiene constructores protegidos de mensaje/causa. Las excepciones concretas se ubican en su contexto. Las reglas usan la jerarquía de dominio; la traducción HTTP pertenece a infraestructura.

`domain` depende de Java. `application` coordina casos de uso y puertos sin Spring, Hibernate ni HTTP. `UnitOfWork` permite transacciones implementadas por Spring en infraestructura. No se exponen entidades JPA al exterior.

## Agregados y operaciones coordinadas

Las líneas de reserva conservan identidad de unidad y tarifa. `reservationitem` tiene su propio mapeo y consulta, pero las escrituras se coordinan mediante la reserva. Lo mismo ocurre con `invoiceitem`: crear factura guarda encabezado y líneas de forma atómica. Los perfiles validan referencias y propiedad; los pagos actuales no llaman proveedores externos.

`AtomicReservationManagement` bloquea la reserva y los registros relacionados con un orden estable, vuelve a consultar unidades y tarifas, verifica publicación/perfiles/moneda/estado operativo y comprueba solapamientos inclusivos. Guarda confirmación y tarifas dentro de la misma transacción. Las solicitudes pendientes no ocupan equipos; cancelar libera disponibilidad antes de crear un alquiler. Una reserva con alquiler no se cancela mediante esta operación.

`AtomicEquipmentUnitRegistration` coordina inventario por proveedor; PostgreSQL refuerza la unicidad sin distinguir mayúsculas mediante un trigger y un bloqueo asesor de transacción. El código de inventario no es único globalmente para todos los proveedores.

Las devoluciones requieren entregas completadas. Finalizar un alquiler exige recibir todas las unidades. Facturas emitidas, tarifas confirmadas y devoluciones registradas carecen de edición genérica que destruya su historia.

## Persistencia

Hay 18 entidades JPA, una por tabla, con mappers separados y referencias escalares entre contextos. Las claves compuestas usan `@EmbeddedId` exclusivamente en infraestructura. Roles conservan `smallint`; clientes, proveedores y alquileres conservan claves compartidas. Se mantienen descripción de categoría, serie/condición de equipo y vencimiento de reserva.

Flyway conserva V1–V3 y ejecuta V4–V24 adaptadas del aporte de Conde. La demostración V101–V118 exige perfil `demo`. Hibernate valida el esquema. Consulta [historial y trazabilidad](database-migrations.md).

## REST y seguridad actual

Controladores delgados convierten DTOs HTTP en comandos. Las rutas privadas requieren un JWT Bearer cuyo `sub` es un UUID. Los casos de uso verifican usuario activo, rol, propiedad y participación; no aceptan un `actorId` arbitrario como autenticación. Registro, login, recuperación de contraseña y catálogo publicado tienen contratos públicos específicos.

El JWT HMAC expira en un periodo corto y no tiene renovación. Logout es stateless y la revocación persistente continúa pendiente. La recuperación devuelve el token firmado únicamente como mecanismo académico mientras no exista proveedor de correo. Las llamadas anónimas a rutas privadas reciben 401; acceso denegado 403; contrato inválido 400; referencia inexistente 404; conflicto de negocio/persistencia 409. No se devuelven hashes o errores SQL internos. La página raíz, `/status/database`, autenticación, catálogo publicado, OpenAPI y Swagger UI son públicos. Consulta [API REST](rest-api.md).

## Verificación y límites

ArchUnit comprueba dependencias y jerarquías. Las pruebas contrastan columnas y propietarios JPA, arranque PostgreSQL, actualización de Flyway, contratos HTTP, rollback y concurrencia. Testcontainers es la ruta predeterminada; una base exclusiva de pruebas permite ejecutar las mismas comprobaciones sin Docker.

No hay publicador externo de eventos ni Outbox. OpenAPI y Swagger UI describen el contrato actual. Renovación/revocación persistente de JWT, frontend funcional, impuestos, protección y otras ampliaciones comerciales siguen pendientes. Consulta [preparación de producción](production-readiness.md).
