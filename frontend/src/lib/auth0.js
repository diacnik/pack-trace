import { createAuth0Client } from '@auth0/auth0-spa-js';
import { writable, derived } from 'svelte/store';

// Auth0 configuration - these should match your backend configuration
const config = {
  domain: import.meta.env.VITE_AUTH0_DOMAIN || '',
  clientId: import.meta.env.VITE_AUTH0_CLIENT_ID || '',
  authorizationParams: {
    redirect_uri: window.location.origin,
    audience: import.meta.env.VITE_AUTH0_AUDIENCE || '',
    scope: 'openid profile email' // Standard scopes for user info + API access
  },
  cacheLocation: 'localstorage', // Persist auth state across page refreshes
  useRefreshTokens: true // Enable refresh tokens for better UX
};

// Store for Auth0 client
let auth0ClientInstance = null;

// Writable stores for auth state
export const isLoading = writable(true);
export const isAuthenticated = writable(false);
export const user = writable(null);
export const authToken = writable(null);

// Auth client wrapper
export const authClient = {
  async initialize() {
    try {
      auth0ClientInstance = await createAuth0Client(config);

      // Check if user is returning from redirect
      const query = window.location.search;
      if (query.includes('code=') && query.includes('state=')) {
        await auth0ClientInstance.handleRedirectCallback();
        window.history.replaceState({}, document.title, '/');
      }

      // Check authentication status
      const authenticated = await auth0ClientInstance.isAuthenticated();
      isAuthenticated.set(authenticated);

      if (authenticated) {
        const userData = await auth0ClientInstance.getUser();
        const token = await auth0ClientInstance.getTokenSilently();
        
        user.set(userData);
        authToken.set(token);

        // Call backend to get/create account
        await this.initializeAccount(token);
      }
    } catch (error) {
      console.error('Auth initialization error:', error);
    } finally {
      isLoading.set(false);
    }
  },

  async login() {
    if (!auth0ClientInstance) return;
    await auth0ClientInstance.loginWithRedirect();
  },

  async logout() {
    if (!auth0ClientInstance) return;
    await auth0ClientInstance.logout({
      logoutParams: {
        returnTo: window.location.origin
      }
    });
    user.set(null);
    authToken.set(null);
    isAuthenticated.set(false);
  },

  async getToken() {
    if (!auth0ClientInstance) return null;
    return await auth0ClientInstance.getTokenSilently();
  },

  async initializeAccount(token) {
    try {
      const response = await fetch('/api/account', {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });

      if (!response.ok) {
        throw new Error('Failed to initialize account');
      }

      const account = await response.json();
      console.log('Account initialized:', account);
      return account;
    } catch (error) {
      console.error('Account initialization error:', error);
      throw error;
    }
  }
};
