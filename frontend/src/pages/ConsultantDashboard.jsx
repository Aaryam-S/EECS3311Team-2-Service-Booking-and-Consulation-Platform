import { Link } from 'react-router-dom';

export default function ConsultantDashboard() {
  return (
    <main>
      <nav>
        <Link to="/">← Back to Home</Link>
      </nav>
      <h1>Consultant Dashboard</h1>
      <p>Welcome, Consultant. Here you will manage your availability and approve bookings.</p>
    </main>
  );
}