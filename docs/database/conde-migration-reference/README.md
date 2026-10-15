# Referencia de las migraciones de developer/conde

Estos archivos conservan la propuesta de migraciones V1–V21 de `developer/conde` para revisión. No son una ubicación de Flyway y no deben ejecutarse sobre una base existente.

El merge conserva el historial aplicado V1–V3 de `developer/daniel`. Reemplazar versiones que ya se aplicaron produciría conflictos de validación y duplicación de tablas. Las modificaciones futuras del esquema deben añadirse mediante versiones nuevas en `infrastructure/src/main/resources/db/migration`.

Los datos demo permanecen en `db/demo`, con activación explícita mediante el perfil adicional `demo`. La conexión local habitual no carga datos ficticios.
