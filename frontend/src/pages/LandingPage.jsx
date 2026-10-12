import { useState } from 'react';
import { CloudSun, Pin, Sparkles, LogIn, Loader2, Wifi, WifiOff, User } from 'lucide-react';
import ErrorBanner from '../components/ErrorBanner.jsx';

// First page of the app: product landing + mock login. Nothing here fetches
// weather, so a stopped backend can never produce an error banner on load.
export default function LandingPage({ onLogin, busy, backendUp }) {
  const [name, setName] = useState('');
  const [error, setError] = useState('');

  const submit = async (e) => {
    e.preventDefault();
    const username = name.trim();
    if (!username || busy) return;
    setError('');
    try {
      await onLogin(username);
    } catch (err) {
      setError(err.message || 'Login failed — please try again.');
    }
  };

  const features = [
    { icon: CloudSun, tint: 'text-weather-sky', title: 'Live + 5-day', text: 'Current conditions and a 5-day outlook for any city.' },
    { icon: Pin, tint: 'text-weather-rose', title: 'Your dashboard', text: 'Pin favorite cities and see them at a glance.' },
    { icon: Sparkles, tint: 'text-weather-navy', title: 'Smart advice', text: 'Trends vs. history, charts, and actionable tips.' },
  ];

  return (
    <div className="flex w-full flex-col items-center gap-8">
      <header className="text-center">
        <p className="inline-flex items-center gap-2 rounded-full bg-weather-navy px-4 py-1.5 text-sm font-semibold text-white shadow-card">
          <CloudSun className="h-4 w-4 text-weather-sky" />
          Smart Weather Forecast
        </p>
        <h1 className="mt-4 text-5xl font-extrabold tracking-tight text-weather-navy">
          Know the sky <span className="text-weather-rose">before</span> you step out
        </h1>
        <p className="mx-auto mt-3 max-w-xl text-weather-navy/70">
          Live conditions, 5-day forecasts, and smart advice for any city —
          with your pinned dashboard and personal search history.
        </p>
        {backendUp === false && (
          <p className="mt-3 inline-flex items-center gap-1.5 rounded-full border border-weather-rose/50 bg-weather-rose/10 px-3 py-1 text-xs font-medium text-weather-navy">
            <WifiOff className="h-3.5 w-3.5 text-weather-rose" />
            Backend unreachable at /api — start Spring Boot on :8080, then log in.
          </p>
        )}
        {backendUp === true && (
          <p className="mt-3 inline-flex items-center gap-1.5 rounded-full border border-weather-sky/50 bg-weather-sky/10 px-3 py-1 text-xs font-medium text-weather-navy">
            <Wifi className="h-3.5 w-3.5 text-weather-sky" />
            Backend connected
          </p>
        )}
      </header>

      <div className="grid w-full max-w-3xl gap-3 sm:grid-cols-3">
        {features.map(({ icon: Icon, tint, title, text }) => (
          <div
            key={title}
            className="rounded-2xl border border-weather-sky/30 bg-white p-4 text-center shadow-card"
          >
            <Icon className={`mx-auto h-7 w-7 ${tint}`} />
            <p className="mt-2 font-semibold text-weather-navy">{title}</p>
            <p className="mt-1 text-xs text-weather-navy/60">{text}</p>
          </div>
        ))}
      </div>

      <form
        onSubmit={submit}
        className="w-full max-w-md rounded-2xl border border-weather-sky/30 bg-white p-6 shadow-card"
      >
        <h2 className="flex items-center gap-2 text-lg font-semibold text-weather-navy">
          <LogIn className="h-5 w-5 text-weather-rose" />
          Log in to continue →
        </h2>
        <p className="mt-1 text-xs text-weather-navy/60">
          Mock login — just pick a username, no password needed.
        </p>
        <div className="mt-4 flex gap-2">
          <div className="relative flex-1">
            <User className="pointer-events-none absolute left-3.5 top-1/2 h-4 w-4 -translate-y-1/2 text-weather-navy/40" />
            <input
              type="text"
              value={name}
              onChange={(e) => setName(e.target.value)}
              placeholder="Pick a username (e.g. ada)…"
              className="w-full rounded-xl border border-weather-sky/40 bg-weather-cream/50 py-2.5 pl-10 pr-4 text-weather-navy placeholder:text-weather-navy/40 focus:border-weather-sky focus:outline-none focus:ring-2 focus:ring-weather-sky/30"
              aria-label="Username"
              autoFocus
            />
          </div>
          <button
            type="submit"
            disabled={busy || !name.trim()}
            className="flex items-center gap-1.5 rounded-xl bg-weather-navy px-5 py-2.5 font-semibold text-white shadow-card transition hover:bg-weather-rose hover:shadow-pop disabled:cursor-not-allowed disabled:opacity-50"
          >
            {busy ? <Loader2 className="h-4 w-4 animate-spin" /> : <LogIn className="h-4 w-4" />}
            {busy ? 'Logging in…' : 'Log in'}
          </button>
        </div>
        {error && (
          <div className="mt-3">
            <ErrorBanner message={error} onDismiss={() => setError('')} />
          </div>
        )}
      </form>
    </div>
  );
}
