# Pendientes de base de datos

## Estado actual

La capa de persistencia ya incluye PostgreSQL, Spring Data JPA, Flyway, perfiles de configuración, migraciones para las 18 relaciones normalizadas, entidades JPA, mappers, adapters para los puertos actuales y un script manual con datos demo para todas las tablas.

La compilación Java fue validada sin ejecutar pruebas. La base todavía no puede considerarse terminada para producción porque falta comprobar el esquema y el comportamiento transaccional contra una instancia PostgreSQL real.

## Pendientes prioritarios

- Proveer una instancia PostgreSQL local, de integración y de producción.
- Ejecutar las migraciones Flyway desde una base vacía y validar su actualización entre versiones.
- Agregar pruebas de integración con Testcontainers para entidades, mappers y repositorios.
- Agregar pruebas concurrentes que demuestren que dos confirmaciones no pueden reservar la misma unidad.
- Probar concurrentemente la unicidad de `inventory_code` dentro de cada proveedor.
- Verificar rollback completo cuando una operación atómica falla después de adquirir bloqueos.
- Ejecutar y revisar `infrastructure/src/main/resources/db/seed/demo-data.sql` después de aplicar Flyway en el ambiente local.

## Capacidades todavía sin persistencia

- Escritura y administración de usuarios, roles, clientes y proveedores.
- Alquileres, entregas, devoluciones y reportes de daños.
- Pagos, reembolsos, facturas y líneas de factura.
- Publicación durable de eventos mediante Outbox u otro mecanismo transaccional.
- Reportes y consultas optimizadas para operaciones administrativas.

Las tablas de estas capacidades existen para preservar el modelo relacional acordado, pero no deben presentarse como funcionalidades implementadas hasta que existan sus agregados, casos de uso y adapters.

## Operación y seguridad

- Crear usuarios PostgreSQL con privilegios mínimos separados por ambiente.
- Administrar secretos fuera del repositorio y rotarlos periódicamente.
- Definir copias de seguridad, restauración comprobada y retención.
- Configurar observabilidad para conexiones, consultas lentas, bloqueos y fallos de migración.
- Establecer límites de pool y timeouts con mediciones reales de carga.
- Incorporar la validación de migraciones y pruebas PostgreSQL al pipeline de CI.

## Criterio para considerar la base lista

La base estará lista cuando una instancia PostgreSQL nueva pueda levantarse, aplicar todas las migraciones, iniciar la aplicación con `ddl-auto: validate`, superar las pruebas de integración y concurrencia, y contar con procedimientos comprobados de respaldo, restauración y despliegue.
