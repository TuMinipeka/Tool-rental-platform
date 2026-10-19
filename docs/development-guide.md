# Guía de desarrollo

## Fuente de decisiones

La estructura sigue la [arquitectura](backend-architecture.md). El [alcance aprobado](product-scope.md) y la [hoja de ruta](pendientes.md) gobiernan reglas comerciales pendientes. No inventes impuestos, precios de protección, deducibles ni condiciones de pago sin una decisión documentada.

## Responsabilidades por capa

| Capa | Contenido | Dependencias permitidas |
| --- | --- | --- |
| Domain | Agregados, entidades internas, objetos de valor, eventos, excepciones y puertos | Java |
| Application | Comandos/respuestas inmutables, excepciones y coordinación de casos de uso | Domain y Java |
| Infrastructure | REST, DTOs HTTP, JPA, mappers, adaptadores y configuración | Application, Domain y frameworks |

Conserva tres módulos y los 18 contextos existentes. Las entidades hijas no ofrecen operaciones que evadan invariantes de reserva/factura. No crees clases vacías para completar un árbol.

## Convenciones

Los nombres de archivos, paquetes, tipos, comentarios y mensajes del código permanecen en inglés. La documentación humana está en español. Paquete raíz: `com.toolrental`. Los nombres de tablas son plurales naturales en `snake_case`; columnas en singular. Java usa `PascalCase` para tipos y `camelCase` para miembros.

| Responsabilidad | Ejemplo |
| --- | --- |
| Caso de uso | `ConfirmReservationUseCase` |
| Entrada | `RequestReservationCommand` |
| Salida | `ReservationResponse` |
| Puerto | `ToolRepository` |
| Controlador | `ReservationController` |
| Configuración | `ReservationBeanConfiguration` |

Los commits se escriben en inglés: `<type>: :<gitmoji>: <CamelCaseSummary>`. Ejemplo: `feat: :card_file_box: IntegrateCondeForwardMigrations`.

## Reglas técnicas

- Protege invariantes dentro del agregado/objeto de valor; evita setters genéricos y colecciones mutables expuestas.
- Usa identificadores tipados, `Money`/`BigDecimal`, moneda explícita y `LocalDate` inclusivas para reservas. Usa `Instant` y `Clock` para tiempos técnicos.
- Mantén comprobación y confirmación de disponibilidad en una transacción; conserva el orden de bloqueos.
- Restaura agregados sin emitir eventos nuevos. Aún no existe Outbox ni publicación externa.
- Separa entidades JPA y mappers; no expongas Spring Data fuera de infraestructura.
- Las interfaces Spring Data van en `repository`; el adaptador del puerto queda en su directorio padre `persistence`.
- Obtén el actor del `Principal` creado desde el `sub` del JWT y valida permisos en el caso de uso. La renovación y revocación persistente de sesiones siguen pendientes.
- Evoluciona PostgreSQL mediante nuevas versiones Flyway; nunca edites archivos ya aplicados. Consulta [migraciones](database-migrations.md).

## Flujo de implementación

1. Describe intención y reglas de negocio.
2. Identifica agregado responsable y límites transaccionales.
3. Implementa comportamiento, objetos de valor y eventos en dominio.
4. Añade puertos estrictamente necesarios y el caso de uso en aplicación.
5. Incorpora REST/JPA y configuración sin filtrar detalles hacia dentro.
6. Prueba reglas, errores, rollback y concurrencia cuando correspondan.
7. Actualiza documentación y ejecuta `clean verify` con JDK 21.

## Revisión antes de integrar

Comprueba ubicación de tipos, dirección de dependencias, invariantes, inmutabilidad, referencias, permisos, claves y nombres SQL. Ejecuta [compilación y pruebas](build-and-test.md); no sustituyas persistencia real por dobles en memoria para acreditar concurrencia. Coordina versiones de migración con el otro desarrollador.

## Entorno local y editor

El perfil `local` usa `infrastructure/src/main/resources/application-local.yml`; las variables de entorno pueden reemplazar sus valores. Su pool predeterminado es 2/0. Nunca publiques contraseñas. El perfil `production` exige variables externas.

SQLSTATE 53300 significa que el rol agotó sus conexiones. Revisa sesiones de clientes como DBeaver y el límite del rol; no termines sesiones de otros proyectos. Reducir el pool no libera sesiones abiertas por otro proceso.

Java usa LF; `.cmd` usa CRLF. Para el error Spotless ejecuta `spotless:apply` y `spotless:check`. Si Maven compila pero el editor no presenta paquetes, reconstruye su índice Java. `catalog` e `identity` se distribuyeron entre los contextos actuales: consulta [el mapa de archivos](source-package-map.md).

Un puerto PostgreSQL abierto no demuestra integración: se requiere conexión, migración/validación Flyway, validación Hibernate y arranque correcto.
