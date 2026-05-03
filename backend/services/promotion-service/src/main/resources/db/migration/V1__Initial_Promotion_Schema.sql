CREATE
EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE promotions
(
    id                   UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    name                 VARCHAR(150) NOT NULL,
    description          VARCHAR(1000),
    type                 VARCHAR(50)  NOT NULL,
    status               VARCHAR(30)  NOT NULL,
    coupon_required      BOOLEAN      NOT NULL,
    stackable            BOOLEAN      NOT NULL,
    priority             INTEGER      NOT NULL,
    rule_config          JSONB        NOT NULL,
    global_usage_limit   INTEGER,
    per_user_usage_limit INTEGER,
    reserved_usage_count INTEGER      NOT NULL DEFAULT 0,
    redeemed_usage_count INTEGER      NOT NULL DEFAULT 0,
    starts_at            TIMESTAMPTZ  NOT NULL,
    ends_at              TIMESTAMPTZ,
    created_at           TIMESTAMPTZ  NOT NULL,
    updated_at           TIMESTAMPTZ  NOT NULL,
    version              BIGINT       NOT NULL DEFAULT 0,

    CONSTRAINT chk_promotions_type CHECK (
        type IN (
                 'PERCENTAGE_DISCOUNT',
                 'FIXED_AMOUNT_DISCOUNT',
                 'CATEGORY_PERCENTAGE_DISCOUNT',
                 'BRAND_PERCENTAGE_DISCOUNT',
                 'PRODUCT_PERCENTAGE_DISCOUNT',
                 'FREE_SHIPPING'
            )
        ),

    CONSTRAINT chk_promotions_status CHECK (
        status IN ('DRAFT', 'ACTIVE', 'PAUSED', 'EXPIRED', 'DELETED')
        ),

    CONSTRAINT chk_promotions_priority CHECK (priority >= 0),
    CONSTRAINT chk_promotions_global_usage_limit CHECK (global_usage_limit IS NULL OR global_usage_limit > 0),
    CONSTRAINT chk_promotions_per_user_usage_limit CHECK (per_user_usage_limit IS NULL OR per_user_usage_limit > 0),
    CONSTRAINT chk_promotions_reserved_usage_count CHECK (reserved_usage_count >= 0),
    CONSTRAINT chk_promotions_redeemed_usage_count CHECK (redeemed_usage_count >= 0),
    CONSTRAINT chk_promotions_usage_limit_not_exceeded CHECK (
        global_usage_limit IS NULL OR reserved_usage_count + redeemed_usage_count <= global_usage_limit
        ),
    CONSTRAINT chk_promotions_date_range CHECK (ends_at IS NULL OR ends_at > starts_at)
);

CREATE INDEX idx_promotions_status_dates
    ON promotions (status, starts_at, ends_at);

CREATE INDEX idx_promotions_type_status
    ON promotions (type, status);

CREATE INDEX idx_promotions_priority
    ON promotions (priority);

CREATE INDEX idx_promotions_rule_config_gin
    ON promotions USING GIN (rule_config);


CREATE TABLE coupons
(
    id                   UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    code                 VARCHAR(80) NOT NULL,
    promotion_id         UUID        NOT NULL,
    status               VARCHAR(30) NOT NULL,
    usage_limit          INTEGER,
    per_user_usage_limit INTEGER,
    reserved_usage_count INTEGER     NOT NULL DEFAULT 0,
    redeemed_usage_count INTEGER     NOT NULL DEFAULT 0,
    starts_at            TIMESTAMPTZ,
    ends_at              TIMESTAMPTZ,
    created_at           TIMESTAMPTZ NOT NULL,
    updated_at           TIMESTAMPTZ NOT NULL,
    version              BIGINT      NOT NULL DEFAULT 0,

    CONSTRAINT uk_coupons_code UNIQUE (code),

    CONSTRAINT fk_coupons_promotion
        FOREIGN KEY (promotion_id)
            REFERENCES promotions (id),

    CONSTRAINT chk_coupons_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'EXPIRED')),
    CONSTRAINT chk_coupons_usage_limit CHECK (usage_limit IS NULL OR usage_limit > 0),
    CONSTRAINT chk_coupons_per_user_usage_limit CHECK (per_user_usage_limit IS NULL OR per_user_usage_limit > 0),
    CONSTRAINT chk_coupons_reserved_usage_count CHECK (reserved_usage_count >= 0),
    CONSTRAINT chk_coupons_redeemed_usage_count CHECK (redeemed_usage_count >= 0),
    CONSTRAINT chk_coupons_usage_limit_not_exceeded CHECK (
        usage_limit IS NULL OR reserved_usage_count + redeemed_usage_count <= usage_limit
        ),
    CONSTRAINT chk_coupons_date_range CHECK (ends_at IS NULL OR starts_at IS NULL OR ends_at > starts_at)
);

CREATE INDEX idx_coupons_promotion_id
    ON coupons (promotion_id);

CREATE INDEX idx_coupons_status
    ON coupons (status);


