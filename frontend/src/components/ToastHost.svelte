<script>
  import { fly, fade, scale } from 'svelte/transition';
  import { toasts, dismissToast } from '../lib/toast';
</script>

<div class="toast-stack" aria-live="polite" aria-atomic="true">
  {#each $toasts as toast (toast.id)}
    <div
      class={`toast ${toast.type}`}
      in:scale={{ start: 0.96, duration: 160 }}
      out:fade={{ duration: 160 }}
    >
      <p>{toast.message}</p>
      <button type="button" aria-label="Dismiss" on:click={() => dismissToast(toast.id)}>
        &times;
      </button>
    </div>
  {/each}
</div>

<style>
  .toast-stack {
    position: fixed;
    top: 24px;
    right: 24px;
    display: grid;
    gap: 12px;
    z-index: 9999;
  }

  .toast {
    min-width: 220px;
    max-width: 320px;
    padding: 12px 14px;
    border-radius: 14px;
    background: rgba(255, 255, 255, 0.95);
    box-shadow: 0 14px 30px rgba(15, 15, 15, 0.15);
    border: 1px solid rgba(15, 15, 15, 0.08);
    display: flex;
    align-items: center;
    gap: 12px;
    color: #1b1b1b;
  }

  .toast p {
    margin: 0;
    font-size: 0.92rem;
    line-height: 1.2rem;
  }

  .toast button {
    margin-left: auto;
    border: none;
    background: transparent;
    font-size: 1.1rem;
    line-height: 1rem;
    color: #555;
  }

  .toast.success {
    border-left: 4px solid #2f7a4f;
  }

  .toast.error {
    border-left: 4px solid #b53a3a;
  }

  .toast.info {
    border-left: 4px solid #3d5a80;
  }

  @media (max-width: 700px) {
    .toast-stack {
      left: 16px;
      right: 16px;
    }

    .toast {
      max-width: 100%;
    }
  }
</style>
