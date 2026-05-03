CREATE TABLE IF NOT EXISTS auth_users (
    id UUID PRIMARY KEY,
    email VARCHAR(320) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_auth_users_email UNIQUE (email),
    CONSTRAINT chk_auth_users_status CHECK (status IN ('ACTIVE', 'DISABLED', 'LOCKED', 'DELETED'))
);

CREATE TABLE IF NOT EXISTS auth_user_roles (
    user_id UUID NOT NULL,
    role VARCHAR(50) NOT NULL,

    CONSTRAINT pk_auth_user_roles PRIMARY KEY (user_id, role),
    CONSTRAINT fk_auth_user_roles_user
        FOREIGN KEY (user_id)
        REFERENCES auth_users(id)
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS refresh_tokens (
    id UUID PRIMARY KEY,
    token_hash VARCHAR(128) NOT NULL,
    user_id UUID NOT NULL,
    family_id UUID NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    revoked_at TIMESTAMP WITH TIME ZONE,
    replaced_by_token_id UUID,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_refresh_tokens_token_hash UNIQUE (token_hash),
    CONSTRAINT fk_refresh_tokens_user
        FOREIGN KEY (user_id)
        REFERENCES auth_users(id)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_auth_users_email
    ON auth_users(email);

CREATE INDEX IF NOT EXISTS idx_auth_users_status
    ON auth_users(status);

CREATE INDEX IF NOT EXISTS idx_auth_user_roles_user_id
    ON auth_user_roles(user_id);

CREATE INDEX IF NOT EXISTS idx_refresh_tokens_user_id
    ON refresh_tokens(user_id);

CREATE INDEX IF NOT EXISTS idx_refresh_tokens_family_id
    ON refresh_tokens(family_id);

CREATE INDEX IF NOT EXISTS idx_refresh_tokens_token_hash
    ON refresh_tokens(token_hash);
