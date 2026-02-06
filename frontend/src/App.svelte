<script>
  import { onMount } from 'svelte';
  import { authClient, isAuthenticated, user, isLoading } from './lib/auth0';
  import { currentPath, navigate } from './lib/router';
  import Landing from './routes/Landing.svelte';
  import Dashboard from './routes/Dashboard.svelte';
  import Closet from './routes/Closet.svelte';
  import ToastHost from './components/ToastHost.svelte';

  onMount(async () => {
    await authClient.initialize();
  });

  $: if ($isAuthenticated && $currentPath !== '/' && $currentPath !== '/closet') {
    navigate('/');
  }
</script>

<ToastHost />

{#if $isLoading}
  <div>Loading...</div>
{:else if $isAuthenticated}
  {#if $currentPath === '/closet'}
    <Closet user={$user} />
  {:else}
    <Dashboard user={$user} />
  {/if}
{:else}
  <Landing />
{/if}
