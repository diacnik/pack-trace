<script>
  import { onMount } from 'svelte';
  import { authClient } from '../lib/auth0';
  import {
    fetchClosets,
    createCloset,
    updateCloset,
    deleteCloset,
    fetchClosetGear,
    addGearToCloset,
    updateClosetGearQuantity,
    removeGearFromCloset,
    fetchGear,
    createGear,
    updateGear,
    deleteGear
  } from '../lib/api';
  import { pushToast } from '../lib/toast';
  import { navigate } from '../lib/router';

  export let user;

  let closets = [];
  let closetsLoading = true;
  let selectedClosetId = '';

  let closetGear = [];
  let closetGearLoading = false;
  let closetGearRequestId = 0;
  let closetQuantities = {};

  let gearLibrary = [];
  let gearLoading = true;
  let selectedGearId = '';

  let closetCreateLoading = false;
  let closetUpdateLoading = false;
  let closetDeleteLoading = false;
  let showAddClosetForm = false;
  let showEditClosetMenu = false;

  let gearSaveLoading = false;
  let gearDeleteLoading = false;
  let addToClosetLoading = false;
  let updateClosetGearLoadingId = null;
  let removeClosetGearLoadingId = null;

  let addClosetForm = {
    name: '',
    description: ''
  };

  let editClosetForm = {
    name: '',
    description: ''
  };

  let gearForm = {
    name: '',
    brand: '',
    weightGrams: '',
    websiteURL: '',
    category: ''
  };

  let selectedLibraryGearId = '';
  let addQuantity = 1;

  onMount(async () => {
    await Promise.all([loadClosets(), loadGearLibrary()]);
  });

  $: selectedCloset = closets.find((closet) => String(closet.id) === selectedClosetId);
  $: selectedGear = gearLibrary.find((gear) => String(gear.id) === selectedGearId);
  $: totalItems = closetGear.reduce((sum, item) => sum + (item.quantity || 0), 0);
  $: totalWeight = closetGear.reduce(
    (sum, item) => sum + (item.weightGrams || 0) * (item.quantity || 0),
    0
  );

  async function loadClosets() {
    closetsLoading = true;
    try {
      const data = await fetchClosets();
      closets = Array.isArray(data) ? data : [];

      if (closets.length === 0) {
        selectedClosetId = '';
        closetGear = [];
        closetQuantities = {};
        return;
      }

      const stillExists = closets.some((closet) => String(closet.id) === selectedClosetId);
      const nextClosetId = stillExists ? selectedClosetId : String(closets[0].id);
      await handleSelectCloset(nextClosetId);
    } catch (error) {
      pushToast(error.message || 'Failed to load closets.', 'error');
    } finally {
      closetsLoading = false;
    }
  }

  async function loadClosetGear(closetId) {
    if (!closetId) {
      closetGear = [];
      closetQuantities = {};
      return;
    }

    const requestId = (closetGearRequestId += 1);
    closetGearLoading = true;

    try {
      const data = await fetchClosetGear(closetId);
      if (requestId !== closetGearRequestId) {
        return;
      }
      closetGear = Array.isArray(data) ? data : [];
      closetQuantities = closetGear.reduce((acc, item) => {
        acc[item.gearId] = item.quantity;
        return acc;
      }, {});
    } catch (error) {
      if (requestId !== closetGearRequestId) {
        return;
      }
      closetGear = [];
      closetQuantities = {};
      pushToast(error.message || 'Failed to load closet gear.', 'error');
    } finally {
      if (requestId === closetGearRequestId) {
        closetGearLoading = false;
      }
    }
  }

  async function loadGearLibrary() {
    gearLoading = true;
    try {
      const data = await fetchGear();
      gearLibrary = Array.isArray(data) ? data : [];

      if (gearLibrary.length === 0) {
        selectedGearId = '';
        selectedLibraryGearId = '';
        resetGearForm();
        return;
      }

      const stillExists = gearLibrary.some((gear) => String(gear.id) === selectedGearId);
      if (!stillExists) {
        selectGearForEdit(String(gearLibrary[0].id));
      }

      if (!selectedLibraryGearId) {
        selectedLibraryGearId = String(gearLibrary[0].id);
      }
    } catch (error) {
      pushToast(error.message || 'Failed to load gear.', 'error');
    } finally {
      gearLoading = false;
    }
  }

  async function handleSelectCloset(closetId) {
    selectedClosetId = closetId;
    showEditClosetMenu = false;
    const closet = closets.find((item) => String(item.id) === closetId);
    editClosetForm = {
      name: closet?.name || '',
      description: closet?.description || ''
    };
    await loadClosetGear(closetId);
  }

  function selectGearForEdit(gearId) {
    selectedGearId = gearId;
    const gear = gearLibrary.find((item) => String(item.id) === gearId);
    gearForm = {
      name: gear?.name || '',
      brand: gear?.brand || '',
      weightGrams: gear?.weightGrams || '',
      websiteURL: gear?.websiteURL || '',
      category: gear?.category || ''
    };
  }

  function resetGearForm() {
    selectedGearId = '';
    gearForm = {
      name: '',
      brand: '',
      weightGrams: '',
      websiteURL: '',
      category: ''
    };
  }

  async function handleCreateCloset() {
    const payload = {
      name: addClosetForm.name?.trim(),
      description: addClosetForm.description?.trim() || null
    };

    if (!payload.name) {
      pushToast('Closet name is required.', 'error');
      return;
    }

    closetCreateLoading = true;
    try {
      const created = await createCloset(payload);
      closets = [created, ...closets];
      addClosetForm = { name: '', description: '' };
      showAddClosetForm = false;
      await handleSelectCloset(String(created.id));
      pushToast('Closet created.', 'success');
    } catch (error) {
      pushToast(error.message || 'Unable to create closet.', 'error');
    } finally {
      closetCreateLoading = false;
    }
  }

  async function handleUpdateCloset() {
    if (!selectedClosetId) {
      pushToast('Select a closet first.', 'error');
      return;
    }

    const payload = {
      name: editClosetForm.name?.trim(),
      description: editClosetForm.description?.trim() || null
    };

    if (!payload.name) {
      pushToast('Closet name is required.', 'error');
      return;
    }

    closetUpdateLoading = true;
    try {
      const updated = await updateCloset(selectedClosetId, payload);
      closets = closets.map((closet) => (String(closet.id) === selectedClosetId ? updated : closet));
      showEditClosetMenu = false;
      pushToast('Closet updated.', 'success');
    } catch (error) {
      pushToast(error.message || 'Unable to update closet.', 'error');
    } finally {
      closetUpdateLoading = false;
    }
  }

  async function handleDeleteCloset() {
    if (!selectedClosetId || closetDeleteLoading) {
      return;
    }

    const confirmed = window.confirm('Delete this closet? This cannot be undone.');
    if (!confirmed) {
      return;
    }

    closetDeleteLoading = true;
    try {
      const closetIdToDelete = selectedClosetId;
      await deleteCloset(closetIdToDelete);
      closets = closets.filter((closet) => String(closet.id) !== closetIdToDelete);
      selectedClosetId = '';
      showEditClosetMenu = false;
      closetGear = [];
      closetQuantities = {};
      if (closets.length > 0) {
        await handleSelectCloset(String(closets[0].id));
      }
      pushToast('Closet deleted.', 'success');
    } catch (error) {
      pushToast(error.message || 'Unable to delete closet.', 'error');
    } finally {
      closetDeleteLoading = false;
    }
  }

  async function handleSaveGear() {
    const payload = {
      name: gearForm.name?.trim(),
      brand: gearForm.brand?.trim() || null,
      weightGrams: Number(gearForm.weightGrams || 0),
      websiteURL: gearForm.websiteURL?.trim() || null,
      category: gearForm.category?.trim() || null
    };

    if (!payload.name) {
      pushToast('Gear name is required.', 'error');
      return;
    }

    gearSaveLoading = true;
    try {
      if (selectedGearId) {
        const updated = await updateGear(selectedGearId, payload);
        gearLibrary = gearLibrary.map((item) => (String(item.id) === selectedGearId ? updated : item));
        if (selectedLibraryGearId === selectedGearId) {
          selectedLibraryGearId = String(updated.id);
        }
        pushToast('Gear updated.', 'success');
      } else {
        const created = await createGear(payload);
        gearLibrary = [created, ...gearLibrary];
        selectedLibraryGearId = String(created.id);
        selectGearForEdit(String(created.id));
        pushToast('Gear created.', 'success');
      }
    } catch (error) {
      pushToast(error.message || 'Unable to save gear.', 'error');
    } finally {
      gearSaveLoading = false;
    }
  }

  async function handleDeleteGear() {
    if (!selectedGearId || gearDeleteLoading) {
      return;
    }

    const confirmed = window.confirm('Delete this gear item? This cannot be undone.');
    if (!confirmed) {
      return;
    }

    gearDeleteLoading = true;
    try {
      const gearIdToDelete = selectedGearId;
      await deleteGear(gearIdToDelete);
      gearLibrary = gearLibrary.filter((item) => String(item.id) !== gearIdToDelete);
      if (selectedLibraryGearId === gearIdToDelete) {
        selectedLibraryGearId = '';
      }
      if (gearLibrary.length > 0) {
        selectGearForEdit(String(gearLibrary[0].id));
        if (!selectedLibraryGearId) {
          selectedLibraryGearId = String(gearLibrary[0].id);
        }
      } else {
        resetGearForm();
      }
      pushToast('Gear deleted.', 'success');
      if (selectedClosetId) {
        await loadClosetGear(selectedClosetId);
      }
    } catch (error) {
      pushToast(error.message || 'Unable to delete gear.', 'error');
    } finally {
      gearDeleteLoading = false;
    }
  }

  async function handleAddGearToCloset() {
    if (!selectedClosetId) {
      pushToast('Select a closet first.', 'error');
      return;
    }

    if (!selectedLibraryGearId) {
      pushToast('Select gear from your library.', 'error');
      return;
    }

    addToClosetLoading = true;
    try {
      await addGearToCloset(selectedClosetId, {
        gearId: Number(selectedLibraryGearId),
        quantity: Number(addQuantity || 1)
      });
      await loadClosetGear(selectedClosetId);
      pushToast('Gear added to closet.', 'success');
    } catch (error) {
      pushToast(error.message || 'Unable to add gear to closet.', 'error');
    } finally {
      addToClosetLoading = false;
    }
  }

  async function handleUpdateClosetGear(gearId) {
    if (!selectedClosetId) {
      return;
    }

    const quantity = Number(closetQuantities[gearId] || 1);
    updateClosetGearLoadingId = gearId;
    try {
      await updateClosetGearQuantity(selectedClosetId, gearId, { gearId, quantity });
      await loadClosetGear(selectedClosetId);
      pushToast('Closet quantity updated.', 'success');
    } catch (error) {
      pushToast(error.message || 'Unable to update closet quantity.', 'error');
    } finally {
      updateClosetGearLoadingId = null;
    }
  }

  async function handleRemoveClosetGear(gearId) {
    if (!selectedClosetId) {
      return;
    }

    removeClosetGearLoadingId = gearId;
    try {
      await removeGearFromCloset(selectedClosetId, gearId);
      await loadClosetGear(selectedClosetId);
      pushToast('Gear removed from closet.', 'success');
    } catch (error) {
      pushToast(error.message || 'Unable to remove gear from closet.', 'error');
    } finally {
      removeClosetGearLoadingId = null;
    }
  }

  async function handleLogout() {
    await authClient.logout();
  }

  function handleClosetQuantityInput(gearId, event) {
    const value = Number(event.target.value || 1);
    closetQuantities = { ...closetQuantities, [gearId]: value };
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
    <button type="button" on:click={() => navigate('/')}>Dashboard</button>
    <button class="active" type="button">Closets</button>
    <button type="button" on:click={() => navigate('/packs')}>Packs</button>
  </nav>

  <section class="layout">
    <article class="card">
      <div class="card-header">
        <h2>Closets</h2>
      </div>

      <div class="section-title">Closet list</div>
      {#if closetsLoading}
        <p class="muted">Loading closets...</p>
      {:else if closets.length === 0}
        <p class="muted">No closets yet.</p>
      {:else}
        <ul class="select-list">
          {#each closets as closet}
            <li>
              <button
                type="button"
                class:active={String(closet.id) === selectedClosetId}
                on:click={() => handleSelectCloset(String(closet.id))}
              >
                <span class="item-name">{closet.name}</span>
                <span class="item-meta">{closet.description || 'No description yet'}</span>
              </button>
            </li>
          {/each}
        </ul>
      {/if}

      <div class="actions top-gap">
        <button class="primary" type="button" on:click={() => (showAddClosetForm = !showAddClosetForm)}>
          {showAddClosetForm ? 'Cancel add closet' : 'Add closet'}
        </button>
      </div>

      {#if showAddClosetForm}
        <div class="subpanel">
          <h3>Add closet</h3>
          <div class="form two-col">
            <label>
              Closet name
              <input type="text" bind:value={addClosetForm.name} placeholder="Backpacking core" required />
            </label>
            <label class="full">
              Description
              <textarea rows="3" bind:value={addClosetForm.description} placeholder="Three-season essentials"></textarea>
            </label>
          </div>
          <div class="actions">
            <button class="primary" type="button" on:click={handleCreateCloset} disabled={closetCreateLoading}>
              {closetCreateLoading ? 'Creating...' : 'Create closet'}
            </button>
          </div>
        </div>
      {/if}

    </article>

    <article class="card emphasis">
      <div class="card-header">
        <h2>Closet details</h2>
      </div>

      {#if !selectedClosetId}
        <p class="muted">Select a closet to manage gear.</p>
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

        <div class="actions">
          <button
            class="ghost"
            type="button"
            on:click={() => (showEditClosetMenu = !showEditClosetMenu)}
            disabled={!selectedClosetId || closetUpdateLoading}
          >
            {showEditClosetMenu ? 'Close update menu' : 'Update'}
          </button>
          <button
            class="ghost danger"
            type="button"
            on:click={handleDeleteCloset}
            disabled={!selectedClosetId || closetDeleteLoading}
          >
            {closetDeleteLoading ? 'Deleting...' : 'Delete'}
          </button>
        </div>

        {#if showEditClosetMenu}
          <div class="subpanel">
            <h3>Update closet</h3>
            <div class="form two-col">
              <label>
                Closet name
                <input type="text" bind:value={editClosetForm.name} placeholder="Backpacking core" required />
              </label>
              <label class="full">
                Description
                <textarea rows="3" bind:value={editClosetForm.description} placeholder="Three-season essentials"></textarea>
              </label>
            </div>
            <div class="actions">
              <button
                class="ghost"
                type="button"
                on:click={handleUpdateCloset}
                disabled={!selectedClosetId || closetUpdateLoading}
              >
                {closetUpdateLoading ? 'Saving...' : 'Save update'}
              </button>
            </div>
          </div>
        {/if}

        <div class="section-title">Closet gear options</div>

        <div class="form inline">
          <label>
            Gear library
            <select bind:value={selectedLibraryGearId} disabled={gearLoading || gearLibrary.length === 0}>
              {#if gearLoading}
                <option>Loading gear...</option>
              {:else if gearLibrary.length === 0}
                <option value="">No gear yet</option>
              {:else}
                {#each gearLibrary as gear}
                  <option value={String(gear.id)}>{gear.name} {gear.brand ? `• ${gear.brand}` : ''}</option>
                {/each}
              {/if}
            </select>
          </label>
          <label>
            Quantity
            <input type="number" min="1" bind:value={addQuantity} />
          </label>
          <button
            class="primary"
            type="button"
            on:click={handleAddGearToCloset}
            disabled={addToClosetLoading || !selectedClosetId || gearLibrary.length === 0}
          >
            {addToClosetLoading ? 'Adding...' : 'Add to closet'}
          </button>
        </div>

        {#if closetGearLoading}
          <p class="muted">Loading closet gear...</p>
        {:else if closetGear.length === 0}
          <p class="muted">No gear in this closet yet.</p>
        {:else}
          <div class="gear-table">
            <div class="gear-row header">
              <span>Item</span>
              <span>Qty</span>
              <span>Weight</span>
              <span>Total</span>
              <span></span>
            </div>
            {#each closetGear as item}
              <div class="gear-row">
                <div class="gear-main">
                  <strong>{item.name}</strong>
                  <span>{item.brand || 'Unbranded'}</span>
                </div>
                <div class="qty-control">
                  <input
                    type="number"
                    min="1"
                    value={closetQuantities[item.gearId] ?? item.quantity}
                    on:input={(event) => handleClosetQuantityInput(item.gearId, event)}
                  />
                  <button
                    class="ghost"
                    type="button"
                    on:click={() => handleUpdateClosetGear(item.gearId)}
                    disabled={updateClosetGearLoadingId === item.gearId}
                  >
                    {updateClosetGearLoadingId === item.gearId ? 'Saving...' : 'Update'}
                  </button>
                </div>
                <span>{formatWeight(item.weightGrams)}</span>
                <span>{formatWeight(item.weightGrams * item.quantity)}</span>
                <button
                  class="ghost danger"
                  type="button"
                  on:click={() => handleRemoveClosetGear(item.gearId)}
                  disabled={removeClosetGearLoadingId === item.gearId}
                >
                  {removeClosetGearLoadingId === item.gearId ? 'Removing...' : 'Remove'}
                </button>
              </div>
            {/each}
          </div>
        {/if}
      {/if}
    </article>

    <article class="card">
      <div class="card-header">
        <h2>Gear CRUD</h2>
      </div>

      <div class="actions">
        <button class="ghost" type="button" on:click={resetGearForm}>New gear</button>
      </div>

      <div class="form two-col">
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
          <input type="number" min="1" bind:value={gearForm.weightGrams} placeholder="385" required />
        </label>
        <label class="full">
          Product URL
          <input type="url" bind:value={gearForm.websiteURL} placeholder="https://" />
        </label>
        <label>
          Category
          <input type="text" bind:value={gearForm.category} placeholder="Cooking" />
        </label>
      </div>

      <div class="actions">
        <button class="primary" type="button" on:click={handleSaveGear} disabled={gearSaveLoading}>
          {gearSaveLoading ? 'Saving...' : selectedGearId ? 'Update gear' : 'Create gear'}
        </button>
        <button class="ghost danger" type="button" on:click={handleDeleteGear} disabled={!selectedGearId || gearDeleteLoading}>
          {gearDeleteLoading ? 'Deleting...' : 'Delete gear'}
        </button>
      </div>

      <div class="section-title">Gear library</div>
      {#if gearLoading}
        <p class="muted">Loading gear...</p>
      {:else if gearLibrary.length === 0}
        <p class="muted">No gear saved yet.</p>
      {:else}
        <ul class="select-list">
          {#each gearLibrary as gear}
            <li>
              <button
                type="button"
                class:active={String(gear.id) === selectedGearId}
                on:click={() => selectGearForEdit(String(gear.id))}
              >
                <span class="item-name">{gear.name}</span>
                <span class="item-meta">{gear.brand || 'Unbranded'} • {formatWeight(gear.weightGrams)}</span>
                <span class="item-meta">{gear.category || 'Uncategorized'}</span>
              </button>
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

  .layout {
    display: grid;
    gap: 24px;
    grid-template-columns: minmax(280px, 350px) minmax(0, 1.3fr) minmax(280px, 350px);
    align-items: start;
  }

  .card {
    background: rgba(255, 255, 255, 0.86);
    border-radius: 24px;
    padding: 24px;
    box-shadow: 0 16px 40px rgba(15, 15, 15, 0.08);
    border: 1px solid rgba(15, 15, 15, 0.06);
  }

  .card.emphasis {
    background: linear-gradient(145deg, rgba(255, 255, 255, 0.96), rgba(255, 245, 228, 0.95));
  }

  .card-header h2 {
    margin: 0 0 8px;
    font-family: var(--font-display);
  }

  .form {
    display: grid;
    gap: 12px;
  }

  .form.two-col {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    align-items: end;
  }

  .form.two-col .full {
    grid-column: 1 / -1;
  }

  .form.inline {
    grid-template-columns: 1.4fr 0.8fr auto;
    align-items: end;
    margin-bottom: 18px;
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

  .actions {
    margin-top: 14px;
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
  }

  .top-gap {
    margin-top: 18px;
  }

  .subpanel {
    margin-top: 16px;
    border: 1px solid rgba(15, 15, 15, 0.1);
    border-radius: 16px;
    padding: 14px;
    background: rgba(255, 255, 255, 0.7);
  }

  .subpanel h3 {
    margin: 0 0 10px;
    font-size: 1rem;
  }

  button.primary {
    border: none;
    border-radius: 999px;
    padding: 12px 18px;
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

  button.ghost.danger {
    border-color: rgba(210, 76, 76, 0.4);
    color: #d24c4c;
  }

  .section-title {
    margin: 20px 0 12px;
    font-size: 0.8rem;
    text-transform: uppercase;
    letter-spacing: 0.16em;
    color: var(--ink-muted);
  }

  .select-list {
    list-style: none;
    padding: 0;
    margin: 0;
    display: grid;
    gap: 10px;
  }

  .select-list button {
    width: 100%;
    text-align: left;
    border: 1px solid rgba(15, 15, 15, 0.08);
    border-radius: 16px;
    padding: 12px;
    background: white;
    display: grid;
    gap: 4px;
  }

  .select-list button.active {
    border-color: rgba(15, 15, 15, 0.4);
    box-shadow: 0 10px 24px rgba(15, 15, 15, 0.08);
  }

  .item-name {
    font-weight: 600;
  }

  .item-meta,
  .muted {
    color: var(--ink-muted);
    font-size: 0.85rem;
  }

  .detail-header {
    display: grid;
    gap: 10px;
    margin-bottom: 12px;
  }

  .detail-stats {
    display: flex;
    gap: 14px;
    font-size: 0.9rem;
    color: var(--ink-muted);
  }

  .gear-table {
    display: grid;
    gap: 12px;
  }

  .gear-row {
    display: grid;
    grid-template-columns: 1.2fr 1fr 0.8fr 0.8fr auto;
    gap: 10px;
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

  @media (max-width: 1200px) {
    .layout {
      grid-template-columns: minmax(0, 1fr);
    }

    .form.inline {
      grid-template-columns: minmax(0, 1fr);
      align-items: stretch;
    }

    .form.two-col {
      grid-template-columns: minmax(0, 1fr);
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
  }
</style>
