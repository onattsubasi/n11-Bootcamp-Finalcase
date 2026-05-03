CREATE TABLE IF NOT EXISTS baskets
(
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    status VARCHAR(30) NOT NULL,
    coupon_code_intent VARCHAR(80),
    order_id UUID,
    checked_out_at TIMESTAMPTZ,
    cleared_at TIMESTAMPTZ,
    abandoned_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT chk_baskets_status
        CHECK (status IN ('ACTIVE', 'CHECKED_OUT', 'ABANDONED', 'CLEARED'))
);

CREATE INDEX IF NOT EXISTS idx_baskets_user_id
    ON baskets(user_id);

CREATE INDEX IF NOT EXISTS idx_baskets_status
    ON baskets(status);

CREATE INDEX IF NOT EXISTS idx_baskets_order_id
    ON baskets(order_id);

CREATE INDEX IF NOT EXISTS idx_baskets_updated_at
    ON baskets(updated_at);

CREATE UNIQUE INDEX IF NOT EXISTS ux_baskets_user_active
    ON baskets(user_id)
    WHERE status = 'ACTIVE';

CREATE TABLE IF NOT EXISTS basket_items
(
    id UUID PRIMARY KEY,
    basket_id UUID NOT NULL,
    product_id UUID NOT NULL,
    quantity INTEGER NOT NULL,
    item_status VARCHAR(30) NOT NULL,
    product_name_snapshot VARCHAR(255),
    image_url_snapshot VARCHAR(1000),
    unit_price_snapshot NUMERIC(19, 2),
    snapshot_currency VARCHAR(3),
    stale_reason VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_basket_items_basket
        FOREIGN KEY (basket_id)
        REFERENCES baskets(id)
        ON DELETE CASCADE,
    CONSTRAINT chk_basket_items_quantity_positive
        CHECK (quantity >= 1),
    CONSTRAINT chk_basket_items_quantity_max
        CHECK (quantity <= 99),
    CONSTRAINT chk_basket_items_status
        CHECK (item_status IN ('ACTIVE', 'STALE', 'UNAVAILABLE', 'PRICE_CHANGED', 'REMOVED_BY_SYSTEM')),
    CONSTRAINT chk_basket_items_snapshot_currency_length
        CHECK (snapshot_currency IS NULL OR char_length(snapshot_currency) = 3),
    CONSTRAINT chk_basket_items_unit_price_positive
        CHECK (unit_price_snapshot IS NULL OR unit_price_snapshot > 0)
);

CREATE INDEX IF NOT EXISTS idx_basket_items_basket_id
    ON basket_items(basket_id);

CREATE INDEX IF NOT EXISTS idx_basket_items_product_id
    ON basket_items(product_id);

CREATE INDEX IF NOT EXISTS idx_basket_items_status
    ON basket_items(item_status);

CREATE UNIQUE INDEX IF NOT EXISTS ux_basket_items_basket_product
    ON basket_items(basket_id, product_id);
