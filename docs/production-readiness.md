# Preparación para producción

El proyecto tiene persistencia y operaciones verificadas en PostgreSQL. Antes de ofrecerlo públicamente deben cerrarse estas condiciones con evidencia del entorno destino.

| Área | Base existente | Condición pendiente |
| --- | --- | --- |
| Identidad | BCrypt, roles, registro de cliente, login y JWT de acceso con expiración | Renovación, revocación persistente y recuperación por canal externo |
| Autorización | Propiedad y participación en casos de uso | Pruebas completas con autenticación real |
| Migraciones | Historial inmutable y pruebas de actualización | Ensayo en copia, ventana de cambio y rol migrador separado |
| Secretos | Variables requeridas en perfil `production` | Excluir credenciales privadas de artefactos, gestor de secretos y rotación |
| Datos | Claves, restricciones y transacciones | Respaldo automático y restauración ensayada |
| Plataforma | JAR y usuario de contenedor sin privilegios | Imágenes revisadas, TLS/proxy, recursos y motor Docker probado |
| Observabilidad | Consulta JDBC y logs | Métricas, alertas, auditoría y acceso al estado público |
| API | DTOs, errores HTTP, OpenAPI y Swagger UI | Versionado aprobado y revisión de exposición/límites |
| Finanzas | Registro, historia y pagos académicos 50/50 idempotentes | Impuestos, pasarela y conciliación/reembolsos externos |
| Producto | Dominio y backend base | Frontend integrado y términos revisados |
| Entrega | Pruebas reproducibles | CI, revisión de dependencias y recuperación |

## Configuración privada y artefactos

`application-local.yml` contiene configuración de desarrollo. No publiques una copia con credenciales privadas dentro de un JAR, imagen o repositorio. Inyecta `DB_URL`, `DB_USERNAME` y `DB_PASSWORD` para producción y verifica el contenido del artefacto y sus capas. Excluye el perfil `demo` de producción.

## Publicación controlada

Verifica código y concurrencia; ensaya migración y recuperación aisladas; revisa autenticación/permisos; comprueba secretos e imagen; valida PostgreSQL y los recorridos de negocio. Mantén responsables y un procedimiento de recuperación. Una respuesta de `/status/database` acredita conectividad en ese momento, no todos estos controles.

Los pendientes comerciales se mantienen en [la hoja de ruta](pendientes.md). Los términos actuales son un borrador y no una aprobación jurídica o comercial.
