# Despliegue y operación del MVP

> Gestión de herramientas, imágenes y persistencia: [contratos y verificación actual](provider-persistence.md). Las evidencias anteriores conservan su revisión histórica.

## Estado y separación de entornos

La entrega prepara una **demo pública académica**. Actualmente se ejecuta en Docker local y no está publicada en Oracle. El proveedor vigente es interno, persistente y usa cinco medios con datos fijos; no requiere claves financieras externas. El cierre depende del [MVP](mvp.md).

| Entorno | Composición | Datos / acceso |
| --- | --- | --- |
| Desarrollo existente | compose.yml | Base de trabajo; puertos locales 5173/8080 y PostgreSQL local |
| Validación aislada | compose.validation.yml | Proyecto tool-rental-mvp, volumen exclusivo, frontend/proxy 15173 y backend 18080, solo loopback |
| Publicación académica | compose.showcase.yml | Base exclusiva, production + showcase, Caddy HTTPS; ningún puerto público de PostgreSQL/backend |

Nunca uses la base de trabajo para fixtures o pruebas de restauración. No ejecutes down -v contra un volumen que debas conservar.

## Validación local reproducible

Requisitos: Git, JDK 21, Node.js 24, Chrome y Docker con Compose operativo. Desde la raíz:

~~~powershell
node scripts/prepare-validation.mjs
docker compose --env-file .env.validation -f compose.validation.yml config --quiet
docker compose --env-file .env.validation -f compose.validation.yml up --build -d
# Espera a que /status/database responda connected: true.
Invoke-RestMethod http://localhost:15173/status/database
node scripts/audit-database.mjs
node frontend/scripts/verify-fictional-mvp.mjs
node scripts/verify-backup.mjs
~~~

prepare-validation conserva cualquier archivo privado existente y no imprime secretos. El frontend usa /api/v1 bajo el mismo origen mediante Caddy. Mailpit de pruebas queda exclusivamente en localhost:18025. Los tres accesos demo documentados en README son válidos solo para esta base privada; no se llevan a la publicación.

Los scripts de integración escriben registros ficticios en la base aislada. verify-backup crea una base de restauración separada, compara conteos y reinicia únicamente PostgreSQL/backend de tool-rental-mvp. El dump permanece privado dentro del contenedor y no se versiona.

Para reiniciar o recompilar después de un merge usa up --build -d; actualizar el navegador no reconstruye las imágenes.

## Preparación de Oracle ARM

