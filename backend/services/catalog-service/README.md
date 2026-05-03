# Catalog Service

`catalog-service` is the authoritative product catalog write model of the marketplace.

It owns products, categories, brands, product attributes, product images, base/list prices, product lifecycle status, and authoritative product snapshots used by Checkout. Search/listing optimization belongs to `search-service`; stock belongs to `inventory-service`; final checkout validation belongs to `checkout-service`.

## Service Boundary

### Owns

- Brand lifecycle: create, update, activate, suspend, soft delete
- Category lifecycle: root/child categories, path/level propagation, activate, suspend, soft delete
- Product lifecycle: draft, activate, suspend, soft delete
- Product SKU and slug uniqueness
- PostgreSQL JSONB attributes and image metadata
- Brand/category snapshots embedded into product records
- Internal authoritative product snapshots for Checkout/Basket/other services
- Catalog events for Search and other projections

### Does Not Own

- Stock quantity or reservation
- Public search ranking/facets
- Basket state
- Promotion/discount calculation
- Checkout orchestration
- Order creation
- Payment or shipment lifecycle
- Reviews and rating truth

## Runtime Port

```text
catalog-service:8083
```

This follows the local topology:

```text
api-gateway:8080
auth-service:8081
user-service:8082
catalog-service:8083
search-service:8084
basket-service:8085
inventory-service:8086
promotion-service:8087
order-service:8088
payment-service:8089
shipment-service:8090
checkout-service:8091
```

## Main Endpoints

### Admin Brand API

```http
POST   /api/admin/brands
PUT    /api/admin/brands/{brandId}
POST   /api/admin/brands/{brandId}/activate
POST   /api/admin/brands/{brandId}/suspend
POST   /api/admin/brands/{brandId}/deactivate
DELETE /api/admin/brands/{brandId}
GET    /api/admin/brands/{brandId}
GET    /api/admin/brands
```

### Admin Category API

```http
POST   /api/admin/categories
PUT    /api/admin/categories/{categoryId}
POST   /api/admin/categories/{categoryId}/activate
POST   /api/admin/categories/{categoryId}/suspend
POST   /api/admin/categories/{categoryId}/deactivate
DELETE /api/admin/categories/{categoryId}
GET    /api/admin/categories/{categoryId}
GET    /api/admin/categories
```

### Admin Product API

```http
POST   /api/admin/products
PUT    /api/admin/products/{productId}
POST   /api/admin/products/{productId}/activate
POST   /api/admin/products/{productId}/suspend
DELETE /api/admin/products/{productId}
GET    /api/admin/products/{productId}
GET    /api/admin/products
```

### Internal Catalog API

```http
POST /internal/catalog/products/snapshots
POST /internal/catalog/products/snapshot
```

Both routes return authoritative product snapshots. The singular route is the canonical route for Feign client compatibility; the plural route is kept for backward compatibility.

## Security Model

Catalog Service does not parse JWT locally. The API Gateway validates JWT, strips spoofable identity headers, and recreates trusted headers:

```http
X-User-Id
X-User-Email
X-User-Roles
X-Correlation-Id
```

Catalog Service uses `common-security` `HeaderAuthenticationFilter`.

Rules:

```text
/api/admin/**              requires ADMIN
/internal/catalog/**       private network only, not routed publicly by Gateway
/actuator/health           public for probes
/actuator/info             public for probes
/actuator/prometheus       public for Prometheus in private infra
/swagger-ui/**             public for documentation
/v3/api-docs/**            public for documentation
```

## Persistence

Database: PostgreSQL.

Flyway migration location:

```text
src/main/resources/db/migration/V1__Initial_Catalog_Schema.sql
```

Tables:

```text
brands
categories
products
```

JSONB fields:

```text
products.images
products.attributes
products.category_ancestors
```

Hibernate runs with:

```yaml
spring.jpa.hibernate.ddl-auto: validate
spring.jpa.open-in-view: false
```

## Events

Catalog publishes events after the local transaction commits.

Product events:

```text
catalog.product.created
catalog.product.updated
catalog.product.price_changed
catalog.product.status_changed
catalog.product.deleted
```

Category events:

```text
catalog.category.created
catalog.category.updated
catalog.category.status_changed
```

Brand events:

```text
catalog.brand.created
catalog.brand.updated
catalog.brand.status_changed
```

These are sent to:

```text
marketplace.events
```

Search Service should consume catalog events and update its PostgreSQL FTS read model.

## Environment Variables

```text
SERVER_PORT=8083
CATALOG_DB_URL=jdbc:postgresql://localhost:5432/catalog_db
CATALOG_DB_USERNAME=postgres
CATALOG_DB_PASSWORD=postgres
RABBITMQ_HOST=localhost
RABBITMQ_PORT=5672
RABBITMQ_USERNAME=guest
RABBITMQ_PASSWORD=guest
EUREKA_DEFAULT_ZONE=http://localhost:8761/eureka/
CATALOG_PLATFORM_STORE_ID=platform-store
CATALOG_PLATFORM_STORE_NAME=Platform Store
```

## Test Strategy

Unit tests cover:

- product domain lifecycle
- brand service behavior
- category service behavior
- product service behavior
- event publisher routing
- controller request/response behavior

Integration tests cover:

- PostgreSQL + Flyway schema migration
- brand repository persistence
- category repository persistence
- product repository persistence with JSONB-backed fields

Run locally with Docker open:

```bash
mvn -pl services/catalog-service -am test
```

or from the service folder:

```bash
./mvnw test
```
