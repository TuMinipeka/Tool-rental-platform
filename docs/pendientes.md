# Hoja de ruta del proyecto y trabajo pendiente

## Propósito

Este documento registra el orden de entrega acordado, las decisiones ya cerradas y las decisiones que todavía bloquean la implementación. Evita que una capa dependa de un contrato inestable.

## Decisiones de producto confirmadas

- El producto completo incluye catálogo, inventario, reservas, alquileres, entregas, devoluciones, gestión de daños, pagos simulados, facturación simulada, administración y reportes.
- Los roles iniciales son Cliente, Proveedor y Administrador. El Proveedor se encarga de las actividades de entrega y devolución.
- Una reserva cobra el 50 % cuando el proveedor la confirma y el 50 % restante después de la devolución y finalización del alquiler.
- Las tarjetas débito y crédito, PayPal, Visa y Mastercard son simulados. Visa y Mastercard son redes de tarjetas, no procesadores de pago independientes.
- El Plan Opcional de Protección contra Daños es opcional, está desactivado de forma predeterminada y no se presenta como un seguro.
- Los precios, deducibles, límites y exclusiones de la protección son parámetros comerciales versionados. No se describen como porcentajes exigidos por la legislación colombiana.
- Los precios usan pesos colombianos. La referencia general del IVA es 19 %, mientras que la regla especial de base gravable para el alquiler de bienes muebles debe implementarse y validarse profesionalmente antes de un lanzamiento real.
- El entorno actual emite documentos de simulación académica, no facturas válidas ante la DIAN.
- El alcance aprobado y las salvaguardas legales se detallan en [Alcance del producto](product-scope.md).
- La futura página pública de términos usa el borrador de [Términos y condiciones](terms-and-conditions.md).

## Tecnología confirmada

| Área | Tecnología | Estado actual |
| --- | --- | --- |
| Lenguaje del backend | Java 21 | Configurado |
| Framework del backend | Spring Boot 3.5.16 | Configurado |
| Servidor de aplicaciones | Apache Tomcat embebido | Suministrado por Spring Boot; todavía no existe una API HTTP |
| Construcción | Maven multimódulo | Configurado |
| Arquitectura | Arquitectura hexagonal y DDD | Implementada para los contextos actuales |
| Base de datos | PostgreSQL 17.11 | Entorno local reproducible y validado con Testcontainers |
| Persistencia | Spring Data JPA y Hibernate con `ddl-auto: validate` | Implementada para catálogo y reservas |
| Migraciones | Flyway | 18 migraciones de tablas y una migración de compatibilidad JPA, validadas desde una base vacía |
| Datos de desarrollo | 18 migraciones locales, una por tabla y con mínimo cinco filas | Validados en PostgreSQL |
| API | REST con OpenAPI y Swagger UI | Confirmada, no implementada |
| Frontend | React con herramientas de Node.js, HTML y CSS | Confirmado, no creado |

Node.js se usa únicamente para las herramientas del frontend. Java y Spring Boot siguen siendo la única pila del backend.

## Decisiones restantes de la Fase 0

Las siguientes decisiones de negocio deben aprobarse antes de implementarlas:

1. Plazos y cargos de cancelación, manejo de inasistencia y relación con el derecho legal de retracto.
2. Periodo de gracia para devoluciones tardías, aprobación de extensiones y cargos por mora.
3. Modalidades de entrega y recogida, límites geográficos, tarifas y manejo de entregas fallidas.
4. Precio inicial, deducible, límite monetario, exclusiones exactas y periodo de disputa del Plan de Protección. Estos valores deben identificarse como supuestos del proyecto hasta contar con una propuesta de un proveedor real.
5. Cálculo del valor de reposición y política de depreciación para equipos perdidos o irreparables.
6. Identidad del futuro operador, canales de atención, detalles de la política de privacidad y periodos de retención.
7. Elección entre JavaScript y TypeScript para el frontend, gestión de estado, biblioteca de componentes y herramientas de pruebas.
8. Versionado de la API, formato de errores, renovación de JWT, recuperación de cuentas y revocación de sesiones.
9. Proveedor de alojamiento, CI/CD, dominios, secretos, observabilidad, copias de seguridad y respuesta a incidentes.

La Fase 0 termina cuando estas decisiones tengan criterios de aceptación y prioridad. Los wireframes pueden comenzar durante esta fase, pero la integración con la API espera un contrato OpenAPI aprobado.

## Fase 1: base de datos ejecutable

