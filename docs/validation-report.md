# Reporte de validación

Fecha: 2026-10-20. Evidencia de la preparación local previa a integrar la nueva entrega de Conde; la publicación sigue bloqueada.

- Maven clean verify con Java 21: 78 pruebas, 0 fallos, 0 errores y 0 omisiones. Incluye PostgreSQL/Testcontainers, actualización desde V200, restricciones, rollback y concurrencia.
- Frontend: lint/build y 68 pruebas Playwright satisfactorias; dos omisiones por dispositivo.
- [Integración real](evidence/mvp/live-integration.json): sesiones, roles, herramientas, unidades, reserva inclusiva, confirmación/cancelación y catálogo sin respaldo ficticio al caer el servidor.
- [Perfil seguro](evidence/mvp/secure-profile.json): credenciales demo desactivadas, bootstrap privado, cookies seguras y revocación por rotación.
- [Respaldo](evidence/mvp/backup-restore.json): 26 tablas restauradas con conteos iguales; datos persistentes tras reiniciar PostgreSQL/backend. La copia externa no está validada.
- [Base](evidence/mvp/database-audit.json): 25 tablas, 20 de negocio con al menos 20 filas, 146 restricciones y 62 índices.
- [Configuración](evidence/mvp/configuration.json): Compose/Caddy/shell válidos; publicación incompleta rechazada.
- [ARM](evidence/mvp/arm-manifests.json): seis imágenes base con manifiestos ARM64; no se acredita ejecución en Oracle.

Los informes incluyen fecha y huella de los archivos probados; sourceCommit identifica el commit base y validatedWorkingTree indica pruebas sobre los cambios de esta entrega. Las capturas son de datos académicos en [screenshots](evidence/mvp/screenshots/). Los logs completos y dumps permanecen privados.

Se repetirá la validación afectada al integrar la nueva entrega de Conde. No se acredita proveedor de pagos externo, SMTP público, DNS/HTTPS público, respaldo externo ni instancia Oracle.
