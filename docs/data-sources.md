# Fuentes de los datos de demostración

Los datos locales usan nombres ficticios para usuarios, proveedores y operaciones. Las herramientas y tarifas diarias se basan en ofertas públicas de alquiler en Colombia consultadas el 15 de octubre de 2026. Son referencias para una simulación académica y no constituyen una cotización ni una recomendación comercial.

| Herramienta de demostración | Tarifa diaria usada | Referencia pública |
| --- | ---: | --- |
| Taladro percutor de 800 W | $10.000 COP | [Ferretería Donda, Medellín](https://ferreteriadonda.com/servicios/alquiler-de-herramienta-electrica/) |
| Rotomartillo de 850 W | $20.000 COP | [Ferretería Donda, Medellín](https://ferreteriadonda.com/servicios/alquiler-de-herramienta-electrica/) |
| Sierra circular de 7 1/4 pulgadas | $10.000 COP | [Ferretería Donda, Medellín](https://ferreteriadonda.com/servicios/alquiler-de-herramienta-electrica/) |
| Compresor silencioso de 42 L y 2 HP | $50.000 COP | [Ferretería Donda, Medellín](https://ferreteriadonda.com/servicios/alquiler-de-herramienta-electrica/) |
| Mezcladora de concreto de dos bultos | $60.000 COP | [Diemec, Bucaramanga](https://diemec.catalog.kyte.site/) |

Como comprobación adicional de escala, otros proveedores colombianos publican mezcladoras desde $80.000 COP por día y rotomartillos alrededor de $35.000 COP por jornadas de 18 horas. Las diferencias dependen de ciudad, capacidad, marca, depósito, duración y servicios incluidos.

Las migraciones ubicadas en `db/demo` solo se cargan con el perfil `local`. Producción conserva exclusivamente las migraciones estructurales de `db/migration` para evitar la creación automática de cuentas, operaciones o documentos simulados.
