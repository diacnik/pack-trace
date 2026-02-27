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

export async function fetchCloset(id) {
  return apiRequest(`/closet/${id}`);
}

export async function createCloset(payload) {
  return apiRequest('/closet', {
    method: 'POST',
    body: JSON.stringify(payload)
  });
}

export async function updateCloset(id, payload) {
  return apiRequest(`/closet/${id}`, {
    method: 'PUT',
    body: JSON.stringify(payload)
  });
}

export async function deleteCloset(id) {
  return apiRequest(`/closet/${id}`, {
    method: 'DELETE'
  });
}

export async function fetchClosetGear(closetId) {
  return apiRequest(`/closet/${closetId}/gear`);
}

export async function updateClosetGearQuantity(closetId, gearId, payload) {
  return apiRequest(`/closet/${closetId}/gear/${gearId}`, {
    method: 'PUT',
    body: JSON.stringify(payload)
  });
}

export async function removeGearFromCloset(closetId, gearId) {
  return apiRequest(`/closet/${closetId}/gear/${gearId}`, {
    method: 'DELETE'
  });
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

export async function updateGear(id, payload) {
  return apiRequest(`/gear/${id}`, {
    method: 'PUT',
    body: JSON.stringify(payload)
  });
}

export async function deleteGear(id) {
  return apiRequest(`/gear/${id}`, {
    method: 'DELETE'
  });
}

export async function addGearToCloset(closetId, payload) {
  return apiRequest(`/closet/${closetId}/gear`, {
    method: 'POST',
    body: JSON.stringify(payload)
  });
}

export async function createGearAndAddToCloset(closetId, payload) {
  return apiRequest(`/closet/${closetId}/gear/create`, {
    method: 'POST',
    body: JSON.stringify(payload)
  });
}

export async function fetchPacks() {
  return apiRequest('/pack');
}

export async function fetchPack(id) {
  return apiRequest(`/pack/${id}`);
}

export async function createPack(payload) {
  return apiRequest('/pack', {
    method: 'POST',
    body: JSON.stringify(payload)
  });
}

export async function updatePack(id, payload) {
  return apiRequest(`/pack/${id}`, {
    method: 'PUT',
    body: JSON.stringify(payload)
  });
}

export async function deletePack(id) {
  return apiRequest(`/pack/${id}`, {
    method: 'DELETE'
  });
}

export async function fetchPackGear(packId) {
  return apiRequest(`/pack/${packId}/gear`);
}

export async function addGearToPack(packId, payload) {
  return apiRequest(`/pack/${packId}/gear`, {
    method: 'POST',
    body: JSON.stringify(payload)
  });
}

export async function createGearAndAddToPack(packId, payload) {
  return apiRequest(`/pack/${packId}/gear/create`, {
    method: 'POST',
    body: JSON.stringify(payload)
  });
}

export async function updatePackGearQuantity(packId, gearId, payload) {
  return apiRequest(`/pack/${packId}/gear/${gearId}`, {
    method: 'PUT',
    body: JSON.stringify(payload)
  });
}

export async function removeGearFromPack(packId, gearId) {
  return apiRequest(`/pack/${packId}/gear/${gearId}`, {
    method: 'DELETE'
  });
}
