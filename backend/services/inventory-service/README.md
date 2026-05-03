# Inventory Service

Inventory Service is the stock authority of the marketplace backend.

It owns:

- inventory item creation and current stock state
- total, reserved, and available quantity calculation
- admin stock adjustments
- checkout-time stock reservation
- payment-success stock confirmation
- payment/checkout-failure stock release
- reservation timeout expiration
- stock movement audit history
- inventory lifecycle events

It does not own product catalog data, product price, basket state, promotion logic, checkout orchestration, order creation, payment provider logic, or shipment lifecycle.

## Architecture

```text
presentation/controller
  AdminInventoryController
  InternalInventoryController

application/service
  InventoryAdminService
  InventoryReservationService
  StockMovementService

application/port
  InventoryEventPublisher

domain/model
  InventoryItem
  StockReservation
  StockReservationItem
  StockMovement
  InventoryProcessedEvent

domain/repository
  Repository ports used by application services

infrastructure/persistence
  Spring Data JPA repositories and repository adapters

infrastructure/messaging
  RabbitMQ event publisher and payloads

infrastructure/scheduler
  ExpiredReservationScheduler

infrastructure/config
  Security, OpenAPI, RabbitMQ, reservation properties
```

## Main Rules

```text
Basket add does not reduce stock.
Checkout reserves stock.
Payment success confirms reserved stock.
Payment failure or checkout failure releases stock.
Expired reservations are released by scheduler.
```

## Public/Admin Endpoints

```http
POST  /api/admin/inventory/items
GET   /api/admin/inventory/items
GET   /api/admin/inventory/products/{productId}
POST  /api/admin/inventory/products/{productId}/increase
POST  /api/admin/inventory/products/{productId}/decrease
PUT   /api/admin/inventory/products/{productId}/stock
PATCH /api/admin/inventory/products/{productId}/low-stock-threshold
GET   /api/admin/inventory/products/{productId}/movements
```

Admin endpoints require Gateway-injected admin user context through `common-security`.

## Internal Checkout Endpoints

```http
POST /internal/inventory/reservations
GET  /internal/inventory/reservations/{reservationId}
POST /internal/inventory/reservations/{reservationId}/confirm
POST /internal/inventory/reservations/{reservationId}/release
GET  /internal/inventory/products/{productId}
```

`POST /internal/inventory/reservations` requires `Idempotency-Key`.

Same key + same payload returns the existing reservation.
Same key + different payload returns an inventory idempotency conflict.

## Reservation Lifecycle

```text
RESERVED → CONFIRMED
RESERVED → RELEASED
RESERVED → EXPIRED
```

Idempotency behavior:

```text
confirm on CONFIRMED with same orderId: success/no-op
release on RELEASED/EXPIRED: success/no-op
confirm on RELEASED/EXPIRED: conflict
release on CONFIRMED: conflict
```

## Stock Quantity Invariant

```text
totalQuantity >= 0
reservedQuantity >= 0
reservedQuantity <= totalQuantity
availableQuantity = totalQuantity - reservedQuantity
```

## Events

Inventory publishes events to `marketplace.events` after transaction commit:

```text
inventory.stock.updated
inventory.stock.low
inventory.stock.out_of_stock
inventory.stock.back_in_stock
inventory.reservation.reserved
inventory.reservation.confirmed
inventory.reservation.released
inventory.reservation.expired
```

## Tests

The test suite contains:

- domain unit tests for stock invariants and reservation status rules
- application service unit tests for admin and reservation flows
- controller tests for admin/internal APIs
- PostgreSQL Testcontainers integration tests for Flyway and repositories

Run locally with Docker open:

```bash
mvn -pl services/inventory-service -am test
```

If your repo module path is different, run from inside `inventory-service`:

```bash
./mvnw test
```
