import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import axios from 'axios';

export default function ConsultantDashboard() {
    // State for Bookings
    const [pendingBookings, setPendingBookings] = useState([]);
    const [confirmedBookings, setConfirmedBookings] = useState([]);

    // State for Availability Form
    const [availDate, setAvailDate] = useState('');
    const [startTime, setStartTime] = useState('');
    const [endTime, setEndTime] = useState('');

    const API_URL = import.meta.env.VITE_BACKEND_URL;
    const DUMMY_CONSULTANT_ID = 1; // Placeholder until auth is added

    // Fetch data when the page loads
    useEffect(() => {
        fetchBookings();
    }, []);

    const fetchBookings = async () => {
        try {
            // Fetch all bookings for this consultant
            const res = await axios.get(`${API_URL}/bookings?consultantId=${DUMMY_CONSULTANT_ID}`);
            const allBookings = res.data || [];

            // Filter bookings by status so we can put them in the right tables
            setPendingBookings(allBookings.filter(b => b.status === 'Requested'));
            setConfirmedBookings(allBookings.filter(b => b.status === 'Confirmed' || b.status === 'Paid'));
        } catch (error) {
            console.error("Error fetching bookings. Backend might be down.", error);
        }
    };

    // Handle Adding Availability
    const handleAddAvailability = async (e) => {
        e.preventDefault();
        try {
            await axios.post(`${API_URL}/timeslots`, {
                consultantId: DUMMY_CONSULTANT_ID,
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
                <Link to="/">← Back to Home</Link>
            </nav>

            <h1>Consultant Dashboard</h1>
            <p>Welcome! Manage your schedule and approve client requests below.</p>

            <hr />

            <section>
                <h2>1. Add Availability</h2>
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
                <h2>2. Pending Booking Requests</h2>
                <table>
                    <thead>
                        <tr>
                            <th>Booking ID</th>
                            <th>Client</th>
                            <th>Service</th>
                            <th>Date</th>
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
                                    <td>{booking.clientName}</td>
                                    <td>{booking.serviceName}</td>
                                    <td>{booking.date}</td>
                                    <td>
                                        <button onClick={() => updateBookingStatus(booking.id, 'accept')} style={{ marginRight: '10px', backgroundColor: '#28a745', borderColor: '#28a745' }}>Accept</button>
                                        <button onClick={() => updateBookingStatus(booking.id, 'reject')} style={{ backgroundColor: '#dc3545', borderColor: '#dc3545' }}>Reject</button>
                                    </td>
                                </tr>
                            ))
                        )}
                    </tbody>
                </table>
            </section>

            <section>
                <h2>3. My Schedule (Confirmed & Paid)</h2>
                <table>
                    <thead>
                        <tr>
                            <th>Booking ID</th>
                            <th>Client</th>
                            <th>Service</th>
                            <th>Date</th>
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
                                    <td>{booking.clientName}</td>
                                    <td>{booking.serviceName}</td>
                                    <td>{booking.date}</td>
                                    <td><strong>{booking.status}</strong></td>
                                    <td>
                                        <button onClick={() => updateBookingStatus(booking.id, 'complete')}>Mark Completed</button>
                                    </td>
                                </tr>
                            ))
                        )}
                    </tbody>
                </table>
            </section>

        </main>
    );
}