CREATE TABLE favorites (
    id uuid PRIMARY KEY,
    user_id uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    target_type varchar(20) NOT NULL CHECK (target_type IN ('PRODUCT', 'STORE')),
    product_id uuid REFERENCES products(id) ON DELETE CASCADE,
    store_id uuid REFERENCES stores(id) ON DELETE CASCADE,
    created_at timestamptz NOT NULL,
    CHECK ((target_type = 'PRODUCT' AND product_id IS NOT NULL AND store_id IS NULL)
        OR (target_type = 'STORE' AND store_id IS NOT NULL AND product_id IS NULL)),
    UNIQUE NULLS NOT DISTINCT (user_id, product_id, store_id)
);

CREATE TABLE search_history (
    id uuid PRIMARY KEY,
    user_id uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    query varchar(120) NOT NULL,
    created_at timestamptz NOT NULL
);

CREATE TABLE reviews (
    id uuid PRIMARY KEY,
    user_id uuid NOT NULL REFERENCES users(id),
    store_id uuid NOT NULL REFERENCES stores(id),
    rating smallint NOT NULL CHECK (rating BETWEEN 1 AND 5),
    text varchar(1000) NOT NULL,
    status varchar(20) NOT NULL CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
    created_at timestamptz NOT NULL,
    updated_at timestamptz NOT NULL,
    UNIQUE (user_id, store_id)
);

CREATE TABLE reports (
    id uuid PRIMARY KEY,
    user_id uuid NOT NULL REFERENCES users(id),
    store_id uuid NOT NULL REFERENCES stores(id),
    store_product_id uuid REFERENCES store_products(id),
    type varchar(40) NOT NULL CHECK (type IN ('PRODUCT_UNAVAILABLE', 'WRONG_PRICE', 'STORE_CLOSED', 'WRONG_ADDRESS', 'INCORRECT_STORE_INFORMATION')),
    details varchar(1000),
    status varchar(20) NOT NULL CHECK (status IN ('OPEN', 'RESOLVED', 'DISMISSED')),
    created_at timestamptz NOT NULL,
    updated_at timestamptz NOT NULL
);

CREATE INDEX idx_favorites_user ON favorites (user_id, created_at DESC);
CREATE INDEX idx_search_history_user ON search_history (user_id, created_at DESC);
CREATE INDEX idx_reviews_store_status ON reviews (store_id, status, created_at DESC);
CREATE INDEX idx_reports_status ON reports (status, created_at DESC);
CREATE UNIQUE INDEX uq_open_report ON reports (user_id, store_id, type, COALESCE(store_product_id, '00000000-0000-0000-0000-000000000000')) WHERE status = 'OPEN';
