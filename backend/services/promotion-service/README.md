# Promotion Service

`promotion-service` is the discount and coupon authority of the marketplace backend.

It owns campaign definitions, coupon definitions, customer coupon assignments, discount quote calculation, and promotion usage reservation/redeem/cancel lifecycle.

## Architectural Role

Promotion Service is intentionally separated from Basket and Checkout.

- Basket stores customer intent: productId + quantity.
- Catalog owns product and price truth.
- Checkout orchestrates the purchase Saga.
- Promotion owns discount eligibility, coupon assignment, quote calculation, and quota protection.
- Order stores immutable discount snapshots after Checkout creates the order.

The important rule is:

```text
Quote is read-only.
Reserve mutates reserved counters.
Redeem finalizes usage after payment success.
Cancel releases usage after payment/checkout failure.
```

## Runtime

```text
Service name: promotion-service
Default port: 8087
Database: PostgreSQL
Messaging: RabbitMQ producer
Discovery: Eureka client
Security: Gateway-injected X-User-* headers through common-security
```

## Package Structure

```text
com.onatsubasi.finalcase.promotion
├── application
│   ├── dto
│   │   ├── internal       # Checkout-facing internal request/response models
│   │   ├── request        # Admin/customer request models
│   │   └── response       # API response models
│   ├── port               # Application ports, e.g. PromotionEventPublisher
│   ├── service            # Use cases / application services
│   └── strategy           # Discount Strategy + Factory implementation
├── domain
│   ├── enums              # Promotion/coupon/reservation statuses and types
│   ├── exception          # PromotionErrorCode
│   ├── model              # JPA-backed domain aggregates
│   └── repository         # Domain repository interfaces
├── infrastructure
│   ├── config             # Security, OpenAPI, RabbitMQ, properties
│   ├── mapper             # Domain-to-response mapping
│   ├── messaging          # RabbitMQ event publisher and payloads
│   ├── persistence        # Spring Data repository adapters
│   └── scheduler          # Expired reservation cleanup job
└── presentation
    ├── controller         # Admin/customer/internal REST controllers
    └── exception          # Global exception handling
```

## Main Domain Objects

### Promotion

Stores campaign lifecycle and strategy configuration.

Important fields:

```text
id
name
type
status
couponRequired
stackable
priority
ruleConfig JSONB
globalUsageLimit
perUserUsageLimit
reservedUsageCount
redeemedUsageCount
startsAt
endsAt
```

Supported initial types:

```text
PERCENTAGE_DISCOUNT
FIXED_AMOUNT_DISCOUNT
CATEGORY_PERCENTAGE_DISCOUNT
BRAND_PERCENTAGE_DISCOUNT
PRODUCT_PERCENTAGE_DISCOUNT
FREE_SHIPPING
```

### Coupon

Stores code-based access to a promotion.

```text
code is normalized to uppercase
usage counters are protected during reservation/redeem/cancel
coupon can be ACTIVE, INACTIVE, or EXPIRED
```

### CouponAssignment

Stores user-specific coupon ownership.

```text
ASSIGNED -> RESERVED -> REDEEMED
ASSIGNED -> EXPIRED
ASSIGNED/RESERVED -> CANCELLED
RESERVED -> ASSIGNED on reservation cancellation
```

### PromotionUsageReservation

Represents reserved promotion/coupon usage during Checkout.

```text
RESERVED -> REDEEMED
RESERVED -> CANCELLED
RESERVED -> EXPIRED
```

Reservation creation is protected by:

```text
Idempotency-Key + requestHash
```

Same key + same payload returns the existing reservation.
Same key + different payload returns conflict.

## Discount Strategy Design

Discount logic does not live inside the `Promotion` entity.

```text
DiscountStrategy
DiscountStrategyFactory
RuleConfigReader
```

Each promotion type has a dedicated strategy class:

```text
PercentageDiscountStrategy
FixedAmountDiscountStrategy
CategoryPercentageDiscountStrategy
BrandPercentageDiscountStrategy
ProductPercentageDiscountStrategy
FreeShippingStrategy
```

This keeps the domain model clean and allows new promotion types to be added without a giant entity switch-case.

## API Overview

### Admin Promotion API

Base path:

```text
/api/admin/promotions
```

