import { Link } from 'react-router-dom';

export default function Home() {
  return (
    <main>
      <h1>Service Booking & Consulting Platform</h1>
      <p>Welcome to our platform! Select your role to continue:</p>

      <section style={{ marginTop: '30px' }}>
        <h2>For Clients</h2>
        <p>Browse services and book consulting sessions with experts.</p>
        <Link to="/client-login" style={{ 
          display: 'inline-block', 
          backgroundColor: '#007bff', 
          color: 'white', 
          padding: '10px 20px', 
          borderRadius: '5px', 
          textDecoration: 'none',
          marginRight: '10px'
        }}>
          Client Login
        </Link>
        <Link to="/client-register" style={{ 
          display: 'inline-block', 
          backgroundColor: '#17a2b8', 
          color: 'white', 
          padding: '10px 20px', 
          borderRadius: '5px', 
          textDecoration: 'none'
        }}>
          Client Register
        </Link>
      </section>

      <section style={{ marginTop: '30px' }}>
        <h2>For Consultants</h2>
        <p>Offer your services and manage your bookings.</p>
        <Link to="/consultant-login" style={{ 
          display: 'inline-block', 
          backgroundColor: '#28a745', 
          color: 'white', 
          padding: '10px 20px', 
          borderRadius: '5px', 
          textDecoration: 'none',
          marginRight: '10px'
        }}>
          Consultant Login
        </Link>
        <Link to="/consultant-registration" style={{ 
          display: 'inline-block', 
          backgroundColor: '#17a2b8', 
          color: 'white', 
          padding: '10px 20px', 
          borderRadius: '5px', 
          textDecoration: 'none'
        }}>
          Consultant Register
        </Link>
      </section>

      <section style={{ marginTop: '30px' }}>
        <h2>For Administrators</h2>
        <p>Approve consultants and manage system policies.</p>
        <Link to="/admin" style={{ 
          display: 'inline-block', 
          backgroundColor: '#dc3545', 
          color: 'white', 
          padding: '10px 20px', 
          borderRadius: '5px', 
          textDecoration: 'none'
        }}>
          Admin Dashboard
        </Link>
      </section>

      <hr style={{ marginTop: '40px' }} />

      <section>
        <h2>Platform Features</h2>
        <ul>
          <li><strong>UC1 - Browse Services:</strong> View available consulting services</li>
          <li><strong>UC2 - Request Booking:</strong> Book sessions with your preferred consultant</li>
          <li><strong>UC3 - Cancel Booking:</strong> Cancel bookings based on cancellation policies</li>
          <li><strong>UC4 - View Booking History:</strong> Track all your past and upcoming bookings</li>
          <li><strong>UC5 - Process Payment:</strong> Multiple payment methods (Credit Card, Debit Card, PayPal, Bank Transfer)</li>
          <li><strong>UC6 - Manage Payment Methods:</strong> Save and manage your payment methods</li>
          <li><strong>UC7 - View Payment History:</strong> Track all your payments and transactions</li>
          <li><strong>UC8 - Manage Availability:</strong> Set your available time slots (Consultant)</li>
          <li><strong>UC9 - Accept/Reject Bookings:</strong> Respond to booking requests (Consultant)</li>
          <li><strong>UC10 - Complete Booking:</strong> Mark sessions as completed (Consultant)</li>
          <li><strong>UC11 - Approve Consultant Registration:</strong> Review and approve new consultants (Admin)</li>
          <li><strong>UC12 - Define System Policies:</strong> Configure pricing and cancellation policies (Admin)</li>
        </ul>
      </section>
    </main>
  );
}