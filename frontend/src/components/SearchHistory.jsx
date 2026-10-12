import { History } from 'lucide-react';

function timeAgo(iso) {
  try {
    const mins = Math.max(0, Math.round((Date.now() - new Date(iso).getTime()) / 60000));
    if (mins < 1) return 'just now';
    if (mins < 60) return `${mins}m ago`;
    const hrs = Math.round(mins / 60);
    if (hrs < 24) return `${hrs}h ago`;
    return new Date(iso).toLocaleDateString();
  } catch {
    return '';
  }
}

export default function SearchHistory({ entries, onSelect, onClear, clearing }) {
  if (!entries || entries.length === 0) return null;

  // De-duplicate consecutive repeats for a cleaner list, keep latest 10 chips.
  const seen = new Set();
  const unique = entries.filter((e) => {
    const k = (e.city || '').toLowerCase();
    if (seen.has(k)) return false;
    seen.add(k);
    return true;
  }).slice(0, 10);

  return (
    <section className="w-full max-w-3xl rounded-2xl border border-weather-sky/30 bg-white p-4 shadow-card">
      <div className="flex items-center justify-between">
        <h3 className="flex items-center gap-1.5 text-sm font-semibold uppercase tracking-wider text-weather-navy/70">
          <History className="h-4 w-4 text-weather-sky" />
          Recent searches
        </h3>
        <button
          onClick={onClear}
          disabled={clearing}
          className="text-xs text-weather-navy/50 transition hover:text-weather-rose disabled:opacity-50"
        >
          {clearing ? 'Clearing…' : 'Clear history'}
        </button>
      </div>
      <div className="mt-2 flex flex-wrap gap-2">
        {unique.map((e) => (
          <button
            key={e.id}
            onClick={() => onSelect(e.city)}
            className="rounded-full border border-weather-sky/40 bg-weather-cream px-3 py-1 text-xs font-medium text-weather-navy transition hover:border-weather-rose hover:text-weather-rose"
            title={timeAgo(e.createdAt)}
          >
            {e.city} <span className="text-weather-navy/50">· {timeAgo(e.createdAt)}</span>
          </button>
        ))}
      </div>
    </section>
  );
}
