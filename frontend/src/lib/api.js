import { get } from 'svelte/store';
import { authToken } from './auth0';

const API_BASE = '/api';

async function apiRequest(path, options = {}) {
  const token = get(authToken);
  if (!token) {
    throw new Error('Missing auth token');
  }

  const headers = new Headers(options.headers || {});
  headers.set('Authorization', `Bearer ${token}`);

  if (options.body && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json');
  }

  const response = await fetch(`${API_BASE}${path}`, {
    ...options,
    headers
  });

  if (!response.ok) {
    let errorMessage = `Request failed (${response.status})`;
    try {
      const payload = await response.json();
      if (payload?.message) {
        errorMessage = payload.message;
      }
    } catch {
      // ignore JSON parse errors
    }
    const error = new Error(errorMessage);
    error.status = response.status;
    throw error;
  }

  if (response.status === 204) {
    return null;
  }

  const contentType = response.headers.get('content-type') || '';
  if (contentType.includes('application/json')) {
    return response.json();
  }

  return response.text();
}

export async function fetchClosets() {
  return apiRequest('/closet');
}

export async function fetchGear() {
  return apiRequest('/gear');
}

export async function createGear(payload) {
  return apiRequest('/gear', {
    method: 'POST',
    body: JSON.stringify(payload)
  });
}

export async function addGearToCloset(closetId, payload) {
  return apiRequest(`/closet/${closetId}/gear`, {
    method: 'POST',
    body: JSON.stringify(payload)
  });
}
