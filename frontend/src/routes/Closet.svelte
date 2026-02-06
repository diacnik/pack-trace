<script>
  import { onMount } from 'svelte';
  import { authClient } from '../lib/auth0';
  import { fetchClosets, fetchClosetGear, createCloset } from '../lib/api';
  import { pushToast } from '../lib/toast';
  import { navigate } from '../lib/router';

  export let user;

  let closets = [];
  let closetsLoading = true;
  let closetsError = '';
  let selectedClosetId = '';

  let closetGear = [];
  let gearLoading = false;
  let gearError = '';
  let gearRequestId = 0;

  let createLoading = false;
  let closetForm = {
    name: '',
    description: ''
  };

  onMount(async () => {
    await loadClosets();
  });

  $: selectedCloset = closets.find((closet) => String(closet.id) === selectedClosetId);
  $: totalItems = closetGear.reduce((sum, item) => sum + (item.quantity || 0), 0);
  $: totalWeight = closetGear.reduce(
    (sum, item) => sum + (item.weightGrams || 0) * (item.quantity || 0),
    0
  );

  async function loadClosets() {
    closetsLoading = true;
    closetsError = '';
    try {
      const data = await fetchClosets();
      closets = Array.isArray(data) ? data : [];
      if (!selectedClosetId && closets.length > 0) {
        await handleSelectCloset(String(closets[0].id));
      } else if (closets.length === 0) {
        selectedClosetId = '';
        closetGear = [];
      }
    } catch (error) {
      closetsError = error.message || 'Failed to load closets.';
      pushToast(closetsError, 'error');
    } finally {
      closetsLoading = false;
    }
  }

  async function loadClosetGear(closetId) {
    if (!closetId) {
      closetGear = [];
      return;
    }

    const requestId = (gearRequestId += 1);
    gearLoading = true;
    gearError = '';

    try {
      const data = await fetchClosetGear(closetId);
      if (requestId !== gearRequestId) {
        return;
      }
      closetGear = Array.isArray(data) ? data : [];
    } catch (error) {
      if (requestId !== gearRequestId) {
        return;
      }
      gearError = error.message || 'Failed to load closet gear.';
      closetGear = [];
      pushToast(gearError, 'error');
    } finally {
      if (requestId === gearRequestId) {
        gearLoading = false;
      }
    }
  }

  async function handleSelectCloset(closetId) {
    selectedClosetId = closetId;
    await loadClosetGear(closetId);
  }

  async function handleCreateCloset() {
    createLoading = true;

    const payload = {
      name: closetForm.name?.trim(),
      description: closetForm.description?.trim() || null
    };

    if (!payload.name) {
      pushToast('Closet name is required.', 'error');
      createLoading = false;
      return;
    }

    try {
      const created = await createCloset(payload);
      closets = [created, ...closets];
      closetForm = { name: '', description: '' };
      await handleSelectCloset(String(created.id));
      pushToast('Closet created.', 'success');
    } catch (error) {
      pushToast(error.message || 'Unable to create closet.', 'error');
    } finally {
      createLoading = false;
    }
  }

  async function handleLogout() {
    await authClient.logout();
  }

  function formatWeight(grams) {
    if (typeof grams !== 'number') {
      return '-';
    }

    if (grams < 1000) {
      return `${grams} g`;
    }

    return `${grams} g (${(grams / 1000).toFixed(2)} kg)`;
  }
</script>

