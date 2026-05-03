# Payment Service

`payment-service` is the payment authority of the Marketplace Microservices E-Commerce Platform.

It owns payment records, payment attempts, provider integration, provider callbacks, refund/cancel records, and payment lifecycle events. It does not own checkout orchestration, order state, stock, promotion usage, shipment lifecycle, or product/user data.

## Architectural Decision

The service follows the final backend architecture decision:

```text
Checkout Service orchestrates the purchase Saga.
Payment Service talks to payment providers and publishes verified payment result events.
Order Service stores order/payment summary state.
```

Payment Service must not directly reserve/release stock, redeem/cancel promotions, create shipments, or mark baskets checked out. Those are Checkout Saga responsibilities.

## Initial Provider Strategy

Initial provider:

```text
IYZICO + CHECKOUT_FORM
```

Reason:

```text
- card data is handled by iyzico Checkout Form
- the app does not store or process raw card data
- callback/retrieve flow is simple enough for bootcamp deployment
- provider-specific SDK classes stay in infrastructure/provider/iyzico
```

The application layer depends on provider ports and strategy/factory abstractions, not directly on iyzico SDK classes.

## Main Runtime Flow

### 1. Initialize Payment

```text
Checkout Service
  -> POST /internal/payments/initialize
     Idempotency-Key: checkout:{checkoutId}:payment-initialize

Payment Service
  -> checks idempotency key + request hash
  -> creates Payment
  -> creates PaymentAttempt
  -> selects PaymentMethodStrategy
  -> selects PaymentProviderPort
  -> initializes iyzico Checkout Form
  -> stores provider token/payment page data
  -> returns payment action to Checkout
```

### 2. Provider Callback

Canonical callback endpoint:

```http
POST /api/payments/providers/iyzico/checkout-form/callback
```

Backward-compatible alias:

```http
POST /api/payments/iyzico/callback
```

Callback handling rule:

```text
Never trust callback status alone.
Use callback token to retrieve/verify the payment result from iyzico.
```

Flow:

```text
iyzico callback
  -> Payment Service receives token
  -> locks/deduplicates payment_callbacks row by provider + event_key
  -> finds PaymentAttempt by provider + provider_token
  -> retrieves payment result from provider
  -> verifies amount/currency
  -> marks Payment + PaymentAttempt SUCCEEDED or FAILED
  -> marks callback processed
  -> publishes payment.succeeded or payment.failed after DB commit
```

### 3. Checkout Finalization

Payment Service only publishes the verified result event.

```text
payment.succeeded/payment.failed
  -> Checkout Service consumes event
  -> Checkout confirms/releases Inventory
  -> Checkout redeems/cancels Promotion usage
  -> Checkout marks Order paid/payment failed
  -> Checkout creates Shipment on success
```

## Package Structure

```text
payment-service/
├── src/main/java/com/onatsubasi/finalcase/payment
│   ├── PaymentServiceApplication.java
│   ├── application
│   │   ├── dto
│   │   │   ├── event
│   │   │   ├── provider
│   │   │   ├── request
│   │   │   └── response
│   │   ├── port
│   │   │   ├── PaymentEventPublisher.java
│   │   │   └── PaymentProviderPort.java
│   │   └── service
│   │       ├── PaymentCommandService.java
│   │       ├── PaymentCallbackService.java
│   │       ├── PaymentRefundService.java
│   │       ├── PaymentQueryService.java
│   │       ├── PaymentIdempotencyService.java
│   │       ├── PaymentRequestHashService.java
│   │       ├── PaymentProviderFactory.java
│   │       ├── PaymentMethodStrategyFactory.java
│   │       └── strategy
│   ├── domain
│   │   ├── enums
│   │   ├── exception
│   │   ├── model
│   │   └── repository
│   ├── infrastructure
│   │   ├── config
│   │   ├── mapper
│   │   ├── messaging
│   │   ├── persistence
│   │   └── provider/iyzico
│   └── presentation
│       ├── controller
│       └── exception
├── src/main/resources
│   ├── application.yml
│   └── db/migration/V1__Initial_Schema.sql
└── src/test/java/com/onatsubasi/finalcase/payment
    ├── application/service
    ├── domain
    ├── presentation/controller
    └── support
```

## Layer Responsibilities

### Domain Layer

Owns payment invariants and lifecycle transitions.

Important aggregates/entities:

```text
Payment
PaymentAttempt
PaymentCallback
PaymentRefund
PaymentCancellation
PaymentIdempotencyRecord
```

Important rules:

```text
- Payment starts as INITIATED.
- Checkout Form initialization moves it to WAITING_PROVIDER_ACTION.
- Verified provider success moves it to SUCCEEDED.
- Verified provider failure moves it to FAILED.
- Duplicate successful callbacks are safe and do not publish duplicate events.
- Refund amount cannot exceed paid amount.
- Refund/cancel requests are protected with idempotency key + request hash.
```

