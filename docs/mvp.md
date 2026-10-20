# Tarea 1 — Daniel Despliegue e integración

### Objetivo: Dejar la aplicación ejecutándose, integrada, documentada y con evidencias de funcionamiento. Actividades asignadas

1. Despliegue completo: configurar Docker Compose, PostgreSQL, backend y frontend. Revisar .env, CORS, JWT, puertos, persistencia y conexión de servicios.,
2. Base de datos: comprobar migraciones Flyway, crear o completar los datos de prueba exigidos (20 registros por entidad según el enunciado) y validar relaciones e índices.,
3. Modelo lógico y físico: entregar el diagrama relacional con PK, FK y cardinalidades; acompañarlo con el esquema físico y la explicación de 4FN. Ya existe un diagrama conceptual que puede aprovecharse.,
4. Integración del frontend: completar las operaciones visibles para cliente, proveedor y administrador usando los endpoints de Conde. Priorizar historial, gestión de reservas, estados, pagos y reportes.,
5. Documentación: terminar README, instrucciones de compilación y despliegue, capturas, configuración y ejemplos. Verificar Swagger en la aplicación desplegada.,
6. Pruebas finales y GitHub: ejecutar la suite de integración con la base real, probar el flujo completo, revisar la gestión de ramas y commits y preparar evidencias para la presentación.,







# Tarea 2 — Conde Objetivo: Completar las funcionalidades que todavía impiden demostrar el flujo real de alquiler. Actividades asignadas

1. Completar los CRUD: revisar especialmente clientes, proveedores, reservas y pagos. Faltan operaciones en algunos controladores, como eliminación de clientes y proveedores. Usar eliminación lógica cuando corresponda para conservar historial financiero.,

2. Sistema de reservas: cerrar el flujo de solicitud, confirmación automática, validación de disponibilidad, expiración, cancelación, entrega y devolución. Actualmente parte del recorrido depende de aprobación del proveedor.,

3. Pagos y facturación: evolucionar el pago simulado hacia una pasarela sandbox verificable, confirmar operaciones de forma segura y generar automáticamente comprobantes o facturas descargables.,

4. Reportes y estadísticas: implementar endpoints de ingresos, herramientas más alquiladas, clientes frecuentes, disponibilidad y rentabilidad, con permisos según el rol.,

5. Seguridad JWT y roles: probar registro, autenticación, permisos de cliente/proveedor/admin y accesos prohibidos. Completar el proceso de incorporación de proveedores y sus validaciones.,

6. Documentación y pruebas backend: documentar endpoints con Swagger, agregar ejemplos JSON y pruebas de lógica, autorización, pagos, disponibilidad y concurrencia. Entregar contratos claros para que Daniel conecte las pantallas.

7. Mejorar Frontend y funcionalidades: Para integrara a futuro la rama developer/daniel/adds y lograr un cambio visual extraordinario




## Prioridades para finalizar el MVP



P0    Pagos, facturación y flujo completo de reservas    Conde

P0    Reportes, CRUD faltantes y permisos    Conde

P0    Despliegue funcional y conexión completa    Daniel

P0    Paneles y recorridos conectados a la API    Daniel + Conde

P1    Datos SQL, diagrama relacional y 4FN    Daniel

P1    Swagger, pruebas, README y GitHub    Ambos

#### Importante: el documento docs/validation-report.md informa que las últimas pruebas dependientes de PostgreSQL no pudieron completarse por problemas del entorno Docker. Daniel debe cerrarlas; Conde debe corremvgir cualquier fallo que aparezca en el backend.



## Lista de cierre del MVP

[ ] Los tres roles pueden iniciar sesión y tienen permisos correctos.
[ ] Un proveedor puede gestionar herramientas y unidades.
[ ] Un cliente puede reservar una herramienta realmente disponible.
[ ] La reserva pasa correctamente por pago, confirmación, entrega y devolución.
[ ] El pago sandbox y la factura funcionan.
[ ] Los paneles muestran reportes con información real.
[ ] Los CRUD y las pruebas principales funcionan.
[ ] La aplicación se despliega y la documentación está completa.
