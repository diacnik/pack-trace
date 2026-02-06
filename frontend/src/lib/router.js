import { writable } from 'svelte/store';

const initialPath = typeof window !== 'undefined' ? window.location.pathname : '/';

export const currentPath = writable(initialPath);

export function navigate(path) {
  if (typeof window === 'undefined') {
    return;
  }

  if (path === window.location.pathname) {
    return;
  }

  window.history.pushState({}, '', path);
  currentPath.set(window.location.pathname);
}

if (typeof window !== 'undefined') {
  window.addEventListener('popstate', () => {
    currentPath.set(window.location.pathname);
  });
}
