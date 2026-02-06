<script>
  import { onMount } from 'svelte';
  import { authClient } from '../lib/auth0';
  import { fetchClosets, fetchGear, createGear, addGearToCloset } from '../lib/api';
  import { pushToast } from '../lib/toast';
  import { navigate } from '../lib/router';

  export let user;

  let closets = [];
  let closetsLoading = true;
  let closetsError = '';

  let createLoading = false;
  let createdGear = null;

  let addLoading = false;

  let selectedClosetId = '';
  let quantity = 1;

  let gearList = [];
  let gearLoading = true;
  let gearError = '';
  let selectedGearId = '';
  let existingQuantity = 1;

  let gearForm = {
    name: '',
    brand: '',
    weightGrams: '',
    websiteURL: '',
    category: ''
  };

  onMount(async () => {
    await Promise.all([loadClosets(), loadGear()]);
  });

  async function loadClosets() {
    closetsLoading = true;
    closetsError = '';
    try {
      const data = await fetchClosets();
      closets = Array.isArray(data) ? data : [];
      if (!selectedClosetId && closets.length > 0) {
        selectedClosetId = String(closets[0].id);
      }
    } catch (error) {
      closetsError = error.message || 'Failed to load closets.';
      pushToast(closetsError, 'error');
    } finally {
      closetsLoading = false;
    }
  }

  async function loadGear() {
    gearLoading = true;
    gearError = '';
    try {
      const data = await fetchGear();
      gearList = Array.isArray(data) ? data : [];
      if (!selectedGearId && gearList.length > 0) {
        selectedGearId = String(gearList[0].id);
      }
    } catch (error) {
      gearError = error.message || 'Failed to load gear.';
      pushToast(gearError, 'error');
    } finally {
      gearLoading = false;
    }
  }

  async function handleLogout() {
    await authClient.logout();
  }

  async function handleCreateGear() {
    createLoading = true;
    createdGear = null;

    const payload = {
      name: gearForm.name?.trim(),
      brand: gearForm.brand?.trim() || null,
      weightGrams: Number(gearForm.weightGrams || 0),
      websiteURL: gearForm.websiteURL?.trim() || null,
      category: gearForm.category?.trim() || null
    };

    try {
      const gear = await createGear(payload);
      createdGear = gear;
      gearList = [gear, ...gearList];
      selectedGearId = String(gear.id);
      pushToast('Gear saved.', 'success');
    } catch (error) {
      pushToast(error.message || 'Unable to create gear.', 'error');
    } finally {
      createLoading = false;
    }
  }

  async function handleAddToCloset() {
    addLoading = true;

    if (!selectedClosetId) {
      pushToast('Select a closet to continue.', 'error');
      addLoading = false;
      return;
    }

    try {
      await addGearToCloset(selectedClosetId, {
        gearId: createdGear.id,
        quantity: Number(quantity || 1)
      });
      pushToast('Added to closet.', 'success');
    } catch (error) {
      pushToast(error.message || 'Unable to add gear to closet.', 'error');
    } finally {
      addLoading = false;
    }
  }

  async function handleAddExistingGear() {
    addLoading = true;

    if (!selectedGearId) {
      pushToast('Select gear to continue.', 'error');
      addLoading = false;
      return;
    }

    if (!selectedClosetId) {
      pushToast('Select a closet to continue.', 'error');
      addLoading = false;
      return;
    }

    try {
      await addGearToCloset(selectedClosetId, {
        gearId: Number(selectedGearId),
        quantity: Number(existingQuantity || 1)
      });
      pushToast('Added to closet.', 'success');
    } catch (error) {
      pushToast(error.message || 'Unable to add gear to closet.', 'error');
    } finally {
      addLoading = false;
    }
  }

  function resetFlow() {
    createdGear = null;
    quantity = 1;
    gearForm = {
      name: '',
      brand: '',
      weightGrams: '',
      websiteURL: '',
      category: ''
    };
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
      <button class="ghost" on:click={handleLogout}>Log Out</button>
    </div>
  </header>

  <nav class="nav-strip">
    <button class="active" type="button">Gear</button>
    <button type="button">Packs</button>
    <button type="button" on:click={() => navigate('/closet')}>Closets</button>
  </nav>

  <section class="grid">
    <article class="card highlight">
      <div class="card-header">
        <h2>New gear</h2>
        <p>Save your latest item, then drop it into a closet.</p>
      </div>

      <form class="form" on:submit|preventDefault={handleCreateGear}>
        <label>
          Name
          <input type="text" bind:value={gearForm.name} placeholder="Jetboil Flash" required />
        </label>
        <label>
          Brand
          <input type="text" bind:value={gearForm.brand} placeholder="Jetboil" />
        </label>
        <label>
          Weight (grams)
          <input type="number" min="1" max="50000" bind:value={gearForm.weightGrams} placeholder="385" required />
        </label>
        <label>
          Product URL
          <input type="url" bind:value={gearForm.websiteURL} placeholder="https://" />
        </label>
        <label>
          Category
          <input type="text" bind:value={gearForm.category} placeholder="Cooking" />
        </label>

        <button class="primary" type="submit" disabled={createLoading}>
          {createLoading ? 'Saving...' : 'Save gear'}
        </button>
      </form>
    </article>

    <article class="card">
      <div class="card-header">
        <h2>Add to a closet</h2>
        <p>Pick where this gear should live and set the quantity.</p>
      </div>

      {#if !createdGear}
        <p class="muted">Create gear first to unlock this step.</p>
      {:else}
        <div class="summary">
          <div>
            <h3>{createdGear.name}</h3>
            <p>{createdGear.brand || 'Unbranded'} • {createdGear.weightGrams}g</p>
          </div>
          <button class="ghost" on:click={resetFlow}>Add another</button>
        </div>

        <div class="form">
          <label>
            Choose closet
            <select bind:value={selectedClosetId} disabled={closetsLoading}>
              {#if closetsLoading}
                <option>Loading closets...</option>
              {:else if closets.length === 0}
                <option value="">No closets yet</option>
              {:else}
                {#each closets as closet}
                  <option value={String(closet.id)}>{closet.name}</option>
                {/each}
              {/if}
            </select>
          </label>
          <label>
            Quantity
            <input type="number" min="1" bind:value={quantity} />
          </label>

          <button class="primary" on:click={handleAddToCloset} disabled={addLoading || closets.length === 0}>
            {addLoading ? 'Adding...' : 'Add to closet'}
          </button>
        </div>
      {/if}
    </article>

    <article class="card">
      <div class="card-header">
        <h2>Add existing gear</h2>
        <p>Drop a saved item into any closet in seconds.</p>
      </div>

      <div class="form">
        <label>
          Choose gear
          <select bind:value={selectedGearId} disabled={gearLoading}>
            {#if gearLoading}
              <option>Loading gear...</option>
            {:else if gearList.length === 0}
              <option value="">No gear saved yet</option>
            {:else}
              {#each gearList as gear}
                <option value={String(gear.id)}>
                  {gear.name} {gear.brand ? `• ${gear.brand}` : ''}
                </option>
              {/each}
            {/if}
          </select>
        </label>
        <label>
          Choose closet
          <select bind:value={selectedClosetId} disabled={closetsLoading}>
            {#if closetsLoading}
              <option>Loading closets...</option>
            {:else if closets.length === 0}
              <option value="">No closets yet</option>
            {:else}
              {#each closets as closet}
                <option value={String(closet.id)}>{closet.name}</option>
              {/each}
            {/if}
          </select>
        </label>
        <label>
          Quantity
          <input type="number" min="1" bind:value={existingQuantity} />
        </label>

        {#if gearError}
          <p class="muted">{gearError}</p>
        {/if}

        <button
          class="primary"
          on:click={handleAddExistingGear}
          disabled={addLoading || gearList.length === 0 || closets.length === 0}
        >
          {addLoading ? 'Adding...' : 'Add existing gear'}
        </button>
      </div>
    </article>

    <article class="card info">
      <h2>Closet snapshot</h2>
      {#if closetsLoading}
        <p class="muted">Loading closets...</p>
      {:else if closetsError}
        <p class="muted">{closetsError}</p>
      {:else if closets.length === 0}
        <p class="muted">Create your first closet to organize gear.</p>
      {:else}
        <ul>
          {#each closets as closet}
            <li>
              <strong>{closet.name}</strong>
              <span>{closet.description || 'No description yet'}</span>
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

  .grid {
    display: grid;
    gap: 24px;
    grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  }

  .card {
    background: rgba(255, 255, 255, 0.86);
    border-radius: 24px;
    padding: 24px;
    box-shadow: 0 16px 40px rgba(15, 15, 15, 0.08);
    border: 1px solid rgba(15, 15, 15, 0.06);
  }

  .card.highlight {
    background: linear-gradient(135deg, rgba(255, 234, 194, 0.8), rgba(255, 255, 255, 0.9));
  }

  .card.info {
    background: linear-gradient(135deg, rgba(201, 230, 255, 0.8), rgba(255, 255, 255, 0.95));
  }

  .card-header h2 {
    margin: 0 0 8px;
    font-family: var(--font-display);
  }

  .card-header p {
    color: var(--ink-muted);
    margin: 0 0 16px;
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
  select {
    border-radius: 12px;
    border: 1px solid rgba(15, 15, 15, 0.15);
    padding: 10px 12px;
    font-size: 0.95rem;
    background: rgba(255, 255, 255, 0.9);
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

  .summary {
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 16px;
    margin-bottom: 16px;
  }

  .summary h3 {
    margin: 0 0 4px;
  }

  .muted {
    color: var(--ink-muted);
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

  li span {
    color: var(--ink-muted);
    font-size: 0.9rem;
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
  }
</style>
