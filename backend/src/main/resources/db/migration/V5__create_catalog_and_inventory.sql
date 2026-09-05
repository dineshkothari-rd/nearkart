CREATE TABLE categories (
    id uuid PRIMARY KEY,
    name varchar(100) NOT NULL,
    slug varchar(120) NOT NULL UNIQUE,
    created_at timestamptz NOT NULL,
    updated_at timestamptz NOT NULL
);

CREATE TABLE products (
    id uuid PRIMARY KEY,
    category_id uuid NOT NULL REFERENCES categories(id),
    name varchar(180) NOT NULL,
    normalized_name varchar(180) NOT NULL,
    brand varchar(120),
    status varchar(20) NOT NULL CHECK (status IN ('ACTIVE', 'INACTIVE')),
    created_at timestamptz NOT NULL,
    updated_at timestamptz NOT NULL,
    UNIQUE NULLS NOT DISTINCT (category_id, normalized_name, brand)
);

CREATE TABLE product_variants (
    id uuid PRIMARY KEY,
    product_id uuid NOT NULL REFERENCES products(id),
    label varchar(120) NOT NULL,
    barcode varchar(64) UNIQUE,
    created_at timestamptz NOT NULL,
    updated_at timestamptz NOT NULL,
    UNIQUE (product_id, label)
);

CREATE TABLE store_products (
    id uuid PRIMARY KEY,
    store_id uuid NOT NULL REFERENCES stores(id),
    product_variant_id uuid NOT NULL REFERENCES product_variants(id),
    status varchar(20) NOT NULL CHECK (status IN ('ACTIVE', 'INACTIVE')),
    created_at timestamptz NOT NULL,
    updated_at timestamptz NOT NULL,
    UNIQUE (store_id, product_variant_id)
);

CREATE TABLE inventory (
    id uuid PRIMARY KEY,
    store_product_id uuid NOT NULL UNIQUE REFERENCES store_products(id) ON DELETE CASCADE,
    quantity integer CHECK (quantity >= 0),
    availability varchar(20) NOT NULL CHECK (availability IN ('AVAILABLE', 'LOW_STOCK', 'OUT_OF_STOCK', 'UNKNOWN')),
    source varchar(32) NOT NULL,
    observed_at timestamptz NOT NULL,
    updated_by uuid REFERENCES users(id),
    version integer NOT NULL DEFAULT 0,
    created_at timestamptz NOT NULL,
    updated_at timestamptz NOT NULL
);

CREATE TABLE prices (
    id uuid PRIMARY KEY,
    store_product_id uuid NOT NULL UNIQUE REFERENCES store_products(id) ON DELETE CASCADE,
    amount numeric(12,2) NOT NULL CHECK (amount > 0),
    currency varchar(3) NOT NULL DEFAULT 'INR',
    version integer NOT NULL DEFAULT 0,
    created_at timestamptz NOT NULL,
    updated_at timestamptz NOT NULL
);

CREATE TABLE price_history (
    id uuid PRIMARY KEY,
    store_product_id uuid NOT NULL REFERENCES store_products(id) ON DELETE CASCADE,
    old_amount numeric(12,2),
    new_amount numeric(12,2) NOT NULL,
    currency varchar(3) NOT NULL,
    changed_by uuid REFERENCES users(id),
    created_at timestamptz NOT NULL,
    updated_at timestamptz NOT NULL
);

CREATE INDEX idx_products_category ON products (category_id, status);
CREATE INDEX idx_products_normalized_name ON products (normalized_name);
CREATE INDEX idx_variants_product ON product_variants (product_id);
CREATE INDEX idx_store_products_variant ON store_products (product_variant_id, status);
CREATE INDEX idx_store_products_store ON store_products (store_id, status);
CREATE INDEX idx_inventory_availability ON inventory (availability, observed_at DESC);
CREATE INDEX idx_price_history_listing ON price_history (store_product_id, created_at DESC);
