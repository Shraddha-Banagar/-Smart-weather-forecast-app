import { useState } from 'react';
import { Pin, MapPin, Droplets, Wind } from 'lucide-react';
import { tempColor } from './CurrentWeather.jsx';

// Pinned favorite cities: each card shows live current weather (served from
// the backend's 10-min cache when fresh) with unpin + click-to-view actions.
export default function FavoritesDashboard({ favorites, weatherByCity, onSelect, onUnpin, unpinning }) {
  const [confirmCity, setConfirmCity] = useState(null);

  if (!favorites || favorites.length === 0) {
    return (
      <section className="w-full max-w-3xl rounded-2xl border border-dashed border-weather-sky/50 bg-white/60 p-5 text-center text-sm text-weather-navy/60">
        <Pin className="mx-auto h-5 w-5 text-weather-sky" />
        <p className="mt-1">
          No pinned cities yet. Search a city, then hit{' '}
          <span className="font-semibold text-weather-navy">“Pin to dashboard”</span> to keep it here.
        </p>
      </section>
    );
  }

  return (
    <section className="w-full max-w-3xl">
      <h3 className="mb-3 flex items-center gap-2 text-lg font-semibold text-weather-navy">
        <Pin className="h-5 w-5 text-weather-rose" />
        Your dashboard <span className="font-normal text-weather-navy/60">· {favorites.length} pinned</span>
      </h3>
      <div className="grid gap-3 sm:grid-cols-2">
        {favorites.map((fav) => {
          const w = weatherByCity?.[fav.city?.toLowerCase()];
          return (
            <article
              key={fav.id}
              className="rounded-2xl border border-weather-sky/30 bg-white p-4 shadow-card"
            >
              <div className="flex items-start justify-between gap-2">
                <button onClick={() => onSelect(fav.city)} className="text-left" title={`View ${fav.city}`}>
                  <p className="flex items-center gap-1 text-lg font-bold text-weather-navy hover:text-weather-rose">
                    <MapPin className="h-4 w-4 text-weather-rose" />
                    {fav.city}
                  </p>
                  {fav.country && <p className="text-xs text-weather-navy/60">{fav.country}</p>}
                </button>
                {confirmCity === fav.city ? (
                  <div className="flex gap-1">
                    <button
                      onClick={() => {
                        onUnpin(fav.city);
                        setConfirmCity(null);
                      }}
                      disabled={unpinning}
                      className="rounded-lg bg-weather-rose px-2 py-1 text-xs font-semibold text-white hover:opacity-90 disabled:opacity-50"
                    >
                      Confirm
                    </button>
                    <button
                      onClick={() => setConfirmCity(null)}
                      className="rounded-lg border border-weather-navy/20 px-2 py-1 text-xs text-weather-navy hover:border-weather-navy/50"
                    >
                      Keep
                    </button>
                  </div>
                ) : (
                  <button
                    onClick={() => setConfirmCity(fav.city)}
                    className="rounded-lg border border-weather-navy/20 px-2 py-1 text-xs text-weather-navy/60 transition hover:border-weather-rose hover:text-weather-rose"
                    title={`Unpin ${fav.city}`}
                  >
                    Unpin
                  </button>
                )}
              </div>
              {w ? (
                <div className="mt-2 flex items-center justify-between">
                  <p className={`text-3xl font-extrabold ${tempColor(w.temperatureC)}`}>
                    {Math.round(w.temperatureC)}°C
                  </p>
                  <div className="text-right text-xs text-weather-navy/60">
                    <p className="font-medium text-weather-navy">{w.description}</p>
                    <p className="mt-0.5 flex items-center justify-end gap-2">
                      <span className="inline-flex items-center gap-0.5">
                        <Droplets className="h-3.5 w-3.5 text-weather-sky" />{w.humidityPct}%
                      </span>
                      <span className="inline-flex items-center gap-0.5">
                        <Wind className="h-3.5 w-3.5 text-weather-sky" />{Math.round(w.windSpeedKph)} km/h
                      </span>
                    </p>
                  </div>
                </div>
              ) : (
                <p className="mt-2 text-xs text-weather-navy/50">Weather loads when you view this city…</p>
              )}
            </article>
          );
        })}
      </div>
    </section>
  );
}
