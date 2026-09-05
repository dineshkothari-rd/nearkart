CREATE TABLE roles (
    id uuid PRIMARY KEY,
    name varchar(32) NOT NULL UNIQUE,
    created_at timestamptz NOT NULL,
    updated_at timestamptz NOT NULL
);

CREATE TABLE users (
    id uuid PRIMARY KEY,
    email varchar(320) NOT NULL UNIQUE,
    password_hash varchar(60) NOT NULL,
    display_name varchar(100) NOT NULL,
    status varchar(20) NOT NULL CHECK (status IN ('ACTIVE', 'SUSPENDED')),
    created_at timestamptz NOT NULL,
    updated_at timestamptz NOT NULL
);

CREATE TABLE user_roles (
    user_id uuid NOT NULL REFERENCES users(id),
    role_id uuid NOT NULL REFERENCES roles(id),
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE refresh_tokens (
    id uuid PRIMARY KEY,
    user_id uuid NOT NULL REFERENCES users(id),
    family_id uuid NOT NULL,
    token_hash char(64) NOT NULL UNIQUE,
    replaced_by_hash char(64),
    expires_at timestamptz NOT NULL,
    revoked_at timestamptz,
    created_at timestamptz NOT NULL,
    updated_at timestamptz NOT NULL
);

CREATE INDEX idx_refresh_tokens_user ON refresh_tokens (user_id);
CREATE INDEX idx_refresh_tokens_family ON refresh_tokens (family_id);
CREATE INDEX idx_refresh_tokens_expiry ON refresh_tokens (expires_at) WHERE revoked_at IS NULL;

INSERT INTO roles (id, name, created_at, updated_at) VALUES
    ('00000000-0000-0000-0000-000000000001', 'CUSTOMER', now(), now()),
    ('00000000-0000-0000-0000-000000000002', 'STORE_OWNER', now(), now()),
    ('00000000-0000-0000-0000-000000000003', 'ADMIN', now(), now());