CREATE TABLE coupon_assignments
(
    id           UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    coupon_id    UUID        NOT NULL,
    user_id      UUID        NOT NULL,
    status       VARCHAR(30) NOT NULL,
    assigned_at  TIMESTAMPTZ NOT NULL,
    expires_at   TIMESTAMPTZ,
    reserved_at  TIMESTAMPTZ,
    redeemed_at  TIMESTAMPTZ,
    cancelled_at TIMESTAMPTZ,
    expired_at   TIMESTAMPTZ,
    updated_at   TIMESTAMPTZ NOT NULL,
    version      BIGINT      NOT NULL DEFAULT 0,

    CONSTRAINT uk_coupon_assignments_coupon_user UNIQUE (coupon_id, user_id),

    CONSTRAINT fk_coupon_assignments_coupon
        FOREIGN KEY (coupon_id)
            REFERENCES coupons (id),

    CONSTRAINT chk_coupon_assignments_status CHECK (
        status IN ('ASSIGNED', 'RESERVED', 'REDEEMED', 'EXPIRED', 'CANCELLED')
        )
);

CREATE INDEX idx_coupon_assignments_user_id
    ON coupon_assignments (user_id);

CREATE INDEX idx_coupon_assignments_coupon_id
    ON coupon_assignments (coupon_id);

CREATE INDEX idx_coupon_assignments_status
    ON coupon_assignments (status);

CREATE INDEX idx_coupon_assignments_expires_at
    ON coupon_assignments (expires_at);


CREATE TABLE promotion_usage_reservations
(
    id              UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    idempotency_key VARCHAR(120) NOT NULL,
    request_hash    VARCHAR(128) NOT NULL,
    checkout_id     UUID         NOT NULL,
    user_id         UUID         NOT NULL,
    order_id        UUID,
    status          VARCHAR(30)  NOT NULL,
    reserved_until  TIMESTAMPTZ  NOT NULL,
    redeemed_at     TIMESTAMPTZ,
    cancelled_at    TIMESTAMPTZ,
    expired_at      TIMESTAMPTZ,
    cancel_reason   VARCHAR(40),
    created_at      TIMESTAMPTZ  NOT NULL,
    updated_at      TIMESTAMPTZ  NOT NULL,
    version         BIGINT       NOT NULL DEFAULT 0,

    CONSTRAINT uk_promotion_usage_reservations_idempotency_key UNIQUE (idempotency_key),

    CONSTRAINT chk_promotion_usage_reservations_status CHECK (
        status IN ('RESERVED', 'REDEEMED', 'CANCELLED', 'EXPIRED')
        ),

    CONSTRAINT chk_promotion_usage_reservations_cancel_reason CHECK (
        cancel_reason IS NULL OR
        cancel_reason IN (
                          'PAYMENT_FAILED',
                          'CHECKOUT_FAILED',
                          'CUSTOMER_CANCELLED',
                          'TIMEOUT',
                          'ADMIN_CANCELLED',
                          'UNKNOWN'
            )
        )
);

CREATE INDEX idx_promotion_usage_reservations_checkout_id
    ON promotion_usage_reservations (checkout_id);

CREATE INDEX idx_promotion_usage_reservations_order_id
    ON promotion_usage_reservations (order_id);

CREATE INDEX idx_promotion_usage_reservations_user_id
    ON promotion_usage_reservations (user_id);

CREATE INDEX idx_promotion_usage_reservations_status
    ON promotion_usage_reservations (status);

CREATE INDEX idx_promotion_usage_reservations_reserved_until
    ON promotion_usage_reservations (reserved_until);


CREATE TABLE promotion_usage_reservation_items
(
    id                       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    reservation_id           UUID           NOT NULL,
    promotion_id             UUID           NOT NULL,
    coupon_id                UUID,
    coupon_assignment_id     UUID,
    coupon_code              VARCHAR(80),
    discount_amount          NUMERIC(19, 2) NOT NULL,
    shipping_discount_amount NUMERIC(19, 2) NOT NULL,
    description              VARCHAR(300),
    created_at               TIMESTAMPTZ    NOT NULL,

    CONSTRAINT fk_promotion_usage_reservation_items_reservation
        FOREIGN KEY (reservation_id)
            REFERENCES promotion_usage_reservations (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_promotion_usage_reservation_items_promotion
        FOREIGN KEY (promotion_id)
            REFERENCES promotions (id),

    CONSTRAINT fk_promotion_usage_reservation_items_coupon
        FOREIGN KEY (coupon_id)
            REFERENCES coupons (id),

    CONSTRAINT fk_promotion_usage_reservation_items_assignment
        FOREIGN KEY (coupon_assignment_id)
            REFERENCES coupon_assignments (id),

    CONSTRAINT chk_promotion_usage_reservation_items_discount
        CHECK (discount_amount >= 0),

    CONSTRAINT chk_promotion_usage_reservation_items_shipping_discount
        CHECK (shipping_discount_amount >= 0)
);

CREATE INDEX idx_promotion_usage_reservation_items_reservation_id
    ON promotion_usage_reservation_items (reservation_id);

CREATE INDEX idx_promotion_usage_reservation_items_promotion_id
    ON promotion_usage_reservation_items (promotion_id);

CREATE INDEX idx_promotion_usage_reservation_items_coupon_id
    ON promotion_usage_reservation_items (coupon_id);

CREATE INDEX idx_promotion_usage_reservation_items_assignment_id
    ON promotion_usage_reservation_items (coupon_assignment_id);