import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import axios from 'axios';
import Chatbot from '../components/Chatbot';

export default function ClientDashboard() {
    // State to hold data from the backend
    const [services, setServices] = useState([]);
    const [bookings, setBookings] = useState([]);
    const [timeslots, setTimeslots] = useState([]);
    const [savedPaymentMethods, setSavedPaymentMethods] = useState([]);
    const [paymentHistory, setPaymentHistory] = useState([]);

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
        fetchTimeslots();
        fetchPaymentMethods();
        fetchPaymentHistory();
    }, [clientId]);

    // Update useSavedPayment when savedPaymentMethods changes
    useEffect(() => {
        setUseSavedPayment(savedPaymentMethods.length > 0);
    }, [savedPaymentMethods]);

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

    const fetchTimeslots = async () => {
        try {
            const res = await axios.get(`${API_URL}/timeslots`);
            setTimeslots(res.data || []);
        } catch (error) {
            console.error("Error fetching timeslots.", error);
        }
    };

    const fetchPaymentMethods = async () => {
        try {
            const res = await axios.get(`${API_URL}/clients/${clientId}/payment-methods`);
            setSavedPaymentMethods(res.data || []);
        } catch (error) {
            console.error("Error fetching payment methods.", error);
        }
    };

    const fetchPaymentHistory = async () => {
        try {
            const res = await axios.get(`${API_URL}/clients/${clientId}/payment-history`);
            setPaymentHistory(res.data || []);
        } catch (error) {
            console.error("Error fetching payment history.", error);
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
        
        let paymentPayload = {};
        
        if (useSavedPayment && selectedPaymentMethodId) {
            // Using saved payment method
            paymentPayload.paymentMethodId = selectedPaymentMethodId;
        } else {
            // Using new payment details - validate based on method
            if (paymentMethod === 'credit-card') {
                if (!cardNumber || cardNumber.length !== 16) {
                    alert("Credit card number must be 16 digits.");
                    return;
                }
                if (!cvv || (cvv.length < 3 || cvv.length > 4)) {
                    alert("CVV must be 3-4 digits.");
                    return;
                }
                if (!expiryDate) {
                    alert("Expiry date is required.");
                    return;
                }
                paymentPayload = {
                    type: 'credit-card',
                    cardNumber: cardNumber,
                    cvv: cvv,
                    expiryDate: expiryDate
                };
            } else if (paymentMethod === 'debit-card') {
                if (!debitCardNumber || debitCardNumber.length !== 16) {
                    alert("Debit card number must be 16 digits.");
                    return;
                }
                if (!debitCvv || (debitCvv.length < 3 || debitCvv.length > 4)) {
                    alert("CVV must be 3-4 digits.");
                    return;
                }
                if (!debitExpiry) {
                    alert("Expiry date is required.");
                    return;
                }
                paymentPayload = {
                    type: 'debit-card',
                    cardNumber: debitCardNumber,
                    cvv: debitCvv,
                    expiryDate: debitExpiry
                };
            } else if (paymentMethod === 'paypal') {
                if (!paypalEmail || !paypalEmail.includes('@')) {
                    alert("Valid PayPal email is required.");
                    return;
                }
                paymentPayload = {
                    type: 'paypal',
                    cardNumber: 'PAYPAL_' + paypalEmail,
                    cvv: '123',
                    expiryDate: '12/99'
                };
            } else if (paymentMethod === 'bank-transfer') {
                if (!bankAccount || !bankRouting) {
                    alert("Account number and routing number are required.");
                    return;
                }
                paymentPayload = {
                    type: 'bank-transfer',
                    cardNumber: 'BANK_' + bankAccount,
                    cvv: '123',
                    expiryDate: '12/99'
                };
            }
        }

        try {
            await axios.post(`${API_URL}/bookings/${paymentBookingId}/pay`, {
                cardNumber: cardNumber
            });
            alert("Payment successful!");
            fetchServicesAndBookings(); // Refresh the tables
            fetchPaymentHistory();
            
            // Reset form
            setPaymentBookingId('');
            setSelectedPaymentMethodId('');
            setCardNumber('');
            setCvv('');
            setExpiryDate('');
            setDebitCardNumber('');
            setDebitCvv('');
            setDebitExpiry('');
            setPaypalEmail('');
            setBankAccount('');
            setBankRouting('');
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
                                <tr key={booking.id} style={{ backgroundColor: '#2d3748' }}>
                                    <td style={{ padding: '12px', border: '1px solid #4a5568', color: '#ffffff' }}>{booking.id}</td>
                                    <td style={{ padding: '12px', border: '1px solid #4a5568', color: '#ffffff' }}>{booking.serviceName}</td>
                                    <td style={{ padding: '12px', border: '1px solid #4a5568', color: '#ffffff' }}>{booking.date}</td>
                                    <td style={{ padding: '12px', border: '1px solid #4a5568', color: '#ffffff' }}><strong>{booking.status}</strong></td>
                                    <td style={{ padding: '12px', border: '1px solid #4a5568' }}>
                                        {(booking.status === 'Requested' || booking.status === 'Confirmed' || booking.status === 'PendingPayment') && (
                                            <button 
                                                onClick={() => handleCancelBooking(booking.id)}
                                                style={{ backgroundColor: '#dc3545', color: 'white', padding: '6px 12px', border: 'none', borderRadius: '4px', cursor: 'pointer', fontSize: '14px' }}
                                            >
                                                Cancel
                                            </button>
                                        )}
                                    </td>
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
            <hr />

            <section>
                <h2>7. Payment History</h2>
                <table style={{ width: '100%', borderCollapse: 'collapse', backgroundColor: '#2d3748', border: '1px solid #4a5568' }}>
                    <thead>
                        <tr style={{ backgroundColor: '#1a202c' }}>
                            <th style={{ padding: '12px', textAlign: 'left', border: '1px solid #4a5568', fontWeight: 'bold', color: '#ffffff' }}>ID</th>
                            <th style={{ padding: '12px', textAlign: 'left', border: '1px solid #4a5568', fontWeight: 'bold', color: '#ffffff' }}>Booking ID</th>
                            <th style={{ padding: '12px', textAlign: 'left', border: '1px solid #4a5568', fontWeight: 'bold', color: '#ffffff' }}>Amount</th>
                            <th style={{ padding: '12px', textAlign: 'left', border: '1px solid #4a5568', fontWeight: 'bold', color: '#ffffff' }}>Method</th>
                            <th style={{ padding: '12px', textAlign: 'left', border: '1px solid #4a5568', fontWeight: 'bold', color: '#ffffff' }}>Transaction ID</th>
                            <th style={{ padding: '12px', textAlign: 'left', border: '1px solid #4a5568', fontWeight: 'bold', color: '#ffffff' }}>Status</th>
                            <th style={{ padding: '12px', textAlign: 'left', border: '1px solid #4a5568', fontWeight: 'bold', color: '#ffffff' }}>Date</th>
                        </tr>
                    </thead>
                    <tbody>
                        {paymentHistory.length === 0 ? (
                            <tr><td colSpan="7" style={{ padding: '12px', textAlign: 'center', border: '1px solid #4a5568', color: '#9da5b0', fontStyle: 'italic' }}>No payment history found.</td></tr>
                        ) : (
                            paymentHistory.map(entry => (
                                <tr key={entry.id} style={{ backgroundColor: '#2d3748' }}>
                                    <td style={{ padding: '12px', border: '1px solid #4a5568', color: '#ffffff' }}>{entry.id}</td>
                                    <td style={{ padding: '12px', border: '1px solid #4a5568', color: '#ffffff' }}>{entry.bookingId}</td>
                                    <td style={{ padding: '12px', border: '1px solid #4a5568', color: '#28a745', fontWeight: 'bold' }}>${entry.amount != null ? Number(entry.amount).toFixed(2) : '0.00'}</td>
                                    <td style={{ padding: '12px', border: '1px solid #4a5568', color: '#ffffff' }}>{entry.methodType}</td>
                                    <td style={{ padding: '12px', border: '1px solid #4a5568', color: '#9da5b0', fontSize: '12px' }}>{entry.transactionId}</td>
                                    <td style={{ padding: '12px', border: '1px solid #4a5568', color: '#28a745' }}>{entry.status}</td>
                                    <td style={{ padding: '12px', border: '1px solid #4a5568', color: '#ffffff' }}>{entry.createdAt ? new Date(entry.createdAt).toLocaleDateString() : '-'}</td>
                                </tr>
            ))
                        )}
                    </tbody>
                </table>
            </section>

            <hr />
            <Chatbot />
        </main>
    );
}