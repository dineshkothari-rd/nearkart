CREATE TABLE stores (
    id uuid PRIMARY KEY,
    owner_id uuid NOT NULL REFERENCES users(id),
    name varchar(150) NOT NULL,
    slug varchar(180) NOT NULL UNIQUE,
    description varchar(1000),
    phone varchar(20) NOT NULL,
    timezone varchar(64) NOT NULL,
    status varchar(20) NOT NULL CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'SUSPENDED')),
    approval_reason varchar(500),
    approved_by uuid REFERENCES users(id),
    approved_at timestamptz,
    version integer NOT NULL DEFAULT 0,
    created_at timestamptz NOT NULL,
    updated_at timestamptz NOT NULL
);

CREATE TABLE store_locations (
    id uuid PRIMARY KEY,
    store_id uuid NOT NULL UNIQUE REFERENCES stores(id) ON DELETE CASCADE,
    address_line varchar(250) NOT NULL,
    locality varchar(100) NOT NULL,
    city varchar(100) NOT NULL,
    state varchar(100) NOT NULL,
    postal_code varchar(10) NOT NULL,
    latitude numeric(9,6) NOT NULL CHECK (latitude BETWEEN -90 AND 90),
    longitude numeric(9,6) NOT NULL CHECK (longitude BETWEEN -180 AND 180),
    created_at timestamptz NOT NULL,
    updated_at timestamptz NOT NULL
);

CREATE TABLE store_hours (
    id uuid PRIMARY KEY,
    store_id uuid NOT NULL REFERENCES stores(id) ON DELETE CASCADE,
    weekday smallint NOT NULL CHECK (weekday BETWEEN 1 AND 7),
    opens_at time,
    closes_at time,
    closed boolean NOT NULL,
    created_at timestamptz NOT NULL,
    updated_at timestamptz NOT NULL,
    UNIQUE (store_id, weekday),
    CHECK ((closed AND opens_at IS NULL AND closes_at IS NULL)
        OR (NOT closed AND opens_at IS NOT NULL AND closes_at IS NOT NULL))
);

CREATE TABLE audit_logs (
    id uuid PRIMARY KEY,
    actor_id uuid REFERENCES users(id),
    action varchar(64) NOT NULL,
    target_type varchar(64) NOT NULL,
    target_id uuid NOT NULL,
    metadata jsonb NOT NULL DEFAULT '{}',
    created_at timestamptz NOT NULL,
    updated_at timestamptz NOT NULL
);

CREATE INDEX idx_stores_owner ON stores (owner_id, created_at DESC);
CREATE INDEX idx_stores_status ON stores (status, created_at DESC);
CREATE INDEX idx_store_locations_coordinates ON store_locations (latitude, longitude);
CREATE INDEX idx_audit_target ON audit_logs (target_type, target_id, created_at DESC);
CREATE INDEX idx_audit_actor ON audit_logs (actor_id, created_at DESC);
