import { Link } from 'react-router-dom';

export default function AdminDashboard() {
  return (
    <main>
      <nav>
        <Link to="/">← Back to Home</Link>
      </nav>
      <h1>Admin Dashboard</h1>
      <p>Welcome, Admin. Here you will approve consultants and manage system policies.</p>
    </main>
  );
}