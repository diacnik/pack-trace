<script>
  import { authClient } from '../lib/auth0';

  async function handleLogin() {
    await authClient.login();
  }
</script>

<main>
  <h1>PackTrace</h1>
  <p>Track Your Gear. Plan Your Adventures.</p>
  
  <section>
    <h2>Features</h2>
    <ul>
      <li>Manage Gear - Track all your outdoor equipment</li>
      <li>Build Packs - Create and organize gear for trips</li>
      <li>Organize Closets - Categorize your gear collection</li>
      <li>Track Weight - Optimize your pack weight</li>
    </ul>
  </section>

  <button on:click={handleLogin}>Log In / Sign Up</button>
</main>

<style>
  main {
    max-width: 600px;
    margin: 50px auto;
    padding: 20px;
  }

  h1 {
    font-size: 2.5rem;
    margin-bottom: 10px;
  }

  p {
    font-size: 1.2rem;
    margin-bottom: 40px;
    color: #666;
  }

  section {
    margin-bottom: 40px;
  }

  h2 {
    font-size: 1.5rem;
    margin-bottom: 15px;
  }

  ul {
    list-style: none;
    padding: 0;
  }

  li {
    padding: 10px 0;
    border-bottom: 1px solid #eee;
  }

  button {
    background-color: #007bff;
    color: white;
    border: none;
    padding: 12px 24px;
    font-size: 1rem;
    border-radius: 4px;
  }

  button:hover {
    background-color: #0056b3;
  }
</style>
