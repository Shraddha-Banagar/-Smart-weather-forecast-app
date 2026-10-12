import { useCallback, useEffect, useRef, useState } from 'react';
import {
  LogOut,
  Pin,
  Download,
  FlaskConical,
  Wifi,
  WifiOff,
  Loader2,
  CloudSun,
} from 'lucide-react';
import SearchBar from '../components/SearchBar.jsx';
import CurrentWeather from '../components/CurrentWeather.jsx';
import ForecastList from '../components/ForecastList.jsx';
import ErrorBanner from '../components/ErrorBanner.jsx';
import FavoritesDashboard from '../components/FavoritesDashboard.jsx';
import SearchHistory from '../components/SearchHistory.jsx';
import TrendsPanel from '../components/TrendsPanel.jsx';
import RecommendationsPanel from '../components/RecommendationsPanel.jsx';
import WeatherCharts from '../components/WeatherCharts.jsx';
import { fetchCurrentWeather, fetchForecast } from '../api/weatherApi.js';
import { getFavorites, addFavorite, removeFavorite, getHistory, clearHistory } from '../api/userApi.js';
import { fetchTrends, fetchChartData, fetchRecommendations, triggerCollect } from '../api/analyticsApi.js';

// Second page of the app: everything weather-related, visible only after login.
export default function Dashboard({ user, onLogout, backendUp }) {
  const [current, setCurrent] = useState(null);
  const [forecast, setForecast] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [lastCity, setLastCity] = useState('');
  const [favorites, setFavorites] = useState([]);
  const [history, setHistory] = useState([]);
  const [weatherByCity, setWeatherByCity] = useState({});
  const [favBusy, setFavBusy] = useState(false);
  const [unpinning, setUnpinning] = useState(false);
  const [clearing, setClearing] = useState(false);
  const [trends, setTrends] = useState(null);
  const [recommendations, setRecommendations] = useState(null);
  const [chartData, setChartData] = useState(null);
  const [intelLoading, setIntelLoading] = useState(false);
  const [collecting, setCollecting] = useState(false);
  const dashFetched = useRef(new Set());

  // Load profile data once per login.
  useEffect(() => {
    let cancelled = false;
    dashFetched.current = new Set();
    Promise.all([getFavorites(user.id), getHistory(user.id)])
      .then(([favs, hist]) => {
        if (cancelled) return;
        setFavorites(favs ?? []);
        setHistory(hist ?? []);
      })
      .catch((e) => {
        if (!cancelled) setError(`Couldn't load profile data: ${e.message}`);
      });
    return () => {
      cancelled = true;
    };
  }, [user.id]);

  // Populate dashboard cards: fetch current weather for pinned cities we haven't loaded yet.
  useEffect(() => {
    if (!favorites.length) return;
    const missing = favorites
      .map((f) => f.city)
      .filter((c) => c && !dashFetched.current.has(c.toLowerCase()));
    if (!missing.length) return;
    missing.forEach((c) => dashFetched.current.add(c.toLowerCase()));
    Promise.allSettled(missing.map((c) => fetchCurrentWeather(c, user?.id))).then((results) => {
      setWeatherByCity((prev) => {
        const next = { ...prev };
        results.forEach((r, i) => {
          if (r.status === 'fulfilled' && r.value) next[missing[i].toLowerCase()] = r.value;
        });
        return next;
      });
    });
  }, [favorites, user?.id]);

  const search = useCallback(
    async (city) => {
      setLoading(true);
      setError('');
      try {
        // Backend is the only caller of the external API (proxy pattern).
        // X-User-Id header lets the backend record search history + serve cache.
        const [cur, fc] = await Promise.all([
          fetchCurrentWeather(city, user?.id),
          fetchForecast(city, user?.id),
        ]);
        setCurrent(cur);
        setForecast(fc);
        setLastCity(city);
        setWeatherByCity((prev) => ({ ...prev, [cur.city.toLowerCase()]: cur }));
        // History was recorded server-side; refresh the list (fire-and-forget).
        getHistory(user.id).then((h) => setHistory(h ?? [])).catch(() => {});
      } catch (e) {
        setError(e.message || 'Failed to fetch weather');
      } finally {
        setLoading(false);
      }
    },
    [user?.id, user.id],
  );

  // Load intelligence (trends + advice + chart data) for the viewed city.
  const intelCity = current?.city;
  useEffect(() => {
    if (!intelCity) return;
    let cancelled = false;
    setIntelLoading(true);
    Promise.allSettled([
      fetchTrends(intelCity),
      fetchRecommendations(intelCity),
      fetchChartData(intelCity),
    ]).then(([t, r, c]) => {
      if (cancelled) return;
      if (t.status === 'fulfilled') setTrends(t.value);
      if (r.status === 'fulfilled') setRecommendations(r.value);
      if (c.status === 'fulfilled') setChartData(c.value);
      setIntelLoading(false);
    });
    return () => {
      cancelled = true;
    };
  }, [intelCity]);

  const isPinned =
    current && favorites.some((f) => f.city?.toLowerCase() === current.city?.toLowerCase());

  const handlePin = async () => {
    if (!user || !current || isPinned) return;
    setFavBusy(true);
    setError('');
    try {
      await addFavorite(user.id, current.city);
      setFavorites(await getFavorites(user.id));
    } catch (e) {
      setError(e.message || 'Could not pin city');
    } finally {
      setFavBusy(false);
    }
  };

  const handleUnpin = async (city) => {
    if (!user) return;
    setUnpinning(true);
    try {
      await removeFavorite(user.id, city);
      setFavorites(await getFavorites(user.id));
    } catch (e) {
      setError(e.message || 'Could not unpin city');
    } finally {
      setUnpinning(false);
    }
  };

  const handleClearHistory = async () => {
    if (!user) return;
    setClearing(true);
    try {
      await clearHistory(user.id);
      setHistory([]);
    } catch (e) {
      setError(e.message || 'Could not clear history');
    } finally {
      setClearing(false);
    }
  };

  const handleCollect = async () => {
    setCollecting(true);
    try {
      const result = await triggerCollect(intelCity || undefined);
      if (intelCity) {
        const [t, c] = await Promise.all([fetchTrends(intelCity), fetchChartData(intelCity)]);
        setTrends(t);
        setChartData(c);
      }
      setError(
        result.failures?.length
          ? `Collected ${result.succeeded}/${result.attempted}: ${result.failures.join('; ')}`
          : '',
      );
    } catch (e) {
      setError(e.message || 'Collection failed');
    } finally {
      setCollecting(false);
    }
  };

  return (
    <div className="flex w-full flex-col items-center gap-6">
      <header className="flex w-full max-w-3xl flex-wrap items-center justify-between gap-3 rounded-2xl border border-weather-sky/30 bg-white px-5 py-4 shadow-card">
        <div>
          <h1 className="flex items-center gap-2 text-2xl font-extrabold tracking-tight text-weather-navy">
            <CloudSun className="h-7 w-7 text-weather-sky" />
            Smart Weather
          </h1>
          <p className="mt-0.5 flex flex-wrap items-center gap-2 text-xs text-weather-navy/70">
            Welcome, <span className="font-semibold text-weather-navy">{user.displayName || user.username}</span>
            {backendUp === false && (
              <span className="inline-flex items-center gap-1 rounded-full border border-weather-rose/50 bg-weather-rose/10 px-2 py-0.5 font-medium text-weather-navy">
                <WifiOff className="h-3 w-3 text-weather-rose" />
                Backend unreachable — is Spring Boot on :8080?
              </span>
            )}
            {backendUp === true && (
              <span className="inline-flex items-center gap-1 rounded-full border border-weather-sky/50 bg-weather-sky/10 px-2 py-0.5 font-medium text-weather-navy">
                <Wifi className="h-3 w-3 text-weather-sky" />
                Connected
              </span>
            )}
          </p>
        </div>
        <button
          onClick={onLogout}
          className="flex items-center gap-1.5 rounded-lg border border-weather-navy/25 px-3 py-1.5 text-sm font-medium text-weather-navy transition hover:border-weather-rose hover:text-weather-rose"
        >
          <LogOut className="h-4 w-4" />
          Log out
        </button>
      </header>

      <SearchBar loading={loading} onSearch={search} />

      <div className="flex flex-wrap justify-center gap-2 text-sm">
        {['London', 'New York', 'Tokyo', 'Berlin'].map((c) => (
          <button
            key={c}
            onClick={() => search(c)}
            disabled={loading}
            className="rounded-full border border-weather-sky/40 bg-white px-4 py-1.5 font-medium text-weather-navy shadow-card transition hover:border-weather-rose hover:text-weather-rose disabled:opacity-50"
          >
            {c}
          </button>
        ))}
      </div>

      <ErrorBanner message={error} onDismiss={() => setError('')} />

      {loading && (
        <div className="flex items-center gap-3 text-weather-navy/70" role="status">
          <Loader2 className="h-5 w-5 animate-spin text-weather-sky" />
          Fetching weather for “{lastCity || '…'}”…
        </div>
      )}

      {!loading && !current && !error && (
        <p className="mt-2 text-center text-weather-navy/60">
          Search a city above to see its current weather and 5-day forecast.
        </p>
      )}

      {current && (
        <div className="flex w-full max-w-3xl justify-end">
          {isPinned ? (
            <p className="flex items-center gap-1.5 rounded-full border border-weather-sky/50 bg-weather-sky/10 px-4 py-1.5 text-sm font-medium text-weather-navy">
              <Pin className="h-4 w-4 text-weather-sky" />
              Pinned to your dashboard
            </p>
          ) : (
            <button
              onClick={handlePin}
              disabled={favBusy}
              className="flex items-center gap-1.5 rounded-full bg-weather-navy px-4 py-1.5 text-sm font-semibold text-white shadow-card transition hover:bg-weather-rose hover:shadow-pop disabled:opacity-50"
            >
              {favBusy ? <Loader2 className="h-4 w-4 animate-spin" /> : <Pin className="h-4 w-4" />}
              {favBusy ? 'Pinning…' : `Pin ${current.city} to dashboard`}
            </button>
          )}
        </div>
      )}

      <CurrentWeather data={current} />
      <ForecastList data={forecast} />

      {current && (
        <div className="flex w-full max-w-3xl flex-col gap-4">
          <div className="flex flex-wrap items-center justify-between gap-2">
            <h2 className="flex items-center gap-2 text-xl font-bold text-weather-navy">
              <FlaskConical className="h-6 w-6 text-weather-rose" />
              Weather Intelligence
            </h2>
            <button
              onClick={handleCollect}
              disabled={collecting}
              className="flex items-center gap-1.5 rounded-full border border-weather-navy/30 bg-white px-4 py-1.5 text-sm font-medium text-weather-navy shadow-card transition hover:border-weather-rose hover:text-weather-rose disabled:opacity-50"
              title="Snapshot current observations into history (automatic job runs hourly)"
            >
              {collecting ? <Loader2 className="h-4 w-4 animate-spin" /> : <Download className="h-4 w-4" />}
              {collecting ? 'Collecting…' : 'Collect now'}
            </button>
          </div>
          {intelLoading && !trends && (
            <p className="text-sm text-weather-navy/60">Analyzing trends and advice…</p>
          )}
          <TrendsPanel trends={trends} />
          <RecommendationsPanel data={recommendations} />
          <WeatherCharts data={chartData} />
        </div>
      )}

      <FavoritesDashboard
        favorites={favorites}
        weatherByCity={weatherByCity}
        onSelect={search}
        onUnpin={handleUnpin}
        unpinning={unpinning}
      />
      <SearchHistory
        entries={history}
        onSelect={search}
        onClear={handleClearHistory}
        clearing={clearing}
      />
    </div>
  );
}
