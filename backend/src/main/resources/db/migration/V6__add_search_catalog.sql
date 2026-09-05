CREATE TABLE product_aliases (
    id uuid PRIMARY KEY,
    product_id uuid NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    alias varchar(180) NOT NULL,
    normalized_alias varchar(180) NOT NULL,
    created_at timestamptz NOT NULL,
    updated_at timestamptz NOT NULL,
    UNIQUE (product_id, normalized_alias)
);

CREATE INDEX idx_products_name_trgm ON products USING gin (normalized_name gin_trgm_ops);
CREATE INDEX idx_product_aliases_trgm ON product_aliases USING gin (normalized_alias gin_trgm_ops);

INSERT INTO categories (id, name, slug, created_at, updated_at) VALUES
    ('10000000-0000-0000-0000-000000000001', 'Dairy', 'dairy', now(), now()),
    ('10000000-0000-0000-0000-000000000002', 'Staples', 'staples', now(), now());

INSERT INTO products (id, category_id, name, normalized_name, brand, status, created_at, updated_at) VALUES
    ('20000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001', 'Amul Butter', 'amul butter', 'Amul', 'ACTIVE', now(), now()),
    ('20000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000001', 'Amul Taaza Milk', 'amul taaza milk', 'Amul', 'ACTIVE', now(), now()),
    ('20000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000002', 'India Gate Basmati Rice', 'india gate basmati rice', 'India Gate', 'ACTIVE', now(), now()),
    ('20000000-0000-0000-0000-000000000004', '10000000-0000-0000-0000-000000000002', 'Aashirvaad Atta', 'aashirvaad atta', 'Aashirvaad', 'ACTIVE', now(), now());

INSERT INTO product_variants (id, product_id, label, barcode, created_at, updated_at) VALUES
    ('30000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000001', '500 g', '8901262010016', now(), now()),
    ('30000000-0000-0000-0000-000000000002', '20000000-0000-0000-0000-000000000002', '1 L', '8901262250122', now(), now()),
    ('30000000-0000-0000-0000-000000000003', '20000000-0000-0000-0000-000000000003', '5 kg', '8906010000019', now(), now()),
    ('30000000-0000-0000-0000-000000000004', '20000000-0000-0000-0000-000000000004', '5 kg', '8901725121624', now(), now());

INSERT INTO product_aliases (id, product_id, alias, normalized_alias, created_at, updated_at) VALUES
    ('40000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000001', 'Butter 500g', 'butter 500g', now(), now()),
    ('40000000-0000-0000-0000-000000000002', '20000000-0000-0000-0000-000000000002', 'Amul Milk', 'amul milk', now(), now()),
    ('40000000-0000-0000-0000-000000000003', '20000000-0000-0000-0000-000000000004', 'Wheat Flour', 'wheat flour', now(), now());
