# Shipment Service

## Purpose

`shipment-service` owns the delivery lifecycle of an order. It creates shipment records after a paid order, stores a shipping address snapshot, selects a carrier, manages tracking/status changes, publishes shipment lifecycle events, and synchronizes shipment status back to Order Service.

The service is intentionally not a checkout orchestrator. Checkout decides when a shipment should be created; Shipment Service owns what happens after that shipment exists.

## Architectural Boundaries

Shipment Service owns:

- shipment record
- shipment number
- carrier selection
- tracking number and tracking URL
- shipping address snapshot
- shipment item snapshot
- shipment status lifecycle
- shipment status history
- carrier adapter abstraction
- shipment lifecycle events
- Order Service shipment-status synchronization

Shipment Service does not own:

- checkout orchestration
- order item pricing or order totals
- payment result verification
- inventory confirmation/release
- promotion redeem/cancel
- user address source of truth
- notification delivery

## Runtime Position

```text
Checkout Service
  -> POST /internal/shipments
  -> Shipment Service creates shipment
  -> Shipment Service calls Order Service shipment sync endpoints
  -> Shipment Service publishes shipment events
  -> Notification Service can consume shipment events
```

## Package Structure

```text
shipment-service/
├── application/
│   ├── client/              # Feign clients for downstream services
│   ├── dto/                 # Request/response/client/provider/event DTOs
│   ├── port/                # Application ports
│   └── service/             # Use cases and orchestration inside shipment domain
├── domain/
│   ├── enums/               # ShipmentCarrier, ShipmentStatus, status source
│   ├── exception/           # Shipment error codes
│   ├── model/               # JPA-backed domain model for this project convention
│   └── repository/          # Domain repository interfaces
├── infrastructure/
│   ├── carrier/             # Manual/mock carrier adapters
│   ├── config/              # Security, OpenAPI, RabbitMQ, Feign, properties
│   ├── mapper/              # Entity/DTO/provider command mapping
│   ├── messaging/           # RabbitMQ shipment event publisher
│   └── persistence/         # Spring Data repository adapters
└── presentation/
    ├── controller/          # Admin/customer/internal REST APIs
    └── exception/           # Global exception handler
```

## Main Flows

### Create shipment for paid order

```text
POST /internal/shipments
Idempotency-Key: checkout:{checkoutId}:shipment-create

1. Validate idempotency key.
2. If stored response exists, return it.
3. If shipment already exists for the order, return existing shipment if compatible.
4. Fetch order snapshot from Order Service.
5. Reject if order is not PAID or PREPARING.
6. Generate shipment number.
7. Create Shipment aggregate with address/items from order snapshot.
8. Call selected carrier adapter.
9. Attach carrier/tracking/label data.
10. Optionally mark shipment READY_TO_SHIP.
11. Synchronize shipment-created summary to Order Service.
12. Publish shipment.created and optionally shipment.ready_to_ship after transaction commit.
```

Canonical route:

```http
POST /internal/shipments
```

Backward-compatible alias:

```http
POST /internal/shipments/orders
```

### Admin status update

```text
PATCH /api/admin/shipments/{shipmentId}/status

Valid status transitions are enforced by the Shipment aggregate.
When status becomes SHIPPED or DELIVERED, Shipment Service synchronizes Order Service.
```

### Customer query

```text
GET /api/customer/shipments
GET /api/customer/shipments/{shipmentId}
```

Customer queries are ownership-protected by `CurrentUser.userId`.

## Shipment Status Lifecycle

Initial supported lifecycle:

```text
CREATED -> READY_TO_SHIP -> SHIPPED -> IN_TRANSIT -> OUT_FOR_DELIVERY -> DELIVERED
CREATED / READY_TO_SHIP -> CANCELLED
SHIPPED / IN_TRANSIT / OUT_FOR_DELIVERY -> DELIVERY_FAILED
DELIVERY_FAILED -> READY_TO_SHIP or CANCELLED
```

`DELIVERED` and `CANCELLED` are terminal states.

## Carrier Strategy

Carrier behavior is behind `ShipmentCarrierPort`.

Initial adapters:

- `MANUAL`: creates a local/manual shipment record; admin can set tracking later.
- `MOCK`: generates mock tracking number, tracking URL, and label URL for demo.

Future real carriers can be added by implementing `ShipmentCarrierPort` without rewriting `ShipmentCommandService`.

## Idempotency

Shipment creation is idempotent and requires `Idempotency-Key`.

Rules:

- same key + same request hash returns existing stored response
- same key + different request hash returns `SHIPMENT_IDEMPOTENCY_CONFLICT`
- same order with compatible carrier returns the existing shipment
- same order with a different explicit carrier returns `SHIPMENT_ALREADY_EXISTS`

Idempotency records are stored in `shipment_idempotency_records`.

## Events

Shipment events are published to `marketplace.events` using routing key equal to event type.

Published events:

- `shipment.created`
- `shipment.ready_to_ship`
- `shipment.shipped`
- `shipment.in_transit`
- `shipment.out_for_delivery`
- `shipment.delivered`
- `shipment.delivery_failed`
- `shipment.cancelled`

Events are published after transaction commit when a transaction is active. This prevents consumers from seeing shipment events before the local database commit succeeds.

## Security

The service follows the Gateway-centric security model:

- API Gateway validates JWT.
- API Gateway strips untrusted `X-User-*` headers and recreates trusted headers.
- Shipment Service reads trusted headers through `common-security`.
- Shipment Service does not parse JWT locally.

Local service security:

- `/actuator/health`, `/actuator/info`, Swagger/OpenAPI are public.
- `/internal/shipments/**` is allowed for internal service calls.
- `/api/customer/shipments/**` requires `CUSTOMER`.
- `/api/admin/shipments/**` requires `ADMIN`.

## Configuration

Default local port:

```text
8090
```

Important environment variables:

```text
SHIPMENT_DB_URL
SHIPMENT_DB_USERNAME
SHIPMENT_DB_PASSWORD
RABBITMQ_HOST
RABBITMQ_PORT
RABBITMQ_USERNAME
RABBITMQ_PASSWORD
EUREKA_DEFAULT_ZONE
SHIPMENT_DEFAULT_CARRIER
SHIPMENT_AUTO_MARK_READY_TO_SHIP
```

## Testing

The test suite contains:

- domain lifecycle tests
- request hash tests
- command service unit tests
- RabbitMQ publisher unit test
- repository/Flyway/PostgreSQL integration test
- internal controller integration test

Run locally with Docker available:

```bash
mvn -pl services/shipment-service -am test
```

or from inside the service folder if it is standalone:

```bash
./mvnw test
```
