import { writable } from 'svelte/store';

let nextId = 1;

export const toasts = writable([]);

export function pushToast(message, type = 'info', timeout = 3500) {
  const id = nextId++;
  const toast = { id, message, type };

  toasts.update((items) => [...items, toast]);

  if (timeout > 0) {
    setTimeout(() => {
      dismissToast(id);
    }, timeout);
  }

  return id;
}

export function dismissToast(id) {
  toasts.update((items) => items.filter((toast) => toast.id !== id));
}
