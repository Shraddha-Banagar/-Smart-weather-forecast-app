// All weather data flows through our Spring Boot backend —
// this module NEVER calls Open-Meteo or any external API directly.
import { apiFetch } from './http.js';

function userHeaders(userId) {
  return userId ? { 'X-User-Id': String(userId) } : {};
}

export function fetchCurrentWeather(city, userId) {
  return apiFetch(`/api/weather/current?city=${encodeURIComponent(city)}`, {
    headers: { ...userHeaders(userId) },
  });
}

export function fetchForecast(city, userId) {
  return apiFetch(`/api/weather/forecast?city=${encodeURIComponent(city)}`, {
    headers: { ...userHeaders(userId) },
  });
}

export function checkHealth() {
  return apiFetch('/api/weather/health').then(() => true);
}
