CREATE
EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE brands
(
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(120) NOT NULL,
    slug        VARCHAR(140) NOT NULL,
    description TEXT,
    logo_url    VARCHAR(1000),
    status      VARCHAR(30)  NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL,
    updated_at  TIMESTAMPTZ  NOT NULL,

    CONSTRAINT uk_brands_slug UNIQUE (slug),
    CONSTRAINT chk_brands_status CHECK (status IN ('ACTIVE', 'SUSPENDED', 'DELETED'))
);

CREATE INDEX idx_brands_status ON brands (status);
CREATE INDEX idx_brands_name ON brands (name);

CREATE TABLE categories
(
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(120)  NOT NULL,
    slug        VARCHAR(140)  NOT NULL,
    description TEXT,
    parent_id   UUID,
    path        VARCHAR(1000) NOT NULL,
    level       INTEGER       NOT NULL,
    status      VARCHAR(30)   NOT NULL,
    sort_order  INTEGER       NOT NULL,
    created_at  TIMESTAMPTZ   NOT NULL,
    updated_at  TIMESTAMPTZ   NOT NULL,

    CONSTRAINT uk_categories_slug UNIQUE (slug),
    CONSTRAINT uk_categories_path UNIQUE (path),
    CONSTRAINT fk_categories_parent
        FOREIGN KEY (parent_id)
            REFERENCES categories (id),
    CONSTRAINT chk_categories_level CHECK (level >= 0),
    CONSTRAINT chk_categories_sort_order CHECK (sort_order >= 0),
    CONSTRAINT chk_categories_status CHECK (status IN ('ACTIVE', 'SUSPENDED', 'DELETED'))
);

CREATE INDEX idx_categories_parent_id ON categories (parent_id);
CREATE INDEX idx_categories_status ON categories (status);
CREATE INDEX idx_categories_level ON categories (level);

CREATE TABLE products
(
    id                  UUID PRIMARY KEY        DEFAULT gen_random_uuid(),

    sku                 VARCHAR(120)   NOT NULL,
    slug                VARCHAR(220)   NOT NULL,
    name                VARCHAR(200)   NOT NULL,
    description         TEXT,

    base_price_amount   NUMERIC(19, 2) NOT NULL,
    base_price_currency VARCHAR(3)     NOT NULL,

    brand_id            UUID           NOT NULL,
    brand_name          VARCHAR(120)   NOT NULL,
    brand_slug          VARCHAR(140)   NOT NULL,

    category_id         UUID           NOT NULL,
    category_name       VARCHAR(120)   NOT NULL,
    category_slug       VARCHAR(140)   NOT NULL,
    category_path       VARCHAR(1000)  NOT NULL,
    category_ancestors  JSONB          NOT NULL DEFAULT '[]'::jsonb,

    owner_type          VARCHAR(30)    NOT NULL,
    owner_store_id      VARCHAR(100)   NOT NULL,
    owner_store_name    VARCHAR(200)   NOT NULL,

    images              JSONB          NOT NULL DEFAULT '[]'::jsonb,
    attributes          JSONB          NOT NULL DEFAULT '{}'::jsonb,

    status              VARCHAR(40)    NOT NULL,

    created_at          TIMESTAMPTZ    NOT NULL,
    updated_at          TIMESTAMPTZ    NOT NULL,

    CONSTRAINT uk_products_sku UNIQUE (sku),
    CONSTRAINT uk_products_slug UNIQUE (slug),

    CONSTRAINT fk_products_brand
        FOREIGN KEY (brand_id)
            REFERENCES brands (id),

    CONSTRAINT fk_products_category
        FOREIGN KEY (category_id)
            REFERENCES categories (id),

    CONSTRAINT chk_products_price_positive CHECK (base_price_amount > 0),
    CONSTRAINT chk_products_currency_length CHECK (char_length(base_price_currency) = 3),
    CONSTRAINT chk_products_status CHECK (
        status IN (
                   'DRAFT',
                   'ACTIVE',
                   'SUSPENDED',
                   'DELETED',
                   'PENDING_APPROVAL',
                   'REJECTED'
            )
        ),
    CONSTRAINT chk_products_owner_type CHECK (owner_type IN ('PLATFORM', 'STORE'))
);

CREATE INDEX idx_products_status ON products (status);
CREATE INDEX idx_products_brand_id ON products (brand_id);
CREATE INDEX idx_products_category_id ON products (category_id);
CREATE INDEX idx_products_updated_at ON products (updated_at);

CREATE INDEX idx_products_attributes_gin ON products USING GIN (attributes);
CREATE INDEX idx_products_images_gin ON products USING GIN (images);
CREATE INDEX idx_products_category_ancestors_gin ON products USING GIN (category_ancestors);