### Application Layer

Coordinates use cases inside Payment Service.

```text
PaymentCommandService:
  initializes payment attempts

PaymentCallbackService:
  handles provider callback, retrieves provider result, finalizes payment state

PaymentRefundService:
  creates provider refund/cancel requests and stores results

PaymentQueryService:
  customer/admin/internal payment read APIs

PaymentIdempotencyService:
  initialize-payment idempotency handling

PaymentRequestHashService:
  stable request payload hashing

PaymentProviderFactory:
  selects provider adapter

PaymentMethodStrategyFactory:
  selects payment method strategy
```

### Infrastructure Layer

Contains persistence adapters, RabbitMQ event publisher, configuration, and iyzico-specific provider mapping.

Iyzico SDK usage must stay under:

```text
infrastructure/provider/iyzico
```

Application services should not import `com.iyzipay.*` classes.

### Presentation Layer

Exposes HTTP endpoints and normalizes errors.

Controllers:

```text
InternalPaymentController:
  /internal/payments/**

IyzicoCallbackController:
  /api/payments/providers/iyzico/checkout-form/callback
  /api/payments/iyzico/callback

CustomerPaymentController:
  /api/customer/payments/**

AdminPaymentController:
  /api/admin/payments/**
```

## API Summary

### Internal APIs

```http
POST /internal/payments/initialize
GET  /internal/payments/orders/{orderId}
GET  /internal/payments/checkouts/{checkoutId}
```

`POST /internal/payments/initialize` requires:

```http
Idempotency-Key: <stable-key>
```

### Provider Callback APIs

```http
POST /api/payments/providers/iyzico/checkout-form/callback
POST /api/payments/iyzico/callback
```

The second endpoint is a compatibility alias.

### Customer APIs

```http
GET /api/customer/payments
GET /api/customer/payments/{paymentId}
```

Customer ownership must be enforced by `CurrentUser.userId`.

### Admin APIs

```http
GET  /api/admin/payments
GET  /api/admin/payments/{paymentId}
GET  /api/admin/payments/{paymentId}/refunds
POST /api/admin/payments/{paymentId}/refunds
POST /api/admin/payments/{paymentId}/cancel
```

Refund/cancel endpoints require `Idempotency-Key`.

## Event Publishing

Published events:

```text
payment.succeeded
payment.failed
payment.cancelled
payment.refunded
```

Events are published after the local database transaction commits. This avoids publishing a payment result event for a DB transaction that later rolls back.

## Idempotency Rules

### Initialize Payment

Table:

```text
payment_idempotency_records
```

Rule:

```text
same Idempotency-Key + same request hash -> return stored response
same Idempotency-Key + different request hash -> 409 PAYMENT_IDEMPOTENCY_CONFLICT
```

### Provider Callback

Table:

```text
payment_callbacks
```

Rule:

```text
same provider + event_key -> process once
already processed callback -> return existing payment detail
```

### Refund / Cancel

Tables:

```text
payment_refunds
payment_cancellations
```

Rule:

```text
same Idempotency-Key + same request hash -> return existing refund/cancellation
same Idempotency-Key + different request hash -> 409 PAYMENT_IDEMPOTENCY_CONFLICT
```

## Database Tables

```text
payments
payment_attempts
payment_idempotency_records
payment_callbacks
payment_refunds
payment_cancellations
```

The service uses PostgreSQL and Flyway. `spring.jpa.hibernate.ddl-auto=validate` is enabled, so `V1__Initial_Schema.sql` must stay aligned with JPA entities.

## Configuration

Default runtime port:

```text
8089
```

Important environment variables:

```text
SERVER_PORT
SPRING_DATASOURCE_URL
SPRING_DATASOURCE_USERNAME
SPRING_DATASOURCE_PASSWORD
RABBITMQ_HOST
RABBITMQ_PORT
EUREKA_URI
PAYMENT_CALLBACK_URL
PAYMENT_DEFAULT_SUCCESS_URL
PAYMENT_DEFAULT_FAILURE_URL
IYZICO_API_KEY
IYZICO_SECRET_KEY
IYZICO_BASE_URL
```

## Testing

Tests are intentionally non-repetitive and cover different risk areas:

```text
PaymentLifecycleTest:
  domain lifecycle and refund invariants

PaymentRequestHashServiceTest:
  stable request hashing

PaymentCommandServiceTest:
  initialize idempotency replay and new attempt creation

PaymentCallbackServiceTest:
  callback success finalization and duplicate callback replay

PaymentRefundServiceTest:
  refund/cancel idempotency and provider success flows

InternalPaymentControllerIntegrationTest:
  internal initialize endpoint validation and delegation

IyzicoCallbackControllerIntegrationTest:
  canonical callback route, legacy route, and validation
```

Run from backend root:

```bash
mvn -pl services/payment-service -am test
```

or from the service folder if your local Maven setup supports it:

```bash
./mvnw test
```
