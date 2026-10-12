import { AlertTriangle, X } from 'lucide-react';

export default function ErrorBanner({ message, onDismiss }) {
  if (!message) return null;
  return (
    <div
      className="flex w-full max-w-3xl items-start justify-between gap-4 rounded-xl border border-weather-rose/60 bg-weather-rose/10 px-4 py-3 text-weather-navy shadow-card"
      role="alert"
    >
      <div className="flex items-start gap-2">
        <AlertTriangle className="mt-0.5 h-5 w-5 shrink-0 text-weather-rose" />
        <p className="text-sm">
          <span className="font-semibold">Something went wrong: </span>
          {message}
        </p>
      </div>
      {onDismiss && (
        <button
          onClick={onDismiss}
          className="text-weather-navy/50 transition hover:text-weather-rose"
          aria-label="Dismiss error"
        >
          <X className="h-5 w-5" />
        </button>
      )}
    </div>
  );
}
