import { Link } from 'react-router-dom';

export default function Home() {
  return (
    <main>
      <h1>Service Booking Platform</h1>
      <p>Welcome! Please select your role to continue:</p>
      <ul>
        <li><Link to="/client">Client Dashboard</Link></li>
        <li><Link to="/consultant">Consultant Dashboard</Link></li>
        <li><Link to="/admin">Admin Dashboard</Link></li>
      </ul>
    </main>
  );
}