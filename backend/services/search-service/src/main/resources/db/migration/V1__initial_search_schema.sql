CREATE EXTENSION IF NOT EXISTS pgcrypto;
CREATE EXTENSION IF NOT EXISTS unaccent;
CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE TABLE product_search_documents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    product_id UUID NOT NULL,
    sku VARCHAR(120) NOT NULL,
    slug VARCHAR(180) NOT NULL,
    name VARCHAR(250) NOT NULL,
    description VARCHAR(3000),

    brand_id UUID,
    brand_name VARCHAR(150),

    category_id UUID,
    category_name VARCHAR(150),
    category_path JSONB NOT NULL DEFAULT '[]'::jsonb,

    base_price NUMERIC(19, 2) NOT NULL,
    discounted_price NUMERIC(19, 2),
    currency VARCHAR(3) NOT NULL,

    image_url VARCHAR(1000),

    attributes JSONB NOT NULL DEFAULT '{}'::jsonb,
    tags JSONB NOT NULL DEFAULT '[]'::jsonb,

    available_quantity INTEGER NOT NULL DEFAULT 0,
    stock_status VARCHAR(30) NOT NULL DEFAULT 'UNKNOWN',

    has_discount BOOLEAN NOT NULL DEFAULT FALSE,
    has_active_promotion BOOLEAN NOT NULL DEFAULT FALSE,
    promotion_badge VARCHAR(200),

    average_rating NUMERIC(3, 2) NOT NULL DEFAULT 0.00,
    review_count BIGINT NOT NULL DEFAULT 0,

    status VARCHAR(30) NOT NULL,
    visible BOOLEAN NOT NULL DEFAULT FALSE,

    search_vector TSVECTOR GENERATED ALWAYS AS (
        setweight(to_tsvector('simple', coalesce(name, '')), 'A') ||
        setweight(to_tsvector('simple', coalesce(brand_name, '')), 'B') ||
        setweight(to_tsvector('simple', coalesce(category_name, '')), 'B') ||
        setweight(to_tsvector('simple', coalesce(description, '')), 'C')
    ) STORED,

    source_updated_at TIMESTAMPTZ,
    stock_updated_at TIMESTAMPTZ,
    promotion_updated_at TIMESTAMPTZ,
    rating_updated_at TIMESTAMPTZ,

    indexed_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uk_product_search_documents_product_id UNIQUE (product_id),
    CONSTRAINT chk_product_search_documents_price CHECK (base_price >= 0),
    CONSTRAINT chk_product_search_documents_discounted_price CHECK (discounted_price IS NULL OR discounted_price >= 0),
    CONSTRAINT chk_product_search_documents_currency CHECK (char_length(currency) = 3),
    CONSTRAINT chk_product_search_documents_stock_status CHECK (stock_status IN ('IN_STOCK', 'LOW_STOCK', 'OUT_OF_STOCK', 'UNKNOWN')),
    CONSTRAINT chk_product_search_documents_average_rating CHECK (average_rating >= 0.00 AND average_rating <= 5.00),
    CONSTRAINT chk_product_search_documents_review_count CHECK (review_count >= 0),
    CONSTRAINT chk_product_search_documents_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'DELETED'))
);

CREATE INDEX idx_product_search_documents_product_id ON product_search_documents(product_id);
CREATE INDEX idx_product_search_documents_slug ON product_search_documents(slug);
CREATE INDEX idx_product_search_documents_brand_id ON product_search_documents(brand_id);
CREATE INDEX idx_product_search_documents_category_id ON product_search_documents(category_id);
CREATE INDEX idx_product_search_documents_status_visible ON product_search_documents(status, visible);
CREATE INDEX idx_product_search_documents_stock_status ON product_search_documents(stock_status);
CREATE INDEX idx_product_search_documents_base_price ON product_search_documents(base_price);
CREATE INDEX idx_product_search_documents_discounted_price ON product_search_documents(discounted_price);
CREATE INDEX idx_product_search_documents_effective_price ON product_search_documents((coalesce(discounted_price, base_price)));
CREATE INDEX idx_product_search_documents_average_rating ON product_search_documents(average_rating);
CREATE INDEX idx_product_search_documents_review_count ON product_search_documents(review_count);
CREATE INDEX idx_product_search_documents_source_updated_at ON product_search_documents(source_updated_at);
CREATE INDEX idx_product_search_documents_indexed_at ON product_search_documents(indexed_at);
CREATE INDEX idx_product_search_documents_search_vector ON product_search_documents USING GIN (search_vector);
CREATE INDEX idx_product_search_documents_name_trgm ON product_search_documents USING GIN (name gin_trgm_ops);
CREATE INDEX idx_product_search_documents_attributes_gin ON product_search_documents USING GIN (attributes);
CREATE INDEX idx_product_search_documents_tags_gin ON product_search_documents USING GIN (tags);
CREATE INDEX idx_product_search_documents_public_listing
    ON product_search_documents(status, visible, indexed_at DESC)
    WHERE status = 'ACTIVE' AND visible = TRUE;

CREATE TABLE processed_search_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    event_id VARCHAR(120) NOT NULL,
    event_type VARCHAR(150) NOT NULL,
    source_service VARCHAR(100),
    aggregate_id VARCHAR(120),
    correlation_id VARCHAR(120),
    occurred_at TIMESTAMPTZ,
    processed_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT uk_processed_search_events_event_id UNIQUE (event_id),
    CONSTRAINT chk_processed_search_events_event_id_not_blank CHECK (length(trim(event_id)) > 0),
    CONSTRAINT chk_processed_search_events_event_type_not_blank CHECK (length(trim(event_type)) > 0)
);

CREATE INDEX idx_processed_search_events_event_id ON processed_search_events(event_id);
CREATE INDEX idx_processed_search_events_event_type ON processed_search_events(event_type);
CREATE INDEX idx_processed_search_events_aggregate_id ON processed_search_events(aggregate_id);
CREATE INDEX idx_processed_search_events_processed_at ON processed_search_events(processed_at);
