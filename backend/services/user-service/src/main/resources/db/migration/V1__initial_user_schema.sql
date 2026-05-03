CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE user_profiles (
    user_id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    phone_number VARCHAR(30),
    avatar_url VARCHAR(1000),
    language VARCHAR(10) NOT NULL,
    marketing_opt_in BOOLEAN NOT NULL DEFAULT FALSE,
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT chk_user_profiles_status
        CHECK (status IN ('ACTIVE', 'DISABLED', 'DELETED')),

    CONSTRAINT chk_user_profiles_email_not_blank
        CHECK (length(trim(email)) > 0)
);

CREATE INDEX idx_user_profiles_email
    ON user_profiles(email);

CREATE INDEX idx_user_profiles_status
    ON user_profiles(status);

CREATE INDEX idx_user_profiles_updated_at
    ON user_profiles(updated_at);


CREATE TABLE user_addresses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    title VARCHAR(100) NOT NULL,
    type VARCHAR(30) NOT NULL,
    recipient_name VARCHAR(150) NOT NULL,
    phone_number VARCHAR(30),
    line1 VARCHAR(500) NOT NULL,
    line2 VARCHAR(500),
    district VARCHAR(100) NOT NULL,
    city VARCHAR(100) NOT NULL,
    country VARCHAR(100) NOT NULL,
    postal_code VARCHAR(20),
    default_shipping BOOLEAN NOT NULL DEFAULT FALSE,
    default_billing BOOLEAN NOT NULL DEFAULT FALSE,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT chk_user_addresses_type
        CHECK (type IN ('SHIPPING', 'BILLING', 'BOTH'))
);

CREATE INDEX idx_user_addresses_user_id
    ON user_addresses(user_id);

CREATE INDEX idx_user_addresses_type
    ON user_addresses(type);

CREATE INDEX idx_user_addresses_deleted
    ON user_addresses(deleted);

CREATE UNIQUE INDEX ux_user_addresses_default_shipping
    ON user_addresses(user_id)
    WHERE default_shipping = TRUE AND deleted = FALSE;

CREATE UNIQUE INDEX ux_user_addresses_default_billing
    ON user_addresses(user_id)
    WHERE default_billing = TRUE AND deleted = FALSE;


CREATE TABLE user_preferences (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    language VARCHAR(10) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    marketing_email_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    notification_email_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    notification_in_app_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uk_user_preferences_user_id UNIQUE (user_id),

    CONSTRAINT chk_user_preferences_currency
        CHECK (char_length(currency) = 3)
);

CREATE INDEX idx_user_preferences_user_id
    ON user_preferences(user_id);


CREATE TABLE favorite_products (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    product_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT uk_favorite_products_user_product
        UNIQUE (user_id, product_id)
);

CREATE INDEX idx_favorite_products_user_id
    ON favorite_products(user_id);

CREATE INDEX idx_favorite_products_product_id
    ON favorite_products(product_id);


CREATE TABLE product_lists (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    name VARCHAR(120) NOT NULL,
    description VARCHAR(500),
    visibility VARCHAR(40) NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT chk_product_lists_visibility
        CHECK (visibility IN ('PRIVATE', 'SHARED_LINK_OPTIONAL'))
);

CREATE INDEX idx_product_lists_user_id
    ON product_lists(user_id);

CREATE INDEX idx_product_lists_deleted
    ON product_lists(deleted);


CREATE TABLE product_list_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    list_id UUID NOT NULL,
    product_id UUID NOT NULL,
    note VARCHAR(300),
    created_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_product_list_items_list
        FOREIGN KEY (list_id)
        REFERENCES product_lists(id)
        ON DELETE CASCADE,

    CONSTRAINT uk_product_list_items_list_product
        UNIQUE (list_id, product_id)
);

CREATE INDEX idx_product_list_items_list_id
    ON product_list_items(list_id);

CREATE INDEX idx_product_list_items_product_id
    ON product_list_items(product_id);
