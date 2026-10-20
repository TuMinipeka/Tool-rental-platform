# Reporte de validación

Fecha de actualización: 2026-10-20.

## Resultado verificado

- `./mvnw clean verify` finalizó correctamente con Java 21 y Docker/Testcontainers: 27 pruebas de Domain, 16 de Application y 37 de Infrastructure, sin fallos ni omisiones.
- Spotless aplicado; arquitectura hexagonal y mapeo de 25 entidades JPA comprobados mediante pruebas sin base de datos.
- `ArchitectureTest` verifica los 25 paquetes en los tres módulos, sus configuraciones, la correspondencia de `package` con la ruta y LF en Java. `BoundedContextBeanWiringTest` verifica los casos de uso ensamblados y `BoundedContextHttpMappingTest` comprueba que las rutas de sesión, recuperación, políticas y tarifas se registran sin colisiones.
- Configuración Compose válida mediante `docker compose config --quiet`.
- PostgreSQL 17 ejecutó una instalación vacía hasta V210, validación Hibernate de las 25 entidades y actualización V200→V210 sin pérdida de datos ni cambio de checksums.
- Límites exactos de 48 horas, dos horas, precisión monetaria y margen de reutilización cubiertos por pruebas de dominio.
- OpenAPI filtra `/api/v1`; el alias `/api` conserva cabeceras de obsolescencia. Ambos contratos se comprobaron mediante HTTP con PostgreSQL activo.
- Las integraciones cubren retenciones concurrentes, checkout/webhook firmado, factura académica, cinco reportes por rol, incorporación de proveedor, permisos prohibidos y revocación de sesiones por baja lógica.

## Evidencia reproducible

La ejecución verificada fue:

```bash
JAVA_HOME=/ruta/al/jdk-21 ./mvnw clean verify
```

El resultado fue `BUILD SUCCESS` en 36.336 segundos. Docker debe estar disponible; Testcontainers crea bases y esquemas desechables y no modifica la base de trabajo.

## Límites

La implementación de reembolso y pagos continúa siendo académica; no ejecuta transferencias reales. SMTP necesita credenciales y TLS de un proveedor en producción. La respuesta de estado JDBC acredita conectividad en ese momento, no respaldo, recuperación, alertas ni alta disponibilidad.
