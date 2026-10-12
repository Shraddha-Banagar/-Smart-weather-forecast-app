-- Phase 2 reference schema (JPA `ddl-auto: update` creates these automatically).
-- PostgreSQL / DB: smartweather

CREATE TABLE IF NOT EXISTS users (
    id           BIGSERIAL PRIMARY KEY,
    username     VARCHAR(50)  NOT NULL UNIQUE,
    display_name VARCHAR(100),
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS saved_cities (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT       NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    city            VARCHAR(100) NOT NULL,
    city_normalized VARCHAR(100) NOT NULL,
    country         VARCHAR(100),
    latitude        DOUBLE PRECISION,
    longitude       DOUBLE PRECISION,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_saved_cities_user_city UNIQUE (user_id, city_normalized)
);
CREATE INDEX IF NOT EXISTS idx_saved_cities_user ON saved_cities (user_id);

CREATE TABLE IF NOT EXISTS search_history (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT       NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    city       VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_search_history_user_created ON search_history (user_id, created_at DESC);
