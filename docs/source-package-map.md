# Source package map

The project uses 18 table-aligned contexts in each of its three Maven modules. `common` contains shared contracts and adapters. Context folders are below `src/main/java/com/toolrental/<module>/`.

## Former package locations

| Former package | Current context/location |
| --- | --- |
| `catalog` category classes | `category` in domain, application and infrastructure |
| `catalog` tool classes | `tool` in all three modules |
| `catalog` physical equipment classes | `equipmentunit` in all three modules |
| Tool images stored inside the original catalog | `toolimage` in all three modules |
| `identity` user entity and repository | `infrastructure/user/adapters/out/persistence` |
| `identity` customer profile entity and repository | `infrastructure/customer/adapters/out/persistence` |
| `identity` supplier profile entity and repository | `infrastructure/supplier/adapters/out/persistence` |
| `identity` participant adapter | `infrastructure/common/adapters/out/persistence/ParticipantsPersistenceAdapter.java` |
| Role definitions and assignments | `role` and `userrole` in all three modules |

`catalog` and `identity` were replaced by these package locations. Their obsolete empty directories are removed locally; no compatibility copies or duplicate JPA mappings are introduced.

## All contexts

| Context | Domain | Application | Infrastructure |
| --- | --- | --- | --- |
| `user` | [5 files](../domain/src/main/java/com/toolrental/domain/user) | [4 files](../application/src/main/java/com/toolrental/application/user) | [8 files](../infrastructure/src/main/java/com/toolrental/infrastructure/user) |
| `role` | [4 files](../domain/src/main/java/com/toolrental/domain/role) | [4 files](../application/src/main/java/com/toolrental/application/role) | [7 files](../infrastructure/src/main/java/com/toolrental/infrastructure/role) |
| `userrole` | [4 files](../domain/src/main/java/com/toolrental/domain/userrole) | [4 files](../application/src/main/java/com/toolrental/application/userrole) | [8 files](../infrastructure/src/main/java/com/toolrental/infrastructure/userrole) |
| `customer` | [4 files](../domain/src/main/java/com/toolrental/domain/customer) | [4 files](../application/src/main/java/com/toolrental/application/customer) | [7 files](../infrastructure/src/main/java/com/toolrental/infrastructure/customer) |
| `supplier` | [4 files](../domain/src/main/java/com/toolrental/domain/supplier) | [4 files](../application/src/main/java/com/toolrental/application/supplier) | [7 files](../infrastructure/src/main/java/com/toolrental/infrastructure/supplier) |
| `category` | [5 files](../domain/src/main/java/com/toolrental/domain/category) | [5 files](../application/src/main/java/com/toolrental/application/category) | [7 files](../infrastructure/src/main/java/com/toolrental/infrastructure/category) |
| `tool` | [5 files](../domain/src/main/java/com/toolrental/domain/tool) | [6 files](../application/src/main/java/com/toolrental/application/tool) | [8 files](../infrastructure/src/main/java/com/toolrental/infrastructure/tool) |
| `toolimage` | [4 files](../domain/src/main/java/com/toolrental/domain/toolimage) | [4 files](../application/src/main/java/com/toolrental/application/toolimage) | [8 files](../infrastructure/src/main/java/com/toolrental/infrastructure/toolimage) |
| `equipmentunit` | [7 files](../domain/src/main/java/com/toolrental/domain/equipmentunit) | [5 files](../application/src/main/java/com/toolrental/application/equipmentunit) | [9 files](../infrastructure/src/main/java/com/toolrental/infrastructure/equipmentunit) |
| `reservation` | [10 files](../domain/src/main/java/com/toolrental/domain/reservation) | [9 files](../application/src/main/java/com/toolrental/application/reservation) | [7 files](../infrastructure/src/main/java/com/toolrental/infrastructure/reservation) |
| `reservationitem` | [5 files](../domain/src/main/java/com/toolrental/domain/reservationitem) | [3 files](../application/src/main/java/com/toolrental/application/reservationitem) | [7 files](../infrastructure/src/main/java/com/toolrental/infrastructure/reservationitem) |
| `rental` | [5 files](../domain/src/main/java/com/toolrental/domain/rental) | [4 files](../application/src/main/java/com/toolrental/application/rental) | [8 files](../infrastructure/src/main/java/com/toolrental/infrastructure/rental) |
| `unitdelivery` | [4 files](../domain/src/main/java/com/toolrental/domain/unitdelivery) | [4 files](../application/src/main/java/com/toolrental/application/unitdelivery) | [9 files](../infrastructure/src/main/java/com/toolrental/infrastructure/unitdelivery) |
| `unitreturn` | [4 files](../domain/src/main/java/com/toolrental/domain/unitreturn) | [4 files](../application/src/main/java/com/toolrental/application/unitreturn) | [8 files](../infrastructure/src/main/java/com/toolrental/infrastructure/unitreturn) |
| `damagereport` | [4 files](../domain/src/main/java/com/toolrental/domain/damagereport) | [4 files](../application/src/main/java/com/toolrental/application/damagereport) | [8 files](../infrastructure/src/main/java/com/toolrental/infrastructure/damagereport) |
| `payment` | [4 files](../domain/src/main/java/com/toolrental/domain/payment) | [4 files](../application/src/main/java/com/toolrental/application/payment) | [7 files](../infrastructure/src/main/java/com/toolrental/infrastructure/payment) |
| `invoice` | [4 files](../domain/src/main/java/com/toolrental/domain/invoice) | [5 files](../application/src/main/java/com/toolrental/application/invoice) | [8 files](../infrastructure/src/main/java/com/toolrental/infrastructure/invoice) |
| `invoiceitem` | [4 files](../domain/src/main/java/com/toolrental/domain/invoiceitem) | [3 files](../application/src/main/java/com/toolrental/application/invoiceitem) | [7 files](../infrastructure/src/main/java/com/toolrental/infrastructure/invoiceitem) |

## Editor troubleshooting

Use the actual repository root `Tool-rental-platform`, rather than the former `ProyectoSpringBoot` folder. In VS Code, the ordinary Explorer (Ctrl+Shift+E) shows the physical source tree. The Java Projects view depends on the Java language server and may fail even while Maven compiles successfully.

A local Java language-server log reported `Java Model Exception ... tool-rental-platform does not exist` while requesting package children. The source audit found no package/path mismatch, duplicate production class or unresolved internal source import. This establishes an editor indexing failure separately from source compilation; it does not prove that the editor index has been repaired.

To rebuild that index, run **Java: Clean Java Language Server Workspace** from the VS Code command palette and allow its restart/reimport. If the same diagnostic persists, retain the exact message and inspect **Java: Open Java Language Server Log File**. Do not recreate classes under the old empty packages to address an editor error.
