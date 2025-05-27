CREATE TABLE lookup_values (
    id           UUID PRIMARY KEY,
    category     VARCHAR,
    code         VARCHAR,
    display_name VARCHAR,
    sort_order   INTEGER,
    active       BOOLEAN,
    created_at   TIMESTAMP WITH TIME ZONE DEFAULT now(),
    updated_at   TIMESTAMP WITH TIME ZONE DEFAULT now()
);
