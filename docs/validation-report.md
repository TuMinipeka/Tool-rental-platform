# Reporte de validación

Validación local del 20 de octubre de 2026, rama developer/daniel, integración de Conde hasta `981a16e`. El código funcional se conserva en `3fbde10`; la [evidencia consolidada](evidence/mvp/validation-summary.json) registra revisión, comandos y huella. **No se ha publicado una demo pública.**

| Comprobación ejecutada | Resultado | Evidencia / alcance |
| --- | --- | --- |
| Maven clean verify, JDK 21.0.12.1 | 83 pruebas; cero fallos, errores u omisiones | 20 suites; PostgreSQL/Testcontainers, arquitectura, Spotless, V200→V210, rollback y concurrencia |
| npm lint y build | Satisfactorios | React compilado sin modificar el diseño existente |
| Playwright aislado | 72 satisfactorias, dos omitidas según dispositivo | Escritorio/móvil, axe, rutas, animación y reintentos; usa fixtures API |
| Navegador y API reales | 43 comprobaciones satisfactorias | [Recorrido sin mocks](evidence/mvp/live-integration.json) |
| Perfil production,showcase privado | 11 comprobaciones satisfactorias | [Cookies, accesos privados y revocación](evidence/mvp/secure-profile.json) |
| Respaldo y reinicio | Restauración con conteos iguales en 26 tablas | [Prueba local](evidence/mvp/backup-restore.json); incluye historial Flyway |
| Esquema y datos | 25 tablas; 20 de negocio con mínimo 20 filas; 147 restricciones y 63 índices | [Auditoría](evidence/mvp/database-audit.json); 56 migraciones hasta V302 |
| Compose público, Caddy y shell | Configuración válida; publicación incompleta rechazada | [Configuración](evidence/mvp/configuration.json); sin abrir servicios públicos |
| Imágenes ARM64 | Seis bases compatibles por manifiesto | [Manifiestos](evidence/mvp/arm-manifests.json); no acredita ejecución en Oracle |

## Recorrido integrado demostrado

El proveedor registra herramienta y unidad; el cliente crea una reserva inclusiva y un checkout del total. El operador de prueba envía un evento HMAC interno: una firma inválida se rechaza y un duplicado no emite otra factura. La reserva se confirma, el comprobante HTML incluye el aviso académico, y el proveedor programa/completa entrega, inicia alquiler, registra devolución y cierra el ciclo. Los reportes provienen del backend; el cliente recibe 403 y el proveedor mantiene su alcance.

También se verifica registro de proveedor suspendido, activación administrativa, desactivación con revocación, registro real del cliente, renovación/logout, correo privado en Mailpit y restablecimiento por enlace de un uso. La sesión funciona antes del cambio de contraseña y es rechazada después. Al caer el backend, el catálogo público informa el error y no fabrica resultados.

Las [capturas](evidence/mvp/screenshots/) contienen únicamente datos académicos. Los informes incluyen fecha y huella de los archivos en el momento de cada prueba; no se actualizan artificialmente esas huellas tras ejecutar otros comandos. La auditoría y el respaldo son instantáneas con sus propios conteos; pruebas posteriores pueden añadir datos. Logs completos, dumps y entornos con secretos permanecen ignorados.

## Límites y siguiente cierre

No se acredita pasarela oficial externa, cobro bancario, SMTP público, copia externa, DNS/HTTPS público ni ejecución en Oracle. La facturación es académica y profitability representa ingreso reservado, no margen contable. El perfil público desactiva credenciales conocidas; las cuentas privadas probadas localmente no sustituyen provisionar accesos en el destino.

La composición aislada permanece en http://localhost:15173. [MVP](mvp.md), [despliegue](deployment.md) y [status.json](evidence/mvp/status.json) separan estos resultados del cierre público. No se alteró el historial aplicado para integrar migraciones inferiores: se usó una base demo nueva.

## Revalidación de tareas de Conde

El 20 de octubre de 2026 a las 14:27:35 (America/Bogota) se repitió clean verify sobre 59bb9c2: 83 pruebas, cero fallos, errores u omisiones, PostgreSQL/Testcontainers y Spotless satisfactorios. La [auditoría en MVP](mvp.md#auditoría-de-las-tareas-originales-de-conde) diferencia las siete tareas originales y las aceptaciones pendientes. No se repitieron las suites de navegador ni se habilitó el despliegue público; sigue faltando pasarela oficial y SMTP público, además de los requisitos operativos del destino.
