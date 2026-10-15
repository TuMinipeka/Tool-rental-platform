# Migraciones de PostgreSQL y trazabilidad de Conde

## Decisión de integración

Conde es otro desarrollador del proyecto. Sus migraciones de `developer/conde`, revisión `ac149a1db7a95d3cc2bb4f313e063927fb7a0915`, forman parte del aporte integrado, junto a las pruebas, Docker y frontend. El primer merge las dejó archivadas: protegía el historial existente, pero no completaba la integración ejecutable. Esta entrega corrige esa decisión.

En la base de Daniel ya estaban aplicadas V1–V3: V1 crea las 18 tablas, V2 carga los roles de cliente/proveedor y V3 añade ADMIN. Reemplazarlas por las V1–V21 originales de Conde produciría conflictos de versiones y checksums. Se conservan byte por byte y se incorporan los 21 scripts como versiones nuevas V4–V24.

## Correspondencia de versiones

| Procedencia | Archivo ejecutable actual |
| --- | --- |
| `V1` de Conde | [`V4__create_users_table.sql`](../infrastructure/src/main/resources/db/migration/V4__create_users_table.sql) |
| `V2` de Conde | [`V5__create_roles_table.sql`](../infrastructure/src/main/resources/db/migration/V5__create_roles_table.sql) |
| `V3` de Conde | [`V6__create_user_roles_table.sql`](../infrastructure/src/main/resources/db/migration/V6__create_user_roles_table.sql) |
| `V4` de Conde | [`V7__create_customers_table.sql`](../infrastructure/src/main/resources/db/migration/V7__create_customers_table.sql) |
| `V5` de Conde | [`V8__create_suppliers_table.sql`](../infrastructure/src/main/resources/db/migration/V8__create_suppliers_table.sql) |
| `V6` de Conde | [`V9__create_categories_table.sql`](../infrastructure/src/main/resources/db/migration/V9__create_categories_table.sql) |
| `V7` de Conde | [`V10__create_tools_table.sql`](../infrastructure/src/main/resources/db/migration/V10__create_tools_table.sql) |
| `V8` de Conde | [`V11__create_tool_images_table.sql`](../infrastructure/src/main/resources/db/migration/V11__create_tool_images_table.sql) |
| `V9` de Conde | [`V12__create_equipment_units_table.sql`](../infrastructure/src/main/resources/db/migration/V12__create_equipment_units_table.sql) |
| `V10` de Conde | [`V13__create_reservations_table.sql`](../infrastructure/src/main/resources/db/migration/V13__create_reservations_table.sql) |
| `V11` de Conde | [`V14__create_reservation_items_table.sql`](../infrastructure/src/main/resources/db/migration/V14__create_reservation_items_table.sql) |
| `V12` de Conde | [`V15__create_rentals_table.sql`](../infrastructure/src/main/resources/db/migration/V15__create_rentals_table.sql) |
| `V13` de Conde | [`V16__create_unit_deliveries_table.sql`](../infrastructure/src/main/resources/db/migration/V16__create_unit_deliveries_table.sql) |
| `V14` de Conde | [`V17__create_unit_returns_table.sql`](../infrastructure/src/main/resources/db/migration/V17__create_unit_returns_table.sql) |
| `V15` de Conde | [`V18__create_damage_reports_table.sql`](../infrastructure/src/main/resources/db/migration/V18__create_damage_reports_table.sql) |
| `V16` de Conde | [`V19__create_payments_table.sql`](../infrastructure/src/main/resources/db/migration/V19__create_payments_table.sql) |
| `V17` de Conde | [`V20__create_invoices_table.sql`](../infrastructure/src/main/resources/db/migration/V20__create_invoices_table.sql) |
| `V18` de Conde | [`V21__create_invoice_items_table.sql`](../infrastructure/src/main/resources/db/migration/V21__create_invoice_items_table.sql) |
| `V19` de Conde | [`V22__align_currency_columns_with_jpa.sql`](../infrastructure/src/main/resources/db/migration/V22__align_currency_columns_with_jpa.sql) |
| `V20` de Conde | [`V23__seed_administrator_role.sql`](../infrastructure/src/main/resources/db/migration/V23__seed_administrator_role.sql) |
| `V21` de Conde | [`V24__align_currency_columns_with_bounded_contexts.sql`](../infrastructure/src/main/resources/db/migration/V24__align_currency_columns_with_bounded_contexts.sql) |

