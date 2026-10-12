# Smart Weather Forecast App — Phase 3 (Weather Intelligence System)

Architecture: `React (Vite) -> Spring Boot (proxy) -> Open-Meteo API`. The frontend NEVER calls the
external API directly. PostgreSQL stores users, favorites, search history, and observed weather.

> No API key needed — Open-Meteo is free and keyless.

## Prerequisites

- Java 21+ (`java -version`)
- Maven 3.9+ (`mvn -version`)
- Node 20+ (`node --version`)
- Docker Desktop (`docker --version`)

## 1. Start PostgreSQL

```powershell
docker compose up -d
# DB: localhost:5432 / user: smartweather / pass: smartweather / db: smartweather
# Adminer UI: http://localhost:8081 (system: PostgreSQL, server: postgres)
```

## 2. Run Backend (port 8080)

```powershell
cd backend
mvn spring-boot:run
# health check: http://localhost:8080/api/weather/health
# try: http://localhost:8080/api/weather/current?city=London
# try: http://localhost:8080/api/weather/forecast?city=London
```

## 3. Run Frontend (port 5173)

```powershell
cd frontend
npm install
npm run dev
# open http://localhost:5173
# backend URL override: copy .env.example to .env and set VITE_API_BASE_URL
```

## API Reference

| Method | Endpoint | Description |
| ------ | -------- | ----------- |
| GET | `/api/weather/health` | Health check |
| GET | `/api/weather/current?city={city}` | Current weather (10-min cached; `X-User-Id` header records history) |
| GET | `/api/weather/forecast?city={city}` | 5-day forecast (10-min cached; `X-User-Id` header records history) |
| POST | `/api/auth/login` | Mock login `{"username": "ada"}` → find-or-create user |
| GET | `/api/users/{id}/favorites` | List pinned cities |
| POST | `/api/users/{id}/favorites` | Pin a city `{"city": "London"}` (validates via weather API, 409 if duplicate) |
| DELETE | `/api/users/{id}/favorites/{city}` | Unpin a city |
| GET | `/api/users/{id}/history` | Latest-50 search history (newest first) |
| DELETE | `/api/users/{id}/history` | Clear search history |
| GET | `/api/analytics/trends?city={city}` | Today's forecast vs. historical averages + verdict |
| GET | `/api/analytics/chart?city={city}&days=14` | Observed history + 5-day forecast for charts |
| GET | `/api/analytics/recommendations?city={city}` | Actionable advice (`info`/`warning`/`alert`) |
| POST | `/api/analytics/collect?city={city?}` | Manual observation run (one city, or all favored cities) |

Error shape (all failures): `{ "timestamp": "...", "status": 404, "error": "...", "message": "...", "path": "..." }`

## Project Layout

```
Smart Weather Forecast App/
├── docker-compose.yml
├── backend/   # Spring Boot 3.x, Java 21 (Records, RestClient, @Cacheable, JPA)
└── frontend/  # React + Vite + Tailwind (mock login, dashboard, history)
```

## Database

Tables auto-created by JPA (`ddl-auto: update`):
- Phase 2 reference DDL: `backend/src/main/resources/schema-phase2.sql` (`users`, `saved_cities`, `search_history`)
- Phase 3 additive DDL: `backend/src/main/resources/schema-phase3.sql` (`historical_weather` — one row per city/day)

## Caching

Caffeine TTL 10 min on `currentWeather` + `forecast` caches keyed by
lowercased city (`CacheConfig`). Repeat searches within the window never hit Open-Meteo.

## Intelligence (Phase 3)

- **Collector**: `HistoryCollectorService` snapshots every favored city hourly
  (`app.history.collect-fixed-rate-ms`, first run 60s after boot; upsert per city/day).
  Manual trigger: `POST /api/analytics/collect`.
- **Trends**: `AnalyticsService` compares today's forecast average against the historical mean
  (`±°C` delta + plain-English verdict).
- **Advice**: `RecommendationService` — deterministic rules (storms, rain %, overnight drops,
  heat/frost, wind, mugginess, day-night swings, history deviation), e.g.
  *"Sharp temperature drop expected tonight, bring a jacket"*.
- **Frontend**: Recharts temperature-swings + rainfall chart and observed-history chart
  (`WeatherCharts.jsx`), `TrendsPanel.jsx`, `RecommendationsPanel.jsx`, plus a
  “Collect now” button that refreshes history on demand.
