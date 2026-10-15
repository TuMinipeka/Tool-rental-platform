# Tool Rental Platform

Backend de renta de herramientas y equipos de construcción, con Java 21, arquitectura hexagonal y DDD. El proyecto prioriza las reglas del negocio y mantiene el dominio independiente de Spring, JPA y seguridad.

## Estado actual

Implementados: estructura Maven multimódulo, cuatro agregados de catálogo y reservas, casos de uso, puertos y pruebas automatizadas. Spring Boot arranca sin base de datos y sin servidor HTTP. Los casos de uso se prueban con adaptadores en memoria ubicados únicamente en `src/test`; todavía no están ensamblados como servicios de producción.

**Pendientes:** adaptadores JPA/PostgreSQL, migraciones Flyway, API REST, identidad y seguridad JWT, alquileres, pagos, facturación y frontend. No existen endpoints de negocio ni persistencia durable en esta entrega.

## Tecnologías

| Componente | Elección |
| --- | --- |
| Java | 21 |
| Spring Boot | 3.5.16 |
| Maven Wrapper | Maven 3.9.9; wrapper 3.3.4, distribución only-script |
| Pruebas | JUnit 5, Spring Boot Test, ArchUnit |
| Formato | Spotless 2.43.0 y Google Java Format 1.27.0 |
| Base de datos prevista | PostgreSQL, por decisión del usuario del 14 de octubre de 2026 |

El [enunciado original](ejercicioDemo.md) menciona MySQL y se conserva intacto. PostgreSQL sustituye ese requisito por decisión explícita; todavía no se fija su versión ni se incluyen controladores JDBC.

## Arquitectura

Estructura basada en [SpringBootDataJPA-DDD](https://github.com/TuMinipeka/SpringBootDataJPA-DDD):

```text
domain/          # Agregados, entidades internas, value objects, eventos y puertos
application/     # Comandos, respuestas, excepciones y casos de uso
infrastructure/  # Arranque Spring Boot; próximos adaptadores y configuración
docs/            # Modelo conceptual, normalización y arquitectura
pom.xml          # Reactor y versiones centralizadas
```

Dirección de dependencias: `infrastructure → application → domain`, con acceso de infraestructura a los puertos del dominio. `domain` y `application` solo usan Java y el módulo interior correspondiente; sus dependencias de pruebas tienen alcance `test`.

Los paquetes raíz son `com.toolrental.domain`, `com.toolrental.application` y `com.toolrental.infrastructure`, organizados por `catalog`, `reservation` y conceptos comunes. Una tabla relacional no equivale a un agregado. [Ver responsabilidades y contratos](docs/arquitectura-backend.md).

## Reglas implementadas

- Agregados: `Categoria`, `Herramienta`, `UnidadEquipo` y `Reserva`.
- Las imágenes pertenecen a la herramienta; cada unidad representa un equipo físico.
- Una reserva requiere unidades distintas, de un único proveedor y moneda común.
- Las fechas de inicio y devolución son inclusivas: del 14 al 15 se cobran dos días; del 14 al 14, uno. Compartir cualquier fecha genera conflicto entre reservas confirmadas de la misma unidad.
- Las solicitudes pendientes no bloquean unidades. El proveedor confirma; la aplicación verifica actor, perfiles, estado operativo, publicación, moneda y disponibilidad dentro de un puerto atómico.
- La tarifa vigente al confirmar se congela en las líneas; cambios posteriores del catálogo no alteran el precio acordado. El precio de una solicitud pendiente es estimado.
- Rechazar solo admite pendientes. Cancelar admite pendientes o confirmadas. Los estados terminales rechazan nuevas transiciones.
- Las identidades de actores son entradas de aplicación: aún no hay autenticación. Se mantiene la decisión de múltiples roles y `UsuarioRol` para la futura implementación de identidad.

## Preparar Java 21

```powershell
git clone https://github.com/TuMinipeka/Tool-rental-platform.git
cd Tool-rental-platform
java -version
mvn -version
```

Durante la implementación, Maven utilizó Java 21, pero el `java` del PATH apuntaba a Java 17. Para alinear esta sesión de PowerShell con la instalación local comprobada:

```powershell
$env:JAVA_HOME = 'C:\Users\danie\scoop\apps\temurin21-jdk\current'
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
java -version
.\mvnw.cmd -version
```

En otra máquina, reemplazar `JAVA_HOME` por la ruta real del JDK 21. El wrapper descarga Maven en el primer uso; no instala Java. No se modifica la configuración global del equipo.

## Compilar, verificar y arrancar

Desde la raíz, con JDK 21:

```powershell
.\mvnw.cmd clean verify
java -jar infrastructure\target\infrastructure-0.1.0-SNAPSHOT.jar
```

En Linux/macOS: `./mvnw clean verify` y `java -jar infrastructure/target/infrastructure-0.1.0-SNAPSHOT.jar`.

El proceso inicia el contexto Spring, escribe el mensaje `Started ToolRentalApplication` y termina normalmente al no tener servidor ni tareas de fondo. No requiere PostgreSQL, credenciales ni `application.properties` local. No ofrece aún una consola interactiva de negocio.

`verify` ejecuta pruebas y comprueba formato Java. Para aplicar el formato tras editar código:

```powershell
.\mvnw.cmd spotless:apply
```

Las pruebas cubren precios y fechas inclusivas, estados, referencias, disponibilidad, tarifas congeladas, ausencia de escrituras parciales, dirección de dependencias y arranque sin DataSource. Las pruebas en memoria no demuestran bloqueos, integridad ni concurrencia de PostgreSQL.

## Documentación y siguientes entregas

1. [Modelo conceptual](docs/modelo-conceptual.md): entidades, atributos, cardinalidades y decisiones de negocio.
2. [Normalización hasta 4FN](docs/normalizacion-4fn.md): esquema lógico de 18 relaciones; todavía no implementado físicamente.
3. [Arquitectura del backend](docs/arquitectura-backend.md): límites de agregados, casos de uso y contratos transaccionales.
4. [Guía de desarrollo](docs/guia-desarrollo.md): estructura obligatoria, responsabilidades por capa, convenciones y lista de comprobación para cambios futuros.

La estructura descrita en la guía es una regla del proyecto. `AGENTS.md` la convierte en instrucciones para futuras tareas automatizadas. Las fronteras de dependencia existentes se comprueban con `ArchitectureTest` y el formato Java con Spotless.

Siguiente entrega: PostgreSQL, Flyway, mapeos JPA separados del dominio, `ddl-auto: validate`, implementación de puertos y pruebas de integración y concurrencia. Después: seguridad JWT y autorización por rol y propietario. Las tecnologías del frontend se acordarán posteriormente. No se conecta un procesador de pagos en esta etapa.

La skill [database-normalization](.agents/skills/database-normalization/SKILL.md), instalada desde [cookiemonsterdev/agents-skills](https://github.com/cookiemonsterdev/agents-skills), sustenta el análisis relacional; su origen y licencia se conservan con `skills-lock.json`.

## Commits

Formato: `feat: :gitmoji: descripciónEnCamelCase`, por ejemplo:

```text
feat: :building_construction: crearEstructuraHexagonalJava21
feat: :sparkles: implementarDominioCatalogoYReservas
feat: :memo: documentarArquitecturaYReglasDeNegocio
```
