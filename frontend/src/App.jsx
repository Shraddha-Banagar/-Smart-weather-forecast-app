import { useEffect, useState } from 'react';
import LandingPage from './pages/LandingPage.jsx';
import Dashboard from './pages/Dashboard.jsx';
import { CloudSun } from 'lucide-react';
import { login } from './api/userApi.js';
import { checkHealth } from './api/weatherApi.js';

const STORAGE_KEY = 'sw_user';

function loadStoredUser() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    return raw ? JSON.parse(raw) : null;
  } catch {
    return null;
  }
}

// Page 1 (logged out): landing + login. Page 2 (logged in): full dashboard.
export default function App() {
  const [user, setUser] = useState(loadStoredUser);
  const [backendUp, setBackendUp] = useState(null);
  const [authBusy, setAuthBusy] = useState(false);

  useEffect(() => {
    checkHealth()
      .then(() => setBackendUp(true))
      .catch(() => setBackendUp(false));
  }, []);

  const handleLogin = async (username) => {
    setAuthBusy(true);
    try {
      const u = await login(username);
      setUser(u);
      localStorage.setItem(STORAGE_KEY, JSON.stringify(u));
      setBackendUp(true);
      return u;
    } catch (e) {
      if (e.status === undefined) setBackendUp(false);
      throw e;
    } finally {
      setAuthBusy(false);
    }
  };

  const handleLogout = () => {
    setUser(null);
    localStorage.removeItem(STORAGE_KEY);
  };

  return (
    <div className="app-backdrop min-h-screen">
      {/* Navbar */}
      <nav className="bg-weather-navy text-white shadow-card">
        <div className="mx-auto flex w-full max-w-5xl items-center justify-between gap-3 px-4 py-3">
          <p className="flex items-center gap-2 text-lg font-bold tracking-tight">
            <CloudSun className="h-6 w-6 text-weather-sky" />
            Smart Weather Forecast
          </p>
          <p className="hidden text-xs text-white/70 sm:block">
            React → Spring Boot proxy → Open-Meteo
          </p>
        </div>
      </nav>

      <main className="mx-auto flex w-full max-w-5xl flex-col items-center gap-6 px-4 py-10">
        {!user ? (
          <LandingPage onLogin={handleLogin} busy={authBusy} backendUp={backendUp} />
        ) : (
          <Dashboard user={user} onLogout={handleLogout} backendUp={backendUp} />
        )}
      </main>

      {/* Footer */}
      <footer className="bg-weather-navy py-4 text-center text-xs text-white/80">
        Data: Open-Meteo (via backend proxy, 10-min cache) · Trends, charts &amp; smart advice;
        observations collected hourly
      </footer>
    </div>
  );
}
