import { TrendingUp } from 'lucide-react';

// Today's forecast vs. historical averages.
export default function TrendsPanel({ trends }) {
  if (!trends) return null;

  const hasHistory = trends.sampleSize > 0;
  const delta = trends.deltaTodayAvgVsHistory;
  const deltaColor =
    delta == null
      ? 'text-weather-navy'
      : Math.abs(delta) < 2
        ? 'text-weather-sky'
        : 'text-weather-rose';

  const cells = hasHistory
    ? [
        { label: 'Hist. avg temp', value: `${trends.avgTempC}°C`, sub: null, color: 'text-weather-navy' },
        {
          label: 'Today vs. hist.',
          value: `${delta > 0 ? '+' : ''}${delta}°C`,
          sub: null,
          color: deltaColor,
        },
        {
          label: 'Hist. humidity',
          value: trends.avgHumidityPct != null ? `${trends.avgHumidityPct}%` : '—',
          sub: null,
          color: 'text-weather-sky',
        },
        {
          label: 'Samples',
          value: `${trends.sampleSize}`,
          sub: `${trends.periodStart} → ${trends.periodEnd}`,
          color: 'text-weather-navy',
        },
      ]
    : [];

  return (
    <section className="w-full max-w-3xl rounded-2xl border border-weather-sky/30 bg-white p-5 shadow-card">
      <h3 className="flex items-center gap-2 text-lg font-semibold text-weather-navy">
        <TrendingUp className="h-5 w-5 text-weather-sky" />
        Trends vs. history
      </h3>
      <p className="mt-1 text-sm text-weather-navy/70">{trends.verdict}</p>

      {hasHistory ? (
        <div className="mt-4 grid grid-cols-2 gap-3 sm:grid-cols-4">
          {cells.map((c) => (
            <div key={c.label} className="rounded-xl bg-weather-cream/70 p-3 text-center">
              <p className="text-xs font-semibold uppercase tracking-wider text-weather-navy/60">
                {c.label}
              </p>
              <p className={`mt-1 text-2xl font-bold ${c.color}`}>{c.value}</p>
              {c.sub && <p className="text-xs text-weather-navy/60">{c.sub}</p>}
            </div>
          ))}
        </div>
      ) : (
        <p className="mt-3 rounded-xl border border-weather-sky/40 bg-weather-sky/10 px-3 py-2 text-sm text-weather-navy">
          No observations yet — pin this city and press “Collect now” below, then re-search to
          build history.
        </p>
      )}
    </section>
  );
}
