# Reporte de validación de migraciones e integración

Fecha: 18 de octubre de 2026. Rama: `developer/conde`.

## Resultado reproducible

**BUILD SUCCESS: 62 pruebas, 0 fallos, 0 errores y 0 omitidas.**

| Módulo | Pruebas | Resultado |
| --- | ---: | --- |
| Domain | 21 | Aprobado |
| Application | 13 | Aprobado |
| Infrastructure | 28 | Aprobado |

```powershell
$env:JAVA_HOME = 'C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot'
./mvnw.cmd clean verify
```

La ejecución utilizó JDK 21, Docker Desktop 29.8.2 y contenedores temporales PostgreSQL 17.11/17 administrados por Testcontainers. Cada suite trabajó con un esquema aislado. Spotless se ejecutó y pasó como parte de `verify`.

## Qué se comprobó

- Compilación y dirección de dependencias de los módulos `domain`, `application` e `infrastructure`.
- Instalación vacía de las migraciones V1–V24 y V200, con 18 tablas de negocio y validación Hibernate.
- Actualización V3 → V200 sin pérdida del registro previo ni modificación de checksums ya aplicados.
- Carga demo aislada de 43 migraciones numeradas, con al menos cinco filas por tabla base.
- Correspondencia entre columnas PostgreSQL y entidades JPA, incluidas las columnas de pago añadidas por `ALTER TABLE`.
- Registro de cliente, login, JWT, cambio de contraseña, cierre de sesión sin estado y rechazo de un token de recuperación como credencial de acceso.
- Catálogo público, listados por participante, disponibilidad, cotización de días inclusivos y publicación de OpenAPI.
- Pagos simulados 50/50 con crédito Visa, PayPal, idempotencia y ausencia de PAN/CVV almacenados.
- Confirmaciones concurrentes incompatibles, unicidad de inventario por proveedor, transacciones con rollback y restricciones de base de datos.

## Alcance de la evidencia

La validación acredita el backend y sus contratos actuales. Los pagos siguen siendo una simulación académica: no se contactó una pasarela real ni se enviaron correos. Tampoco se inventaron impuestos, precios de protección, deducibles ni reglas comerciales pendientes; la cotización informa expresamente que el cálculo tributario requiere validación legal.

El frontend no fue modificado ni ejecutado en esta entrega. Sus pantallas deben integrarse con los contratos descritos en [API REST](rest-api.md) y con el esquema publicado en `/v3/api-docs` o `/swagger-ui.html`.
