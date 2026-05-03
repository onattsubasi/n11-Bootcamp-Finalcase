# Review Service

Review Service is the product review and rating authority of the marketplace platform.

It owns:

- verified-purchase product reviews
- 1-5 star ratings
- public product rating summaries
- review image references
- helpful / unhelpful votes
- customer review reports
- admin moderation lifecycle
- review and rating-summary events for Search/Notification projections

It does not own product catalog, stock, order, payment, shipment, or notification delivery.

## Service Boundary

Correct ownership:

```text
Order Service:
  verifies delivered purchase eligibility

Review Service:
  owns review content, review status, votes, reports, rating summary

Search Service:
  stores projected averageRating/reviewCount for listing cards

Notification Service:
  consumes review events if notification is needed
```

Review creation is allowed only when Order Service confirms that the current user has a delivered order containing the product.

## Runtime

```text
service name: review-service
port: 8093
package: com.onatsubasi.finalcase.review
database: PostgreSQL
migration: Flyway
security: Gateway-injected headers via common-security
messaging: RabbitMQ producer
```

## Package Structure

```text
review-service/
├── application/
│   ├── dto/
│   ├── port/
│   └── service/
├── domain/
│   ├── enums/
│   ├── exception/
│   ├── model/
│   └── repository/
├── infrastructure/
│   ├── client/
│   ├── config/
│   ├── mapper/
│   ├── messaging/
│   └── persistence/
├── presentation/
│   ├── controller/
│   └── exception/
└── resources/db/migration/
```

Existing `domain/model` naming is preserved intentionally. No package rename is required.

## Public APIs

```http
GET /api/public/reviews/products/{productId}/summary
GET /api/public/reviews/products/{productId}
```

Public APIs return only approved, visible, non-deleted reviews.

## Customer APIs

```http
POST   /api/customer/reviews
PUT    /api/customer/reviews/{reviewId}
DELETE /api/customer/reviews/{reviewId}
GET    /api/customer/reviews/me
GET    /api/customer/reviews/products/{productId}/me
POST   /api/customer/reviews/{reviewId}/votes
DELETE /api/customer/reviews/{reviewId}/votes
POST   /api/customer/reviews/{reviewId}/reports
```

Customer APIs use `@CurrentUser UserContext`; they never accept `userId` from the request body for ownership decisions.

## Admin APIs

```http
GET    /api/admin/reviews
GET    /api/admin/reviews/{reviewId}
POST   /api/admin/reviews/{reviewId}/approve
POST   /api/admin/reviews/{reviewId}/reject
POST   /api/admin/reviews/{reviewId}/hide
POST   /api/admin/reviews/{reviewId}/restore
DELETE /api/admin/reviews/{reviewId}

GET    /api/admin/review-reports
POST   /api/admin/review-reports/{reportId}/resolve
POST   /api/admin/review-reports/{reportId}/dismiss
```

## Order Verification

Review Service calls:

```http
GET /internal/orders/verify-purchase?userId={userId}&productId={productId}
```

Expected response shape:

```json
{
  "data": {
    "verified": true,
    "orderId": "uuid",
    "orderItemId": "uuid",
    "orderNumber": "ORD-20260503-000001",
    "deliveredAt": "2026-05-03T10:00:00Z"
  }
}
```

The client also tolerates an unenveloped direct response for easier local testing.

## Review Lifecycle

```text
PENDING_MODERATION -> APPROVED
PENDING_MODERATION -> REJECTED
APPROVED -> HIDDEN
HIDDEN -> APPROVED
APPROVED/HIDDEN/PENDING_MODERATION/REJECTED -> DELETED
```

Only reviews with this condition affect rating summary:

```text
status = APPROVED
visible = true
deleted_at is null
```

## Events

Published routing keys:

```text
review.submitted
review.approved
review.rejected
review.hidden
review.restored
review.deleted
review.updated
review.rating_summary.updated
review.voted
review.vote.removed
review.reported
review.report.resolved
```

Events are published after transaction commit when a transaction exists.

## Database

Main tables:

```text
reviews
product_rating_summaries
review_votes
review_reports
```

There must be only one `V1` Flyway migration. Duplicate versioned migrations will break startup.

## Testing

Run locally with Docker available:

```bash
mvn -pl services/review-service -am test
```

Test coverage includes:

- domain lifecycle rules
- rating summary arithmetic
- verified-purchase review creation flow
- duplicate active review protection
- self-vote rejection
- RabbitMQ event publishing
- PostgreSQL + Flyway schema validation
- partial unique active-review constraint
