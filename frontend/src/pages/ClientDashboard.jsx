import { Link } from 'react-router-dom';

export default function ClientDashboard() {
  return (
    <main>
      <nav>
        <Link to="/">← Back to Home</Link>
      </nav>
      <h1>Client Dashboard</h1>
      <p>Welcome, Client. Here you will be able to book services and view your history.</p>
    </main>
  );
}