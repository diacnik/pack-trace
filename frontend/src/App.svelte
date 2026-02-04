<script>
  import { onMount } from 'svelte';
  import { authClient, isAuthenticated, user, isLoading } from './lib/auth0';
  import Landing from './routes/Landing.svelte';
  import Dashboard from './routes/Dashboard.svelte';

  onMount(async () => {
    await authClient.initialize();
  });
</script>

{#if $isLoading}
  <div>Loading...</div>
{:else if $isAuthenticated}
  <Dashboard user={$user} />
{:else}
  <Landing />
{/if}
