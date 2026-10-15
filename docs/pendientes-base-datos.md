# Trabajo pendiente de base de datos

## Estado actual

La capa de persistencia incluye PostgreSQL 17.11, Spring Data JPA, Flyway, perfiles de entorno, una migración por cada una de las 18 tablas normalizadas, una migración de compatibilidad JPA, una migración para el rol administrador y 18 migraciones locales de demostración con un mínimo de cinco filas por tabla.

Las 18 tablas tienen mapeos JPA, referencias escalares, claves compartidas o compuestas, casos de uso y adaptadores REST. El entorno local se aprovisiona con Docker Compose. Testcontainers crea una base vacía y verifica las 21 migraciones en el escenario demo, `ddl-auto: validate`, contratos HTTP, datos demo, instantáneas históricas, rollback transaccional, unicidad concurrente de inventario y prevención de doble reserva.

La base de datos todavía no está lista para producción porque faltan reglas comerciales, validación de actualizaciones sobre datos persistentes, controles operativos y las ampliaciones descritas abajo.

## Trabajo operativo prioritario

- [x] Aprovisionar PostgreSQL local de forma reproducible.
- [x] Aplicar Flyway desde una base de datos vacía y validar las migraciones actuales.
- [x] Ejecutar e inspeccionar las migraciones de `db/demo` mediante el perfil adicional `demo`; producción no incluye esa ubicación.
- [x] Agregar cobertura con Testcontainers para mapeos, repositorios, migraciones, rollback y bloqueos.
- [x] Demostrar que las confirmaciones concurrentes no pueden reservar la misma unidad de equipo.
- [x] Demostrar la unicidad de `inventory_code` por proveedor bajo escrituras concurrentes.
- [ ] Aprovisionar los futuros entornos compartidos de integración, pruebas y producción.
- [ ] Validar las actualizaciones incrementales entre versiones cuando exista un entorno persistente compartido.
- Definir roles de base de datos con mínimo privilegio, límites del pool de conexiones, tiempos de espera, monitoreo, copias de seguridad, restauración y retención.

## Adiciones al esquema requeridas por el alcance aprobado del producto

Estos son requisitos de diseño, no tablas implementadas. Requieren migraciones versionadas de Flyway después de aprobar los parámetros restantes de la Fase 0.

### Precios e impuestos

- Guardar una instantánea de precios de la reserva con subtotal del alquiler, cargo de entrega, cargo de protección, descuentos, base gravable, tarifa de IVA, valor del IVA, redondeo y total final.
- Conservar la versión de la regla tributaria o del método de cálculo usada en cada transacción.
- Conservar el costo del equipo u otra entrada requerida por el cálculo legalmente validado de la base gravable, sin permitir que cambios posteriores del catálogo reescriban totales históricos.
- Guardar el detalle tributario por línea de factura en lugar de derivar impuestos históricos de la configuración actual.

### Plan Opcional de Protección contra Daños

- Versionar las definiciones del Plan de Protección por separado de las reservas.
- Guardar con cada reserva protegida la versión seleccionada del plan, precio, deducible, límite, exclusiones aceptadas y marca de tiempo de aceptación.
- Mantener las condiciones del plan independientes de los reportes de daños. Combinar varias cláusulas del plan y varios reportes de daños en una fila crearía productos cruzados falsos y anomalías de actualización; las relaciones separadas conservan la orientación actual hacia 4FN.

### Entrega, devolución, evidencias y disputas

- Admitir varios elementos de evidencia por entrega, devolución y reporte de daño mediante relaciones de evidencia separadas, no listas de URL ni columnas repetidas.
- Guardar metadatos de integridad del contenido, tipo de medio, marca de tiempo de creación, autor y referencia de almacenamiento para cada evidencia.
- Agregar estimaciones de reparación, instantáneas del valor de reposición, decisiones de valoración, avisos al cliente, registros de disputas, decisiones del administrador e historial de ajustes.
- Garantizar que una misma pérdida no se cobre como reparación completa y también como reposición completa.

### Pagos y reembolsos

- Reemplazar el modelo genérico actual de pago por un modelo auditable de intención y transacción que admita el esquema 50/50, cargos adicionales, reembolsos, claves de idempotencia, intentos y motivos de fallo.
- Guardar el tipo de método simulado, procesador, red de tarjeta, token, últimos cuatro dígitos y metadatos de vencimiento sin almacenar el número completo de tarjeta ni el CVV.
- Conservar la relación entre cada reembolso o ajuste y su cargo original.

### Documentos legales y consentimiento

- Versionar los Términos y condiciones, avisos de privacidad y condiciones del Plan de Protección como documentos publicados inmutables.
- Registrar la aceptación explícita de cada usuario con versión del documento, propósito, marca de tiempo, transacción relacionada cuando corresponda y referencia de integridad.
- Mantener como hechos independientes la aceptación obligatoria de la transacción, la autorización de datos personales, el consentimiento opcional de marketing y la selección opcional de protección. Una fila de consentimiento combinada forzaría hechos multivaluados no relacionados a combinaciones falsas y perjudicaría el historial de revocación.

### Facturas simuladas

- Agregar subtotal, base gravable, IVA, total, moneda y marcadores de documento académico.
- Conservar instantáneas de identidad del proveedor y del cliente para que los documentos históricos no se reescriban cuando cambien los perfiles.
- Impedir que los documentos simulados se presenten como facturas válidas ante la DIAN.

## Capacidades de persistencia todavía pendientes

- Definiciones, selecciones y valoraciones de protección, evidencias y disputas.
- Intenciones e intentos de pago mediante una pasarela, reembolsos y ajustes.
- Impuestos e instantáneas completas del cálculo de precios.
- Publicación y aceptación versionada de documentos legales.
- Publicación durable de eventos mediante un Outbox u otro mecanismo transaccional.
- Reportes administrativos y consultas operativas optimizadas.

Los registros administrativos de pagos y las facturas base ya tienen dominio, casos de uso, persistencia y REST, pero no sustituyen las capacidades comerciales, tributarias o legales anteriores.

## Criterio de finalización

La base de datos estará lista únicamente cuando una instancia nueva de PostgreSQL pueda aplicar todas las migraciones, iniciar la aplicación con `ddl-auto: validate`, superar los escenarios aprobados de integración y concurrencia, soportar el contrato completo del producto y usar procedimientos verificados de copias de seguridad, restauración, seguridad y despliegue.
