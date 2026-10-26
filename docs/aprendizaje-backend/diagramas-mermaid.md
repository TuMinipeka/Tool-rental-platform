# Diagramas estáticos de respaldo

Estos diagramas resumen el mismo contenido que las vistas Archify. GitHub puede renderizarlos directamente y no requieren conexión externa.

## 1. Arquitectura hexagonal

```mermaid
flowchart LR
    Supplier[Proveedor / Insomnia] -->|POST + JWT| Security[Spring Security]
    Security -->|Principal| Controller[ReservationController]
    Controller -->|ReservationId + SupplierId| UseCase[Confirm / Reject UseCase]
    UseCase -->|confirmWithRates / reject| Aggregate[Reservation Aggregate]
    UseCase -->|execute| Port[AtomicReservationManagement]
    Port -. implementado por .-> Adapter[ReservationPersistenceAdapter]
    Adapter -->|JPA + locks| Database[(PostgreSQL)]

    subgraph Infrastructure
      Security
      Controller
      Adapter
    end
    subgraph Application
      UseCase
    end
    subgraph Domain
      Aggregate
      Port
    end
```

## 2. Secuencia de confirmación y rechazo

```mermaid
sequenceDiagram
    actor P as Proveedor
    participant C as ReservationController
    participant U as UseCase
    participant A as AtomicReservationManagement
    participant R as Reservation
    participant DB as PostgreSQL

    P->>C: POST /reservations/{id}/confirmation + JWT
    C->>U: execute(id, actor)
    U->>A: execute(id, operación)
    A->>DB: SELECT reserva/unidades FOR UPDATE
    DB-->>A: estado y contexto bloqueado
    A-->>U: Context
    U->>U: valida propiedad, perfiles y disponibilidad
    alt todo válido
        U->>R: confirmWithRates(...)
        R-->>U: CONFIRMED + evento
        U-->>A: agregado actualizado
        A->>DB: UPDATE + COMMIT
        A-->>C: ReservationResponse
        C-->>P: 200 status=CONFIRMED
    else ocupación o regla inválida
        U--xA: excepción
        A->>DB: ROLLBACK
        C-->>P: 403, 404 o 409 ProblemDetail
    end

    Note over P,R: Rechazar reutiliza el límite atómico y llama a Reservation.reject
```

## 3. Estados de la reserva

```mermaid
stateDiagram-v2
    [*] --> PENDING: request
    PENDING --> CONFIRMED: proveedor confirma
    PENDING --> REJECTED: proveedor rechaza
    PENDING --> PENDING_PAYMENT: se crea retención
    PENDING --> CANCELLED: cliente cancela
    PENDING_PAYMENT --> CONFIRMED: pago confirmado
    PENDING_PAYMENT --> PAYMENT_FAILED: pago falla
    PENDING_PAYMENT --> EXPIRED: retención vence
    CONFIRMED --> CANCELLATION_REVIEW: retracto o revisión
    CANCELLATION_REVIEW --> CONFIRMED: revisión rechazada
    CANCELLATION_REVIEW --> CANCELLED: revisión aprobada
    CONFIRMED --> NO_SHOW: inasistencia
    CONFIRMED --> CANCELLED: cancelación permitida
    REJECTED --> [*]
    CANCELLED --> [*]
    PAYMENT_FAILED --> [*]
    EXPIRED --> [*]
    NO_SHOW --> [*]
```

## 4. JWT y autorización

```mermaid
sequenceDiagram
    actor P as Proveedor
    participant F as SecurityFilterChain
    participant D as JwtDecoder
    participant S as SessionJwtValidator
    participant C as ReservationController
    participant U as ConfirmReservationUseCase

    P->>F: Authorization Bearer token
    F->>D: valida HS256, issuer, exp y purpose=access
    D->>S: valida sid y sesión activa
    S-->>D: sesión válida
    D-->>F: sub + roles
    F->>C: Principal autenticado
    C->>C: Actor.id(principal)
    C->>U: ReservationId + SupplierId del sub
    U->>U: compara propietario y proveedor activo
    alt propietario activo
        U-->>P: operación autorizada
    else usuario ajeno o perfil inactivo
        U-->>P: 403 ACCESS_DENIED
    end
```

## 5. Modelo relacional reducido

```mermaid
erDiagram
    USERS ||--o| SUPPLIERS : posee_perfil
    SUPPLIERS ||--o{ TOOLS : publica
    TOOLS ||--o{ EQUIPMENT_UNITS : contiene
    RESERVATIONS ||--|{ RESERVATION_ITEMS : incluye
    EQUIPMENT_UNITS ||--o{ RESERVATION_ITEMS : es_reservada

    USERS {
        uuid user_id PK
        varchar email UK
        varchar account_status
    }
    SUPPLIERS {
        uuid user_id PK,FK
        varchar business_name
    }
    TOOLS {
        uuid tool_id PK
        uuid supplier_id FK
        numeric current_daily_rate
        varchar publication_status
    }
    EQUIPMENT_UNITS {
        uuid equipment_unit_id PK
        uuid tool_id FK
        varchar inventory_code
        varchar operational_status
    }
    RESERVATIONS {
        uuid reservation_id PK
        uuid customer_id FK
        varchar reservation_status
        date start_date
        date effective_return_date
        timestamptz confirmed_at
    }
    RESERVATION_ITEMS {
        uuid reservation_id PK,FK
        uuid equipment_unit_id PK,FK
        numeric agreed_daily_rate
    }
```

