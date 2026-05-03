CREATE TABLE checkout_sessions
(
    id                             UUID PRIMARY KEY,
    user_id                        UUID                     NOT NULL,
    basket_id                      UUID,
    order_id                       UUID,
    order_number                   VARCHAR(80),
    payment_id                     UUID,
    payment_attempt_id             UUID,
    payment_session_id             VARCHAR(180),
    payment_redirect_url           VARCHAR(1000),
    inventory_reservation_id       UUID,
    promotion_usage_reservation_id UUID,
    shipment_id                    UUID,
    idempotency_key                VARCHAR(120)             NOT NULL,
    request_hash                   VARCHAR(128)             NOT NULL,
    status                         VARCHAR(50)              NOT NULL,
    compensation_status            VARCHAR(50)              NOT NULL DEFAULT 'NOT_REQUIRED',
    currency                       VARCHAR(10)              NOT NULL,
    subtotal_amount                NUMERIC(19, 2)           NOT NULL DEFAULT 0,
    item_discount_amount           NUMERIC(19, 2)           NOT NULL DEFAULT 0,
    promotion_discount_amount      NUMERIC(19, 2)           NOT NULL DEFAULT 0,
    shipping_fee                   NUMERIC(19, 2)           NOT NULL DEFAULT 0,
    shipping_discount_amount       NUMERIC(19, 2)           NOT NULL DEFAULT 0,
    tax_amount                     NUMERIC(19, 2)           NOT NULL DEFAULT 0,
    grand_total_amount             NUMERIC(19, 2)           NOT NULL DEFAULT 0,
    failure_code                   VARCHAR(100),
    failure_message                VARCHAR(1000),
    quote_snapshot                 JSONB                    NOT NULL DEFAULT '{}'::jsonb,
    payment_action_snapshot        JSONB                    NOT NULL DEFAULT '{}'::jsonb,
    expires_at                     TIMESTAMP WITH TIME ZONE,
    payment_initialized_at         TIMESTAMP WITH TIME ZONE,
    completed_at                   TIMESTAMP WITH TIME ZONE,
    failed_at                      TIMESTAMP WITH TIME ZONE,
    cancelled_at                   TIMESTAMP WITH TIME ZONE,
    created_at                     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version                        BIGINT                   NOT NULL DEFAULT 0,

    CONSTRAINT uk_checkout_sessions_idempotency_key UNIQUE (idempotency_key),
    CONSTRAINT chk_checkout_sessions_amounts_non_negative CHECK (
        subtotal_amount >= 0
        AND item_discount_amount >= 0
        AND promotion_discount_amount >= 0
        AND shipping_fee >= 0
        AND shipping_discount_amount >= 0
        AND tax_amount >= 0
        AND grand_total_amount >= 0
    )
);

CREATE INDEX idx_checkout_sessions_user_id
    ON checkout_sessions (user_id);

CREATE INDEX idx_checkout_sessions_basket_id
    ON checkout_sessions (basket_id);

CREATE INDEX idx_checkout_sessions_order_id
    ON checkout_sessions (order_id);

CREATE INDEX idx_checkout_sessions_payment_id
    ON checkout_sessions (payment_id);

CREATE INDEX idx_checkout_sessions_status
    ON checkout_sessions (status);

CREATE INDEX idx_checkout_sessions_idempotency_key
    ON checkout_sessions (idempotency_key);

CREATE INDEX idx_checkout_sessions_expires_at
    ON checkout_sessions (expires_at);


CREATE TABLE checkout_items
(
    id                  UUID PRIMARY KEY,
    checkout_id UUID                     NOT NULL,
    product_id          UUID                     NOT NULL,
    sku                 VARCHAR(120)             NOT NULL,
    product_name        VARCHAR(255)             NOT NULL,
    slug                VARCHAR(300),
    main_image_url      VARCHAR(1000),
    brand_id            UUID,
    brand_name          VARCHAR(150),
    category_id         UUID,
    category_name       VARCHAR(150),
    unit_price          NUMERIC(19, 2)           NOT NULL,
    quantity            INTEGER                  NOT NULL,
    line_subtotal       NUMERIC(19, 2)           NOT NULL,
    line_discount       NUMERIC(19, 2)           NOT NULL DEFAULT 0,
    line_total          NUMERIC(19, 2)           NOT NULL,
    currency            VARCHAR(10)              NOT NULL,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_checkout_items_checkout
        FOREIGN KEY (checkout_id)
            REFERENCES checkout_sessions (id)
            ON DELETE CASCADE,
    CONSTRAINT chk_checkout_items_quantity_positive CHECK (quantity > 0),
    CONSTRAINT chk_checkout_items_amounts_non_negative CHECK (
        unit_price >= 0
        AND line_subtotal >= 0
        AND line_discount >= 0
        AND line_total >= 0
    )
);

CREATE INDEX idx_checkout_items_checkout_id
    ON checkout_items (checkout_id);

CREATE INDEX idx_checkout_items_product_id
    ON checkout_items (product_id);


