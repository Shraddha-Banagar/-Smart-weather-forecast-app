// Mock auth + favorites + search history — all via Spring Boot.
import { apiFetch } from './http.js';

export function login(username) {
  return apiFetch('/api/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username }),
  });
}

export function getFavorites(userId) {
  return apiFetch(`/api/users/${userId}/favorites`);
}

export function addFavorite(userId, city) {
  return apiFetch(`/api/users/${userId}/favorites`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ city }),
  });
}

export function removeFavorite(userId, city) {
  return apiFetch(`/api/users/${userId}/favorites/${encodeURIComponent(city)}`, {
    method: 'DELETE',
  });
}

export function getHistory(userId) {
  return apiFetch(`/api/users/${userId}/history`);
}

export function clearHistory(userId) {
  return apiFetch(`/api/users/${userId}/history`, { method: 'DELETE' });
}