<main>
  <header class="top-bar">
    <div>
      <p class="eyebrow">PackTrace</p>
      <h1>Closets</h1>
    </div>
    <div class="profile">
      <span>Welcome, {user?.name || user?.email}</span>
      <button class="ghost" type="button" on:click={handleLogout}>Log Out</button>
    </div>
  </header>

  <nav class="nav-strip">
    <button type="button" on:click={() => navigate('/')}>Gear</button>
    <button type="button" on:click={() => navigate('/packs')}>Packs</button>
    <button class="active" type="button">Closets</button>
  </nav>

  <section class="closet-layout">
    <article class="card">
      <div class="card-header">
        <h2>Closet list</h2>
        <p>Create a closet and keep gear organized by purpose.</p>
      </div>

      <form class="form" on:submit|preventDefault={handleCreateCloset}>
        <label>
          Closet name
          <input type="text" bind:value={closetForm.name} placeholder="Backpacking core" required />
        </label>
        <label>
          Description
          <textarea
            rows="3"
            bind:value={closetForm.description}
            placeholder="Three-season backpacking essentials"
          ></textarea>
        </label>
        <button class="primary" type="submit" disabled={createLoading}>
          {createLoading ? 'Saving...' : 'Create closet'}
        </button>
      </form>

      <div class="section-title">Your closets</div>

      {#if closetsLoading}
        <p class="muted">Loading closets...</p>
      {:else if closetsError}
        <p class="muted">{closetsError}</p>
      {:else if closets.length === 0}
        <p class="muted">No closets yet. Create one to get started.</p>
      {:else}
        <ul class="closet-list">
          {#each closets as closet}
            <li>
              <button
                type="button"
                class:active={String(closet.id) === selectedClosetId}
                on:click={() => handleSelectCloset(String(closet.id))}
              >
                <span class="closet-name">{closet.name}</span>
                <span class="closet-desc">{closet.description || 'No description yet'}</span>
              </button>
            </li>
          {/each}
        </ul>
      {/if}
    </article>

    <article class="card">
      <div class="card-header">
        <h2>Closet details</h2>
        <p>Review and track gear assigned to this closet.</p>
      </div>

      {#if !selectedClosetId}
        <p class="muted">Select a closet to see its gear.</p>
      {:else}
        <div class="detail-header">
          <div>
            <h3>{selectedCloset?.name}</h3>
            <p class="muted">{selectedCloset?.description || 'No description yet'}</p>
          </div>
          <div class="detail-stats">
            <span>{totalItems} items</span>
            <span>{formatWeight(totalWeight)}</span>
          </div>
        </div>

        {#if gearLoading}
          <p class="muted">Loading gear...</p>
        {:else if gearError}
          <p class="muted">{gearError}</p>
        {:else if closetGear.length === 0}
          <p class="muted">No gear assigned yet. Add gear from the dashboard.</p>
        {:else}
          <div class="gear-table">
            <div class="gear-row header">
              <span>Item</span>
              <span>Qty</span>
              <span>Weight</span>
              <span>Total</span>
            </div>
            {#each closetGear as item}
              <div class="gear-row">
                <div class="gear-main">
                  <strong>{item.name}</strong>
                  <span>{item.brand || 'Unbranded'}</span>
                </div>
                <span>{item.quantity}</span>
                <span>{formatWeight(item.weightGrams)}</span>
                <span>{formatWeight(item.weightGrams * item.quantity)}</span>
              </div>
            {/each}
          </div>
        {/if}
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

  h3 {
    margin: 0 0 6px;
    font-size: 1.2rem;
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

  .closet-layout {
    display: grid;
    gap: 24px;
    grid-template-columns: minmax(260px, 360px) minmax(0, 1fr);
    align-items: start;
  }

  .card {
    background: rgba(255, 255, 255, 0.86);
    border-radius: 24px;
    padding: 24px;
    box-shadow: 0 16px 40px rgba(15, 15, 15, 0.08);
    border: 1px solid rgba(15, 15, 15, 0.06);
  }

  .card-header h2 {
    margin: 0 0 8px;
    font-family: var(--font-display);
  }

  .card-header p {
    color: var(--ink-muted);
    margin: 0 0 16px;
  }

  .section-title {
    margin: 20px 0 12px;
    font-size: 0.8rem;
    text-transform: uppercase;
    letter-spacing: 0.16em;
    color: var(--ink-muted);
  }

  .form {
    display: grid;
    gap: 14px;
  }

  label {
    display: grid;
    gap: 6px;
    font-size: 0.9rem;
    color: var(--ink-muted);
  }

  input,
  textarea {
    border-radius: 12px;
    border: 1px solid rgba(15, 15, 15, 0.15);
    padding: 10px 12px;
    font-size: 0.95rem;
    background: rgba(255, 255, 255, 0.9);
    font-family: inherit;
  }

  textarea {
    resize: vertical;
  }

  button.primary {
    border: none;
    border-radius: 999px;
    padding: 12px 18px;
    background: var(--ink);
    color: var(--paper);
    font-weight: 600;
  }

  button.primary:disabled {
    opacity: 0.6;
  }

  button.ghost {
    border: 1px solid rgba(15, 15, 15, 0.2);
    background: transparent;
    padding: 8px 14px;
    border-radius: 999px;
  }

  .closet-list {
    list-style: none;
    padding: 0;
    margin: 0;
    display: grid;
    gap: 12px;
  }

  .closet-list button {
    width: 100%;
    text-align: left;
    display: grid;
    gap: 6px;
    border-radius: 16px;
    border: 1px solid rgba(15, 15, 15, 0.12);
    background: rgba(255, 255, 255, 0.75);
    padding: 12px 14px;
    transition: border-color 0.2s ease, box-shadow 0.2s ease;
  }

  .closet-list button.active {
    border-color: rgba(15, 15, 15, 0.6);
    box-shadow: 0 10px 26px rgba(15, 15, 15, 0.12);
  }

  .closet-name {
    font-weight: 600;
  }

  .closet-desc {
    color: var(--ink-muted);
    font-size: 0.9rem;
  }

  .detail-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 16px;
    margin-bottom: 16px;
  }

  .detail-stats {
    display: grid;
    gap: 6px;
    text-align: right;
    color: var(--ink-muted);
    font-size: 0.9rem;
  }

  .gear-table {
    display: grid;
    gap: 8px;
  }

  .gear-row {
    display: grid;
    grid-template-columns: minmax(140px, 1fr) 70px 140px 140px;
    gap: 12px;
    align-items: center;
    padding: 10px 0;
    border-bottom: 1px solid rgba(15, 15, 15, 0.08);
  }

  .gear-row.header {
    font-size: 0.72rem;
    letter-spacing: 0.18em;
    text-transform: uppercase;
    color: var(--ink-muted);
    border-bottom: 1px solid rgba(15, 15, 15, 0.12);
    padding-bottom: 12px;
  }

  .gear-main {
    display: grid;
    gap: 4px;
  }

  .gear-main span {
    color: var(--ink-muted);
    font-size: 0.9rem;
  }

  .muted {
    color: var(--ink-muted);
  }

  @media (max-width: 960px) {
    .closet-layout {
      grid-template-columns: 1fr;
    }

    .detail-header {
      flex-direction: column;
      align-items: flex-start;
    }

    .detail-stats {
      text-align: left;
    }

    .gear-row {
      grid-template-columns: minmax(140px, 1fr) 60px;
      grid-auto-rows: auto;
    }

    .gear-row span:nth-child(3),
    .gear-row span:nth-child(4) {
      justify-self: start;
    }
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

    .gear-row {
      grid-template-columns: 1fr 60px;
      gap: 8px;
    }
  }
</style>
