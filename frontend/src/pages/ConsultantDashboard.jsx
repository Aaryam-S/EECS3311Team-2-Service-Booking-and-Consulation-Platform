import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';

export default function ConsultantDashboard() {
    const navigate = useNavigate();
    const consultantId = localStorage.getItem('consultantId');
    const consultantName = localStorage.getItem('consultantName');

    // Redirect to login if not authenticated
    useEffect(() => {
        if (!consultantId) {
            navigate('/consultant-login');
        }
    }, [consultantId, navigate]);

    // State for Bookings
    const [pendingBookings, setPendingBookings] = useState([]);
    const [confirmedBookings, setConfirmedBookings] = useState([]);
    const [completedBookings, setCompletedBookings] = useState([]);

    // State for Availability Form
    const [availDate, setAvailDate] = useState('');
    const [startTime, setStartTime] = useState('');
    const [endTime, setEndTime] = useState('');

    const API_URL = import.meta.env.VITE_BACKEND_URL;

    // Fetch data when the page loads
    useEffect(() => {
        fetchBookings();
    }, [consultantId]);

    const fetchBookings = async () => {
        try {
            // Fetch all bookings for this consultant
            const res = await axios.get(`${API_URL}/bookings?consultantId=${consultantId}`);
            const allBookings = res.data || [];

            // Filter bookings by status so we can put them in the right tables
            setPendingBookings(allBookings.filter(b => b.status === 'Requested'));
            setConfirmedBookings(allBookings.filter(b => b.status === 'Confirmed' || b.status === 'Paid'));
            setCompletedBookings(allBookings.filter(b => b.status === 'Completed'));
        } catch (error) {
            console.error("Error fetching bookings. Backend might be down.", error);
        }
    };

    // Handle Adding Availability
    const handleAddAvailability = async (e) => {
        e.preventDefault();
        try {
            await axios.post(`${API_URL}/timeslots`, {
                consultantId: parseInt(consultantId),
                date: availDate,
                startTime: startTime,
                endTime: endTime
            });
            alert("Availability added successfully!");
            // Clear the form
            setAvailDate('');
            setStartTime('');
            setEndTime('');
        } catch (error) {
            alert("Failed to add availability.");
            console.error(error);
        }
    };

    // Handle Accepting/Rejecting/Completing Bookings
    const updateBookingStatus = async (bookingId, action) => {
        try {
            // Action will be 'accept', 'reject', or 'complete'
            await axios.post(`${API_URL}/bookings/${bookingId}/${action}`);
            alert(`Booking ${action}ed successfully!`);
            fetchBookings(); // Refresh the tables
        } catch (error) {
            alert(`Failed to ${action} booking.`);
            console.error(error);
        }
    };

    return (
        <main>
            <nav>
                <button onClick={() => navigate('/')} style={{ background: 'none', border: 'none', color: '#007bff', cursor: 'pointer', textDecoration: 'underline' }}>
                    ← Back to Home
                </button>
                <button 
                    onClick={() => {
                        localStorage.clear();
                        navigate('/');
                    }} 
                    style={{ background: 'none', border: 'none', color: '#dc3545', cursor: 'pointer', textDecoration: 'underline', float: 'right' }}
                >
                    Logout
                </button>
            </nav>

            <h1>Consultant Dashboard</h1>
            <p>Welcome, {consultantName}! Manage your availability and respond to client booking requests.</p>

            <hr />

            <section>
                <h2>1. Manage Availability (UC8)</h2>
                <p>Set your available time slots for consulting sessions.</p>
                <form onSubmit={handleAddAvailability}>
                    <p>
                        <label>Date:</label><br />
                        <input
                            type="date"
                            value={availDate}
                            onChange={(e) => setAvailDate(e.target.value)}
                            required
                        />
                    </p>
                    <p>
                        <label>Start Time:</label><br />
                        <input
                            type="time"
                            value={startTime}
                            onChange={(e) => setStartTime(e.target.value)}
                            required
                        />
                    </p>
                    <p>
                        <label>End Time:</label><br />
                        <input
                            type="time"
                            value={endTime}
                            onChange={(e) => setEndTime(e.target.value)}
                            required
                        />
                    </p>
                    <button type="submit">Add Time Slot</button>
                </form>
            </section>

            <section>
                <h2>2. Accept or Reject Booking Requests (UC9)</h2>
                <p>Review and respond to incoming client booking requests.</p>
                <table>
                    <thead>
                        <tr>
                            <th>Booking ID</th>
                            <th>Client</th>
                            <th>Service</th>
                            <th>Date & Time</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        {pendingBookings.length === 0 ? (
                            <tr><td colSpan="5">No pending requests.</td></tr>
                        ) : (
                            pendingBookings.map(booking => (
                                <tr key={booking.id}>
                                    <td>{booking.id}</td>
                                    <td>{booking.clientName || 'Client ' + booking.id}</td>
                                    <td>{booking.serviceName}</td>
                                    <td>{booking.date}</td>
                                    <td>
                                        <button onClick={() => updateBookingStatus(booking.id, 'accept')} style={{ marginRight: '10px', backgroundColor: '#28a745', borderColor: '#28a745', color: 'white', padding: '8px 15px', cursor: 'pointer', border: 'none', borderRadius: '4px' }}>
                                            Accept
                                        </button>
                                        <button onClick={() => updateBookingStatus(booking.id, 'reject')} style={{ backgroundColor: '#dc3545', borderColor: '#dc3545', color: 'white', padding: '8px 15px', cursor: 'pointer', border: 'none', borderRadius: '4px' }}>
                                            Reject
                                        </button>
                                    </td>
                                </tr>
                            ))
                        )}
                    </tbody>
                </table>
            </section>

            <section>
                <h2>3. View Booking Schedule</h2>
                <p>Your confirmed and paid bookings.</p>
                <table>
                    <thead>
                        <tr>
                            <th>Booking ID</th>
                            <th>Client</th>
                            <th>Service</th>
                            <th>Date & Time</th>
                            <th>Status</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        {confirmedBookings.length === 0 ? (
                            <tr><td colSpan="6">No upcoming bookings.</td></tr>
                        ) : (
                            confirmedBookings.map(booking => (
                                <tr key={booking.id}>
                                    <td>{booking.id}</td>
                                    <td>{booking.clientName || 'Client ' + booking.id}</td>
                                    <td>{booking.serviceName}</td>
                                    <td>{booking.date}</td>
                                    <td><strong>{booking.status}</strong></td>
                                    <td>
                                        <button onClick={() => updateBookingStatus(booking.id, 'complete')} style={{ backgroundColor: '#007bff', borderColor: '#007bff', color: 'white', padding: '8px 15px', cursor: 'pointer', border: 'none', borderRadius: '4px' }}>
                                            Mark Completed
                                        </button>
                                    </td>
                                </tr>
                            ))
                        )}
                    </tbody>
                </table>
            </section>

            <section>
                <h2>4. Completed Bookings (UC10)</h2>
                <p>Your completed consulting sessions.</p>
                <table>
                    <thead>
                        <tr>
                            <th>Booking ID</th>
                            <th>Client</th>
                            <th>Service</th>
                            <th>Date & Time</th>
                            <th>Status</th>
                        </tr>
                    </thead>
                    <tbody>
                        {completedBookings.length === 0 ? (
                            <tr><td colSpan="5">No completed bookings yet.</td></tr>
                        ) : (
                            completedBookings.map(booking => (
                                <tr key={booking.id}>
                                    <td>{booking.id}</td>
                                    <td>{booking.clientName || 'Client ' + booking.id}</td>
                                    <td>{booking.serviceName}</td>
                                    <td>{booking.date}</td>
                                    <td><strong style={{ color: '#28a745' }}>Completed</strong></td>
                                </tr>
                            ))
                        )}
                    </tbody>
                </table>
            </section>

        </main>
    );
}