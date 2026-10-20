# Integración por rol y contratos pendientes

| Vista / operación | Contrato actual | Autoridad / estado |
| --- | --- | --- |
| Catálogo público y disponibilidad | GET /api/v1/public/catalog y disponibilidad por herramienta | Backend; datos reales, sin respaldo ficticio en el build público |
| Registro y acceso | /api/v1/auth/register y login | Cliente público; roles privilegiados requieren autorización |
| Cuenta: reservas | GET /api/v1/reservations | Filtrado por actor en backend |
| Cuenta: alquileres, pagos, comprobantes | GET /api/v1/rentals, payments, invoices | Datos persistidos; no equivalen a reportes ni a pasarela/factura descargable |
| Proveedor: publicaciones/unidades | /api/v1/tools, equipment-units, categories | Registro y consulta existentes; propietario validado en servidor |
| Proveedor: confirmar/rechazar | POST /api/v1/reservations/{id}/confirmation y rejection | Flujo heredado, pendiente de reemplazar por webhook sandbox |
| Cliente: cancelación | POST /api/v1/reservations/{id}/cancellation | Idempotency-Key estable por intento; conflictos se muestran sin modificar el estado local como si hubiera éxito |
| Administrador: usuarios y categorías | GET /api/v1/users y categories | Respuesta real; controles visuales no sustituyen permisos |
| Reportes, sandbox, comprobante descargable | Pendiente de Conde | Esperar OpenAPI, ejemplos y pruebas; no inventar endpoints |
| Correo y restablecimiento | Contratos backend existentes; validación final pendiente de Conde | No acreditar entrega SMTP ni recorrido completo hasta comprobarlos |

Las pantallas presentan carga, vacío y error. Las acciones solo actualizan la vista después de una respuesta satisfactoria del backend. La disponibilidad, el importe, la autorización y las transiciones no se recalculan como decisiones del navegador.

El build de publicación usa VITE_API_BASE_URL vacío, VITE_DEMO_MODE=false y VITE_REQUIRE_LIVE_API=true. Una caída de catálogo se comunica como error; no se reemplaza por el catálogo local. El diseño, las imágenes y las animaciones de Conde se conservan.

## Entrega esperada de Conde

Por cada capacidad pendiente: commit de referencia, OpenAPI versionado, roles/propietario, ejemplos de entrada/salida, códigos de error, estados permitidos y pruebas con PostgreSQL. Para pasarela: configuración sandbox, firma del webhook, duplicados, rechazo, expiración y reembolso; para correo: proveedor configurado y token de un uso sin exposición. Daniel conectará las pantallas cuando exista esa entrega.
