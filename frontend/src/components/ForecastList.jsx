import { CalendarDays, Droplets } from 'lucide-react';
import { tempColor } from './CurrentWeather.jsx';

function formatDate(iso) {
  try {
    return new Date(`${iso}T12:00:00`).toLocaleDateString(undefined, {
      weekday: 'short',
      month: 'short',
      day: 'numeric',
    });
  } catch {
    return iso;
  }
}

export default function ForecastList({ data }) {
  const days = data?.daily ?? [];
  if (days.length === 0) return null;

  return (
    <section className="w-full max-w-3xl">
      <h3 className="mb-3 flex items-center gap-2 text-lg font-semibold text-weather-navy">
        <CalendarDays className="h-5 w-5 text-weather-sky" />
        5-day forecast <span className="font-normal text-weather-navy/60">· {data.city}</span>
      </h3>
      <div className="grid gap-3 sm:grid-cols-5">
        {days.map((d) => (
          <article
            key={d.date}
            className="rounded-2xl border border-weather-sky/30 bg-white p-4 text-center shadow-card transition hover:-translate-y-0.5 hover:shadow-pop"
          >
            <p className="text-xs font-semibold uppercase tracking-wide text-weather-navy/60">
              {formatDate(d.date)}
            </p>
            <p className="mt-2 text-xl font-bold">
              <span className={tempColor(d.tempMaxC)}>{Math.round(d.tempMaxC)}°</span>{' '}
              <span className={`font-normal ${tempColor(d.tempMinC)}`}>/ {Math.round(d.tempMinC)}°</span>
            </p>
            <p className="mt-1 text-xs text-weather-navy/70">{d.description}</p>
            <p className="mt-2 inline-flex items-center gap-1 rounded-full bg-weather-sky/15 px-2 py-0.5 text-xs font-medium text-weather-navy">
              <Droplets className="h-3.5 w-3.5 text-weather-sky" />
              {d.precipitationProbabilityPct}%
            </p>
          </article>
        ))}
      </div>
    </section>
  );
}
