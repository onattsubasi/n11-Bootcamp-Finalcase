# Basket Service

`basket-service` owns the customer's temporary shopping intent in the marketplace platform.

The most important boundary is:

```text
Basket stores customer intent.
Checkout validates the purchase.
```

Basket stores selected `productId` values, quantities, optional UX snapshots, and optional coupon-code intent. It does not own product truth, price truth, stock, promotion eligibility, final totals, order creation, payment, or shipment.

## Runtime Role

```text
Customer → API Gateway → Basket Service
Checkout Service → Basket Service internal snapshot / mark checked out endpoints
```

The frontend never calls internal endpoints directly. API Gateway must not expose `/internal/**` routes publicly.

## Package Structure

```text
com.onatsubasi.finalcase.basket
├── application
│   ├── dto
│   │   ├── internal
│   │   ├── request
│   │   └── response
│   ├── port
│   └── service
├── domain
│   ├── enums
│   ├── exception
│   ├── model
│   └── repository
├── infrastructure
│   ├── config
│   ├── mapper
│   ├── messaging
│   ├── persistence
│   └── scheduler
└── presentation
    └── controller
```

## Main Responsibilities

- Create or return one active basket per customer.
- Add item to basket.
- Increase quantity if the same product is added again.
- Update item quantity.
- Remove item idempotently.
- Clear basket idempotently while keeping the basket `ACTIVE`.
- Store optional `couponCodeIntent` as intent only.
- Expose internal checkout basket snapshot.
- Mark basket as checked out after successful checkout finalization.
- Publish basket lifecycle events after transaction commit.

## Non-Responsibilities

Basket Service must not:

- Reserve stock.
- Calculate final monetary totals.
- Validate or redeem coupons.
- Create orders.
- Start payments.
- Create shipments.
- Trust product price snapshots for checkout.

Checkout revalidates basket contents through Catalog, Inventory, Promotion, User, Order, Payment, and Shipment services.

## Public Customer API

Base path:

```text
/api/customer/basket
```

Endpoints:

```text
GET    /api/customer/basket
POST   /api/customer/basket/items
PUT    /api/customer/basket/items/{productId}
DELETE /api/customer/basket/items/{productId}
DELETE /api/customer/basket
DELETE /api/customer/basket/items        # backward-compatible clear alias
PUT    /api/customer/basket/coupon-intent
DELETE /api/customer/basket/coupon-intent
```

Customer routes use `@CurrentUser UserContext` from `common-security`. They do not accept `userId` in path/body.

## Internal API

Base path:

```text
/internal/baskets
```

Endpoints:

```text
GET  /internal/baskets/users/{userId}/active
POST /internal/baskets/{basketId}/mark-checked-out
```

The internal active basket endpoint rejects an empty basket with `BASKET_EMPTY`, because Checkout should not continue with an empty purchase intent.

## Status Lifecycle

Basket statuses:

```text
ACTIVE
CHECKED_OUT
ABANDONED
CLEARED
```

Normal customer clear does not set `CLEARED`; it removes items and keeps the basket `ACTIVE` so the customer can continue shopping.

Checkout finalization changes:

```text
ACTIVE → CHECKED_OUT
```

`markCheckedOut(orderId)` is idempotent for the same `orderId` and rejects a different `orderId` once checked out.

## Item Rules

A basket item is unique by:

```text
basket_id + product_id
```

Quantity must be:

```text
1 <= quantity <= 99
```

Adding the same product again increments the quantity instead of creating a duplicate row.

## Coupon Intent Rule

`couponCodeIntent` is only a customer-entered candidate. Basket does not validate it, calculate discount, reserve usage, or redeem it.

Promotion Service remains the coupon and discount authority. Checkout sends the intent to Promotion during quote/submit if needed.

## Events

Events are published to `marketplace.events` after the local transaction commits.

Routing keys:

```text
basket.created
basket.item.added
basket.item.quantity_updated
basket.item.removed
basket.cleared
basket.coupon_intent.updated
basket.coupon_intent.cleared
basket.checked_out
```

## Database

Main tables:

```text
baskets
basket_items
```

Important constraints:

```text
unique active basket per user:
  ux_baskets_user_active on baskets(user_id) where status = 'ACTIVE'

unique basket item per product:
  ux_basket_items_basket_product on basket_items(basket_id, product_id)
```

Flyway location:

```text
classpath:db/migration
```

## Security

Basket Service does not parse JWT. API Gateway validates JWT and forwards trusted identity headers.

Basket Service uses:

```text
HeaderAuthenticationFilter
@CurrentUser UserContext
```

Customer routes require `CUSTOMER` or `ADMIN`. Internal routes are private service-to-service endpoints and must not be routed publicly by API Gateway.

## Local Test Command

From the backend root:

```bash
mvn -pl services/basket-service -am test
```

Tests use PostgreSQL Testcontainers + Flyway + Hibernate `ddl-auto: validate`.
