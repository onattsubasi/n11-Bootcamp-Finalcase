# Notification Service

`notification-service` is the event-driven notification authority of the marketplace backend.

It consumes domain events, creates notification records, renders templates, creates in-app notifications, sends mock email deliveries, tracks delivery attempts, retries retryable failures, and exposes customer/admin notification APIs.

## Architecture Boundary

Notification Service owns:

- notification templates
- notification records
- in-app notification inbox
- delivery records and attempts
- retry scheduling
- provider abstraction for notification delivery
- idempotent event consumption

Notification Service does not own:

- order state
- payment state
- shipment state
- checkout orchestration
- user authentication
- stock or promotion state

Domain services publish events. Notification Service reacts asynchronously. A notification failure must never roll back checkout, payment, order, or shipment state.

## Package Structure

```text
com.onatsubasi.finalcase.notification
├── application
│   ├── dto
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
│   ├── provider
│   ├── scheduler
│   └── template
└── presentation
    ├── controller
    └── exception
```

## HTTP APIs

Customer APIs:

```text
GET   /api/customer/notifications
GET   /api/customer/notifications/{notificationId}
PATCH /api/customer/notifications/{notificationId}/read
PATCH /api/customer/notifications/read-all
GET   /api/customer/notifications/unread-count
```

Admin APIs:

```text
GET   /api/admin/notifications
GET   /api/admin/notifications/{notificationId}
POST  /api/admin/notifications/direct
POST  /api/admin/notifications/deliveries/{deliveryId}/retry
POST  /api/admin/notification-templates
PATCH /api/admin/notification-templates/{templateId}/activate
PATCH /api/admin/notification-templates/{templateId}/deactivate
```

## Messaging

Consumed events include order, payment, shipment, checkout, inventory and promotion lifecycle events.

Produced events include:

```text
notification.created
notification.sent
notification.failed
notification.delivery_retry_scheduled
```

Event publishing is deferred until local transaction commit when a transaction is active.

## Security

The service follows the project-wide Gateway-centric security model:

- API Gateway validates JWT.
- Gateway strips spoofable user headers.
- Gateway forwards trusted `X-User-*` headers.
- Notification Service reads the current user through `common-security`.
- Notification Service does not parse JWT locally.

## Database

PostgreSQL + Flyway. Main tables:

- `notification_templates`
- `notifications`
- `notification_deliveries`
- `notification_delivery_attempts`
- `notification_processed_events`
- `notification_preferences`
- `user_product_interests`

## Tests

Run from the backend root:

```bash
mvn -pl services/notification-service -am test
```

Integration tests use PostgreSQL Testcontainers and Flyway with `ddl-auto=validate`.
