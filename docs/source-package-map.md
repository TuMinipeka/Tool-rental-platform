# Mapa de paquetes

Cada contexto conserva el mismo sentido en los tres módulos:

```text
domain/<context>/
  event/ model/aggregate/ model/entity/ model/valueobject/ port/repository/
application/<context>/
  command/ dto/ exception/ usecase/
infrastructure/<context>/
  adapters/in/rest/controllers/ adapters/in/rest/dtos/
  adapters/out/persistence/entity/ mapper/ repository/ config/
```

Solo se crean carpetas con clases reales. Los adaptadores de persistencia están directamente en `adapters/out/persistence`; `repository` queda para Spring Data.

## Contextos base

`user`, `role`, `userrole`, `customer`, `supplier`, `category`, `tool`, `toolimage`, `equipmentunit`, `reservation`, `reservationitem`, `rental`, `unitdelivery`, `unitreturn`, `damagereport`, `payment`, `invoice`, `invoiceitem`.

## Contextos críticos

`usersession`, `passwordreset`, `cancellationpolicy`, `reservationtermination`, `deliveryrate`, `reservationextension`, `refund`.

En estos contextos, la coordinación y los adaptadores pertenecen a su paquete propietario. `passwordreset` conserva el modelo y persistencia histórica sin exponer endpoints; sesión permanece en `infrastructure.usersession` y políticas/tarifas en sus contextos respectivos. Las rutas públicas conservan `/api/v1/auth` y `/api/v1/commercial-configuration`.

`common` no representa una tabla. Reúne `AggregateRoot`, `DomainEvent`, `Money`, puertos transversales, Unit of Work, seguridad HTTP, transacciones y configuración compartida.

Los nombres de paquetes y archivos están en inglés. Las relaciones entre contextos usan identificadores tipados en Domain y UUID escalares en JPA.
