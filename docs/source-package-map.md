# Mapa de paquetes y archivos

El proyecto tiene 18 contextos por tabla en cada uno de los tres módulos Maven. `common` contiene contratos y adaptadores compartidos. Las rutas parten de `src/main/java/com/toolrental/<module>/`.

## Reubicación de paquetes anteriores

| Ubicación anterior | Ubicación actual |
| --- | --- |
| `catalog`: categorías | `category` en los tres módulos |
| `catalog`: herramientas | `tool` en los tres módulos |
| `catalog`: unidades físicas | `equipmentunit` en los tres módulos |
| Imágenes del catálogo | `toolimage` en los tres módulos |
| `identity`: usuario y perfiles | `user`, `customer`, `supplier` |
| `identity`: participantes | `infrastructure/common/adapters/out/persistence/ParticipantsPersistenceAdapter.java` |
| Roles y asignaciones | `role` y `userrole` |

Las clases fueron reubicadas; no se deben crear copias en `catalog` o `identity`, porque duplicarían responsabilidades y mapeos JPA.

## Navegación de los 18 contextos

| Contexto | Domain | Application | Infrastructure |
| --- | --- | --- | --- |
| `user` | [5 archivos](../domain/src/main/java/com/toolrental/domain/user) | [4 archivos](../application/src/main/java/com/toolrental/application/user) | [8 archivos](../infrastructure/src/main/java/com/toolrental/infrastructure/user) |
| `role` | [4 archivos](../domain/src/main/java/com/toolrental/domain/role) | [4 archivos](../application/src/main/java/com/toolrental/application/role) | [7 archivos](../infrastructure/src/main/java/com/toolrental/infrastructure/role) |
| `userrole` | [4 archivos](../domain/src/main/java/com/toolrental/domain/userrole) | [4 archivos](../application/src/main/java/com/toolrental/application/userrole) | [8 archivos](../infrastructure/src/main/java/com/toolrental/infrastructure/userrole) |
| `customer` | [4 archivos](../domain/src/main/java/com/toolrental/domain/customer) | [4 archivos](../application/src/main/java/com/toolrental/application/customer) | [7 archivos](../infrastructure/src/main/java/com/toolrental/infrastructure/customer) |
| `supplier` | [4 archivos](../domain/src/main/java/com/toolrental/domain/supplier) | [4 archivos](../application/src/main/java/com/toolrental/application/supplier) | [7 archivos](../infrastructure/src/main/java/com/toolrental/infrastructure/supplier) |
| `category` | [5 archivos](../domain/src/main/java/com/toolrental/domain/category) | [5 archivos](../application/src/main/java/com/toolrental/application/category) | [7 archivos](../infrastructure/src/main/java/com/toolrental/infrastructure/category) |
| `tool` | [5 archivos](../domain/src/main/java/com/toolrental/domain/tool) | [6 archivos](../application/src/main/java/com/toolrental/application/tool) | [8 archivos](../infrastructure/src/main/java/com/toolrental/infrastructure/tool) |
| `toolimage` | [4 archivos](../domain/src/main/java/com/toolrental/domain/toolimage) | [4 archivos](../application/src/main/java/com/toolrental/application/toolimage) | [8 archivos](../infrastructure/src/main/java/com/toolrental/infrastructure/toolimage) |
| `equipmentunit` | [7 archivos](../domain/src/main/java/com/toolrental/domain/equipmentunit) | [5 archivos](../application/src/main/java/com/toolrental/application/equipmentunit) | [9 archivos](../infrastructure/src/main/java/com/toolrental/infrastructure/equipmentunit) |
| `reservation` | [10 archivos](../domain/src/main/java/com/toolrental/domain/reservation) | [9 archivos](../application/src/main/java/com/toolrental/application/reservation) | [7 archivos](../infrastructure/src/main/java/com/toolrental/infrastructure/reservation) |
| `reservationitem` | [5 archivos](../domain/src/main/java/com/toolrental/domain/reservationitem) | [3 archivos](../application/src/main/java/com/toolrental/application/reservationitem) | [7 archivos](../infrastructure/src/main/java/com/toolrental/infrastructure/reservationitem) |
| `rental` | [5 archivos](../domain/src/main/java/com/toolrental/domain/rental) | [4 archivos](../application/src/main/java/com/toolrental/application/rental) | [8 archivos](../infrastructure/src/main/java/com/toolrental/infrastructure/rental) |
| `unitdelivery` | [4 archivos](../domain/src/main/java/com/toolrental/domain/unitdelivery) | [4 archivos](../application/src/main/java/com/toolrental/application/unitdelivery) | [9 archivos](../infrastructure/src/main/java/com/toolrental/infrastructure/unitdelivery) |
| `unitreturn` | [4 archivos](../domain/src/main/java/com/toolrental/domain/unitreturn) | [4 archivos](../application/src/main/java/com/toolrental/application/unitreturn) | [8 archivos](../infrastructure/src/main/java/com/toolrental/infrastructure/unitreturn) |
| `damagereport` | [4 archivos](../domain/src/main/java/com/toolrental/domain/damagereport) | [4 archivos](../application/src/main/java/com/toolrental/application/damagereport) | [8 archivos](../infrastructure/src/main/java/com/toolrental/infrastructure/damagereport) |
| `payment` | [4 archivos](../domain/src/main/java/com/toolrental/domain/payment) | [4 archivos](../application/src/main/java/com/toolrental/application/payment) | [7 archivos](../infrastructure/src/main/java/com/toolrental/infrastructure/payment) |
| `invoice` | [4 archivos](../domain/src/main/java/com/toolrental/domain/invoice) | [5 archivos](../application/src/main/java/com/toolrental/application/invoice) | [8 archivos](../infrastructure/src/main/java/com/toolrental/infrastructure/invoice) |
| `invoiceitem` | [4 archivos](../domain/src/main/java/com/toolrental/domain/invoiceitem) | [3 archivos](../application/src/main/java/com/toolrental/application/invoiceitem) | [7 archivos](../infrastructure/src/main/java/com/toolrental/infrastructure/invoiceitem) |


## Diagnóstico del editor

Abre la raíz real `Tool-rental-platform`, no la antigua carpeta `ProyectoSpringBoot`. El explorador de archivos muestra el árbol físico; la vista Java Projects depende del servidor de lenguaje y puede fallar aunque Maven compile.

Si aparecen paquetes ausentes después de reimportar Maven, ejecuta **Java: Clean Java Language Server Workspace** y permite reiniciar/reindexar. Si persiste el error, consulta **Java: Open Java Language Server Log File**. Un build correcto no acredita por sí solo que el índice del editor se haya reparado.
