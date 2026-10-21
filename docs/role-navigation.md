# Navegación por rol

## Decisión

Con sesión iniciada, el proveedor y el administrador ven las secciones de su dashboard directamente en el header. Ya no aparecen como una barra de pestañas separada dentro de la página. La integración solo cambia la navegación. **No concede ni retira permisos**: el backend sigue validando cada operación con el rol del usuario autenticado.

Sin sesión, o con sesión de cliente, el header no cambia.

## Contenido del header por rol

| Elemento | Visitante | Cliente | Proveedor | Administrador |
| --- | --- | --- | --- | --- |
| Catálogo, Cómo funciona, Sobre nosotros | Sí | Sí | No | No |
| Selector de municipio | Sí | Sí | No | No |
| Publica tu herramienta | Sí | Sí | No | No |
| Resumen | — | — | Sí | Sí |
| Herramientas | — | — | Sí | — |
| Proveedores | — | — | — | Sí |
| Administradores | — | — | — | Sí |
| Mi cuenta | — | Sí | Sí | Sí |
| Ingresar / avatar | Ingresar | Avatar | Avatar | Avatar |

Si una cuenta tiene los roles `ADMIN` y `SUPPLIER`, se muestra el dashboard administrativo. Esta prioridad ya existía.

## Por qué el proveedor y el administrador no ven el catálogo en el header

Las reglas de negocio vigentes reservan para el cliente las operaciones de alquiler:

- Cotizar y reservar exigen el rol `CUSTOMER` (`application/.../reservation/usecase/QuoteReservationUseCase.java`) y un perfil de cliente activo (`RequestReservationUseCase.java`).
- La decisión de pago exige cliente propietario autenticado (`DecideFictionalCheckoutUseCase.java`). El proveedor y terceros no pueden aprobarla.
- Los Términos y Condiciones indican que cada usuario usa únicamente los permisos de su rol.

Mostrar el catálogo y "Publica tu herramienta" a estos roles los llevaría a flujos que el backend rechaza. Por eso se mantiene la decisión previa de ocultarlos. El proveedor publica desde la sección **Herramientas** de su dashboard.

El catálogo sigue siendo público. Cualquier persona puede consultarlo por su URL; ocultarlo del header no es una medida de seguridad.

## Contrato de URL

La sección activa forma parte de la URL, por lo que es enlazable y funciona con atrás y adelante:

| Rol | Resumen | Otras secciones |
| --- | --- | --- |
| Proveedor | `/panel/proveedor` | `?seccion=herramientas` |
| Administrador | `/panel/admin` | `?seccion=proveedores`, `?seccion=administradores` |

Un valor desconocido abre el resumen. La definición está en `frontend/src/lib/dashboardSections.js` y la usan el header y la página del dashboard.

## Comportamiento por dispositivo

- Desde 821 px las secciones aparecen solo en el header. Las pestañas internas se ocultan para no duplicar la navegación.
- Hasta 820 px el header se colapsa en el menú. Las pestañas internas siguen visibles y desplazables, y las mismas secciones también están en el menú.
- El título y la descripción del dashboard cambian según la sección activa.

## Decisiones de negocio abiertas

Ninguna de estas decisiones se aplicó; necesitan aprobación del producto:

1. ¿El proveedor o el administrador deben poder consultar el catálogo público desde su header, en modo solo lectura?
2. ¿Una misma persona puede ser cliente y proveedor con una sola cuenta? Hoy, una cuenta con rol de staff solo ve la navegación de su dashboard.
3. ¿Debe existir un acceso directo a "Mi cuenta" separado del avatar, o basta con uno de los dos?
