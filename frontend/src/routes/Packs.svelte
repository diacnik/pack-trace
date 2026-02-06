<script>
  import { onMount } from 'svelte';
  import { authClient } from '../lib/auth0';
  import {
    fetchPacks,
    createPack,
    deletePack,
    fetchPackGear,
    addGearToPack,
    updatePackGearQuantity,
    removeGearFromPack,
    fetchClosets,
    fetchClosetGear
  } from '../lib/api';
  import { pushToast } from '../lib/toast';
  import { navigate } from '../lib/router';

  export let user;

  let packs = [];
  let packsLoading = true;
  let packsError = '';
  let selectedPackId = '';

  let packGear = [];
  let packGearLoading = false;
  let packGearError = '';
  let packGearRequestId = 0;

  let closets = [];
  let closetsLoading = true;
  let closetsError = '';
  let selectedClosetId = '';
  let closetGear = [];
  let closetGearLoading = false;
  let closetGearError = '';
  let closetGearRequestId = 0;
  let selectedClosetGearId = '';

  let createLoading = false;
  let deleteLoading = false;
  let addLoading = false;
  let updateLoadingId = null;
  let removeLoadingId = null;

  let packForm = {
    name: '',
    description: ''
  };

  let addQuantity = 1;
  let packQuantities = {};

  onMount(async () => {
    await Promise.all([loadPacks(), loadClosets()]);
  });

  $: selectedPack = packs.find((pack) => String(pack.id) === selectedPackId);
  $: packItemCount = packGear.reduce((sum, item) => sum + (item.quantity || 0), 0);
  $: packWeight = packGear.reduce(
    (sum, item) => sum + (item.weightGrams || 0) * (item.quantity || 0),
    0
  );

  async function loadPacks() {
    packsLoading = true;
    packsError = '';

    try {
      const data = await fetchPacks();
      packs = Array.isArray(data) ? data : [];
      if (!selectedPackId && packs.length > 0) {
        await handleSelectPack(String(packs[0].id));
      } else if (packs.length === 0) {
        selectedPackId = '';
        packGear = [];
        packQuantities = {};
      }
    } catch (error) {
      packsError = error.message || 'Failed to load packs.';
      pushToast(packsError, 'error');
    } finally {
      packsLoading = false;
    }
  }

  async function loadPackGear(packId) {
    if (!packId) {
      packGear = [];
      packQuantities = {};
      return;
    }

    const requestId = (packGearRequestId += 1);
    packGearLoading = true;
    packGearError = '';

    try {
      const data = await fetchPackGear(packId);
      if (requestId !== packGearRequestId) {
        return;
      }
      packGear = Array.isArray(data) ? data : [];
      packQuantities = packGear.reduce((acc, item) => {
        acc[item.gearId] = item.quantity;
        return acc;
      }, {});
    } catch (error) {
      if (requestId !== packGearRequestId) {
        return;
      }
      packGearError = error.message || 'Failed to load pack gear.';
      packGear = [];
      packQuantities = {};
      pushToast(packGearError, 'error');
    } finally {
      if (requestId === packGearRequestId) {
        packGearLoading = false;
      }
    }
  }

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
        selectedClosetGearId = '';
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
      selectedClosetGearId = '';
      return;
    }

    const requestId = (closetGearRequestId += 1);
    closetGearLoading = true;
    closetGearError = '';

    try {
      const data = await fetchClosetGear(closetId);
      if (requestId !== closetGearRequestId) {
        return;
      }
      closetGear = Array.isArray(data) ? data : [];
      if (!selectedClosetGearId && closetGear.length > 0) {
        selectedClosetGearId = String(closetGear[0].gearId);
      }
    } catch (error) {
      if (requestId !== closetGearRequestId) {
        return;
      }
      closetGearError = error.message || 'Failed to load closet gear.';
      closetGear = [];
      selectedClosetGearId = '';
      pushToast(closetGearError, 'error');
    } finally {
      if (requestId === closetGearRequestId) {
        closetGearLoading = false;
      }
    }
  }

  async function handleSelectPack(packId) {
    selectedPackId = packId;
    await loadPackGear(packId);
  }

  async function handleSelectCloset(closetId) {
    selectedClosetId = closetId;
    selectedClosetGearId = '';
    await loadClosetGear(closetId);
  }

  async function handleCreatePack() {
    createLoading = true;

    const payload = {
      name: packForm.name?.trim(),
      description: packForm.description?.trim() || null
    };

    if (!payload.name) {
      pushToast('Pack name is required.', 'error');
      createLoading = false;
      return;
    }

    try {
      const created = await createPack(payload);
      packs = [created, ...packs];
      packForm = { name: '', description: '' };
      await handleSelectPack(String(created.id));
      pushToast('Pack created.', 'success');
    } catch (error) {
      pushToast(error.message || 'Unable to create pack.', 'error');
    } finally {
      createLoading = false;
    }
  }

  async function handleDeletePack() {
    if (!selectedPackId || deleteLoading) {
      return;
    }

    const confirmed = window.confirm('Delete this pack? This cannot be undone.');
    if (!confirmed) {
      return;
    }

    deleteLoading = true;
    try {
      await deletePack(selectedPackId);
      packs = packs.filter((pack) => String(pack.id) !== selectedPackId);
      selectedPackId = '';
      packGear = [];
      packQuantities = {};
      if (packs.length > 0) {
        await handleSelectPack(String(packs[0].id));
      }
      pushToast('Pack deleted.', 'success');
    } catch (error) {
      pushToast(error.message || 'Unable to delete pack.', 'error');
    } finally {
      deleteLoading = false;
    }
  }

  async function handleAddFromCloset() {
    if (!selectedPackId) {
      pushToast('Select a pack first.', 'error');
      return;
    }

    if (!selectedClosetGearId) {
      pushToast('Select gear from the closet list.', 'error');
      return;
    }

    addLoading = true;
    try {
      await addGearToPack(selectedPackId, {
        gearId: Number(selectedClosetGearId),
        quantity: Number(addQuantity || 1)
      });
      await loadPackGear(selectedPackId);
      pushToast('Added gear to pack.', 'success');
    } catch (error) {
      pushToast(error.message || 'Unable to add gear to pack.', 'error');
    } finally {
      addLoading = false;
    }
  }

  async function handleUpdateQuantity(gearId) {
    if (!selectedPackId) {
      return;
    }

    const quantity = Number(packQuantities[gearId] || 1);
    updateLoadingId = gearId;
    try {
      await updatePackGearQuantity(selectedPackId, gearId, { gearId, quantity });
      await loadPackGear(selectedPackId);
      pushToast('Quantity updated.', 'success');
    } catch (error) {
      pushToast(error.message || 'Unable to update quantity.', 'error');
    } finally {
      updateLoadingId = null;
    }
  }

  async function handleRemoveGear(gearId) {
    if (!selectedPackId) {
      return;
    }

    removeLoadingId = gearId;
    try {
      await removeGearFromPack(selectedPackId, gearId);
      await loadPackGear(selectedPackId);
      pushToast('Gear removed.', 'success');
    } catch (error) {
      pushToast(error.message || 'Unable to remove gear.', 'error');
    } finally {
      removeLoadingId = null;
    }
  }

  async function handleLogout() {
    await authClient.logout();
  }

  function handleQuantityInput(gearId, event) {
    const value = Number(event.target.value || 1);
    packQuantities = { ...packQuantities, [gearId]: value };
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
  <div class="backdrop"></div>

  <header class="top-bar">
    <div>
      <p class="eyebrow">PackTrace</p>
      <h1>Packs</h1>
    </div>
    <div class="profile">
      <span>Welcome, {user?.name || user?.email}</span>
      <button class="ghost" type="button" on:click={handleLogout}>Log Out</button>
    </div>
  </header>

  <nav class="nav-strip">
    <button type="button" on:click={() => navigate('/')}>Gear</button>
    <button class="active" type="button">Packs</button>
    <button type="button" on:click={() => navigate('/closet')}>Closets</button>
  </nav>

  <section class="pack-layout">
    <article class="card accent">
      <div class="card-header">
        <h2>Create a pack</h2>
        <p>Name the trip and outline the gear you want to carry.</p>
      </div>

      <form class="form" on:submit|preventDefault={handleCreatePack}>
        <label>
          Pack name
          <input type="text" bind:value={packForm.name} placeholder="Fall weekend loop" required />
        </label>
        <label>
          Description
          <textarea
            rows="3"
            bind:value={packForm.description}
            placeholder="Two nights in the Enchantments, shoulder season"
          ></textarea>
        </label>
        <button class="primary" type="submit" disabled={createLoading}>
          {createLoading ? 'Saving...' : 'Create pack'}
        </button>
      </form>

      <div class="section-title">Your packs</div>

      {#if packsLoading}
        <p class="muted">Loading packs...</p>
      {:else if packsError}
        <p class="muted">{packsError}</p>
      {:else if packs.length === 0}
        <p class="muted">No packs yet. Create your first one to start planning.</p>
      {:else}
        <ul class="pack-list">
          {#each packs as pack}
            <li>
              <button
                type="button"
                class:active={String(pack.id) === selectedPackId}
                on:click={() => handleSelectPack(String(pack.id))}
              >
                <span class="pack-name">{pack.name}</span>
                <span class="pack-desc">{pack.description || 'No description yet'}</span>
              </button>
            </li>
          {/each}
        </ul>
      {/if}
    </article>

    <article class="card spotlight">
      <div class="card-header">
        <h2>Pack details</h2>
        <p>Dial in your kit with weight and quantity controls.</p>
      </div>

      {#if !selectedPackId}
        <p class="muted">Select a pack to see its gear list.</p>
      {:else}
        <div class="detail-header">
          <div>
            <h3>{selectedPack?.name}</h3>
            <p class="muted">{selectedPack?.description || 'No description yet'}</p>
          </div>
          <div class="detail-stats">
            <span>{packItemCount} items</span>
            <span>{formatWeight(packWeight)}</span>
          </div>
          <button class="danger" type="button" on:click={handleDeletePack} disabled={deleteLoading}>
            {deleteLoading ? 'Deleting...' : 'Delete pack'}
          </button>
        </div>

        {#if packGearLoading}
          <p class="muted">Loading gear...</p>
        {:else if packGearError}
          <p class="muted">{packGearError}</p>
        {:else if packGear.length === 0}
          <p class="muted">No gear in this pack yet. Add items from a closet.</p>
        {:else}
          <div class="gear-table">
            <div class="gear-row header">
              <span>Item</span>
              <span>Qty</span>
              <span>Weight</span>
              <span>Total</span>
              <span></span>
            </div>
            {#each packGear as item}
              <div class="gear-row">
                <div class="gear-main">
                  <strong>{item.name}</strong>
                  <span>{item.brand || 'Unbranded'}</span>
                </div>
                <div class="qty-control">
                  <input
                    type="number"
                    min="1"
                    value={packQuantities[item.gearId] ?? item.quantity}
                    on:input={(event) => handleQuantityInput(item.gearId, event)}
                  />
                  <button
                    class="ghost"
                    type="button"
                    on:click={() => handleUpdateQuantity(item.gearId)}
                    disabled={updateLoadingId === item.gearId}
                  >
                    {updateLoadingId === item.gearId ? 'Saving...' : 'Update'}
                  </button>
                </div>
                <span>{formatWeight(item.weightGrams)}</span>
                <span>{formatWeight(item.weightGrams * item.quantity)}</span>
                <button
                  class="ghost danger"
                  type="button"
                  on:click={() => handleRemoveGear(item.gearId)}
                  disabled={removeLoadingId === item.gearId}
                >
                  {removeLoadingId === item.gearId ? 'Removing...' : 'Remove'}
                </button>
              </div>
            {/each}
          </div>
        {/if}
      {/if}
    </article>

    <article class="card">
      <div class="card-header">
        <h2>Add from closets</h2>
        <p>Pull gear from your closets and drop it into this pack.</p>
      </div>

      <div class="form">
        <label>
          Choose closet
          <select bind:value={selectedClosetId} on:change={(event) => handleSelectCloset(event.target.value)} disabled={closetsLoading}>
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

        {#if closetsError}
          <p class="muted">{closetsError}</p>
        {/if}

        <div class="closet-gear">
          {#if closetGearLoading}
            <p class="muted">Loading closet gear...</p>
          {:else if closetGearError}
            <p class="muted">{closetGearError}</p>
          {:else if closetGear.length === 0}
            <p class="muted">No gear in this closet yet.</p>
          {:else}
            {#each closetGear as item}
              <button
                type="button"
                class:active={String(item.gearId) === selectedClosetGearId}
                on:click={() => (selectedClosetGearId = String(item.gearId))}
              >
                <span class="gear-title">{item.name}</span>
                <span class="gear-meta">
                  {item.brand || 'Unbranded'} • {item.quantity} available
                </span>
              </button>
            {/each}
          {/if}
        </div>

        <label>
          Quantity
          <input type="number" min="1" bind:value={addQuantity} />
        </label>

        <button
          class="primary"
          type="button"
          on:click={handleAddFromCloset}
          disabled={addLoading || closets.length === 0 || !selectedPackId}
        >
          {addLoading ? 'Adding...' : 'Add gear to pack'}
        </button>
      </div>
    </article>
  </section>
</main>

<style>
  main {
    position: relative;
    padding: 32px 40px 70px;
    overflow: hidden;
  }

  .backdrop {
    position: absolute;
    inset: 0;
    background: radial-gradient(circle at 15% 15%, rgba(255, 232, 199, 0.7), transparent 45%),
      radial-gradient(circle at 80% 20%, rgba(196, 230, 255, 0.7), transparent 50%),
      radial-gradient(circle at 70% 80%, rgba(255, 208, 235, 0.3), transparent 55%);
    pointer-events: none;
    opacity: 0.8;
  }

  .top-bar {
    position: relative;
    display: flex;
    justify-content: space-between;
    align-items: flex-end;
    gap: 24px;
    margin-bottom: 24px;
    z-index: 1;
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
    position: relative;
    display: inline-flex;
    gap: 10px;
    padding: 6px;
    border-radius: 999px;
    background: rgba(255, 255, 255, 0.65);
    border: 1px solid rgba(15, 15, 15, 0.08);
    margin-bottom: 32px;
    backdrop-filter: blur(8px);
    z-index: 1;
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

  .pack-layout {
    position: relative;
    display: grid;
    gap: 24px;
    grid-template-columns: minmax(260px, 360px) minmax(0, 1.2fr) minmax(260px, 360px);
    align-items: start;
    z-index: 1;
  }

  .card {
    background: rgba(255, 255, 255, 0.9);
    border-radius: 24px;
    padding: 24px;
    box-shadow: 0 18px 40px rgba(15, 15, 15, 0.08);
    border: 1px solid rgba(15, 15, 15, 0.06);
    animation: floatIn 0.6s ease both;
  }

  .card.accent {
    background: linear-gradient(160deg, rgba(255, 255, 255, 0.95), rgba(255, 245, 228, 0.95));
  }

  .card.spotlight {
    background: linear-gradient(145deg, rgba(255, 255, 255, 0.96), rgba(229, 244, 255, 0.92));
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
  textarea,
  select {
    border-radius: 12px;
    border: 1px solid rgba(15, 15, 15, 0.15);
    padding: 10px 12px;
    font-size: 0.95rem;
    background: rgba(255, 255, 255, 0.95);
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

  button.danger {
    border: none;
    background: #d24c4c;
    color: white;
    padding: 10px 16px;
    border-radius: 999px;
    font-weight: 600;
  }

  button.ghost.danger {
    border-color: rgba(210, 76, 76, 0.4);
    color: #d24c4c;
  }

  .pack-list {
    list-style: none;
    display: grid;
    gap: 10px;
  }

  .pack-list button {
    width: 100%;
    text-align: left;
    border: 1px solid rgba(15, 15, 15, 0.08);
    border-radius: 16px;
    padding: 12px;
    background: white;
    display: grid;
    gap: 6px;
  }

  .pack-list button.active {
    border-color: rgba(15, 15, 15, 0.4);
    box-shadow: 0 10px 24px rgba(15, 15, 15, 0.08);
  }

  .pack-name {
    font-weight: 600;
  }

  .pack-desc {
    color: var(--ink-muted);
    font-size: 0.85rem;
  }

  .detail-header {
    display: grid;
    gap: 12px;
    margin-bottom: 16px;
  }

  .detail-stats {
    display: flex;
    gap: 16px;
    font-size: 0.9rem;
    color: var(--ink-muted);
  }

  .gear-table {
    display: grid;
    gap: 12px;
  }

  .gear-row {
    display: grid;
    grid-template-columns: 1.4fr 1fr 0.8fr 0.8fr auto;
    gap: 12px;
    align-items: center;
    font-size: 0.9rem;
  }

  .gear-row.header {
    text-transform: uppercase;
    letter-spacing: 0.12em;
    font-size: 0.7rem;
    color: var(--ink-muted);
  }

  .gear-main {
    display: grid;
    gap: 4px;
  }

  .gear-main span {
    color: var(--ink-muted);
    font-size: 0.85rem;
  }

  .qty-control {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .qty-control input {
    width: 70px;
  }

  .closet-gear {
    display: grid;
    gap: 10px;
    padding: 12px;
    border-radius: 16px;
    border: 1px solid rgba(15, 15, 15, 0.08);
    background: rgba(255, 255, 255, 0.7);
    max-height: 240px;
    overflow-y: auto;
  }

  .closet-gear button {
    border: 1px solid rgba(15, 15, 15, 0.08);
    border-radius: 14px;
    padding: 10px;
    text-align: left;
    background: white;
    display: grid;
    gap: 4px;
  }

  .closet-gear button.active {
    border-color: rgba(15, 15, 15, 0.4);
    background: rgba(15, 15, 15, 0.04);
  }

  .gear-title {
    font-weight: 600;
  }

  .gear-meta {
    color: var(--ink-muted);
    font-size: 0.85rem;
  }

  .muted {
    color: var(--ink-muted);
  }

  @keyframes floatIn {
    from {
      opacity: 0;
      transform: translateY(16px);
    }
    to {
      opacity: 1;
      transform: translateY(0);
    }
  }

  @media (max-width: 1100px) {
    .pack-layout {
      grid-template-columns: minmax(0, 1fr);
    }
  }
</style>
