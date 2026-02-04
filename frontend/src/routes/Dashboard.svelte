<script>
  import { authClient } from '../lib/auth0';
  
  export let user;

  async function handleLogout() {
    await authClient.logout();
  }
</script>

<main>
  <header>
    <h1>PackTrace</h1>
    <div>
      <span>Welcome, {user?.name || user?.email}</span>
      <button on:click={handleLogout}>Log Out</button>
    </div>
  </header>

  <nav>
    <button>Gear</button>
    <button>Packs</button>
    <button>Closets</button>
  </nav>

  <section>
    <h2>Dashboard</h2>
    <p>Your gear management starts here.</p>
  </section>
</main>

<style>
  main {
    padding: 20px;
  }

  header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-bottom: 20px;
    border-bottom: 1px solid #eee;
    margin-bottom: 20px;
  }

  header h1 {
    margin: 0;
  }

  header div {
    display: flex;
    gap: 15px;
    align-items: center;
  }

  header button {
    background-color: #dc3545;
    color: white;
    border: none;
    padding: 8px 16px;
    border-radius: 4px;
  }

  header button:hover {
    background-color: #c82333;
  }

  nav {
    display: flex;
    gap: 10px;
    margin-bottom: 30px;
  }

  nav button {
    background-color: #f8f9fa;
    border: 1px solid #ddd;
    padding: 10px 20px;
    border-radius: 4px;
  }

  nav button:hover {
    background-color: #e9ecef;
  }

  section {
    padding: 20px;
    background-color: #f8f9fa;
    border-radius: 4px;
  }
</style>
