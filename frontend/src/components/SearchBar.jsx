import { useState } from 'react';
import { Search, Loader2 } from 'lucide-react';

export default function SearchBar({ initialValue = '', loading = false, onSearch }) {
  const [value, setValue] = useState(initialValue);

  const submit = (e) => {
    e.preventDefault();
    const city = value.trim();
    if (!city || loading) return;
    onSearch(city);
  };

  return (
    <form onSubmit={submit} className="flex w-full max-w-xl gap-2">
      <div className="relative flex-1">
        <Search className="pointer-events-none absolute left-4 top-1/2 h-5 w-5 -translate-y-1/2 text-weather-navy/40" />
        <input
          type="text"
          value={value}
          onChange={(e) => setValue(e.target.value)}
          placeholder="Search a city… e.g. London, Tokyo, New York"
          className="w-full rounded-xl border border-weather-sky/40 bg-white py-3 pl-11 pr-4 text-weather-navy shadow-card placeholder:text-weather-navy/40 focus:border-weather-sky focus:outline-none focus:ring-2 focus:ring-weather-sky/30"
          aria-label="City name"
        />
      </div>
      <button
        type="submit"
        disabled={loading || !value.trim()}
        className="flex items-center gap-2 rounded-xl bg-weather-navy px-5 py-3 font-semibold text-white shadow-card transition hover:bg-weather-rose hover:shadow-pop disabled:cursor-not-allowed disabled:opacity-50"
      >
        {loading ? <Loader2 className="h-5 w-5 animate-spin" /> : <Search className="h-5 w-5" />}
        {loading ? 'Loading…' : 'Search'}
      </button>
    </form>
  );
}
