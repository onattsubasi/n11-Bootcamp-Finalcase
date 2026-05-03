# User Service

`user-service` owns customer profile data and customer-owned product references for the Marketplace Microservices E-Commerce Platform.

It intentionally does **not** own authentication credentials, passwords, refresh tokens, JWT issuing, roles as a source of truth, coupons, baskets, orders, payments, or shipment lifecycle.

## Architectural Position

```text
Auth Service
  owns AuthAccount, password hash, roles, access tokens, refresh tokens
  creates the platform-wide userId

API Gateway
  validates JWT
  strips spoofable incoming X-User-* headers
  recreates trusted X-User-Id, X-User-Email, X-User-Roles headers

User Service
  reads trusted Gateway headers through common-security
  lazily creates UserProfile using the same userId as AuthAccount.id
  owns profile, addresses, preferences, favorites, product lists
```

The most important rule:

```text
AuthAccount.id == UserProfile.userId
```

There are not two unrelated users. Auth owns account identity; User owns profile data.

## Responsibilities

```text
- current customer profile read/update
- lazy profile creation from Gateway user context
- customer address CRUD
- default shipping and billing address handling
- checkout address snapshot endpoint
- customer preferences
- favorite product references
- custom product lists and product references
- admin user profile listing/viewing/profile lifecycle
- user/profile/address/favorite/product-list events
```

## Non-Responsibilities

```text
- login/register/password hashing
- JWT issuing or JWT parsing
- refresh token lifecycle
- role source of truth
- coupon assignment and coupon ownership
- basket state
- product catalog truth
- product price truth
- order/payment/shipment state
```

## Package Structure

```text
com.onatsubasi.finalcase.user
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
│   └── persistence
└── presentation
    ├── controller
    └── exception
```

## Main Domains

### UserProfile

`UserProfile` stores customer-facing profile data:

```text
userId
email reference
firstName
lastName
phoneNumber
avatarUrl
language
marketingOptIn
status
```

The `email` field is a display/reference copy from Auth/Gateway context. Login email change still belongs to Auth Service.

### UserAddress

`UserAddress` stores shipping/billing addresses. Existing order/shipment history must not depend on live address data, so Checkout calls the internal snapshot endpoint and Order/Shipment store immutable copies.

### UserPreference

`UserPreference` stores customer UI/notification preferences such as language, currency, and notification channel preferences.

### FavoriteProduct

Favorites store only:

```text
userId
productId
createdAt
```

Product names, prices, images, and availability are hydrated from Search/Catalog on the frontend or through product read APIs.

### ProductList

Product lists store user-owned lists and productId references. Duplicate product entries in the same list are prevented; adding an existing product updates its note instead of creating another row.

## Public Customer Endpoints

```http
GET    /api/customer/profile/me
PUT    /api/customer/profile/me

GET    /api/customer/addresses
POST   /api/customer/addresses
PUT    /api/customer/addresses/{addressId}
DELETE /api/customer/addresses/{addressId}
POST   /api/customer/addresses/{addressId}/default-shipping
POST   /api/customer/addresses/{addressId}/default-billing

GET    /api/customer/preferences/me
PUT    /api/customer/preferences/me

GET    /api/customer/favorites
POST   /api/customer/favorites
DELETE /api/customer/favorites/{productId}

GET    /api/customer/product-lists
GET    /api/customer/product-lists/{listId}
POST   /api/customer/product-lists
PUT    /api/customer/product-lists/{listId}
DELETE /api/customer/product-lists/{listId}
POST   /api/customer/product-lists/{listId}/items
DELETE /api/customer/product-lists/{listId}/items/{productId}
```

Customer endpoints must use `CurrentUser.userId` from Gateway headers. Do not accept customer `userId` in request body/path.

## Admin Endpoints

```http
GET    /api/admin/users
GET    /api/admin/users/{userId}
POST   /api/admin/users/{userId}/disable
POST   /api/admin/users/{userId}/activate
DELETE /api/admin/users/{userId}
```

Admin profile lifecycle here does not replace Auth account status. Auth Service still owns credential/account security status.

## Internal Endpoints

```http
POST /internal/users/address-snapshots
GET  /internal/users/{userId}/profile
```

`/internal/**` must not be exposed publicly by API Gateway. It is intended for service-to-service calls such as Checkout address snapshot creation.

## Events

Published events:

```text
user.profile.created
user.profile.updated
user.address.created
user.address.updated
user.address.deleted
user.favorite.added
user.favorite.removed
user.product_list.created
user.product_list.updated
user.product_list.deleted
user.product_list.item_added
user.product_list.item_removed
```

Events are registered for after-commit publishing when a Spring transaction is active. This prevents a message from being sent for a database transaction that later rolls back.

## Database Tables

```text
user_profiles
user_addresses
user_preferences
favorite_products
product_lists
product_list_items
```

Important constraints:

```text
user_profiles.user_id primary key = AuthAccount.id
one default shipping address per user among non-deleted addresses
one default billing address per user among non-deleted addresses
unique favorite product per user/product pair
unique product list item per list/product pair
```

## Runtime Configuration

Required environment variables:

```text
USER_DB_URL
USER_DB_USERNAME
USER_DB_PASSWORD
EUREKA_DEFAULT_ZONE
RABBITMQ_HOST
RABBITMQ_PORT
RABBITMQ_USERNAME
RABBITMQ_PASSWORD
```

Spring-compatible aliases are also supported for local reuse:

```text
SPRING_DATASOURCE_URL
SPRING_DATASOURCE_USERNAME
SPRING_DATASOURCE_PASSWORD
RABBITMQ_HOST
RABBITMQ_PORT
RABBITMQ_USERNAME
RABBITMQ_PASSWORD
```

Default local port:

```text
8082
```

## Testing Strategy

Unit tests cover:

```text
- UserProfile domain behavior
- UserAddress domain behavior
- ProductList duplicate/no-op behavior
- UserProfileService lazy creation and email-reference refresh
- UserAddressService default/snapshot rules
- FavoriteProductService idempotent add/remove behavior
- ProductListService item event behavior
- UserPreferenceService defaults and validation
- UserMapper snapshot/response mapping
- RabbitUserEventPublisher event send behavior
```

Controller tests cover:

```text
- customer profile API
- address API
- preference API
- favorite API
- product list API
- admin user API
- internal user API
```

Integration tests cover:

```text
- Flyway schema with PostgreSQL Testcontainers
- profile persistence using AuthAccount userId
- partial unique default-address constraints
- favorite unique user/product constraint
- product list duplicate item behavior
```

Run locally:

```bash
mvn -pl services/user-service -am test
```

or if this module is opened directly:

```bash
./mvnw test
```

## Operational Notes

Logs must not include:

```text
passwords
access tokens
refresh tokens
Authorization headers
full address payloads in high-volume logs
```

Useful event names:

```text
user.profile.created
user.profile.updated
user.address.created
user.address.updated
user.address.deleted
user.favorite.added
user.favorite.removed
user.product_list.created
user.product_list.item_added
user.address_snapshot.requested
```
