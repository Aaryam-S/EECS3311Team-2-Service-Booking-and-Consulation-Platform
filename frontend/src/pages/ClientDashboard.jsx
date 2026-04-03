import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import axios from 'axios';

export default function ClientDashboard() {
  // State to hold data from the backend
  const [services, setServices] = useState([]);
  const [bookings, setBookings] = useState([]);
  
  // State for the Booking Form
  const [selectedServiceId, setSelectedServiceId] = useState('');
  const [bookingDate, setBookingDate] = useState('');
  
  // State for the Payment Form
  const [paymentBookingId, setPaymentBookingId] = useState('');
  const [cardNumber, setCardNumber] = useState('');

  const API_URL = import.meta.env.VITE_BACKEND_URL;
  const DUMMY_CLIENT_ID = 1; // Placeholder until auth is added

  // Fetch data when the page loads
  useEffect(() => {
    fetchServicesAndBookings();
  }, []);

  const fetchServicesAndBookings = async () => {
    try {
      // Fetch available services
      const servicesRes = await axios.get(`${API_URL}/services`);
      setServices(servicesRes.data);

      // Fetch client's booking history
      const bookingsRes = await axios.get(`${API_URL}/bookings?clientId=${DUMMY_CLIENT_ID}`);
      setBookings(bookingsRes.data);
    } catch (error) {
      console.error("Error fetching data. Backend might be down.", error);
    }
  };

  // Handle Booking Submission
  const handleBookingSubmit = async (e) => {
    e.preventDefault();
    try {
      await axios.post(`${API_URL}/bookings`, {
        clientId: DUMMY_CLIENT_ID,
        serviceId: selectedServiceId,
        date: bookingDate
      });
      alert("Booking requested successfully!");
      fetchServicesAndBookings(); // Refresh the tables
    } catch (error) {
      alert("Failed to request booking.");
      console.error(error);
    }
  };

  // Handle Payment Submission
  const handlePaymentSubmit = async (e) => {
    e.preventDefault();
    try {
      await axios.post(`${API_URL}/bookings/${paymentBookingId}/pay`, {
        cardNumber: cardNumber
      });
      alert("Payment successful!");
      fetchServicesAndBookings(); // Refresh the tables
    } catch (error) {
      alert("Payment failed.");
      console.error(error);
    }
  };

  return (
    <main>
      <nav>
        <Link to="/">← Back to Home</Link>
      </nav>
      
      <h1>Client Dashboard</h1>
      <p>Welcome! Here you can book services and manage your appointments.</p>

      <hr />

      <section>
        <h2>1. Browse Services</h2>
        <table>
          <thead>
            <tr>
              <th>Service ID</th>
              <th>Name</th>
              <th>Consultant</th>
              <th>Price</th>
            </tr>
          </thead>
          <tbody>
            {services.length === 0 ? (
              <tr><td colSpan="4">No services available (or backend is offline).</td></tr>
            ) : (
              services.map(service => (
                <tr key={service.id}>
                  <td>{service.id}</td>
                  <td>{service.name}</td>
                  <td>{service.consultantName}</td>
                  <td>${service.price}</td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </section>

      <section>
        <h2>2. Request a Booking</h2>
        <form onSubmit={handleBookingSubmit}>
          <p>
            <label>Select Service:</label><br />
            <select 
              value={selectedServiceId} 
              onChange={(e) => setSelectedServiceId(e.target.value)} 
              required
            >
              <option value="">-- Choose a Service --</option>
              {services.map(service => (
                <option key={service.id} value={service.id}>
                  {service.name} - ${service.price}
                </option>
              ))}
            </select>
          </p>
          <p>
            <label>Select Date & Time:</label><br />
            <input 
              type="datetime-local" 
              value={bookingDate} 
              onChange={(e) => setBookingDate(e.target.value)} 
              required 
            />
          </p>
          <button type="submit">Submit Request</button>
        </form>
      </section>

      <section>
        <h2>3. Booking History</h2>
        <table>
          <thead>
            <tr>
              <th>Booking ID</th>
              <th>Service</th>
              <th>Date</th>
              <th>Status</th>
            </tr>
          </thead>
          <tbody>
            {bookings.length === 0 ? (
              <tr><td colSpan="4">No bookings found.</td></tr>
            ) : (
              bookings.map(booking => (
                <tr key={booking.id}>
                  <td>{booking.id}</td>
                  <td>{booking.serviceName}</td>
                  <td>{booking.date}</td>
                  <td><strong>{booking.status}</strong></td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </section>

      <section>
        <h2>4. Simulate Payment</h2>
        <form onSubmit={handlePaymentSubmit}>
          <p>
            <label>Booking ID to Pay For:</label><br />
            <input 
              type="number" 
              value={paymentBookingId} 
              onChange={(e) => setPaymentBookingId(e.target.value)} 
              required 
            />
          </p>
          <p>
            <label>Credit Card Number:</label><br />
            <input 
              type="text" 
              placeholder="1234-5678-9012-3456" 
              value={cardNumber} 
              onChange={(e) => setCardNumber(e.target.value)} 
              required 
            />
          </p>
          <button type="submit">Pay Now</button>
        </form>
      </section>

    </main>
  );
}