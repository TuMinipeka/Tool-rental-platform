# Cobertura de la rúbrica y del enunciado

La referencia es el inicio del [enunciado original](../ejercicioDemo.md), que se conserva intacto. La matriz distingue código, documentación y validación.

## Criterios explícitos de evaluación

| Criterio | Evidencia | Estado y límite |
| --- | --- | --- |
| Arquitectura hexagonal, DDD y módulos | [Arquitectura](backend-architecture.md), POM raíz, módulos y ArchUnit | Implementado; invariantes y dependencias hacia dominio |
| README principal | [README](../README.md) | Español, navegación, diagramas y estado verificable |
| Documentación de compilación | [Compilación y pruebas](build-and-test.md) | JDK 21, Wrapper, PostgreSQL y frontend |
| Despliegue Docker o Tomcat nativo | [Despliegue](deployment.md), `Dockerfile`, `compose.yml` y contenedores frontend | Configuración disponible; falta ejecutar contenedores en el entorno destino |
| Frontend permitido | Carpeta `frontend`, React y JavaScript | Base de Conde; recorridos pendientes |
| Normalización hasta 4FN | [Análisis 1FN–4FN](fourth-normal-form.md), [conceptual](conceptual-model.md) y Excalidraw | 18 relaciones bajo supuestos documentados; PostgreSQL autorizado |

## Funciones de negocio

| Área | Entregado | Pendiente |
| --- | --- | --- |
| Usuarios y roles | Perfiles, varios roles, BCrypt y permisos en casos de uso | JWT, login y alta inicial segura |
| Catálogo | Categorías, herramientas, imágenes y unidades | Interfaz conectada |
| Reservas | Solicitud, confirmación, rechazo/cancelación y concurrencia | Caducidad automática y pago como condición de entrega |
| Operación | Alquiler, entrega, devolución y daños | Evidencias, disputas y ampliaciones comerciales |
| Pagos | Registro administrativo y estados | Pasarela y reembolsos externos |
| Facturación | Documento y líneas atómicos con instantáneas | Impuestos, numeración fiscal y reglas definitivas |
| Paneles e informes | Base frontend | Paneles cliente/proveedor, estadísticas e informes |
| Base de datos | 18 tablas, claves, índices, JPA y migraciones de ambos desarrolladores | Entorno productivo y separación de privilegios |

El cumplimiento documental no completa todo el producto. Revisa [producción](production-readiness.md) y [hoja de ruta](pendientes.md).
