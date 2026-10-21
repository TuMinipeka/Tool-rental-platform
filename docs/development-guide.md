# Guía de desarrollo

## Fuente de decisiones

La estructura sigue la [arquitectura](backend-architecture.md). El [alcance aprobado](product-scope.md) y la [hoja de ruta](pendientes.md) gobiernan reglas comerciales pendientes. No inventes impuestos, precios de protección, deducibles ni condiciones de pago sin una decisión documentada.

## Responsabilidades por capa

| Capa | Contenido | Dependencias permitidas |
| --- | --- | --- |
| Domain | Agregados, entidades internas, objetos de valor, eventos, excepciones y puertos | Java |
| Application | Comandos/respuestas inmutables, excepciones y coordinación de casos de uso | Domain y Java |
| Infrastructure | REST, DTOs HTTP, JPA, mappers, adaptadores y configuración | Application, Domain y frameworks |

Conserva tres módulos y los 25 contextos existentes. Las entidades hijas no ofrecen operaciones que evadan invariantes de reserva, terminación, extensión, reembolso o factura. No crees clases vacías para completar un árbol.

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
- Obtén el actor del `Principal` creado desde el `sub` del JWT y valida permisos en el caso de uso. La sesión persistente asociada al `sid` debe continuar activa.
- Evoluciona PostgreSQL mediante nuevas versiones Flyway; nunca edites archivos ya aplicados. Consulta [migraciones](database-migrations.md).

## Flujo de implementación

Durante la preparación de la presentación, cada funcionalidad de usuario debe incluir su recorrido frontend y quedar aplicada en la web local. Se priorizan pruebas críticas de las rutas afectadas y la persistencia real cuando corresponda; no se repite por rutina toda la batería de Java, navegador y auditorías. La verificación amplia se reserva para cambios que la necesiten o una petición explícita. Los cambios de frontend deben reconstruirse en el servicio activo antes de entregarse.

1. Describe intención y reglas de negocio.
2. Identifica agregado responsable y límites transaccionales.
3. Implementa comportamiento, objetos de valor y eventos en dominio.
4. Añade puertos estrictamente necesarios y el caso de uso en aplicación.
5. Incorpora REST/JPA y configuración sin filtrar detalles hacia dentro.
6. Prueba reglas, errores, rollback y concurrencia cuando correspondan.
7. Actualiza documentación, ejecuta las comprobaciones críticas y reconstruye el servicio modificado. Usa JDK 21 y `clean verify` cuando el alcance del cambio de backend lo requiera.

## Revisión antes de integrar

Comprueba ubicación de tipos, dirección de dependencias, invariantes, inmutabilidad, referencias, permisos, claves y nombres SQL. Ejecuta [compilación y pruebas](build-and-test.md); no sustituyas persistencia real por dobles en memoria para acreditar concurrencia. Coordina versiones de migración con el otro desarrollador.

## Entorno local y editor

La skill de diseño de interfaces `emil-design-eng` está instalada para Codex en `.agents/skills/emil-design-eng/SKILL.md`, con su origen fijado en `skills-lock.json`. Úsala en tareas de diseño, interacción y animación del frontend; no cambia las reglas del backend ni instala dependencias de ejecución.

Para rendimiento y QA del frontend están instaladas `performance`, `web-quality-audit`, `vercel-react-best-practices` y `webapp-testing`. La prioridad es medir antes de editar y repetir las mismas condiciones después. El procedimiento, las métricas y los límites de la validación están en [la auditoría del frontend](frontend-quality.md).

El perfil `local` usa `infrastructure/src/main/resources/application-local.yml`; las variables de entorno pueden reemplazar sus valores. Su pool predeterminado es 2/0. Nunca publiques contraseñas. El perfil `production` exige variables externas.

SQLSTATE 53300 significa que el rol agotó sus conexiones. Revisa sesiones de clientes como DBeaver y el límite del rol; no termines sesiones de otros proyectos. Reducir el pool no libera sesiones abiertas por otro proceso.

Java usa LF; `.cmd` usa CRLF. Para el error Spotless ejecuta `spotless:apply` y `spotless:check`. Si Maven compila pero el editor no presenta paquetes, reconstruye su índice Java. `catalog` e `identity` se distribuyeron entre los contextos actuales: consulta [el mapa de archivos](source-package-map.md).

Abre como carpeta de VS Code la raíz real `Tool-rental-platform`, que contiene el POM agregador. El archivo compartido `.vscode/settings.json` permite que la extensión Java actualice automáticamente la configuración Maven. Después ejecuta **Java: Clean Java Language Server Workspace** y vuelve a importar el proyecto si el editor conserva errores de imports que Maven no reproduce. Usa **Java: Configure Java Runtime** para asociar JavaSE-21 a tu JDK 21 local; no se versiona una ruta absoluta de otra máquina. La comprobación de paquetes y saltos de línea está en `ArchitectureTest`.

Un puerto PostgreSQL abierto no demuestra integración: se requiere conexión, migración/validación Flyway, validación Hibernate y arranque correcto.

## Integración y publicación MVP

Consulta [MVP](mvp.md), [contratos por rol](integration-contracts.md), [despliegue](deployment.md) y [reglas visuales consolidadas](DESIGN.md). La composición de validación usa proyecto, base y puertos separados. No restaures skills eliminadas por el usuario ni incluyas esas eliminaciones en un commit de otra entrega.
