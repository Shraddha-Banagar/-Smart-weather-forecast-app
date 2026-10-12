import { MapPin, Droplets, Wind, Gauge, CloudSun, Thermometer } from 'lucide-react';

// Warm temps pop in rose, cool temps in sky, everything else in navy.
export function tempColor(tempC) {
  if (tempC >= 28) return 'text-weather-rose';
  if (tempC <= 10) return 'text-weather-sky';
  return 'text-weather-navy';
}

function Stat({ icon: Icon, iconClass, label, value, sub }) {
  return (
    <div className="rounded-xl bg-weather-cream/60 p-4 text-center">
      <p className="flex items-center justify-center gap-1 text-xs font-semibold uppercase tracking-wider text-weather-navy/60">
        <Icon className={`h-4 w-4 ${iconClass}`} />
        {label}
      </p>
      <p className="mt-1 text-2xl font-bold text-weather-navy">{value}</p>
      {sub && <p className="mt-0.5 text-xs text-weather-navy/60">{sub}</p>}
    </div>
  );
}

export default function CurrentWeather({ data }) {
  if (!data) return null;
  const temp = Math.round(data.temperatureC);
  return (
    <section className="w-full max-w-3xl rounded-2xl border border-weather-sky/30 bg-white p-6 shadow-card">
      <div className="flex flex-wrap items-end justify-between gap-3">
        <div>
          <h2 className="flex items-center gap-2 text-3xl font-bold text-weather-navy">
            <MapPin className="h-7 w-7 text-weather-rose" />
            {data.city}
            {data.country ? (
              <span className="text-lg font-normal text-weather-navy/60">{data.country}</span>
            ) : null}
          </h2>
          <p className="mt-1 flex items-center gap-1.5 font-medium text-weather-navy/70">
            <CloudSun className="h-5 w-5 text-weather-sky" />
            {data.description}
          </p>
        </div>
        <div className="text-right">
          <p className={`flex items-center justify-end gap-1 text-5xl font-extrabold ${tempColor(temp)}`}>
            <Thermometer className="h-9 w-9" />
            {temp}°C
          </p>
          <p className="text-sm text-weather-navy/60">Feels like {Math.round(data.feelsLikeC)}°C</p>
        </div>
      </div>

      <div className="mt-5 grid grid-cols-2 gap-3 sm:grid-cols-4">
        <Stat icon={Droplets} iconClass="text-weather-sky" label="Humidity" value={`${data.humidityPct}%`} />
        <Stat
          icon={Wind}
          iconClass="text-weather-sky"
          label="Wind"
          value={`${Math.round(data.windSpeedKph)} km/h`}
          sub={`${data.windDirectionDeg}°`}
        />
        <Stat
          icon={Gauge}
          iconClass="text-weather-navy"
          label="Pressure"
          value={`${Math.round(data.pressureHpa)}`}
          sub="hPa"
        />
        <Stat
          icon={CloudSun}
          iconClass="text-weather-rose"
          label="Condition"
          value={data.weatherCode}
          sub={data.description}
        />
      </div>
    </section>
  );
}
