# Tool Rental Platform

Plataforma de renta de herramientas y equipos de construcción para clientes, proveedores y administradores. El producto previsto permitirá consultar inventario, reservar equipos, gestionar entregas y devoluciones, registrar pagos y emitir facturas.

**Estado actual: diseño de datos.** Este repositorio contiene el análisis conceptual del enunciado y su derivación lógica hasta cuarta forma normal (4FN). Todavía no contiene una aplicación ejecutable, una base de datos implementada ni endpoints.

## Documentación

Leer en este orden:

1. [Enunciado original](ejercicioDemo.md): requisitos del producto completo.
2. [Modelo conceptual](docs/modelo-conceptual.md): entidades, atributos, cardinalidades, supuestos y diagrama Mermaid.
3. [Normalización hasta 4FN](docs/normalizacion-4fn.md): anomalías, DF/DMV, descomposición de 1FN a 4FN, 18 relaciones y auditoría de claves.

La decisión principal es distinguir **Herramienta** (oferta del catálogo) de **UnidadEquipo** (equipo físico). Cada línea de reserva identifica una unidad. Las imágenes del catálogo y sus unidades se modelan por separado para evitar combinaciones redundantes. Las reglas de negocio asumidas están explícitas en el modelo conceptual.

## Estructura

```text
.
├── ejercicioDemo.md
├── README.md
├── docs/
│   ├── modelo-conceptual.md
│   └── normalizacion-4fn.md
├── .agents/skills/database-normalization/
└── skills-lock.json
```

## Consultar el proyecto

```bash
git clone https://github.com/TuMinipeka/Tool-rental-platform.git
cd Tool-rental-platform
```

Abrir los Markdown en GitHub o en un visor compatible con Mermaid. No se requieren Java, Maven o MySQL para leer esta entrega. El diagrama conceptual se mantiene como texto Mermaid; aún no se ha exportado una imagen del diagrama relacional físico.

## Skill utilizada

Se instaló localmente [database-normalization](.agents/skills/database-normalization/SKILL.md) desde [cookiemonsterdev/agents-skills](https://github.com/cookiemonsterdev/agents-skills), con su licencia original y el registro de procedencia en `skills-lock.json`:

```bash
npx skills add https://github.com/cookiemonsterdev/agents-skills --skill database-normalization --agent codex --yes
```

La skill aporta referencias y ejemplos; los supuestos y dependencias específicos del negocio se documentan en este repositorio. La instalación se detectará en futuros turnos de Codex dentro del proyecto.

## Tecnologías previstas y trabajo pendiente

Según el enunciado: Java 21, Spring Boot, Spring Security/JWT, Spring Data JPA y MySQL; arquitectura hexagonal con DDD y módulos. Frontend con HTML, CSS y JavaScript; posibles alternativas sujetas a la autorización del trainer. Swagger y despliegue en Docker o Tomcat quedan para la implementación.

El enunciado contiene referencias contradictorias a versiones mínimas de Java; se toma Java 21 del requisito específico de backend como objetivo. No se fija todavía una versión concreta de Spring Boot ni dependencias.

- Validar los supuestos de roles múltiples, reserva de un solo proveedor, unidades concretas, pagos parciales y una factura por alquiler.
- Diseñar el modelo físico MySQL, las migraciones, índices y datos de ejemplo requeridos por el enunciado.
- Implementar módulos, reglas transaccionales, seguridad, API y pruebas.
- Añadir instrucciones reales de compilación, configuración y despliegue cuando existan los archivos correspondientes.
- Incorporar documentación de endpoints, ejemplos JSON, Swagger y frontend al implementarlos.

No se incluyen comandos Maven, `application.properties`, credenciales ni rutas de API de una aplicación que aún no existe.

## Validación de esta entrega

La revisión documental cubre trazabilidad con el enunciado, cardinalidades, claves candidatas, dependencias funcionales y multivaluadas, y separación de hechos históricos. Los documentos distinguen las restricciones locales de las que requieren transacciones y reglas del dominio.

No se han ejecutado pruebas de backend ni scripts en MySQL. La conformidad con 4FN se sostiene bajo los supuestos declarados; deberá revisarse si cambian las reglas del negocio.

## Convención de commits

Formato solicitado: `feat: :gitmoji: descripciónEnCamelCase`. Se usan códigos de [Gitmoji](https://gitmoji.dev/), por ejemplo:

```text
feat: :card_file_box: definirModeloConceptualYNormalizacion4fn
feat: :memo: documentarProyectoEnReadme
```

`:card_file_box:` corresponde a cambios de base de datos y `:memo:` a documentación. Se conserva el prefijo `feat:` indicado para esta entrega.
