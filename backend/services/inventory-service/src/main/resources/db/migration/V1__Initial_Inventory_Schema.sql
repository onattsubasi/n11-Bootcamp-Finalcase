CREATE
EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE inventory_items
(
    id                  UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    product_id          UUID        NOT NULL,
    total_quantity      INTEGER     NOT NULL,
    reserved_quantity   INTEGER     NOT NULL,
    low_stock_threshold INTEGER     NOT NULL,
    status              VARCHAR(30) NOT NULL,
    created_at          TIMESTAMPTZ NOT NULL,
    updated_at          TIMESTAMPTZ NOT NULL,
    version             BIGINT      NOT NULL DEFAULT 0,

    CONSTRAINT uk_inventory_items_product_id UNIQUE (product_id),

    CONSTRAINT chk_inventory_items_total_quantity
        CHECK (total_quantity >= 0),

    CONSTRAINT chk_inventory_items_reserved_quantity
        CHECK (reserved_quantity >= 0),

    CONSTRAINT chk_inventory_items_reserved_not_greater_than_total
        CHECK (reserved_quantity <= total_quantity),

    CONSTRAINT chk_inventory_items_low_stock_threshold
        CHECK (low_stock_threshold >= 0),

    CONSTRAINT chk_inventory_items_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'DELETED'))
);

CREATE INDEX idx_inventory_items_product_id
    ON inventory_items (product_id);

CREATE INDEX idx_inventory_items_status
    ON inventory_items (status);

CREATE INDEX idx_inventory_items_updated_at
    ON inventory_items (updated_at);


CREATE TABLE stock_reservations
(
    id              UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    idempotency_key VARCHAR(120) NOT NULL,
    request_hash    VARCHAR(128) NOT NULL,
    checkout_id     UUID         NOT NULL,
    user_id         UUID         NOT NULL,
    order_id        UUID,
    status          VARCHAR(30)  NOT NULL,
    reserved_until  TIMESTAMPTZ  NOT NULL,
    confirmed_at    TIMESTAMPTZ,
    released_at     TIMESTAMPTZ,
    release_reason  VARCHAR(40),
    created_at      TIMESTAMPTZ  NOT NULL,
    updated_at      TIMESTAMPTZ  NOT NULL,
    version         BIGINT       NOT NULL DEFAULT 0,

    CONSTRAINT uk_stock_reservations_idempotency_key
        UNIQUE (idempotency_key),

    CONSTRAINT chk_stock_reservations_status
        CHECK (status IN ('RESERVED', 'CONFIRMED', 'RELEASED', 'EXPIRED')),

    CONSTRAINT chk_stock_reservations_release_reason
        CHECK (
            release_reason IS NULL OR
            release_reason IN (
                               'PAYMENT_FAILED',
                               'CHECKOUT_FAILED',
                               'CUSTOMER_CANCELLED',
                               'TIMEOUT',
                               'ADMIN_RELEASE',
                               'UNKNOWN'
                )
            )
);

CREATE INDEX idx_stock_reservations_user_id
    ON stock_reservations (user_id);

CREATE INDEX idx_stock_reservations_checkout_id
    ON stock_reservations (checkout_id);

CREATE INDEX idx_stock_reservations_order_id
    ON stock_reservations (order_id);

CREATE INDEX idx_stock_reservations_status
    ON stock_reservations (status);

CREATE INDEX idx_stock_reservations_reserved_until
    ON stock_reservations (reserved_until);


CREATE TABLE stock_reservation_items
(
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    reservation_id UUID        NOT NULL,
    product_id     UUID        NOT NULL,
    quantity       INTEGER     NOT NULL,
    created_at     TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_stock_reservation_items_reservation
        FOREIGN KEY (reservation_id)
            REFERENCES stock_reservations (id)
            ON DELETE CASCADE,

    CONSTRAINT chk_stock_reservation_items_quantity
        CHECK (quantity > 0)
);

CREATE INDEX idx_stock_reservation_items_reservation_id
    ON stock_reservation_items (reservation_id);

CREATE INDEX idx_stock_reservation_items_product_id
    ON stock_reservation_items (product_id);


CREATE TABLE stock_movements
(
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    inventory_item_id UUID        NOT NULL,
    product_id        UUID        NOT NULL,
    movement_type     VARCHAR(40) NOT NULL,
    quantity_change   INTEGER     NOT NULL,
    total_before      INTEGER     NOT NULL,
    reserved_before   INTEGER     NOT NULL,
    total_after       INTEGER     NOT NULL,
    reserved_after    INTEGER     NOT NULL,
    reservation_id    UUID,
    checkout_id       UUID,
    order_id          UUID,
    reason            VARCHAR(500),
    reference_id      VARCHAR(120),
    occurred_at       TIMESTAMPTZ NOT NULL,

    CONSTRAINT chk_stock_movements_type
        CHECK (
            movement_type IN (
                              'INITIAL_STOCK',
                              'ADMIN_INCREASE',
                              'ADMIN_DECREASE',
                              'ADMIN_SET',
                              'RESERVED',
                              'CONFIRMED_SOLD',
                              'RELEASED',
                              'EXPIRED_RELEASE',
                              'RETURNED',
                              'DAMAGED_REMOVED'
                )
            ),

    CONSTRAINT chk_stock_movements_total_before
        CHECK (total_before >= 0),

    CONSTRAINT chk_stock_movements_reserved_before
        CHECK (reserved_before >= 0),

    CONSTRAINT chk_stock_movements_total_after
        CHECK (total_after >= 0),

    CONSTRAINT chk_stock_movements_reserved_after
        CHECK (reserved_after >= 0),

    CONSTRAINT chk_stock_movements_reserved_after_not_greater_than_total_after
        CHECK (reserved_after <= total_after)
);

CREATE INDEX idx_stock_movements_product_id
    ON stock_movements (product_id);

CREATE INDEX idx_stock_movements_inventory_item_id
    ON stock_movements (inventory_item_id);

CREATE INDEX idx_stock_movements_reservation_id
    ON stock_movements (reservation_id);

CREATE INDEX idx_stock_movements_type
    ON stock_movements (movement_type);

CREATE INDEX idx_stock_movements_occurred_at
    ON stock_movements (occurred_at);


CREATE TABLE inventory_processed_events
(
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id     VARCHAR(120) NOT NULL,
    event_type   VARCHAR(120) NOT NULL,
    processed_at TIMESTAMPTZ  NOT NULL,

    CONSTRAINT uk_inventory_processed_events_event_id
        UNIQUE (event_id)
);

CREATE INDEX idx_inventory_processed_events_event_type
    ON inventory_processed_events (event_type);

CREATE INDEX idx_inventory_processed_events_processed_at
    ON inventory_processed_events (processed_at);