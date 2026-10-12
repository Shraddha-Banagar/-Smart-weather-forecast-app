import {
  ResponsiveContainer,
  ComposedChart,
  Line,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
} from 'recharts';
import { Thermometer, BarChart3 } from 'lucide-react';

const NAVY = '#425B9A';
const SKY = '#76C0EC';
const ROSE = '#FF95A5';
const GRID = '#E4D9BE';
const CREAM_DARK = '#F5EAD0';

function shortDate(iso) {
  try {
    const d = iso.includes('T') ? new Date(iso) : new Date(`${iso}T12:00:00`);
    return d.toLocaleDateString(undefined, { month: 'short', day: 'numeric' });
  } catch {
    return iso;
  }
}

function ChartTip({ active, payload, label }) {
  if (!active || !payload?.length) return null;
  return (
    <div className="rounded-lg border border-weather-sky/40 bg-white px-3 py-2 text-xs text-weather-navy shadow-card">
      <p className="font-semibold">{label}</p>
      {payload.map((p) => (
        <p key={p.dataKey} style={{ color: p.color }}>
          {p.name}: {p.value}
          {p.dataKey === 'precipitation' ? '%' : '°C'}
        </p>
      ))}
    </div>
  );
}

// Observed temperature history + 5-day forecast (swings + rainfall).
export default function WeatherCharts({ data }) {
  if (!data) return null;

  const historyRows = (data.history ?? []).map((h) => ({
    label: shortDate(h.date),
    observedTemp: h.tempC,
  }));

  const forecastRows = (data.forecast ?? []).map((f) => ({
    label: shortDate(f.date),
    tempMax: f.tempMaxC,
    tempMin: f.tempMinC,
    precipitation: f.precipitationProbabilityPct,
  }));

  return (
    <section className="flex w-full max-w-3xl flex-col gap-4">
      <div className="rounded-2xl border border-weather-sky/30 bg-white p-5 shadow-card">
        <h3 className="flex items-center gap-2 text-lg font-semibold text-weather-navy">
          <Thermometer className="h-5 w-5 text-weather-rose" />
          Temperature swings <span className="font-normal text-weather-navy/60">· 5-day forecast</span>
        </h3>
        {forecastRows.length === 0 ? (
          <p className="mt-2 text-sm text-weather-navy/60">No forecast data.</p>
        ) : (
          <div className="mt-2 h-64">
            <ResponsiveContainer width="100%" height="100%">
              <ComposedChart data={forecastRows} margin={{ top: 8, right: 8, left: -12, bottom: 0 }}>
                <CartesianGrid strokeDasharray="3 3" stroke={GRID} />
                <XAxis dataKey="label" stroke={NAVY} fontSize={12} opacity={0.7} />
                <YAxis stroke={NAVY} fontSize={12} unit="°" opacity={0.7} />
                <Tooltip content={<ChartTip />} />
                <Legend wrapperStyle={{ fontSize: 12, color: NAVY }} />
                <Line type="monotone" dataKey="tempMax" name="Max °C" stroke={ROSE} strokeWidth={2.5} dot={false} />
                <Line type="monotone" dataKey="tempMin" name="Min °C" stroke={SKY} strokeWidth={2.5} dot={false} />
                <Bar dataKey="precipitation" name="Rain %" fill={SKY} opacity={0.4} />
              </ComposedChart>
            </ResponsiveContainer>
          </div>
        )}
      </div>

      <div className="rounded-2xl border border-weather-sky/30 bg-white p-5 shadow-card">
        <h3 className="flex items-center gap-2 text-lg font-semibold text-weather-navy">
          <BarChart3 className="h-5 w-5 text-weather-sky" />
          Observed temperatures{' '}
          <span className="font-normal text-weather-navy/60">· {historyRows.length} snapshots</span>
        </h3>
        {historyRows.length === 0 ? (
          <p className="mt-2 text-sm text-weather-navy/60">
            No observations collected yet — pin cities and use “Collect now” to start building
            history.
          </p>
        ) : (
          <div className="mt-2 h-52">
            <ResponsiveContainer width="100%" height="100%">
              <ComposedChart data={historyRows} margin={{ top: 8, right: 8, left: -12, bottom: 0 }}>
                <CartesianGrid strokeDasharray="3 3" stroke={GRID} />
                <XAxis dataKey="label" stroke={NAVY} fontSize={12} opacity={0.7} />
                <YAxis stroke={NAVY} fontSize={12} unit="°" opacity={0.7} />
                <Tooltip content={<ChartTip />} />
                <Legend wrapperStyle={{ fontSize: 12, color: NAVY }} />
                <Line
                  type="monotone"
                  dataKey="observedTemp"
                  name="Observed °C"
                  stroke={NAVY}
                  strokeWidth={2.5}
                  dot={{ fill: ROSE, strokeWidth: 0, r: 3 }}
                  activeDot={{ fill: ROSE, r: 5 }}
                />
              </ComposedChart>
            </ResponsiveContainer>
          </div>
        )}
      </div>
    </section>
  );
}