Destino previsto: Ubuntu ARM64 en una VM Always Free A1, hasta el cupo que muestre la cuenta. Verifica las condiciones y disponibilidad en la [documentación oficial](https://docs.oracle.com/en-us/iaas/Content/FreeTier/freetier_topic-Always_Free_Resources.htm). No se autoriza contratar capacidad de pago. La disponibilidad y la permanencia de una instancia gratuita no están garantizadas.

Instala Git, Docker Engine/Compose, curl y rsync. Reserva una IPv4 si está incluida en la cuenta, registra el subdominio gratuito y configura DNS. En OCI y en el firewall del host permite 80/443; limita SSH a la dirección de administración. No abras 5432, 8080, 5173 ni Mailpit.

La [verificación de imágenes](evidence/mvp/arm-manifests.json) comprueba manifiestos ARM64, no ejecución en Oracle. La JVM usa una base Ubuntu compatible y usuario sin privilegios. El ensayo ARM real queda pendiente.

## Imágenes identificadas por revisión

Desde una copia limpia y validada, autentica Docker en el registro elegido. Con IMAGE_PREFIX configurado, ejecuta scripts/build-release.sh. Construye backend y frontend para amd64/arm64, publica etiquetas con el SHA completo del commit y configura BACKEND_IMAGE/FRONTEND_IMAGE con esas etiquetas o sus digests. El frontend público exige API real y no incorpora un segundo catálogo ficticio cuando falla el servidor.

La publicación de imágenes no despliega la aplicación. No uses latest para una revisión aprobada ni cambies la imagen sin repetir las pruebas afectadas.

## Configuración pública privada

Copia .env.showcase.example a .env.showcase, limita sus permisos a 600 y completa sus campos. Nunca subas ese archivo. No actives showcase sobre la base de trabajo: el arranque exige production y un nombre de base explícitamente aislado que contenga showcase.

| Variables | Requisito |
| --- | --- |
| PUBLIC_HOST / ACME_EMAIL | Subdominio registrado y contacto para certificados |
| POSTGRES_DB / POSTGRES_USER / POSTGRES_PASSWORD | Base y acceso exclusivos; contraseña aleatoria |
| JWT_SECRET | Secreto aleatorio externo de al menos 32 bytes |
| PAYMENT_PROVIDER | `fictional` |
| PAYMENTS_PUBLIC_URL | Origen HTTPS público para el retorno a la aplicación |
| BACKEND_IMAGE / FRONTEND_IMAGE | Imágenes verificadas por commit o digest |


CORS permite exactamente el origen HTTPS configurado; el refresh es HttpOnly/Secure/SameSite=Lax y la API conserva CSRF. Spring procesa cabeceras reenviadas solo detrás del proxy privado. La especificación conserva exclusivamente /api/v1; el alias heredado no se anuncia como contrato nuevo.

V303 desactiva todos los usuarios precargados y revoca sesiones/tokens anteriores únicamente al instalar showcase. Antes de abrir la demo provisiona evaluadores privados. Con EVALUATOR_EMAIL (prefijo evaluator.), EVALUATOR_PASSWORD y EVALUATOR_ROLE configurados en la sesión privada, scripts/provision-evaluator.sh ejecuta una herramienta offline, guarda BCrypt y revoca sesiones/tokens anteriores. El operador necesita acceso al servidor y al contenedor; no existe un endpoint HTTP de bootstrap. Retira después la contraseña del entorno de la terminal. No reutilices cuentas o contraseñas demo conocidas.

## DNS y HTTPS

Registra un subdominio disponible en DuckDNS. Configura DUCKDNS_SUBDOMAIN, DUCKDNS_TOKEN y PUBLIC_IPV4 en el entorno privado y ejecuta scripts/update-duckdns.sh; el token se transmite por stdin, no en argumentos ni logs. Comprueba el registro A antes de arrancar Caddy. No se ha registrado ni actualizado ningún dominio durante esta entrega.

Caddy conserva certificados en volúmenes, sirve la SPA y enruta /api, Swagger, OpenAPI y /status/database al backend. Consulta [la documentación del proxy](https://caddyserver.com/docs/caddyfile/directives/reverse_proxy).

## Publicación y verificación final

Todos los campos de publicReleaseGates deben estar verificados en evidence/mvp/status.json, con evidencias actuales. scripts/release.sh falla si falta alguno. Solo entonces habilita Caddy, descarga las imágenes aprobadas y ejecuta Compose con espera de arranque. El Compose público mantiene Caddy bloqueado por defecto aunque alguien omita el script.

Comprobar externamente HTTPS válido, `/`, `/status/database`, Swagger, rutas React, tres roles, renovación/CSRF, los cinco medios fijos, el comprobante descargable y los reportes. Capturar evidencias sin tokens ni secretos.

## Respaldo y recuperación

Configura acceso SSH restringido a un destino externo y BACKUP_TARGET=user@host:/private/path/. Ejecuta scripts/backup.sh: genera pg_dump custom, copia fuera del servidor y solo después conserva las últimas siete copias locales. La retención externa se configura también para siete copias en el destino. Si la copia externa falla, no purga el historial local.

Programa diariamente a las 03:00 UTC desde cron, ejecutando el script desde la ruta del repositorio y cargando BACKUP_TARGET desde un archivo privado. Verifica espacio, permisos, recepción y alertas del cron. El ensayo local no acredita esta copia externa.

Para restaurar, crea una base exclusiva y usa pg_restore --no-owner --no-acl --exit-on-error. Compara conteos, historial Flyway y restricciones antes de aceptar el respaldo. [Referencia PostgreSQL](https://www.postgresql.org/docs/17/app-pgrestore.html).

Antes de actualizar guarda las referencias de imágenes y el respaldo probado. Para revertir una aplicación selecciona la imagen anterior compatible y repite Compose y los smoke checks. No reviertas Flyway ni restaures sobre la base activa automáticamente. Si el esquema ya no es compatible, detén escrituras y restaura en un destino nuevo durante una ventana controlada.

## Límites restantes

Cuenta/cupo Oracle, DNS/HTTPS real, ensayo ARM, revisión manual del pago fijo en el destino público, respaldo externo y accesos del entorno destino mantienen bloqueada la publicación. Los recorridos internos, facturas, reportes y roles tienen evidencia histórica local. La separación del usuario migrador y usuario de ejecución, alertas avanzadas y validación fiscal/legal se mantienen como requisitos adicionales de una operación comercial.

## Migraciones tras integrar Conde

V209–V210 llegaron después de bases demo que ya habían aplicado V302. Para el ensayo conjunto se creó una base nueva exclusiva: no se habilitó out-of-order ni se alteraron checksums. Si tu base desechable ya superó esas versiones, crea otra base aislada; en una base con datos necesarios prepara y revisa una migración hacia delante. Las nuevas versiones compartidas deben coordinarse por encima del máximo del historial combinado, actualmente V307.

## Apache Tomcat integrado

La modalidad confirmada es JAR ejecutable con Apache Tomcat integrado. Spring Boot administra el ciclo de vida del servidor al arrancar la aplicación. El backend conserva spring-boot-starter-web, que incorpora Tomcat; no se excluye esa dependencia ni se marca como provided.

| Ejecución | Qué ocurre |
| --- | --- |
| `java -jar infrastructure/target/infrastructure-0.1.0-SNAPSHOT.jar` | Java 21 arranca Spring Boot y Apache Tomcat en 8080 |
| Dockerfile del backend | Compila el JAR, incorpora un JRE 21 y ejecuta java -jar como usuario no root |
| Compose público | Mantiene Tomcat y PostgreSQL privados; Caddy expone HTTPS y enruta /api al backend:8080 |

Para comprobarlo localmente:

```powershell
.\mvnw.cmd clean verify
docker compose --env-file .env.validation -f compose.validation.yml up -d --build
node scripts/verify-tomcat.mjs
node frontend/scripts/verify-fictional-mvp.mjs
```

El verificador registra la versión real de Tomcat, el comando de arranque y la conexión JDBC en [evidencia de Tomcat](evidence/mvp/tomcat-runtime.json). La instalación de Maven utiliza JDK 21; el Java del PATH puede ser distinto. Docker ya incluye su propio JRE y no depende del Java del host.

Para publicar, completa `.env.showcase` con dominio, certificados, PostgreSQL, JWT e imágenes. El proveedor académico no requiere SMTP ni credenciales financieras externas. Los valores fijos no se administran desde la web.

No se ha aprovisionado una VM ni un dominio en esta entrega. El servidor local no acredita una publicación pública. Las operaciones de actualización/reversión y respaldo descritas arriba siguen vigentes.

El WAR corresponde a otro modo: desplegar dentro de un Tomcat instalado por separado. No es la opción elegida. [Documentación oficial de Spring Boot](https://docs.spring.io/spring-boot/3.5/how-to/deployment/traditional-deployment.html).
