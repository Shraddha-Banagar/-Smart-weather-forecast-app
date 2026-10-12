-- Phase 3 additive schema (JPA `ddl-auto: update` creates this automatically).
-- One observed snapshot per (city, day) for favored cities.

CREATE TABLE IF NOT EXISTS historical_weather (
    id              BIGSERIAL PRIMARY KEY,
    city            VARCHAR(100) NOT NULL,
    city_normalized VARCHAR(100) NOT NULL,
    country         VARCHAR(100),
    latitude        DOUBLE PRECISION,
    longitude       DOUBLE PRECISION,
    observed_date   DATE         NOT NULL,
    observed_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    temperature_c   DOUBLE PRECISION,
    feels_like_c    DOUBLE PRECISION,
    humidity_pct    INTEGER,
    wind_speed_kph  DOUBLE PRECISION,
    pressure_hpa    DOUBLE PRECISION,
    weather_code    INTEGER,
    description     VARCHAR(50),
    CONSTRAINT uq_history_city_date UNIQUE (city_normalized, observed_date)
);
CREATE INDEX IF NOT EXISTS idx_history_city_date ON historical_weather (city_normalized, observed_date);
