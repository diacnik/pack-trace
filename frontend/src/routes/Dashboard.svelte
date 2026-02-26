<script>
  import { onMount } from 'svelte';
  import { authClient } from '../lib/auth0';
  import { fetchClosets, fetchPacks } from '../lib/api';
  import { pushToast } from '../lib/toast';
  import { navigate } from '../lib/router';

  export let user;

  let closets = [];
  let packs = [];
  let loading = true;

  onMount(async () => {
    await loadOverview();
  });

  $: closetCount = closets.length;
  $: packCount = packs.length;

  async function loadOverview() {
    loading = true;
    try {
      const [closetData, packData] = await Promise.all([fetchClosets(), fetchPacks()]);
      closets = Array.isArray(closetData) ? closetData : [];
      packs = Array.isArray(packData) ? packData : [];
    } catch (error) {
      pushToast(error.message || 'Unable to load dashboard.', 'error');
    } finally {
      loading = false;
    }
  }

  async function handleLogout() {
    await authClient.logout();
  }
</script>

<main>
  <header class="top-bar">
    <div>
      <p class="eyebrow">PackTrace</p>
      <h1>Dashboard</h1>
    </div>
    <div class="profile">
      <span>Welcome, {user?.name || user?.email}</span>
      <button class="ghost" type="button" on:click={handleLogout}>Log Out</button>
    </div>
  </header>

  <nav class="nav-strip">
    <button class="active" type="button">Dashboard</button>
    <button type="button" on:click={() => navigate('/closet')}>Closets</button>
    <button type="button" on:click={() => navigate('/packs')}>Packs</button>
  </nav>

  <section class="overview-grid">
    <article class="card closets">
      <div class="card-head">
        <h2>Closets</h2>
        <button class="primary" type="button" on:click={() => navigate('/closet')}>Open closet flow</button>
      </div>
      <p class="muted">Your primary workspace for gear and closet operations.</p>

      {#if loading}
        <p class="muted">Loading closets...</p>
      {:else if closetCount === 0}
        <p class="muted">No closets yet. Create your first closet.</p>
      {:else}
        <p class="stat">{closetCount} total closets</p>
        <ul>
          {#each closets.slice(0, 5) as closet}
            <li>
              <strong>{closet.name}</strong>
              <span>{closet.description || 'No description yet'}</span>
            </li>
          {/each}
        </ul>
      {/if}
    </article>

    <article class="card packs">
      <div class="card-head">
        <h2>Packs</h2>
        <button class="primary" type="button" on:click={() => navigate('/packs')}>Open pack flow</button>
      </div>
      <p class="muted">Organize trip-specific gear from closets into pack categories.</p>

      {#if loading}
        <p class="muted">Loading packs...</p>
      {:else if packCount === 0}
        <p class="muted">No packs yet. Create your first pack.</p>
      {:else}
        <p class="stat">{packCount} total packs</p>
        <ul>
          {#each packs.slice(0, 5) as pack}
            <li>
              <strong>{pack.name}</strong>
              <span>{pack.description || 'No description yet'}</span>
            </li>
          {/each}
        </ul>
      {/if}
    </article>
  </section>
</main>

<style>
  main {
    padding: 32px 40px 60px;
  }

  .top-bar {
    display: flex;
    justify-content: space-between;
    align-items: flex-end;
    gap: 24px;
    margin-bottom: 24px;
  }

  .eyebrow {
    text-transform: uppercase;
    letter-spacing: 0.2em;
    font-size: 0.7rem;
    color: var(--ink-muted);
    margin-bottom: 6px;
  }

  h1 {
    font-family: var(--font-display);
    font-size: clamp(2rem, 3vw, 2.8rem);
    margin: 0;
  }

  .profile {
    display: flex;
    align-items: center;
    gap: 16px;
    color: var(--ink-muted);
  }

  .nav-strip {
    display: inline-flex;
    gap: 10px;
    padding: 6px;
    border-radius: 999px;
    background: rgba(255, 255, 255, 0.65);
    border: 1px solid rgba(15, 15, 15, 0.08);
    margin-bottom: 32px;
    backdrop-filter: blur(8px);
  }

  .nav-strip button {
    border: none;
    background: transparent;
    padding: 10px 16px;
    border-radius: 999px;
    color: var(--ink-muted);
  }

  .nav-strip button.active {
    background: var(--ink);
    color: var(--paper);
  }

  .overview-grid {
    display: grid;
    gap: 24px;
    grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  }

  .card {
    background: rgba(255, 255, 255, 0.86);
    border-radius: 24px;
    padding: 24px;
    box-shadow: 0 16px 40px rgba(15, 15, 15, 0.08);
    border: 1px solid rgba(15, 15, 15, 0.06);
  }

  .card.closets {
    background: linear-gradient(145deg, rgba(255, 255, 255, 0.96), rgba(255, 245, 228, 0.95));
  }

  .card.packs {
    background: linear-gradient(145deg, rgba(255, 255, 255, 0.96), rgba(229, 244, 255, 0.92));
  }

  .card-head {
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 12px;
  }

  h2 {
    margin: 0;
    font-family: var(--font-display);
  }

  .stat {
    margin: 16px 0 10px;
    font-weight: 600;
  }

  ul {
    list-style: none;
    padding: 0;
    margin: 0;
    display: grid;
    gap: 12px;
  }

  li {
    display: grid;
    gap: 4px;
  }

  li span,
  .muted {
    color: var(--ink-muted);
  }

  button.primary {
    border: none;
    border-radius: 999px;
    padding: 10px 14px;
    background: var(--ink);
    color: var(--paper);
    font-weight: 600;
  }

  button.ghost {
    border: 1px solid rgba(15, 15, 15, 0.2);
    background: transparent;
    padding: 8px 14px;
    border-radius: 999px;
  }

  @media (max-width: 700px) {
    main {
      padding: 24px;
    }

    .top-bar {
      flex-direction: column;
      align-items: flex-start;
    }

    .profile {
      width: 100%;
      justify-content: space-between;
    }

    .card-head {
      flex-direction: column;
      align-items: flex-start;
    }
  }
</style>
