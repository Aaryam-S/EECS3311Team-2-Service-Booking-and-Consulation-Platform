import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import Chatbot from '../components/Chatbot';

export default function ClientDashboard() {
    const navigate = useNavigate();
    const clientId = localStorage.getItem('clientId');
    const clientName = localStorage.getItem('clientName');

    // Redirect to login if not authenticated
    useEffect(() => {
        if (!clientId) {
            navigate('/client-login');
        }
    }, [clientId, navigate]);

    // State to hold data from the backend
    const [services, setServices] = useState([]);
    const [bookings, setBookings] = useState([]);
    const [timeslots, setTimeslots] = useState([]);
    const [savedPaymentMethods, setSavedPaymentMethods] = useState([]);
    const [paymentHistory, setPaymentHistory] = useState([]);

    // State for the Booking Form
    const [selectedServiceId, setSelectedServiceId] = useState('');
    const [bookingDate, setBookingDate] = useState('');
    const [selectedTimeslotId, setSelectedTimeslotId] = useState('');

    // State for the Payment Form
    const [paymentBookingId, setPaymentBookingId] = useState('');
    const [useSavedPayment, setUseSavedPayment] = useState(true);
    const [selectedPaymentMethodId, setSelectedPaymentMethodId] = useState('');
    const [paymentMethod, setPaymentMethod] = useState('credit-card');
    const [cardNumber, setCardNumber] = useState('');
    const [cvv, setCvv] = useState('');
    const [expiryDate, setExpiryDate] = useState('');
    const [debitCardNumber, setDebitCardNumber] = useState('');
    const [debitCvv, setDebitCvv] = useState('');
    const [debitExpiry, setDebitExpiry] = useState('');
    const [paypalEmail, setPaypalEmail] = useState('');
    const [bankAccount, setBankAccount] = useState('');
    const [bankRouting, setBankRouting] = useState('');

    // State for Add Payment Method Form
    const [showAddPaymentForm, setShowAddPaymentForm] = useState(false);
    const [addPaymentType, setAddPaymentType] = useState('credit-card');
    const [addCardNumber, setAddCardNumber] = useState('');
    const [addCvv, setAddCvv] = useState('');
    const [addExpiryDate, setAddExpiryDate] = useState('');
    const [addPaypalEmail, setAddPaypalEmail] = useState('');
    const [addBankAccount, setAddBankAccount] = useState('');
    const [addBankRouting, setAddBankRouting] = useState('');

    const API_URL = import.meta.env.VITE_BACKEND_URL;

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

            // Fetch available timeslots
            const bookingsRes = await axios.get(`${API_URL}/bookings?clientId=${clientId}`);
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
            const payload = {
                clientId: parseInt(clientId),
                serviceId: selectedServiceId,
                date: bookingDate
            };
            if (selectedTimeslotId) {
                payload.timeslotId = selectedTimeslotId;
            }

            await axios.post(`${API_URL}/bookings`, payload);
            alert("Booking requested successfully!");
            fetchServicesAndBookings(); // Refresh the tables
            fetchTimeslots();

            // Reset form
            setSelectedServiceId('');
            setBookingDate('');
            setSelectedTimeslotId('');
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
            await axios.post(`${API_URL}/bookings/${paymentBookingId}/pay`, paymentPayload);
            alert("Payment successful!");
            fetchServicesAndBookings();
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
            alert("Payment failed: " + (error.response?.data?.error || error.message));
            console.error(error);
        }
    };

    const handleAddPaymentMethod = async (e) => {
        e.preventDefault();
        
        // Validate based on type
        if (addPaymentType === 'credit-card' || addPaymentType === 'debit-card') {
            if (!addCardNumber || addCardNumber.length !== 16) {
                alert("Card number must be 16 digits.");
                return;
            }
            if (!addCvv || (addCvv.length < 3 || addCvv.length > 4)) {
                alert("CVV must be 3-4 digits.");
                return;
            }
            if (!addExpiryDate) {
                alert("Expiry date is required.");
                return;
            }
        } else if (addPaymentType === 'paypal') {
            if (!addPaypalEmail || !addPaypalEmail.includes('@')) {
                alert("Valid PayPal email is required.");
                return;
            }
        } else if (addPaymentType === 'bank-transfer') {
            if (!addBankAccount || !addBankRouting) {
                alert("Account number and routing number are required.");
                return;
            }
        }

        try {
            const payload = {
                type: addPaymentType
            };

            if (addPaymentType === 'credit-card' || addPaymentType === 'debit-card') {
                payload.cardNumber = addCardNumber;
                payload.cvv = addCvv;
                payload.expiryDate = addExpiryDate;
            } else if (addPaymentType === 'paypal') {
                payload.email = addPaypalEmail;
            } else if (addPaymentType === 'bank-transfer') {
                payload.accountNumber = addBankAccount;
                payload.routingNumber = addBankRouting;
            }

            await axios.post(`${API_URL}/clients/${clientId}/payment-methods`, payload);
            alert("Payment method added successfully!");
            fetchPaymentMethods(); // Refresh payment methods
            
            // Reset form
            setShowAddPaymentForm(false);
            setAddPaymentType('credit-card');
            setAddCardNumber('');
            setAddCvv('');
            setAddExpiryDate('');
            setAddPaypalEmail('');
            setAddBankAccount('');
            setAddBankRouting('');
        } catch (error) {
            alert("Failed to add payment method: " + (error.response?.data?.error || error.message));
            console.error(error);
        }
    };

    const handleDeletePaymentMethod = async (methodId) => {
        if (!window.confirm('Are you sure you want to delete this payment method?')) {
            return;
        }

        try {
            await axios.delete(`${API_URL}/clients/${clientId}/payment-methods/${methodId}`);
            alert("Payment method deleted successfully!");
            fetchPaymentMethods(); // Refresh payment methods
        } catch (error) {
            alert("Failed to delete payment method: " + (error.response?.data?.error || error.message));
            console.error(error);
        }
    };

    const handleCancelBooking = async (bookingId) => {
        try {
            const response = await axios.post(`${API_URL}/bookings/${bookingId}/cancel`);
            const cancellationFee = response.data.cancellationFee || 0;
            alert(`Booking ${bookingId} cancelled. Cancellation fee: $${cancellationFee.toFixed(2)}`);
            fetchServicesAndBookings();
            fetchTimeslots();
        } catch (error) {
            alert("Cancellation failed: " + (error.response?.data?.error || error.message));
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

            <h1>Client Dashboard</h1>
            <p>Welcome, {clientName}! Here you can book services and manage your appointments.</p>

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
                    <p>
                        <label>Select Timeslot (Optional):</label><br />
                        <select
                            value={selectedTimeslotId}
                            onChange={(e) => setSelectedTimeslotId(e.target.value)}
                        >
                            <option value="">-- No specific timeslot --</option>
                            {timeslots
                                .filter((t) => !t.booked)
                                .map((slot) => (
                                    <option key={slot.id} value={slot.id}>
                                        {slot.date} {slot.startTime}-{slot.endTime} (Consultant {slot.consultantId})
                                    </option>
                                ))}
                        </select>
                    </p>
                    <button type="submit">Submit Request</button>
                </form>
            </section>

            <section>
                <h2>3. Booking History</h2>
                <table style={{ width: '100%', borderCollapse: 'collapse', backgroundColor: '#2d3748', border: '1px solid #4a5568' }}>
                    <thead>
                        <tr style={{ backgroundColor: '#1a202c' }}>
                            <th style={{ padding: '12px', textAlign: 'left', border: '1px solid #4a5568', fontWeight: 'bold', color: '#ffffff' }}>Booking ID</th>
                            <th style={{ padding: '12px', textAlign: 'left', border: '1px solid #4a5568', fontWeight: 'bold', color: '#ffffff' }}>Service</th>
                            <th style={{ padding: '12px', textAlign: 'left', border: '1px solid #4a5568', fontWeight: 'bold', color: '#ffffff' }}>Date</th>
                            <th style={{ padding: '12px', textAlign: 'left', border: '1px solid #4a5568', fontWeight: 'bold', color: '#ffffff' }}>Status</th>
                            <th style={{ padding: '12px', textAlign: 'left', border: '1px solid #4a5568', fontWeight: 'bold', color: '#ffffff' }}>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        {bookings.length === 0 ? (
                            <tr><td colSpan="5" style={{ padding: '12px', textAlign: 'center', border: '1px solid #4a5568', color: '#9da5b0', fontStyle: 'italic' }}>No bookings found.</td></tr>
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
                <h2>4. Available Time Slots</h2>
                <table style={{ width: '100%', borderCollapse: 'collapse', backgroundColor: '#2d3748', border: '1px solid #4a5568' }}>
                    <thead>
                        <tr style={{ backgroundColor: '#1a202c' }}>
                            <th style={{ padding: '12px', textAlign: 'left', border: '1px solid #4a5568', fontWeight: 'bold', color: '#ffffff' }}>Slot ID</th>
                            <th style={{ padding: '12px', textAlign: 'left', border: '1px solid #4a5568', fontWeight: 'bold', color: '#ffffff' }}>Consultant</th>
                            <th style={{ padding: '12px', textAlign: 'left', border: '1px solid #4a5568', fontWeight: 'bold', color: '#ffffff' }}>Date</th>
                            <th style={{ padding: '12px', textAlign: 'left', border: '1px solid #4a5568', fontWeight: 'bold', color: '#ffffff' }}>Time</th>
                            <th style={{ padding: '12px', textAlign: 'left', border: '1px solid #4a5568', fontWeight: 'bold', color: '#ffffff' }}>Status</th>
                        </tr>
                    </thead>
                    <tbody>
                        {timeslots.length === 0 ? (
                            <tr><td colSpan="5" style={{ padding: '12px', textAlign: 'center', border: '1px solid #4a5568', color: '#9da5b0', fontStyle: 'italic' }}>No timeslots configured yet.</td></tr>
                        ) : (
                            timeslots.map((slot) => (
                                <tr key={slot.id} style={{ backgroundColor: '#2d3748' }}>
                                    <td style={{ padding: '12px', border: '1px solid #4a5568', color: '#ffffff' }}>{slot.id}</td>
                                    <td style={{ padding: '12px', border: '1px solid #4a5568', color: '#ffffff' }}>{slot.consultantId}</td>
                                    <td style={{ padding: '12px', border: '1px solid #4a5568', color: '#ffffff' }}>{slot.date}</td>
                                    <td style={{ padding: '12px', border: '1px solid #4a5568', color: '#ffffff' }}>{slot.startTime} - {slot.endTime}</td>
                                    <td style={{ padding: '12px', border: '1px solid #4a5568', color: slot.booked ? '#dc3545' : '#28a745', fontWeight: 'bold' }}>
                                        {slot.booked ? 'Booked' : 'Available'}
                                    </td>
                                </tr>
                            ))
                        )}
                    </tbody>
                </table>
            </section>

            <section>
                <h2>5. Manage Payment Methods</h2>
                <button onClick={() => setShowAddPaymentForm(!showAddPaymentForm)}>
                    {showAddPaymentForm ? 'Cancel' : 'Add Payment Method'}
                </button>
                
                {showAddPaymentForm && (
                    <div style={{ marginTop: '20px', padding: '15px', border: '1px solid #4a5568', borderRadius: '5px', backgroundColor: '#2d3748' }}>
                        <p style={{ color: '#ffffff' }}><strong>Add New Payment Method</strong></p>
                        <form onSubmit={handleAddPaymentMethod}>
                            <p style={{ color: '#ffffff' }}>
                                <label>Payment Method Type:</label><br />
                                <select
                                    value={addPaymentType}
                                    onChange={(e) => setAddPaymentType(e.target.value)}
                                    required
                                >
                                    <option value="credit-card">Credit Card</option>
                                    <option value="debit-card">Debit Card</option>
                                    <option value="paypal">PayPal</option>
                                    <option value="bank-transfer">Bank Transfer</option>
                                </select>
                            </p>

                            {(addPaymentType === 'credit-card' || addPaymentType === 'debit-card') && (
                                <>
                                    <p style={{ color: '#ffffff' }}>
                                        <label>Card Number (16 digits):</label><br />
                                        <input
                                            type="text"
                                            placeholder="1234567890123456"
                                            value={addCardNumber}
                                            onChange={(e) => setAddCardNumber(e.target.value)}
                                            maxLength="16"
                                            required
                                        />
                                    </p>
                                    <p>
                                        <label>CVV (3-4 digits):</label><br />
                                        <input
                                            type="text"
                                            placeholder="123"
                                            value={addCvv}
                                            onChange={(e) => setAddCvv(e.target.value)}
                                            maxLength="4"
                                            required
                                        />
                                    </p>
                                    <p>
                                        <label>Expiry Date (MM/YY):</label><br />
                                        <input
                                            type="text"
                                            placeholder="12/27"
                                            value={addExpiryDate}
                                            onChange={(e) => setAddExpiryDate(e.target.value)}
                                            required
                                        />
                                    </p>
                                </>
                            )}

                            {addPaymentType === 'paypal' && (
                                <p style={{ color: '#ffffff' }}>
                                    <label>PayPal Email:</label><br />
                                    <input
                                        type="email"
                                        placeholder="example@paypal.com"
                                        value={addPaypalEmail}
                                        onChange={(e) => setAddPaypalEmail(e.target.value)}
                                        required
                                    />
                                </p>
                            )}

                            {addPaymentType === 'bank-transfer' && (
                                <>
                                    <p style={{ color: '#ffffff' }}>
                                        <label>Bank Account Number:</label><br />
                                        <input
                                            type="text"
                                            placeholder="1234567890"
                                            value={addBankAccount}
                                            onChange={(e) => setAddBankAccount(e.target.value)}
                                            required
                                        />
                                    </p>
                                    <p>
                                        <label>Routing Number:</label><br />
                                        <input
                                            type="text"
                                            placeholder="021000021"
                                            value={addBankRouting}
                                            onChange={(e) => setAddBankRouting(e.target.value)}
                                            required
                                        />
                                    </p>
                                </>
                            )}

                            <button type="submit">Save Payment Method</button>
                        </form>
                    </div>
                )}

                {savedPaymentMethods.length > 0 && (
                    <div style={{ marginTop: '20px' }}>
                        <p style={{ color: '#ffffff' }}><strong>Saved Payment Methods:</strong></p>
                        <table style={{ width: '100%', borderCollapse: 'collapse', backgroundColor: '#2d3748', border: '1px solid #4a5568' }}>
                            <thead>
                                <tr style={{ backgroundColor: '#1a202c' }}>
                                    <th style={{ padding: '12px', textAlign: 'left', border: '1px solid #4a5568', fontWeight: 'bold', color: '#ffffff' }}>Type</th>
                                    <th style={{ padding: '12px', textAlign: 'left', border: '1px solid #4a5568', fontWeight: 'bold', color: '#ffffff' }}>Details</th>
                                    <th style={{ padding: '12px', textAlign: 'left', border: '1px solid #4a5568', fontWeight: 'bold', color: '#ffffff' }}>Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                {savedPaymentMethods.map(method => (
                                    <tr key={method.id} style={{ backgroundColor: '#2d3748' }}>
                                        <td style={{ padding: '12px', border: '1px solid #4a5568', color: '#ffffff' }}>{method.type.replace(/-/g, ' ').toUpperCase()}</td>
                                        <td style={{ padding: '12px', border: '1px solid #4a5568', color: '#ffffff' }}>{method.lastFour}</td>
                                        <td style={{ padding: '12px', border: '1px solid #4a5568' }}>
                                            <button 
                                                onClick={() => handleDeletePaymentMethod(method.id)}
                                                style={{ backgroundColor: '#dc3545', color: 'white', padding: '6px 12px', border: 'none', borderRadius: '4px', cursor: 'pointer', fontSize: '14px' }}
                                            >
                                                Delete
                                            </button>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                )}

                {savedPaymentMethods.length === 0 && !showAddPaymentForm && (
                    <p style={{ marginTop: '20px', fontStyle: 'italic', color: '#9da5b0' }}>No payment methods saved yet. Add one to get started.</p>
                )}
            </section>

            <section>
                <h2>6. Process Payment</h2>
                <form onSubmit={handlePaymentSubmit}>
                    <p style={{ color: '#ffffff' }}>
                        <label>Booking ID to Pay For:</label><br />
                        <input
                            type="number"
                            value={paymentBookingId}
                            onChange={(e) => setPaymentBookingId(e.target.value)}
                            required
                        />
                    </p>

                    {savedPaymentMethods.length > 0 && (
                        <div style={{ marginBottom: '20px', padding: '15px', border: '1px solid #4a5568', borderRadius: '5px', backgroundColor: '#2d3748' }}>
                            <p style={{ color: '#ffffff' }}>
                                <label>
                                    <input
                                        type="radio"
                                        name="paymentOption"
                                        checked={useSavedPayment}
                                        onChange={() => setUseSavedPayment(true)}
                                    />
                                    Use Saved Payment Method
                                </label>
                            </p>
                            {useSavedPayment && (
                                <p style={{ color: '#ffffff' }}>
                                    <label>Select Payment Method:</label><br />
                                    <select
                                        value={selectedPaymentMethodId}
                                        onChange={(e) => setSelectedPaymentMethodId(e.target.value)}
                                        required={useSavedPayment}
                                    >
                                        <option value="">-- Select a saved payment method --</option>
                                        {savedPaymentMethods.map(method => (
                                            <option key={method.id} value={method.id}>
                                                {method.type.replace(/-/g, ' ').toUpperCase()} ending in {method.lastFour}
                                            </option>
                                        ))}
                                    </select>
                                </p>
                            )}
                        </div>
                    )}

                    <div style={{ marginBottom: '20px' }}>
                        <p style={{ color: '#ffffff' }}>
                            <label>
                                <input
                                    type="radio"
                                    name="paymentOption"
                                    checked={!useSavedPayment}
                                    onChange={() => setUseSavedPayment(false)}
                                />
                                Enter New Payment Details
                            </label>
                        </p>
                    </div>

                    {!useSavedPayment && (
                        <>
                            <p style={{ color: '#ffffff' }}>
                                <label>Payment Method:</label><br />
                                <select
                                    value={paymentMethod}
                                    onChange={(e) => setPaymentMethod(e.target.value)}
                                    required
                                >
                                    <option value="credit-card">Credit Card</option>
                                    <option value="debit-card">Debit Card</option>
                                    <option value="paypal">PayPal</option>
                                    <option value="bank-transfer">Bank Transfer</option>
                                </select>
                            </p>

                            {(paymentMethod === 'credit-card' || paymentMethod === 'debit-card') && (
                                <>
                                    <p style={{ color: '#ffffff' }}>
                                        <label>Card Number (16 digits):</label><br />
                                        <input
                                            type="text"
                                            placeholder="1234567890123456"
                                            value={paymentMethod === 'credit-card' ? cardNumber : debitCardNumber}
                                            onChange={(e) => paymentMethod === 'credit-card' ? setCardNumber(e.target.value) : setDebitCardNumber(e.target.value)}
                                            maxLength="16"
                                            required
                                        />
                                    </p>
                                    <p style={{ color: '#ffffff' }}>
                                        <label>CVV (3-4 digits):</label><br />
                                        <input
                                            type="text"
                                            placeholder="123"
                                            value={paymentMethod === 'credit-card' ? cvv : debitCvv}
                                            onChange={(e) => paymentMethod === 'credit-card' ? setCvv(e.target.value) : setDebitCvv(e.target.value)}
                                            maxLength="4"
                                            required
                                        />
                                    </p>
                                    <p style={{ color: '#ffffff' }}>
                                        <label>Expiry Date (MM/YY):</label><br />
                                        <input
                                            type="text"
                                            placeholder="12/27"
                                            value={paymentMethod === 'credit-card' ? expiryDate : debitExpiry}
                                            onChange={(e) => paymentMethod === 'credit-card' ? setExpiryDate(e.target.value) : setDebitExpiry(e.target.value)}
                                            required
                                        />
                                    </p>
                                </>
                            )}

                            {paymentMethod === 'paypal' && (
                                <p style={{ color: '#ffffff' }}>
                                    <label>PayPal Email:</label><br />
                                    <input
                                        type="email"
                                        placeholder="example@paypal.com"
                                        value={paypalEmail}
                                        onChange={(e) => setPaypalEmail(e.target.value)}
                                        required
                                    />
                                </p>
                            )}

                            {paymentMethod === 'bank-transfer' && (
                                <>
                                    <p style={{ color: '#ffffff' }}>
                                        <label>Bank Account Number:</label><br />
                                        <input
                                            type="text"
                                            placeholder="1234567890"
                                            value={bankAccount}
                                            onChange={(e) => setBankAccount(e.target.value)}
                                            required
                                        />
                                    </p>
                                    <p>
                                        <label>Routing Number:</label><br />
                                        <input
                                            type="text"
                                            placeholder="021000021"
                                            value={bankRouting}
                                            onChange={(e) => setBankRouting(e.target.value)}
                                            required
                                        />
                                    </p>
                                </>
                            )}
                        </>
                    )}

                    <button type="submit">Process Payment</button>
                </form>
            </section>

            <section>
                <h2>7. Payment History</h2>
                <button onClick={fetchPaymentHistory} style={{ marginBottom: '10px' }}>Refresh</button>
                <table style={{ width: '100%', borderCollapse: 'collapse', backgroundColor: '#2d3748', border: '1px solid #4a5568' }}>
                    <thead>
                        <tr style={{ backgroundColor: '#1a202c' }}>
                            <th style={{ padding: '12px', textAlign: 'left', border: '1px solid #4a5568', fontWeight: 'bold', color: '#ffffff' }}>ID</th>
                            <th style={{ padding: '12px', textAlign: 'left', border: '1px solid #4a5568', fontWeight: 'bold', color: '#ffffff' }}>Booking ID</th>
                            <th style={{ padding: '12px', textAlign: 'left', border: '1px solid #4a5568', fontWeight: 'bold', color: '#ffffff' }}>Amount</th>
                            <th style={{ padding: '12px', textAlign: 'left', border: '1px solid #4a5568', fontWeight: 'bold', color: '#ffffff' }}>Payment Method</th>
                            <th style={{ padding: '12px', textAlign: 'left', border: '1px solid #4a5568', fontWeight: 'bold', color: '#ffffff' }}>Type</th>
                            <th style={{ padding: '12px', textAlign: 'left', border: '1px solid #4a5568', fontWeight: 'bold', color: '#ffffff' }}>Date</th>
                        </tr>
                    </thead>
                    <tbody>
                        {paymentHistory.length === 0 ? (
                            <tr><td colSpan="6" style={{ padding: '12px', textAlign: 'center', border: '1px solid #4a5568', color: '#9da5b0', fontStyle: 'italic' }}>No payment history found.</td></tr>
                        ) : (
                            paymentHistory.map(ph => (
                                <tr key={ph.id} style={{ backgroundColor: '#2d3748' }}>
                                    <td style={{ padding: '12px', border: '1px solid #4a5568', color: '#ffffff' }}>{ph.id}</td>
                                    <td style={{ padding: '12px', border: '1px solid #4a5568', color: '#ffffff' }}>{ph.booking_id}</td>
                                    <td style={{ padding: '12px', border: '1px solid #4a5568', color: '#ffffff' }}>${Number(ph.amount).toFixed(2)}</td>
                                    <td style={{ padding: '12px', border: '1px solid #4a5568', color: '#ffffff' }}>{ph.payment_method}</td>
                                    <td style={{ padding: '12px', border: '1px solid #4a5568', color: '#ffffff' }}>{ph.type}</td>
                                    <td style={{ padding: '12px', border: '1px solid #4a5568', color: '#ffffff' }}>{new Date(ph.created_at).toLocaleString()}</td>
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