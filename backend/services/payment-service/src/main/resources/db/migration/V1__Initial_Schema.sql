CREATE TABLE payments
(
    id                       UUID PRIMARY KEY,

    checkout_id              UUID                     NOT NULL,
    order_id                 UUID                     NOT NULL UNIQUE,
    order_number             VARCHAR(80)              NOT NULL,
    user_id                  UUID                     NOT NULL,

    provider                 VARCHAR(40)              NOT NULL,
    method                   VARCHAR(50)              NOT NULL,
    status                   VARCHAR(50)              NOT NULL,

    amount                   NUMERIC(19, 2)           NOT NULL,
    paid_amount              NUMERIC(19, 2)           NOT NULL,
    refunded_amount          NUMERIC(19, 2)           NOT NULL,
    currency                 VARCHAR(3)               NOT NULL,

    provider_payment_id      VARCHAR(150),
    provider_transaction_id  VARCHAR(150),
    provider_conversation_id VARCHAR(150),
    provider_status          VARCHAR(100),
    failure_reason           VARCHAR(1000),

    version                  BIGINT,

    created_at               TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at               TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at             TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_payments_checkout_id ON payments (checkout_id);
CREATE UNIQUE INDEX idx_payments_order_id ON payments (order_id);
CREATE INDEX idx_payments_user_id ON payments (user_id);
CREATE INDEX idx_payments_status ON payments (status);
CREATE INDEX idx_payments_provider ON payments (provider);


CREATE TABLE payment_attempts
(
    id                         UUID PRIMARY KEY,
    payment_id                 UUID                     NOT NULL,

    attempt_number             INTEGER                  NOT NULL,
    idempotency_key            VARCHAR(120)             NOT NULL,
    request_hash               VARCHAR(128)             NOT NULL,

    provider                   VARCHAR(40)              NOT NULL,
    method                     VARCHAR(50)              NOT NULL,
    status                     VARCHAR(50)              NOT NULL,

    amount                     NUMERIC(19, 2)           NOT NULL,
    paid_amount                NUMERIC(19, 2)           NOT NULL,
    currency                   VARCHAR(3)               NOT NULL,

    provider_token             VARCHAR(250),
    provider_payment_id        VARCHAR(150),
    provider_transaction_id    VARCHAR(150),
    provider_conversation_id   VARCHAR(150),
    provider_status            VARCHAR(100),

    payment_page_url           VARCHAR(1500),
    checkout_form_content      TEXT,

    failure_reason             VARCHAR(1000),
    provider_response_snapshot JSONB                    NOT NULL DEFAULT '{}'::jsonb,

    created_at                 TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                 TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at               TIMESTAMP WITH TIME ZONE,

    CONSTRAINT fk_payment_attempts_payment
        FOREIGN KEY (payment_id)
            REFERENCES payments (id)
            ON DELETE CASCADE
);

CREATE INDEX idx_payment_attempts_payment_id ON payment_attempts (payment_id);
CREATE UNIQUE INDEX idx_payment_attempts_provider_token
    ON payment_attempts (provider, provider_token) WHERE provider_token IS NOT NULL;
CREATE INDEX idx_payment_attempts_idempotency_key ON payment_attempts (idempotency_key);
CREATE INDEX idx_payment_attempts_status ON payment_attempts (status);


CREATE TABLE payment_idempotency_records
(
    id                 UUID PRIMARY KEY,

    idempotency_key    VARCHAR(120)             NOT NULL UNIQUE,
    request_hash       VARCHAR(128)             NOT NULL,

    payment_id         UUID,
    payment_attempt_id UUID,

    http_status        INTEGER,
    response_payload   JSONB                    NOT NULL DEFAULT '{}'::jsonb,

    locked_until       TIMESTAMP WITH TIME ZONE,

    created_at         TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX idx_payment_idempotency_key ON payment_idempotency_records (idempotency_key);
CREATE INDEX idx_payment_idempotency_payment_id ON payment_idempotency_records (payment_id);
CREATE INDEX idx_payment_idempotency_attempt_id ON payment_idempotency_records (payment_attempt_id);


CREATE TABLE payment_callbacks
(
    id               UUID PRIMARY KEY,

    provider         VARCHAR(40)              NOT NULL,
    event_key        VARCHAR(250)             NOT NULL,
    provider_token   VARCHAR(250),

    processed        BOOLEAN                  NOT NULL DEFAULT FALSE,
    processing_error VARCHAR(1000),

    payload_snapshot JSONB                    NOT NULL DEFAULT '{}'::jsonb,

    received_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at     TIMESTAMP WITH TIME ZONE
);

CREATE UNIQUE INDEX idx_payment_callbacks_provider_event_key ON payment_callbacks (provider, event_key);
CREATE INDEX idx_payment_callbacks_provider_token ON payment_callbacks (provider, provider_token);
CREATE INDEX idx_payment_callbacks_processed ON payment_callbacks (processed);


CREATE TABLE payment_refunds
(
    id                 UUID PRIMARY KEY,
    payment_id         UUID                     NOT NULL,

    idempotency_key    VARCHAR(120)             NOT NULL UNIQUE,
    request_hash       VARCHAR(128)             NOT NULL,

    amount             NUMERIC(19, 2)           NOT NULL,
    currency           VARCHAR(3)               NOT NULL,

    status             VARCHAR(40)              NOT NULL,

    provider_refund_id VARCHAR(150),
    provider_status    VARCHAR(100),
    failure_reason     VARCHAR(1000),

    created_at         TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at       TIMESTAMP WITH TIME ZONE,

    CONSTRAINT fk_payment_refunds_payment
        FOREIGN KEY (payment_id)
            REFERENCES payments (id)
            ON DELETE CASCADE
);

CREATE INDEX idx_payment_refunds_payment_id ON payment_refunds (payment_id);
CREATE UNIQUE INDEX idx_payment_refunds_idempotency_key ON payment_refunds (idempotency_key);
CREATE INDEX idx_payment_refunds_status ON payment_refunds (status);


CREATE TABLE payment_cancellations
(
    id                 UUID PRIMARY KEY,
    payment_id         UUID                     NOT NULL,

    idempotency_key    VARCHAR(120)             NOT NULL UNIQUE,
    request_hash       VARCHAR(128)             NOT NULL,

    status             VARCHAR(40)              NOT NULL,

    provider_cancel_id VARCHAR(150),
    provider_status    VARCHAR(100),
    failure_reason     VARCHAR(1000),

    created_at         TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at       TIMESTAMP WITH TIME ZONE,

    CONSTRAINT fk_payment_cancellations_payment
        FOREIGN KEY (payment_id)
            REFERENCES payments (id)
            ON DELETE CASCADE
);

CREATE INDEX idx_payment_cancellations_payment_id ON payment_cancellations (payment_id);
CREATE UNIQUE INDEX idx_payment_cancellations_idempotency_key ON payment_cancellations (idempotency_key);
CREATE INDEX idx_payment_cancellations_status ON payment_cancellations (status);
