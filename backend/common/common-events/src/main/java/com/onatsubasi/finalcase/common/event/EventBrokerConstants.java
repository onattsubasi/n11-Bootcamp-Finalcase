package com.onatsubasi.finalcase.common.event;

public final class EventBrokerConstants {

    private EventBrokerConstants() {
    }

    public static final String MAIN_EXCHANGE = "marketplace.events";
    public static final String DEAD_LETTER_EXCHANGE = "marketplace.events.dlx";
    public static final String RETRY_EXCHANGE = "marketplace.events.retry";

    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    public static final String EVENT_ID_HEADER = "X-Event-Id";
    public static final String EVENT_TYPE_HEADER = "X-Event-Type";
}