CREATE TABLE checkout_addresses
(
    id                  UUID PRIMARY KEY,
    checkout_id UUID                     NOT NULL,
    address_type        VARCHAR(30)              NOT NULL,
    original_address_id UUID,
    recipient_name      VARCHAR(150)             NOT NULL,
    recipient_phone     VARCHAR(30),
    country             VARCHAR(100)             NOT NULL,
    city                VARCHAR(100)             NOT NULL,
    district            VARCHAR(100)             NOT NULL,
    neighborhood        VARCHAR(150),
    address_line1       VARCHAR(500)             NOT NULL,
    address_line2       VARCHAR(500),
    postal_code         VARCHAR(30),
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_checkout_addresses_checkout
        FOREIGN KEY (checkout_id)
            REFERENCES checkout_sessions (id)
            ON DELETE CASCADE
);

CREATE INDEX idx_checkout_addresses_checkout_id
    ON checkout_addresses (checkout_id);

CREATE INDEX idx_checkout_addresses_original_address_id
    ON checkout_addresses (original_address_id);


CREATE TABLE checkout_discounts
(
    id                  UUID PRIMARY KEY,
    checkout_id UUID                     NOT NULL,
    promotion_id        UUID,
    coupon_id           UUID,
    coupon_code         VARCHAR(100),
    promotion_name      VARCHAR(150),
    promotion_type      VARCHAR(50),
    discount_amount     NUMERIC(19, 2)           NOT NULL DEFAULT 0,
    currency            VARCHAR(10)              NOT NULL,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_checkout_discounts_checkout
        FOREIGN KEY (checkout_id)
            REFERENCES checkout_sessions (id)
            ON DELETE CASCADE,
    CONSTRAINT chk_checkout_discounts_amount_non_negative CHECK (discount_amount >= 0)
);

CREATE INDEX idx_checkout_discounts_checkout_id
    ON checkout_discounts (checkout_id);

CREATE INDEX idx_checkout_discounts_promotion_id
    ON checkout_discounts (promotion_id);

CREATE INDEX idx_checkout_discounts_coupon_id
    ON checkout_discounts (coupon_id);


CREATE TABLE checkout_saga_steps
(
    id                    UUID PRIMARY KEY,
    checkout_id   UUID                     NOT NULL,
    step_name             VARCHAR(100)             NOT NULL,
    status                VARCHAR(50)              NOT NULL,
    attempt_count         INTEGER                  NOT NULL DEFAULT 1,
    external_reference_id VARCHAR(150),
    error_code            VARCHAR(100),
    error_message         VARCHAR(1000),
    started_at            TIMESTAMP WITH TIME ZONE NOT NULL,
    completed_at          TIMESTAMP WITH TIME ZONE,
    failed_at             TIMESTAMP WITH TIME ZONE,
    created_at            TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_checkout_saga_steps_checkout
        FOREIGN KEY (checkout_id)
            REFERENCES checkout_sessions (id)
            ON DELETE CASCADE,
    CONSTRAINT chk_checkout_saga_steps_attempt_count_positive CHECK (attempt_count > 0)
);

CREATE INDEX idx_checkout_saga_steps_checkout_id
    ON checkout_saga_steps (checkout_id);

CREATE INDEX idx_checkout_saga_steps_step_name
    ON checkout_saga_steps (step_name);

CREATE INDEX idx_checkout_saga_steps_status
    ON checkout_saga_steps (status);


CREATE TABLE checkout_idempotency_records
(
    id                  UUID PRIMARY KEY,
    idempotency_key     VARCHAR(120)             NOT NULL UNIQUE,
    user_id             UUID                     NOT NULL,
    request_hash        VARCHAR(128)             NOT NULL,
    checkout_session_id UUID,
    http_status         INTEGER,
    response_payload    JSONB                    NOT NULL DEFAULT '{}'::jsonb,
    locked_until        TIMESTAMP WITH TIME ZONE,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_checkout_idempotency_key
    ON checkout_idempotency_records (idempotency_key);

CREATE INDEX idx_checkout_idempotency_user_id
    ON checkout_idempotency_records (user_id);

CREATE INDEX idx_checkout_idempotency_checkout_id
    ON checkout_idempotency_records (checkout_session_id);

CREATE INDEX idx_checkout_idempotency_locked_until
    ON checkout_idempotency_records (locked_until);


CREATE TABLE processed_payment_events
(
    id                  UUID PRIMARY KEY,
    event_id            VARCHAR(120)             NOT NULL UNIQUE,
    event_type          VARCHAR(120)             NOT NULL,
    payment_id          UUID,
    checkout_session_id UUID,
    processed_at        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_processed_payment_events_event_id
    ON processed_payment_events (event_id);

CREATE INDEX idx_processed_payment_events_event_type
    ON processed_payment_events (event_type);

CREATE INDEX idx_processed_payment_events_payment_id
    ON processed_payment_events (payment_id);

CREATE INDEX idx_processed_payment_events_checkout_id
    ON processed_payment_events (checkout_session_id);
