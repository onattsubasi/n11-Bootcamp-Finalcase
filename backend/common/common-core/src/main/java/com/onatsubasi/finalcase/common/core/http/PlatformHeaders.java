package com.onatsubasi.finalcase.common.core.http;

/**
 * Header constants used by API Gateway to forward user context.
 * SRS §4.2 - Gateway sets these after JWT validation.
 */
public final class PlatformHeaders {

    private PlatformHeaders() {}

    public static final String X_USER_ID = "X-User-Id";
    public static final String X_USER_EMAIL = "X-User-Email";
    public static final String X_USER_ROLES = "X-User-Roles";
    public static final String X_CORRELATION_ID = "X-Correlation-Id";
    public static final String AUTHORIZATION = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";
    public static final String X_INTERNAL_REQUEST = "X-Internal-Request";
    public static final String X_SERVICE_NAME = "X-Service-Name";
}
