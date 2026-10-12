import { Lightbulb, AlertTriangle, OctagonAlert, Sparkles } from 'lucide-react';

const STYLES = {
  info: 'border-weather-sky/50 bg-weather-sky/10 text-weather-navy',
  warning: 'border-weather-rose/40 bg-weather-rose/10 text-weather-navy',
  alert: 'border-weather-rose bg-weather-rose/15 text-weather-navy',
};

const ICONS = {
  info: <Lightbulb className="h-5 w-5 shrink-0 text-weather-sky" />,
  warning: <AlertTriangle className="h-5 w-5 shrink-0 text-weather-rose" />,
  alert: <OctagonAlert className="h-5 w-5 shrink-0 text-weather-rose" />,
};

// Actionable text advice from the recommendation service.
export default function RecommendationsPanel({ data }) {
  if (!data?.advice?.length) return null;

  return (
    <section className="w-full max-w-3xl rounded-2xl border border-weather-sky/30 bg-white p-5 shadow-card">
      <h3 className="flex items-center gap-2 text-lg font-semibold text-weather-navy">
        <Sparkles className="h-5 w-5 text-weather-rose" />
        Smart advice <span className="font-normal text-weather-navy/60">· {data.city}</span>
      </h3>
      <ul className="mt-3 flex flex-col gap-2">
        {data.advice.map((a, i) => (
          <li
            key={i}
            className={`flex items-start gap-2 rounded-xl border px-4 py-2.5 text-sm ${STYLES[a.severity] || STYLES.info}`}
          >
            {ICONS[a.severity] || ICONS.info}
            <span>{a.message}</span>
          </li>
        ))}
      </ul>
    </section>
  );
}
