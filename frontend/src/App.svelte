<script>
  import { onMount } from 'svelte';
  import { authClient, isAuthenticated, user, isLoading } from './lib/auth0';
  import Landing from './routes/Landing.svelte';
  import Dashboard from './routes/Dashboard.svelte';
  import ToastHost from './components/ToastHost.svelte';

  onMount(async () => {
    await authClient.initialize();
  });
</script>

<ToastHost />

{#if $isLoading}
  <div>Loading...</div>
{:else if $isAuthenticated}
  <Dashboard user={$user} />
{:else}
  <Landing />
{/if}
