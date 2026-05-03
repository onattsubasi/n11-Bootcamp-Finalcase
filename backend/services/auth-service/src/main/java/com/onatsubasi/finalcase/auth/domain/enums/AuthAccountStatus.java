package com.onatsubasi.finalcase.auth.domain.enums;

/**
 * Auth-account lifecycle state. Auth Service owns this status; User Service may
 * keep a separate profile status, but credentials and login eligibility are
 * decided here.
 */
public enum AuthAccountStatus {
    ACTIVE,
    DISABLED,
    LOCKED,
    DELETED;

    public boolean canAuthenticate() {
        return this == ACTIVE;
    }
}
