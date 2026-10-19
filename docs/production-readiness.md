# Preparación para producción

| Área | Implementado | Cierre requerido |
| --- | --- | --- |
| Identidad | BCrypt, JWT corto, sesiones rotativas y revocación | MFA opcional, alertas y revisión de claves |
| Recuperación | Token opaco de un uso y SMTP/Mailpit | Proveedor SMTP, TLS, DKIM/SPF/DMARC y métricas |
| HTTP | CORS explícito, CSRF, rate limit y ProblemDetail | Proxy confiable, límites distribuidos y WAF según riesgo |
| Datos | Flyway aditivo, JPA validate y 25 tablas | Usuario migrador separado, backup y recuperación ensayada |
| Negocio | Políticas versionadas, extensiones, mora y liquidación | Validación legal/fiscal y pasarela real |
| Operación | Dockerfiles, Compose, health JDBC y documentación | CI/CD, métricas, trazas, alertas, SLO y runbooks |

## Variables críticas

Configura mediante un gestor de secretos: `JWT_SECRET`, credenciales PostgreSQL y SMTP. Define `CORS_ALLOWED_ORIGINS` sin comodines; el perfil de producción fuerza cookies seguras detrás de HTTPS. Producción no carga V300; la tarifa de entrega se crea mediante el endpoint administrativo.

## Criterios de salida

- `mvnw.cmd clean verify` pasa con PostgreSQL real.
- `docker compose config --quiet` y la imagen inmutable pasan escaneo.
- La rotación concurrente, reutilización, revocación y CSRF tienen evidencia automatizada.
- Se ensayan migración V200→V208, restauración de backup y rollback de aplicación.
- Se verifican logs sin tokens ni credenciales.
- Producto, legal y finanzas aprueban porcentajes, retracto, impuestos y textos.
