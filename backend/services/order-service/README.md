# Order Service

Order Service is the source of truth for customer orders in the marketplace microservices platform.

It owns immutable order records, order item/address/discount/payment/shipment snapshots, order status lifecycle, order status history, customer/admin order reads, review purchase verification, and order lifecycle events.

It does **not** orchestrate checkout. Checkout Service remains the Saga coordinator.

---

## 1. Service Boundary

Order Service owns:

```text
- orders
- order_items
- order_discounts
- embedded shipping/billing address snapshots
- embedded payment summary
- embedded shipment summary
- order_status_history
- order lifecycle events
```

Order Service does not own:

```text
- basket state
- current product truth
- current product price
- stock reservation
- promotion calculation
- coupon usage reservation
- payment provider integration
- shipment carrier integration
- checkout orchestration
```

Correct flow:

```text
Customer → Checkout Service → Order Service internal create endpoint
```

Incorrect flow:

```text
Customer → Order Service direct create order endpoint
Order Service → Inventory/Promotion/Payment as Saga orchestrator
```

---

## 2. Package Structure

```text
com.onatsubasi.finalcase.order
├── application
│   ├── dto
│   │   ├── internal      # DTOs used by Checkout, Shipment, Review
│   │   ├── request       # customer/admin request DTOs
│   │   └── response      # customer/admin/internal response DTOs
│   ├── port              # application ports such as OrderEventPublisher
│   └── service           # use cases: command/query/order-number generation
├── domain
│   ├── enums             # OrderStatus, address type, change source
│   ├── exception         # OrderErrorCode
│   ├── model             # JPA aggregate/entities/value objects
│   └── repository        # repository port
├── infrastructure
│   ├── config            # OpenAPI/RabbitMQ config
│   ├── mapper            # DTO/domain mapper
│   ├── messaging         # RabbitMQ publisher/event payloads
│   └── persistence       # Spring Data JPA adapter
└── presentation
    ├── controller        # customer/admin/internal REST APIs
    └── exception         # global error handling
```

---

## 3. Runtime Port

```yaml
server:
  port: 8088
```

The service is private behind API Gateway. `/internal/orders/**` must not be publicly exposed.

---

## 4. Main Endpoints

### Customer API

```http
GET  /api/customer/orders
GET  /api/customer/orders/{orderId}
POST /api/customer/orders/{orderId}/cancel
```

Customer identity comes from Gateway-injected headers through `@CurrentUser`.

### Admin API

```http
GET  /api/admin/orders
GET  /api/admin/orders/{orderId}
GET  /api/admin/orders/number/{orderNumber}
POST /api/admin/orders/{orderId}/cancel
POST /api/admin/orders/{orderId}/preparing
```

### Internal API

```http
POST /internal/orders
GET  /internal/orders/{orderId}
GET  /internal/orders/checkout/{checkoutId}
POST /internal/orders/{orderId}/mark-paid
POST /internal/orders/{orderId}/mark-payment-failed
POST /internal/orders/{orderId}/cancel
POST /internal/orders/{orderId}/shipment-created
POST /internal/orders/{orderId}/mark-shipped
POST /internal/orders/{orderId}/mark-delivered
GET  /internal/orders/{orderId}/items/{orderItemId}/review-eligibility
```

---

## 5. Idempotency

Internal order creation is idempotent by `checkoutId` and `requestHash`.

```text
same checkoutId + same requestHash    → return existing order
same checkoutId + different hash      → 409 ORDER_IDEMPOTENCY_CONFLICT
```

This protects Checkout retries after network errors.

Payment/shipment status updates are idempotent for repeated same final status but reject invalid opposite transitions.

---

## 6. Status Lifecycle

Initial status:

```text
PENDING_PAYMENT
```

Main transitions:

```text
PENDING_PAYMENT → PAID
PENDING_PAYMENT → PAYMENT_FAILED
PENDING_PAYMENT → CANCELLED
PAYMENT_FAILED  → CANCELLED
PAID            → PREPARING
PAID            → CANCELLED
PAID            → SHIPPED
PREPARING       → SHIPPED
PREPARING       → CANCELLED
SHIPPED         → DELIVERED
```

Every status transition writes `order_status_history`.

---

## 7. Event Publishing

Order Service publishes RabbitMQ events after transaction commit:

```text
order.created
order.paid
order.payment_failed
order.cancelled
order.preparing
order.shipped
order.delivered
```

Event payload type:

```java
OrderChangedEvent
```

Events include order id, order number, checkout id, user id, status, grand total, and currency.

---

## 8. Database

Flyway migration:

```text
src/main/resources/db/migration/V1__Initial_Schema.sql
```

Tables:

```text
orders
order_items
order_discounts
order_status_history
```

Order address, payment, and shipment snapshots are embedded in the `orders` table for stable historical reads.

---

## 9. Tests

Test groups:

```text
- OrderDomainTest
- OrderMapperTest
- OrderCommandServiceTest
- OrderQueryServiceTest
- CustomerOrderControllerIntegrationTest
- InternalOrderControllerIntegrationTest
```

Run:

```bash
mvn -pl services/order-service -am test
```

or, when running inside service folder:

```bash
./mvnw test
```

---

## 10. Required Environment Variables

```text
ORDER_DB_URL
ORDER_DB_USERNAME
ORDER_DB_PASSWORD
EUREKA_DEFAULT_ZONE
RABBITMQ_HOST
RABBITMQ_PORT
RABBITMQ_USERNAME
RABBITMQ_PASSWORD
```

Fallback `SPRING_DATASOURCE_*` and `RABBITMQ_*` variables are also supported for Docker Compose compatibility.
