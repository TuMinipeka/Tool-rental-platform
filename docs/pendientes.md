# Pendientes y orden de trabajo

## Objetivo

Este documento define qué está confirmado, qué falta decidir y en qué orden conviene avanzar. El objetivo es evitar que una capa dependa de contratos inestables: el frontend puede diseñarse temprano, pero no debe integrarse hasta que el alcance y la API del backend estén definidos y funcionando.

## Stack tecnológico

### Confirmado

| Área | Tecnología | Estado |
| --- | --- | --- |
| Lenguaje backend | Java 21 | Configurado |
| Framework backend | Spring Boot 3.5.16 | Configurado |
| Construcción | Maven multi-módulo | Configurado |
| Arquitectura | Hexagonal, DDD y dependencia `infrastructure -> application -> domain` | Implementada |
| Base de datos | PostgreSQL | Esquema y configuración preparados; instancia pendiente |
| Persistencia | Spring Data JPA y Hibernate con `ddl-auto: validate` | Implementada para catálogo y reservas |
| Migraciones | Flyway | Dos migraciones disponibles |
| Datos de desarrollo | SQL manual con cinco `INSERT` por tabla | Preparado |
| Pruebas actuales | JUnit 5, Spring Boot Test y ArchUnit | Disponibles para dominio, aplicación y arquitectura |
| Formato | Spotless con Google Java Format | Configurado |

### Pendiente de decisión

| Área | Decisión necesaria |
| --- | --- |
| Alcance MVP | Decidir si la primera entrega termina en reserva o incluye alquiler, entrega, devolución, daños, pagos y facturación. |
| API | Aprobar REST, formato de errores, versionado y contrato OpenAPI. |
| Seguridad | Aprobar Spring Security, estrategia JWT, renovación de tokens y recuperación de cuenta. |
| Frontend | Elegir framework, lenguaje, manejo de estado, librería visual y estrategia de pruebas. |
| Despliegue | Elegir proveedor, contenedores, registro de imágenes, CI/CD, dominios y manejo de secretos. |
| Observabilidad | Elegir métricas, logs, trazas y alertas. |

El stack de base de datos y backend central está claro. El stack completo todavía no está cerrado porque frontend, seguridad y despliegue requieren decisiones del equipo antes de implementarse.

## Orden recomendado

### Fase 0: alcance del producto

Definir primero:

- usuarios y roles del MVP;
- recorridos completos de cliente, proveedor y administrador;
- funcionalidades obligatorias y funcionalidades posteriores;
- reglas de cancelación, pagos, daños y facturación que todavía no estén cerradas;
- criterios de aceptación y prioridades.

Se puede avanzar cuando existe un backlog priorizado y todos entienden qué incluye la primera entrega. Durante esta fase el frontend puede producir wireframes, pero no integraciones.

### Fase 1: base de datos ejecutable

Orden:

1. Levantar PostgreSQL local.
2. Aplicar las migraciones Flyway desde una base vacía.
3. Ejecutar `infrastructure/src/main/resources/db/seed/demo-data.sql`.
4. Revisar claves, restricciones y datos con consultas reales.
5. Incorporar Testcontainers y pruebas de migración, repositorios, rollback y concurrencia.

Esta fase termina cuando una base nueva puede crearse de forma repetible y el backend puede leer y escribir datos sin errores de mapeo.

### Fase 2: backend y contrato de API

Orden:

1. Cerrar los casos de uso incluidos en el MVP.
2. Implementar identidad, autenticación y autorización.
3. Agregar Spring Web, validación de entrada y manejo uniforme de errores.
4. Exponer endpoints de catálogo y reservas.
5. Publicar y aprobar el contrato OpenAPI.
6. Implementar los contextos adicionales aprobados en la fase 0.

Esta fase termina cuando los recorridos del MVP funcionan contra PostgreSQL y el contrato HTTP está versionado. El frontend no debe adivinar nombres de campos, estados ni errores antes de este punto.

### Fase 3: ambiente compartido de integración

Crear una imagen del backend y desplegar una instancia de desarrollo o staging con PostgreSQL, migraciones automáticas, secretos separados y datos controlados. Esto permite que frontend y QA consuman una API común sin depender de una máquina local.

Esta fase no es el despliegue final de producción; es una plataforma temprana de integración.

### Fase 4: frontend

Después de aprobar alcance, flujos y OpenAPI:

1. Elegir y documentar el stack frontend.
2. Definir navegación, componentes, estados de carga y errores.
3. Implementar autenticación.
4. Implementar catálogo, detalle de herramienta y disponibilidad.
5. Implementar solicitud y administración de reservas.
6. Agregar las pantallas de capacidades adicionales incluidas en el MVP.
7. Integrar exclusivamente contra el contrato publicado.

Los prototipos visuales pueden comenzar antes, pero la integración funcional debe esperar una API estable.

### Fase 5: pruebas integrales y endurecimiento

Las pruebas no deben dejarse para el final: cada fase agrega las suyas. En este punto se completa la cobertura transversal:

- pruebas unitarias de dominio y aplicación;
- pruebas PostgreSQL con Testcontainers;
- pruebas de concurrencia de reservas e inventario;
- pruebas de contrato API;
- pruebas de seguridad y permisos;
- pruebas end-to-end de los recorridos del MVP;
- pruebas de carga sobre búsquedas y confirmación de reservas;
- verificación de respaldo y restauración.

La salida exige cero fallos críticos, criterios de aceptación cumplidos y riesgos conocidos documentados.

### Fase 6: despliegue de producción

Completar:

- imágenes reproducibles y pipeline CI/CD;
- infraestructura y PostgreSQL administrado o correctamente operado;
- secretos, TLS, dominios y políticas de acceso;
- migraciones con estrategia de rollback compatible;
- monitoreo, alertas, copias de seguridad y restauración probada;
- checklist de lanzamiento y plan de respuesta a incidentes.

Producción solo debe habilitarse después de validar staging con el mismo mecanismo de construcción y migración.

## Siguiente paso inmediato

El siguiente paso recomendado es levantar PostgreSQL y validar migraciones más datos demo. En paralelo, el equipo debe cerrar el alcance del MVP. Después se puede terminar el backend REST y congelar OpenAPI; ese contrato marcará el momento correcto para iniciar la integración frontend.

Los pendientes específicos de persistencia se mantienen en [Pendientes de base de datos](pendientes-base-datos.md).
