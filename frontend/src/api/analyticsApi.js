// Weather intelligence — all via Spring Boot, never external APIs directly.
import { apiFetch } from './http.js';

export function fetchTrends(city) {
  return apiFetch(`/api/analytics/trends?city=${encodeURIComponent(city)}`);
}

export function fetchChartData(city, days = 14) {
  return apiFetch(`/api/analytics/chart?city=${encodeURIComponent(city)}&days=${days}`);
}

export function fetchRecommendations(city) {
  return apiFetch(`/api/analytics/recommendations?city=${encodeURIComponent(city)}`);
}

// Manual observation run — collects for one city, or all favored cities when omitted.
export function triggerCollect(city) {
  const qs = city ? `?city=${encodeURIComponent(city)}` : '';
  return apiFetch(`/api/analytics/collect${qs}`, { method: 'POST' });
}
