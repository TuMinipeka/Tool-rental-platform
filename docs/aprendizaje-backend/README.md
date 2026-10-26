# Kit de preparación para el examen de backend

Este kit enseña a resolver cambios de Spring Boot dentro de este proyecto sin memorizar una lista de archivos. El método es **Ver → Hacer → Explicar**:

1. **Ver:** recorre un flujo real desde HTTP hasta PostgreSQL.
2. **Hacer:** resuelve un enunciado sin consultar el solucionario.
3. **Explicar:** reconstruye el recorrido y justifica cada decisión sin mirar el código.

El caso de referencia es la decisión de un proveedor sobre una solicitud de reserva. En el lenguaje del enunciado, una reserva pasa de `PENDIENTE` a `APROBADA` o `RECHAZADA`. En el código real, los nombres equivalentes son `PENDING`, `CONFIRMED` y `REJECTED`.

## Ruta recomendada

| Sesión | Objetivo | Material | Criterio de salida |
| --- | --- | --- | --- |
| 1 | Entender el recorrido completo | [Caso de reservas](caso-reservas.md) | Puedes dibujar las seis capas sin mirar |
| 2 | Reconocer estados, actores y puertos | [Diagramas Mermaid](diagramas-mermaid.md) | Explicas por qué la regla no vive en el controlador |
| 3 | Resolver cambios con límite de tiempo | [Cinco simulacros](simulacros.md) | Terminas uno en 120 minutos |
| 4 | Corregir tu razonamiento | [Solucionario](soluciones.md) | Identificas el primer punto donde te desviaste |
| 5 | Practicar sustentación | Guiones de dos minutos | Explicas intención, flujo, pruebas y evidencia |

## Mapa mental que debes recordar

```text
HTTP/JWT
   ↓
Controller          traduce HTTP y obtiene el actor autenticado
   ↓
UseCase             coordina autorización, puertos y resultado
   ↓
Aggregate           protege estados e invariantes del negocio
   ↓
Domain Port         expresa lo que el caso de uso necesita
   ↓
JPA Adapter         abre la transacción, bloquea, consulta y guarda
   ↓
PostgreSQL/Flyway   conserva datos y restricciones físicas
```

La dirección de compilación es la inversa a la entrada de la petición:

```text
infrastructure → application → domain
```

Domain no conoce Spring, HTTP, JPA ni PostgreSQL. Application conoce Domain, pero no conoce Infrastructure. Infrastructure ensambla todo.

## Visualizaciones interactivas

Abre [el índice interactivo](archify/index.html) para explorar cinco vistas generadas con Archify:

1. módulos y dependencias hexagonales;
2. secuencia de confirmación y rechazo;
3. ciclo de vida de una reserva;
4. recorrido JWT y autorización del proveedor;
5. modelo relacional reducido.

Las referencias están fijadas al commit `e3da9000e88168cb10c56d730ffd114c35c1bd69`. Por tanto, los enlaces describen ese estado del repositorio, aunque después cambie la rama.

## Estrategia de 120 minutos

| Tiempo | Acción | Resultado visible |
| ---: | --- | --- |
| 0–10 | Subraya actor, verbo, estado inicial, estado final y errores | Tabla de reglas |
| 10–25 | Busca un flujo parecido con `rg` | Lista corta de archivos reales |
| 25–45 | Modifica agregado, enum y pruebas de dominio | Regla de estado demostrada |
| 45–70 | Implementa caso de uso y puertos necesarios | Coordinación sin HTTP ni JPA |
| 70–95 | Añade adaptadores REST/JPA y migración si aplica | Flujo compilable |
| 95–110 | Ejecuta pruebas y llamadas de Insomnia | Evidencia funcional y negativa |
| 110–120 | Revisa diff, base de datos y mensaje del commit | Entrega coherente |

Si una etapa se atasca, no saltes directamente al controlador. Vuelve a escribir estas cinco preguntas:

1. ¿Quién ejecuta la acción?
2. ¿Qué agregado posee la regla?
3. ¿Desde qué estado se permite?
4. ¿Qué información externa necesita el agregado?
5. ¿Qué debe seguir igual si la operación falla?

## Búsquedas de emergencia

```powershell
# Encontrar un endpoint o verbo parecido
rg -n "@PostMapping|confirmation|rejection" infrastructure/src/main/java

# Encontrar estados y reglas
rg -n "enum .*Status|requirePending|BusinessRuleException" domain/src/main/java

# Encontrar casos de uso, puertos y adaptadores
rg -n "class .*UseCase|interface .*Repository|implements .*Repository" application domain infrastructure

# Encontrar pruebas equivalentes
rg -n "assertThrows|CONFIRMED|REJECTED|AvailabilityConflict" domain/src/test application/src/test infrastructure/src/test

# Encontrar la migración y las restricciones
rg -n "reservations|reservation_status|CHECK" infrastructure/src/main/resources/db/migration
```

## Cómo entrenar sin notas

- Cierra esta carpeta y dibuja el recorrido de una petición en menos de 90 segundos.
- Explica la diferencia entre autenticación y autorización con un ejemplo del repositorio.
- Recita el orden: **regla → coordinación → puerto → adaptador → evidencia**.
- Después de cada simulacro, anota solo el primer error de razonamiento, no todos los errores de sintaxis.
- Repite el mismo patrón al día siguiente con nombres distintos.

## Definición de listo

- [ ] Puedo ubicar una regla en la capa correcta.
- [ ] Puedo seguir el actor desde el JWT hasta el caso de uso.
- [ ] Puedo explicar por qué disponibilidad y confirmación son atómicas.
- [ ] Puedo escribir un caso feliz y tres casos de error antes de programar.
- [ ] Puedo demostrar el cambio con prueba automática, Insomnia y SQL.
- [ ] Puedo dar una explicación de dos minutos sin abrir el código.