Estado: la base técnica del modelo actual quedó terminada el 15 de octubre de 2026. La ampliación del esquema para el contrato completo del producto continúa bloqueada por las decisiones comerciales y legales de la Fase 0.

- [x] Aprovisionar PostgreSQL 17.11 local mediante Docker Compose y variables en `.env`.
- [x] Aplicar las 18 migraciones de tablas y la migración de compatibilidad JPA sobre una base de datos vacía.
- [x] Verificar que el perfil `local` aplique además las 18 migraciones de datos de demostración.
- [x] Revisar claves, restricciones y consultas representativas contra el motor real.
- [ ] Agregar migraciones para pagos fraccionados, impuestos, protección, documentos legales, evidencias y disputas. Requiere cerrar primero las reglas correspondientes de la Fase 0.
- [x] Agregar cobertura con Testcontainers para migraciones, mapeos JPA, repositorios, rollback, unicidad concurrente del inventario y doble confirmación de reservas.

La parte verificable del criterio de salida ya se cumple: una base de datos nueva se crea de forma repetible y la aplicación valida sus mapeos. La Fase 1 completa se cerrará cuando las decisiones de la Fase 0 permitan demostrar que el esquema soporta todo el contrato del producto. Véase [Trabajo pendiente de base de datos](pendientes-base-datos.md).

## Fase 2: backend y contrato de API

1. Completar los agregados y casos de uso aprobados.
2. Implementar identidad, autenticación y autorización.
3. Agregar Spring Web, validación de entradas y manejo uniforme de errores.
4. Implementar la API REST completa.
5. Publicar y aprobar el contrato OpenAPI mediante Swagger UI.
6. Implementar adaptadores de pagos simulados, protección, impuestos, facturación, evidencias y disputas.

Criterio de salida: todos los recorridos aprobados funcionan con PostgreSQL y el contrato HTTP versionado es estable.

## Fase 3: entorno de integración compartido

Construir una imagen reproducible del backend y desplegar un entorno de desarrollo o pruebas con PostgreSQL, migraciones automáticas, secretos aislados, datos controlados, registros y comprobaciones de salud.

Criterio de salida: el frontend y el trabajo de calidad pueden usar una API compartida sin depender del equipo de un desarrollador.

## Fase 4: frontend

1. Crear el proyecto React con las herramientas de Node.js.
2. Establecer navegación, accesibilidad y estados de carga, vacío, éxito y error.
3. Implementar autenticación y navegación según el rol.
4. Implementar las pantallas de catálogo, disponibilidad, reservas, alquileres, pagos, devoluciones, revisión de daños, facturas y administración.
5. Implementar la página pública `/terms-and-conditions` y enlazarla desde el pie de página y todos los puntos de aceptación requeridos.
6. Integrar únicamente contra el contrato OpenAPI aprobado.

Criterio de salida: los tres roles pueden completar sus recorridos aprobados mediante una interfaz adaptable.

## Fase 5: validación de extremo a extremo y robustecimiento

Las pruebas se agregan con cada comportamiento implementado; esta fase completa la validación transversal:

- cobertura unitaria del dominio y la aplicación;
- cobertura de migraciones, mapeos, rollback y concurrencia en PostgreSQL;
- cobertura del contrato REST y de autorización;
- cobertura de extremo a extremo de los recorridos críticos de cada rol;
- escenarios de precios, IVA, pagos fraccionados, reembolsos y cálculo de daños;
- validación de accesibilidad, seguridad, rendimiento, copias de seguridad y restauración.

Criterio de salida: se cumplen los criterios de aceptación, no queda abierto ningún defecto crítico y los riesgos aceptados están documentados.

## Fase 6: despliegue en producción

Completar construcciones reproducibles, CI/CD, infraestructura administrada, TLS, dominios, secretos, acceso de mínimo privilegio, procedimientos de migración y rollback, monitoreo, alertas, copias de seguridad, ejercicios de restauración y un plan de respuesta a incidentes.

Un lanzamiento comercial real requiere además revisión profesional colombiana en materia legal, tributaria, de privacidad y de seguridad. Hasta completar esa revisión y una integración con la DIAN, los despliegues seguirán siendo simulaciones académicas.

## Próxima decisión inmediata

Finalizar la Fase 0 una regla a la vez. La siguiente decisión recomendada es la política de cancelación e inasistencia, porque afecta los totales de las reservas, reembolsos, información al consumidor, transiciones de estado del backend, historial de la base de datos y textos del frontend.