Endpoints:

```text
POST   /api/admin/promotions
PUT    /api/admin/promotions/{promotionId}
POST   /api/admin/promotions/{promotionId}/activate
POST   /api/admin/promotions/{promotionId}/pause
POST   /api/admin/promotions/{promotionId}/expire
DELETE /api/admin/promotions/{promotionId}
GET    /api/admin/promotions/{promotionId}
GET    /api/admin/promotions?status=ACTIVE
```

If `status` is omitted, admin listing returns all promotions.

### Admin Coupon API

Base path:

```text
/api/admin/coupons
```

Endpoints:

```text
POST /api/admin/coupons
PUT  /api/admin/coupons/{couponId}
POST /api/admin/coupons/{couponId}/activate
POST /api/admin/coupons/{couponId}/deactivate
POST /api/admin/coupons/{couponId}/expire
POST /api/admin/coupons/batch
POST /api/admin/coupons/assignments
GET  /api/admin/coupons/promotion/{promotionId}
```

### Customer Coupon API

Base path:

```text
/api/customer/coupons
```

Endpoint:

```text
GET /api/customer/coupons
```

Returns the current authenticated customer's assigned coupons.

### Internal Promotion API

Base path:

```text
/internal/promotions
```

Used by Checkout Service.

```text
POST /internal/promotions/quote
POST /internal/promotions/usage-reservations
GET  /internal/promotions/usage-reservations/{reservationId}
POST /internal/promotions/usage-reservations/{reservationId}/redeem
POST /internal/promotions/usage-reservations/{reservationId}/cancel
```

Gateway must not expose `/internal/**` publicly.

## Checkout Integration Flow

### Quote

```text
Checkout -> Promotion quote
Promotion calculates eligible discounts
Promotion returns selected/best discount
No counters are changed
```

### Reserve

```text
Checkout -> Promotion reserve with Idempotency-Key
Promotion re-quotes/revalidates
Promotion locks selected promotion rows
Promotion locks coupon/coupon assignment rows if coupon is used
Promotion increments reserved counters
Promotion creates reservation
```

### Redeem

```text
Payment success
Checkout -> Promotion redeem
reserved counters decrease
redeemed counters increase
coupon assignment becomes REDEEMED
```

### Cancel

```text
Payment failure / checkout failure / timeout
Checkout -> Promotion cancel
reserved counters decrease
coupon assignment returns to ASSIGNED when applicable
```

## Database

Migration file:

```text
src/main/resources/db/migration/V1__Initial_Promotion_Schema.sql
```

Main tables:

```text
promotions
coupons
coupon_assignments
promotion_usage_reservations
promotion_usage_reservation_items
```

JSONB is used only for flexible promotion `rule_config`.

## Events

Published through RabbitMQ after successful transaction commit:

```text
promotion.created
promotion.updated
promotion.activated
promotion.paused
promotion.expired
promotion.deleted
promotion.coupon.created
promotion.coupon.updated
promotion.coupon.activated
promotion.coupon.deactivated
promotion.coupon.expired
promotion.coupon.assigned
promotion.usage.reserved
promotion.usage.redeemed
promotion.usage.cancelled
promotion.usage.expired
```

Event publication failure is logged but does not roll back already-committed business state.

## Scheduler

Expired reservation cleanup is controlled by:

```yaml
promotion:
  usage-reservation:
    expiration-scheduler-enabled: true
    expiration-fixed-delay-ms: 60000
    expiration-batch-size: 100
```

The scheduler expires old `RESERVED` reservations and releases counters.

## Tests

Added test coverage:

```text
PromotionDomainTest
DiscountStrategyTest
PromotionQuoteServiceTest
PromotionUsageReservationServiceTest
RabbitPromotionEventPublisherTest
InternalPromotionControllerIntegrationTest
AdminCouponControllerIntegrationTest
```

Test focus:

```text
domain lifecycle
strategy calculation
quote read-only behavior
idempotent reservation replay
idempotency conflict detection
redeem/cancel counter movement
RabbitMQ after-commit event registration
controller validation and request forwarding
```

Run locally from the backend root:

```bash
mvn -pl services/promotion-service -am test
```

or from the service folder if it is standalone:

```bash
./mvnw test
```
