// Shared fetch wrapper for all backend calls.
// The backend already returns friendly `{ message }` bodies for known errors —
// this layer only translates transport failures and bare status codes so the UI
// never shows raw "Request failed with status 500" text.

export const API_BASE = import.meta.env.VITE_API_BASE_URL || '';

export function connectionError() {
  const err = new Error('Cannot reach the backend — is Spring Boot running on :8080?');
  err.status = undefined;
  return err;
}

function fallbackMessage(status) {
  if (status === 400) return 'Invalid request — please check your input and try again.';
  if (status === 404) return 'Not found — check the spelling and try again.';
  if (status === 409) return 'That already exists — nothing to add.';
  if (status === 502 || status === 503 || status === 504)
    return 'The weather provider is temporarily unavailable — please try again in a moment.';
  if (status >= 500) return 'The weather service hit a snag — please try again in a moment.';
  return 'Something went wrong — please try again.';
}

export async function apiFetch(path, options) {
  let res;
  try {
    res = await fetch(`${API_BASE}${path}`, options);
  } catch {
    throw connectionError();
  }
  if (res.ok) {
    if (res.status === 204) return null;
    const text = await res.text();
    return text ? JSON.parse(text) : null;
  }
  const contentType = res.headers.get('content-type') || '';
  if (!contentType.includes('application/json')) {
    // Non-JSON error (e.g. the Vite proxy's HTML 500 page when Spring Boot
    // is down) means the request never reached our backend — say so plainly.
    throw connectionError();
  }
  let message = null;
  try {
    const body = JSON.parse(await res.text());
    if (body?.message) message = body.message;
  } catch {
    /* unparseable JSON → status-based fallback below */
  }
  const err = new Error(message || fallbackMessage(res.status));
  err.status = res.status;
  throw err;
}
