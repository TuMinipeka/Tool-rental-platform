# Vistas Archify

Abre [index.html](index.html) para navegar las cinco vistas. Cada HTML es autónomo y funciona sin internet después de generado.

| Vista | Fuente editable | Perfil |
| --- | --- | --- |
| Arquitectura hexagonal | `sources/hexagonal-architecture.json` | showcase |
| Secuencia de decisión | `sources/reservation-decision-sequence.json` | showcase |
| Ciclo de vida | `sources/reservation-lifecycle.json` | showcase |
| JWT y autorización | `sources/jwt-authorization-sequence.json` | showcase |
| Modelo relacional | `sources/reservation-erd.json` | standard por densidad |

Las fuentes se verificaron contra el repositorio `https://github.com/TuMinipeka/Tool-rental-platform.git` en la revisión `e3da9000e88168cb10c56d730ffd114c35c1bd69`.

Los archivos `*.delivery.json`, `*.finalize.json`, `*.finalize-summary.json` y `*.browser-check.json` son recibos reproducibles del generador. Los cinco resultados finales aprobaron esquema, evidencia de repositorio, entrega, integridad del HTML y comprobación automatizada en navegador.

El ciclo de vida conserva un cruce de rutas resuelto automáticamente y el ERD conserva un desvío para rodear `delivery_rates`. Son avisos visuales del trazador, no pérdida de relaciones ni fallos de validación.
