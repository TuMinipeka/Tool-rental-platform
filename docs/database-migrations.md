# Migraciones de base de datos

## Secuencia vigente

| Rango | Propósito |
| --- | --- |
| V1–V3 | Modelo inicial, roles y administrador |
| Historial anterior a V200 | Modelo base y alineaciones ya aplicadas; permanece inmutable |
| V200 | Método, etapa e idempotencia de pagos académicos |
| V201 | Sesiones de usuario y familias de refresh |
| V202 | Tokens opacos de recuperación |
| V203 | Política versionada de cancelación e inasistencia |
| V204 | Terminaciones y revisión de retracto |
| V205 | Tarifa global de entrega |
| V206 | Extensiones de reserva |
| V207 | Reembolsos idempotentes |
| V208 | Modalidad, hora, dirección, fecha efectiva, fallos, mora y ajustes |
| V209 | Estados y vencimiento de retenciones de inventario pendientes de pago |
| V210 | Pago total sandbox, unicidad por etapa y factura previa al alquiler |
| V101–V118 | Datos académicos base, solo perfil demo |
| V300 | Tarifa académica de entrega, solo perfil demo |

V1–V200 no se modifican. Las evoluciones compartidas continúan hasta V210. Las migraciones demo tienen una ubicación separada (`db/demo`) y no se cargan en producción.

## Reglas

1. No edites ni renombres una versión aplicada.
2. Agrega una migración hacia delante y coordina el número con el equipo.
3. Mantén `ddl-auto: validate`.
4. No uses `baseline-on-migrate` para esconder un historial incompatible.
5. Valida instalación vacía y actualización desde V200 en PostgreSQL.
6. Ejecuta datos demo únicamente en una base desechable.

## Pruebas

`MappingContractTest` compara las 25 tablas y entidades JPA. `FlywayUpgradeIntegrationTest` conserva checksums y datos al actualizar desde V200 hasta V210. Las pruebas PostgreSQL verifican restricciones, rollback, retenciones y confirmación concurrente. Testcontainers se usa por defecto; también puede suministrarse una base con permiso para crear esquemas:

```powershell
.\mvnw.cmd clean verify `
  -Dpostgres.test.url=jdbc:postgresql://localhost:5432/tool_rental_test `
  -Dpostgres.test.username=test_owner `
  -Dpostgres.test.password='<secret>'
```

El usuario de prueba necesita `CREATE` sobre la base para generar y eliminar esquemas aislados. El usuario de ejecución de producción debe tener privilegios menores y seguir una estrategia separada de migración.

## Entrega MVP académica

V301 amplía el catálogo y los accesos exclusivamente locales. V302 completa escenarios históricos y la matriz de 20 filas por entidad de negocio. V303 está exclusivamente en db/showcase: desactiva credenciales conocidas y revoca sesiones y tokens anteriores. Se activa mediante production,showcase, sobre una base exclusiva con nombre validado; no se aplica a la base de trabajo. No se modificaron V1–V301. La instalación normal no carga fixtures.

## Integración de historiales

La instalación demo combinada tiene 56 migraciones hasta V302; showcase añade V303. V209–V210 se integraron sobre una base nueva de validación porque bases demo previas ya habían aplicado versiones superiores. No se activó out-of-order, repair ni baseline para ocultar la divergencia. Coordinar las nuevas versiones por encima de V303 y ensayar una evolución explícita para bases que deban conservarse.
