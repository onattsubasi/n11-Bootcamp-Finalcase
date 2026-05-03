CREATE TABLE notification_templates
(
    id                 UUID PRIMARY KEY,

    type               VARCHAR(80)              NOT NULL,
    channel            VARCHAR(40)              NOT NULL,
    locale             VARCHAR(10)              NOT NULL,

    title_template     VARCHAR(500)             NOT NULL,
    message_template   TEXT                     NOT NULL,
    required_variables JSONB                    NOT NULL DEFAULT '[]'::jsonb,

    active             BOOLEAN                  NOT NULL DEFAULT TRUE,

    created_at         TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_notification_template_type_channel_locale
        UNIQUE (type, channel, locale)
);

CREATE INDEX idx_notification_templates_type
    ON notification_templates (type);

CREATE INDEX idx_notification_templates_channel
    ON notification_templates (channel);

CREATE INDEX idx_notification_templates_locale
    ON notification_templates (locale);

CREATE INDEX idx_notification_templates_active
    ON notification_templates (active);


CREATE TABLE notifications
(
    id                UUID PRIMARY KEY,

    recipient_user_id UUID                     NOT NULL,
    recipient_email   VARCHAR(320),
    recipient_phone   VARCHAR(50),

    type              VARCHAR(80)              NOT NULL,
    status            VARCHAR(40)              NOT NULL,

    reference_type    VARCHAR(40)              NOT NULL,
    reference_id      VARCHAR(100),

    locale            VARCHAR(10)              NOT NULL,
    title             VARCHAR(500)             NOT NULL,
    message           TEXT                     NOT NULL,

    payload_snapshot  JSONB                    NOT NULL DEFAULT '{}'::jsonb,

    read_at           TIMESTAMP WITH TIME ZONE,

    created_at        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_notifications_recipient_user_id
    ON notifications (recipient_user_id);

CREATE INDEX idx_notifications_type
    ON notifications (type);

CREATE INDEX idx_notifications_status
    ON notifications (status);

CREATE INDEX idx_notifications_reference
    ON notifications (reference_type, reference_id);

CREATE INDEX idx_notifications_read_at
    ON notifications (read_at);

CREATE INDEX idx_notifications_created_at
    ON notifications (created_at);


CREATE TABLE notification_deliveries
(
    id                  UUID PRIMARY KEY,

    notification_id     UUID                     NOT NULL,

    channel             VARCHAR(40)              NOT NULL,
    provider            VARCHAR(60)              NOT NULL,
    recipient_address   VARCHAR(320),

    status              VARCHAR(40)              NOT NULL,

    attempt_count       INTEGER                  NOT NULL DEFAULT 0,
    max_attempts        INTEGER                  NOT NULL DEFAULT 3,

    provider_message_id VARCHAR(200),
    last_error          VARCHAR(1000),

    next_retry_at       TIMESTAMP WITH TIME ZONE,
    sent_at             TIMESTAMP WITH TIME ZONE,

    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_notification_deliveries_notification
        FOREIGN KEY (notification_id)
            REFERENCES notifications (id)
            ON DELETE CASCADE
);

CREATE INDEX idx_notification_deliveries_notification_id
    ON notification_deliveries (notification_id);

CREATE INDEX idx_notification_deliveries_status
    ON notification_deliveries (status);

CREATE INDEX idx_notification_deliveries_channel
    ON notification_deliveries (channel);

CREATE INDEX idx_notification_deliveries_next_retry_at
    ON notification_deliveries (next_retry_at);


CREATE TABLE notification_delivery_attempts
(
    id                  UUID PRIMARY KEY,

    delivery_id         UUID                     NOT NULL,

    attempt_number      INTEGER                  NOT NULL,
    status              VARCHAR(40)              NOT NULL,

    provider_message_id VARCHAR(200),
    error_message       VARCHAR(1000),
    retryable           BOOLEAN                  NOT NULL DEFAULT FALSE,

    request_snapshot    JSONB                    NOT NULL DEFAULT '{}'::jsonb,
    response_snapshot   JSONB                    NOT NULL DEFAULT '{}'::jsonb,

    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_notification_delivery_attempts_delivery
        FOREIGN KEY (delivery_id)
            REFERENCES notification_deliveries (id)
            ON DELETE CASCADE
);

CREATE INDEX idx_notification_delivery_attempts_delivery_id
    ON notification_delivery_attempts (delivery_id);

CREATE INDEX idx_notification_delivery_attempts_status
    ON notification_delivery_attempts (status);

CREATE INDEX idx_notification_delivery_attempts_created_at
    ON notification_delivery_attempts (created_at);


CREATE TABLE notification_processed_events
(
    id            UUID PRIMARY KEY,

    event_id      VARCHAR(120)             NOT NULL UNIQUE,
    event_type    VARCHAR(120)             NOT NULL,
    status        VARCHAR(40)              NOT NULL,
    error_message VARCHAR(1000),

    processed_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX idx_notification_processed_events_event_id
    ON notification_processed_events (event_id);

CREATE INDEX idx_notification_processed_events_event_type
    ON notification_processed_events (event_type);

CREATE INDEX idx_notification_processed_events_status
    ON notification_processed_events (status);


CREATE TABLE user_product_interests
(
    id               UUID PRIMARY KEY,

    user_id          UUID                     NOT NULL,
    product_id       VARCHAR(100)             NOT NULL,
    interest_type    VARCHAR(80)              NOT NULL,

    active           BOOLEAN                  NOT NULL DEFAULT TRUE,
    last_notified_at TIMESTAMP WITH TIME ZONE,

    created_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_user_product_interest
        UNIQUE (user_id, product_id, interest_type)
);

CREATE INDEX idx_user_product_interests_user_id
    ON user_product_interests (user_id);

CREATE INDEX idx_user_product_interests_product_id
    ON user_product_interests (product_id);

CREATE INDEX idx_user_product_interests_active
    ON user_product_interests (active);


CREATE TABLE notification_preferences
(
    id            UUID PRIMARY KEY,

    user_id       UUID                     NOT NULL,
    type          VARCHAR(80)              NOT NULL,
    channel       VARCHAR(40)              NOT NULL,

    enabled       BOOLEAN                  NOT NULL DEFAULT TRUE,
    transactional BOOLEAN                  NOT NULL DEFAULT FALSE,

    created_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_notification_preference_user_type_channel
        UNIQUE (user_id, type, channel)
);

CREATE INDEX idx_notification_preferences_user_id
    ON notification_preferences (user_id);

CREATE INDEX idx_notification_preferences_type
    ON notification_preferences (type);

CREATE INDEX idx_notification_preferences_enabled
    ON notification_preferences (enabled);