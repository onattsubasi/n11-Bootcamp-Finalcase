# Search Service

Search Service is the PostgreSQL-backed CQRS read model for product listing, search, autocomplete, facets, and denormalized product-card data.

## Architectural role

Search Service is not the product source of truth.

- Catalog Service owns product/category/brand/base price truth.
- Inventory Service owns stock truth.
- Promotion Service owns discount truth.
- Review Service owns rating/review truth.
- Search Service stores an eventually consistent projection for fast public browsing.

Checkout must never use Search Service for authoritative price, stock, promotion, or product sellability validation. Checkout must call Catalog, Inventory, and Promotion directly.

## Runtime endpoints

Public endpoints:

```text
GET /api/products/search
GET /api/products/autocomplete
GET /api/products/facets
```

Compatibility aliases are also available:

```text
GET /api/search
GET /api/search/autocomplete
GET /api/search/facets
```

Admin endpoints:

```text
GET  /api/admin/search/documents/{productId}
GET  /api/admin/search/documents?status=ACTIVE
POST /api/admin/search/rebuild
```

Internal endpoints:

```text
GET  /internal/search/documents/{productId}
POST /internal/search/rebuild
```

API Gateway must not expose `/internal/**` publicly.

## Projection events

Search consumes RabbitMQ events from:

```text
catalog.#
inventory.#
promotion.#
review.#
```

Main projection updates:

```text
catalog.product.created / updated       -> upsert product_search_documents
catalog.product.deleted                 -> mark document DELETED and invisible
catalog.product.status_changed          -> update status / visibility
catalog.category.updated                -> patch category names/path
catalog.brand.updated                   -> patch brand names
inventory.stock.updated / low / back... -> patch availableQuantity and stockStatus
promotion.product_projection.updated    -> patch discount badge / discounted price
promotion.product_projection.cleared    -> clear discount badge / discounted price
review.rating_summary.updated           -> patch averageRating and reviewCount
```

Duplicate events are skipped through `processed_search_events.event_id`.

## PostgreSQL features

The service uses:

```text
- tsvector generated column for keyword search
- pg_trgm for autocomplete / typo-tolerant fallback
- unaccent extension for query-side normalization
- JSONB for product attributes, tags, and category path
- GIN indexes for search vector, trigram, attributes, and tags
```

The migration intentionally does not use `unaccent()` inside the generated column because PostgreSQL generated columns require immutable expressions. Query-side fallback still uses `unaccent`.

## Security model

Search public endpoints are open read endpoints. Admin endpoints require `ROLE_ADMIN` through Gateway-injected trusted headers and `common-security`'s `HeaderAuthenticationFilter`.

Search Service does not parse JWTs. API Gateway validates JWTs, strips spoofable identity headers, and forwards trusted `X-User-*` headers.

## Environment variables

```text
SERVER_PORT=8084
SEARCH_DB_URL=jdbc:postgresql://localhost:5432/search_db
SEARCH_DB_USERNAME=postgres
SEARCH_DB_PASSWORD=postgres
RABBITMQ_HOST=localhost
RABBITMQ_PORT=5672
RABBITMQ_USERNAME=guest
RABBITMQ_PASSWORD=guest
EUREKA_DEFAULT_ZONE=http://localhost:8761/eureka/
CATALOG_SERVICE_BASE_URL=http://catalog-service
```

## Local test command

Run from backend root:

```bash
mvn -pl services/search-service -am test
```

The integration tests use PostgreSQL Testcontainers and Flyway, not H2.
