CREATE TABLE shipments
(
    id                  UUID PRIMARY KEY,

    shipment_number     VARCHAR(80)              NOT NULL UNIQUE,

    order_id            UUID                     NOT NULL UNIQUE,
    order_number        VARCHAR(80)              NOT NULL,
    user_id             UUID                     NOT NULL,

    carrier             VARCHAR(50)              NOT NULL,
    status              VARCHAR(50)              NOT NULL,

    carrier_shipment_id VARCHAR(150),
    tracking_number     VARCHAR(150),
    tracking_url        VARCHAR(1000),
    label_url           VARCHAR(1000),
    carrier_status      VARCHAR(100),
    failure_reason      VARCHAR(1000),

    recipient_name      VARCHAR(150)             NOT NULL,
    recipient_phone     VARCHAR(30),
    country             VARCHAR(100)             NOT NULL,
    city                VARCHAR(100)             NOT NULL,
    district            VARCHAR(100),
    neighborhood        VARCHAR(150),
    address_line_1      VARCHAR(500)             NOT NULL,
    address_line_2      VARCHAR(500),
    postal_code         VARCHAR(20),

    version             BIGINT,

    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    ready_to_ship_at    TIMESTAMP WITH TIME ZONE,
    shipped_at          TIMESTAMP WITH TIME ZONE,
    in_transit_at       TIMESTAMP WITH TIME ZONE,
    out_for_delivery_at TIMESTAMP WITH TIME ZONE,
    delivered_at        TIMESTAMP WITH TIME ZONE,
    delivery_failed_at  TIMESTAMP WITH TIME ZONE,
    cancelled_at        TIMESTAMP WITH TIME ZONE
);

CREATE UNIQUE INDEX idx_shipments_shipment_number
    ON shipments (shipment_number);

CREATE UNIQUE INDEX idx_shipments_order_id
    ON shipments (order_id);

CREATE INDEX idx_shipments_user_id
    ON shipments (user_id);

CREATE INDEX idx_shipments_status
    ON shipments (status);

CREATE INDEX idx_shipments_carrier
    ON shipments (carrier);

CREATE INDEX idx_shipments_tracking_number
    ON shipments (tracking_number);


CREATE TABLE shipment_items
(
    id           UUID PRIMARY KEY,

    shipment_id  UUID         NOT NULL,

    product_id   VARCHAR(100) NOT NULL,
    sku          VARCHAR(120) NOT NULL,
    product_name VARCHAR(250) NOT NULL,
    quantity     INTEGER      NOT NULL,

    CONSTRAINT fk_shipment_items_shipment
        FOREIGN KEY (shipment_id)
            REFERENCES shipments (id)
            ON DELETE CASCADE
);

CREATE INDEX idx_shipment_items_shipment_id
    ON shipment_items (shipment_id);

CREATE INDEX idx_shipment_items_product_id
    ON shipment_items (product_id);


CREATE TABLE shipment_status_history
(
    id          UUID PRIMARY KEY,

    shipment_id UUID                     NOT NULL,

    from_status VARCHAR(50),
    to_status   VARCHAR(50)              NOT NULL,
    source      VARCHAR(50)              NOT NULL,

    changed_by  VARCHAR(100),
    reason      VARCHAR(500),

    created_at  TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_shipment_status_history_shipment
        FOREIGN KEY (shipment_id)
            REFERENCES shipments (id)
            ON DELETE CASCADE
);

CREATE INDEX idx_shipment_status_history_shipment_id
    ON shipment_status_history (shipment_id);

CREATE INDEX idx_shipment_status_history_created_at
    ON shipment_status_history (created_at);


CREATE TABLE shipment_idempotency_records
(
    id               UUID PRIMARY KEY,

    idempotency_key  VARCHAR(120)             NOT NULL UNIQUE,
    request_hash     VARCHAR(128)             NOT NULL,

    order_id         UUID,
    shipment_id      UUID,

    http_status      INTEGER,
    response_payload JSONB                    NOT NULL DEFAULT '{}'::jsonb,

    locked_until     TIMESTAMP WITH TIME ZONE,

    created_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX idx_shipment_idempotency_key
    ON shipment_idempotency_records (idempotency_key);

CREATE INDEX idx_shipment_idempotency_shipment_id
    ON shipment_idempotency_records (shipment_id);

CREATE INDEX idx_shipment_idempotency_order_id
    ON shipment_idempotency_records (order_id);