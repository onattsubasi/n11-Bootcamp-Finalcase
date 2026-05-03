CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE reviews (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    product_id UUID NOT NULL,
    user_id UUID NOT NULL,
    order_id UUID NOT NULL,
    order_item_id UUID,
    order_number VARCHAR(80) NOT NULL,
    delivered_at TIMESTAMPTZ,

    author_display_name VARCHAR(120) NOT NULL,

    rating INTEGER NOT NULL,
    title VARCHAR(150),
    comment VARCHAR(5000),
    images JSONB NOT NULL DEFAULT '[]'::jsonb,

    status VARCHAR(40) NOT NULL,
    visible BOOLEAN NOT NULL DEFAULT FALSE,
    verified_purchase BOOLEAN NOT NULL DEFAULT TRUE,

    helpful_count INTEGER NOT NULL DEFAULT 0,
    unhelpful_count INTEGER NOT NULL DEFAULT 0,
    report_count INTEGER NOT NULL DEFAULT 0,

    moderation_metadata JSONB NOT NULL DEFAULT '{}'::jsonb,

    approved_at TIMESTAMPTZ,
    rejected_at TIMESTAMPTZ,
    hidden_at TIMESTAMPTZ,
    deleted_at TIMESTAMPTZ,

    last_moderated_by UUID,
    last_moderated_at TIMESTAMPTZ,

    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT chk_reviews_rating
        CHECK (rating BETWEEN 1 AND 5),

    CONSTRAINT chk_reviews_status
        CHECK (status IN ('PENDING_MODERATION', 'APPROVED', 'REJECTED', 'HIDDEN', 'DELETED')),

    CONSTRAINT chk_reviews_helpful_count
        CHECK (helpful_count >= 0),

    CONSTRAINT chk_reviews_unhelpful_count
        CHECK (unhelpful_count >= 0),

    CONSTRAINT chk_reviews_report_count
        CHECK (report_count >= 0),

    CONSTRAINT chk_reviews_order_number_not_blank
        CHECK (length(trim(order_number)) > 0),

    CONSTRAINT chk_reviews_author_display_name_not_blank
        CHECK (length(trim(author_display_name)) > 0)
);

CREATE UNIQUE INDEX ux_reviews_user_product_active
    ON reviews(user_id, product_id)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_reviews_product_id
    ON reviews(product_id);

CREATE INDEX idx_reviews_user_id
    ON reviews(user_id);

CREATE INDEX idx_reviews_order_id
    ON reviews(order_id);

CREATE INDEX idx_reviews_status
    ON reviews(status);

CREATE INDEX idx_reviews_visible
    ON reviews(visible);

CREATE INDEX idx_reviews_rating
    ON reviews(rating);

CREATE INDEX idx_reviews_created_at
    ON reviews(created_at);

CREATE INDEX idx_reviews_deleted_at
    ON reviews(deleted_at);

CREATE INDEX idx_reviews_public_product_created
    ON reviews(product_id, created_at DESC)
    WHERE status = 'APPROVED'
      AND visible = TRUE
      AND deleted_at IS NULL;

CREATE INDEX idx_reviews_images_gin
    ON reviews USING GIN (images);

CREATE INDEX idx_reviews_moderation_metadata_gin
    ON reviews USING GIN (moderation_metadata);


CREATE TABLE product_rating_summaries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id UUID NOT NULL,
    average_rating NUMERIC(3, 2) NOT NULL DEFAULT 0.00,
    review_count BIGINT NOT NULL DEFAULT 0,
    rating_1_count BIGINT NOT NULL DEFAULT 0,
    rating_2_count BIGINT NOT NULL DEFAULT 0,
    rating_3_count BIGINT NOT NULL DEFAULT 0,
    rating_4_count BIGINT NOT NULL DEFAULT 0,
    rating_5_count BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uk_product_rating_summaries_product_id
        UNIQUE (product_id),

    CONSTRAINT chk_product_rating_summaries_average
        CHECK (average_rating >= 0.00 AND average_rating <= 5.00),

    CONSTRAINT chk_product_rating_summaries_review_count
        CHECK (review_count >= 0),

    CONSTRAINT chk_product_rating_summaries_rating_1
        CHECK (rating_1_count >= 0),

    CONSTRAINT chk_product_rating_summaries_rating_2
        CHECK (rating_2_count >= 0),

    CONSTRAINT chk_product_rating_summaries_rating_3
        CHECK (rating_3_count >= 0),

    CONSTRAINT chk_product_rating_summaries_rating_4
        CHECK (rating_4_count >= 0),

    CONSTRAINT chk_product_rating_summaries_rating_5
        CHECK (rating_5_count >= 0)
);

CREATE INDEX idx_product_rating_summaries_product_id
    ON product_rating_summaries(product_id);

CREATE INDEX idx_product_rating_summaries_average_rating
    ON product_rating_summaries(average_rating);

CREATE INDEX idx_product_rating_summaries_review_count
    ON product_rating_summaries(review_count);


CREATE TABLE review_votes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    review_id UUID NOT NULL,
    user_id UUID NOT NULL,
    vote_type VARCHAR(30) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_review_votes_review
        FOREIGN KEY (review_id)
        REFERENCES reviews(id)
        ON DELETE CASCADE,

    CONSTRAINT uk_review_votes_review_user
        UNIQUE (review_id, user_id),

    CONSTRAINT chk_review_votes_type
        CHECK (vote_type IN ('HELPFUL', 'UNHELPFUL'))
);

CREATE INDEX idx_review_votes_review_id
    ON review_votes(review_id);

CREATE INDEX idx_review_votes_user_id
    ON review_votes(user_id);


CREATE TABLE review_reports (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    review_id UUID NOT NULL,
    reporter_user_id UUID NOT NULL,
    reason VARCHAR(40) NOT NULL,
    description VARCHAR(1000),
    status VARCHAR(30) NOT NULL,
    resolved_by UUID,
    resolved_at TIMESTAMPTZ,
    resolution_note VARCHAR(1000),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT fk_review_reports_review
        FOREIGN KEY (review_id)
        REFERENCES reviews(id)
        ON DELETE CASCADE,

    CONSTRAINT uk_review_reports_review_reporter
        UNIQUE (review_id, reporter_user_id),

    CONSTRAINT chk_review_reports_reason
        CHECK (reason IN ('SPAM', 'OFFENSIVE', 'FAKE', 'IRRELEVANT', 'PERSONAL_DATA', 'OTHER')),

    CONSTRAINT chk_review_reports_status
        CHECK (status IN ('OPEN', 'RESOLVED', 'DISMISSED'))
);

CREATE INDEX idx_review_reports_review_id
    ON review_reports(review_id);

CREATE INDEX idx_review_reports_reporter_user_id
    ON review_reports(reporter_user_id);

CREATE INDEX idx_review_reports_status
    ON review_reports(status);

CREATE INDEX idx_review_reports_created_at
    ON review_reports(created_at);