Cada archivo identifica su versión original y la revisión de Conde. Los originales archivados permanecen como evidencia, fuera del classpath de Flyway. **La implementación ejecutable está en `db/migration`, no en `docs`.**

## Adaptaciones necesarias

V4–V21 conservan las definiciones por tabla del aporte y añaden `IF NOT EXISTS` para reconocer el esquema creado por V1. También protegen los índices existentes. La función de unicidad de inventario utiliza `CREATE OR REPLACE`; su trigger se crea solo si no existe en la tabla del esquema actual.

Esto no crea 18 tablas adicionales. En una instalación nueva V1 sigue creando el esquema completo; las siguientes versiones incorporan la secuencia compatible del trabajo de Conde. `IF NOT EXISTS` no repara diferencias arbitrarias: Hibernate y las pruebas validan columnas; una base incompatible requiere diagnóstico y una migración específica.

V22 conserva el ajuste a `varchar(3)`, V23 conserva la inserción idempotente de ADMIN y V24 restablece `char(3)` conforme al mapeo JPA actual. Se conservan ambos ajustes para mantener la trazabilidad del aporte. Las conversiones pueden adquirir bloqueos fuertes sobre `tools` y `reservations`; limitan la espera de bloqueo a cinco segundos y deben ejecutarse en una ventana de cambio. Si expira la espera, resuelve las transacciones activas y vuelve a iniciar sin forzar el historial.

## Ubicaciones y perfiles

| Ubicación | Versiones | Activación |
| --- | --- | --- |
| `classpath:db/migration` | V1–V24, 24 versiones | Normal, local y producción |
| `classpath:db/demo` | V101–V118, 18 versiones | Perfil adicional `demo` exclusivamente |

La demostración aplica 42 versiones numeradas. Su versión más alta es 118; la historia normal termina en 24. Demo debe usar una base exclusiva.

## Historial y próximas migraciones

No edites archivos publicados/aplicados, no reutilices versiones y no ejecutes `repair` para encubrir diferencias. Flyway valida historial, deshabilita baseline automático y `clean`. Hibernate utiliza `validate`.

Coordina una secuencia global entre desarrolladores. Como demo reserva V101–V118, la próxima evolución compartida debe usar una versión superior a 118, por ejemplo V200, para aplicarse también en bases demo sin habilitar ejecución fuera de orden. Evita crear V25 después de aplicar V118 en un entorno demo.

Una base con **los V1–V21 originales de Conde** constituye otro historial: se valida la base vacía y la línea V1–V3 de Daniel, no una conversión automática de aquella. No arranques estos scripts sobre ese historial ni uses `repair`: conserva respaldo, compara esquema y diseña una transición revisada.

## Actualizar una base existente

1. Verifica rama, artefacto, perfil y destino sin registrar credenciales.
2. Genera copia privada con `pg_dump -Fc`; ensaya su restauración aislada antes de una publicación productiva.
3. Revisa que el historial previo sea V1–V3, sin fallos ni checksums inesperados.
4. Detén el backend anterior y evita escrituras durante la ventana de cambio.
5. Arranca el nuevo artefacto: Flyway aplica V4–V24 y Hibernate valida.
6. Comprueba 18 tablas de negocio, historial exitoso hasta V24, datos conservados y `/status/database` conectado.

No se ejecuta demo durante la actualización. El respaldo contiene datos privados y no debe incluirse en Git.

## Pruebas de compatibilidad

`FlywayUpgradeIntegrationTest` migra hasta V3, inserta un registro previo y captura checksums; luego aplica 21 versiones, conserva registro y checksums, verifica 18 tablas y un solo trigger, y comprueba que repetir `migrate` ejecuta cero cambios. Las otras suites validan instalación vacía con 24 versiones y demo con 42, persistencia y concurrencia.

Consulta [validación](validation-report.md) y [despliegue](deployment.md).
