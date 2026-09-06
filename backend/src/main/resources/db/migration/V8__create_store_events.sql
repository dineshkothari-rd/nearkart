CREATE TABLE store_events (
    id uuid PRIMARY KEY,
    store_id uuid NOT NULL REFERENCES stores(id) ON DELETE CASCADE,
    event_type varchar(32) NOT NULL CHECK (event_type IN ('SEARCH_IMPRESSION', 'STORE_VIEW', 'DIRECTIONS_CLICK')),
    created_at timestamptz NOT NULL
);

CREATE INDEX idx_store_events_analytics ON store_events (store_id, event_type, created_at DESC);
