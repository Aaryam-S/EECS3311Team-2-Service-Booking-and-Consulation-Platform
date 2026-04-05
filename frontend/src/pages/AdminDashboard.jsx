import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';

export default function AdminDashboard() {
    const navigate = useNavigate();

    // State for Consultants
    const [pendingConsultants, setPendingConsultants] = useState([]);
    const [approvedConsultants, setApprovedConsultants] = useState([]);

    // State for System Policies
    const [pricingStrategy, setPricingStrategy] = useState('BasePrice');
    const [cancellationPolicy, setCancellationPolicy] = useState('Flexible');
    const [taxRate, setTaxRate] = useState(0.13);
    const [cancellationFee, setCancellationFee] = useState(20.0);
    const [notificationsEnabled, setNotificationsEnabled] = useState(true);
    const [refundsEnabled, setRefundsEnabled] = useState(true);

    // State for System Status
    const [systemStatus, setSystemStatus] = useState({
        totalConsultants: 0,
        approvedConsultants: 0,
        pendingConsultants: 0,
        totalClients: 0,
        totalBookings: 0,
        paidBookings: 0,
        completedBookings: 0,
        cancelledBookings: 0,
        totalRevenue: 0
    });

    const API_URL = import.meta.env.VITE_BACKEND_URL;

    // Fetch data when the page loads
    useEffect(() => {
        fetchAdminData();
    }, []);

    const fetchAdminData = async () => {
        try {
            // Fetch pending consultants waiting for approval
            const pendingRes = await axios.get(`${API_URL}/admin/consultants/pending`);
            setPendingConsultants(pendingRes.data || []);

            // Fetch approved consultants
            const approvedRes = await axios.get(`${API_URL}/admin/consultants/approved`);
            setApprovedConsultants(approvedRes.data || []);

            // Fetch current system policies
            const policiesRes = await axios.get(`${API_URL}/admin/policies`);
            if (policiesRes.data) {
                setPricingStrategy(policiesRes.data.pricingStrategy || 'BasePrice');
                setCancellationPolicy(policiesRes.data.cancellationPolicy || 'Flexible');
                setTaxRate(policiesRes.data.taxRate || 0.13);
                setCancellationFee(policiesRes.data.cancellationFee || 20.0);
                setNotificationsEnabled(policiesRes.data.notificationsEnabled !== false);
                setRefundsEnabled(policiesRes.data.refundsEnabled !== false);
            }

            // Fetch system status
            const statusRes = await axios.get(`${API_URL}/admin/system-status`);
            if (statusRes.data) {
                setSystemStatus(statusRes.data);
            }
        } catch (error) {
            console.error("Error fetching admin data. Backend might be down.", error);
        }
    };

    // Handle Approving/Rejecting Consultants
    const handleConsultantAction = async (consultantId, action) => {
        try {
            // Action will be 'approve' or 'reject'
            await axios.post(`${API_URL}/admin/consultants/${consultantId}/${action}`);
            alert(`Consultant ${action}ed successfully!`);
            fetchAdminData(); // Refresh the table
        } catch (error) {
            alert(`Failed to ${action} consultant.`);
            console.error(error);
        }
    };

    // Handle Policy Updates
    const handlePolicySubmit = async (e) => {
        e.preventDefault();
        try {
            await axios.post(`${API_URL}/admin/policies`, {
                pricingStrategy: pricingStrategy,
                cancellationPolicy: cancellationPolicy,
                taxRate: parseFloat(taxRate),
                cancellationFee: parseFloat(cancellationFee),
                notificationsEnabled: notificationsEnabled,
                refundsEnabled: refundsEnabled
            });
            alert("System policies updated successfully!");
            fetchAdminData();
        } catch (error) {
            alert("Failed to update policies: " + (error.response?.data || error.message));
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

            <h1>Admin Dashboard</h1>
            <p>Welcome, Admin. Manage system policies, approve new consultants, and view system status.</p>

            <hr />

            <section>
                <h2>System Status Overview</h2>
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: '20px', marginBottom: '30px' }}>
                    <div style={{ padding: '15px', border: '1px solid #4a5568', borderRadius: '5px', backgroundColor: '#2d3748' }}>
                        <p style={{ color: '#ffffff' }}><strong>Total Consultants:</strong> {systemStatus.totalConsultants}</p>
                    </div>
                    <div style={{ padding: '15px', border: '1px solid #4a5568', borderRadius: '5px', backgroundColor: '#2d3748' }}>
                        <p style={{ color: '#ffffff' }}><strong>Total Clients:</strong> {systemStatus.totalClients}</p>
                    </div>
                    <div style={{ padding: '15px', border: '1px solid #4a5568', borderRadius: '5px', backgroundColor: '#2d3748' }}>
                        <p style={{ color: '#ffffff' }}><strong>Total Bookings:</strong> {systemStatus.totalBookings}</p>
                    </div>
                    <div style={{ padding: '15px', border: '1px solid #4a5568', borderRadius: '5px', backgroundColor: '#2d3748' }}>
                        <p style={{ color: '#ffffff' }}><strong>Total Revenue:</strong> ${systemStatus.totalRevenue.toFixed(2)}</p>
                    </div>
                </div>
            </section>

            <section>
                <h2>1. Pending Consultant Approvals (UC11)</h2>
                <p>Review and approve new consultant registrations.</p>
                <table style={{ width: '100%', borderCollapse: 'collapse', backgroundColor: '#2d3748', border: '1px solid #4a5568' }}>
                    <thead>
                        <tr style={{ backgroundColor: '#1a202c' }}>
                            <th style={{ padding: '12px', textAlign: 'left', border: '1px solid #4a5568', fontWeight: 'bold', color: '#ffffff' }}>ID</th>
                            <th style={{ padding: '12px', textAlign: 'left', border: '1px solid #4a5568', fontWeight: 'bold', color: '#ffffff' }}>Name</th>
                            <th style={{ padding: '12px', textAlign: 'left', border: '1px solid #4a5568', fontWeight: 'bold', color: '#ffffff' }}>Email</th>
                            <th style={{ padding: '12px', textAlign: 'left', border: '1px solid #4a5568', fontWeight: 'bold', color: '#ffffff' }}>Specialty</th>
                            <th style={{ padding: '12px', textAlign: 'left', border: '1px solid #4a5568', fontWeight: 'bold', color: '#ffffff' }}>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        {pendingConsultants.length === 0 ? (
                            <tr><td colSpan="5" style={{ padding: '12px', textAlign: 'center', border: '1px solid #4a5568', color: '#9da5b0', fontStyle: 'italic' }}>No pending consultant registrations.</td></tr>
                        ) : (
                            pendingConsultants.map(consultant => (
                                <tr key={consultant.id} style={{ backgroundColor: '#2d3748' }}>
                                    <td style={{ padding: '12px', border: '1px solid #4a5568', color: '#ffffff' }}>{consultant.id}</td>
                                    <td style={{ padding: '12px', border: '1px solid #4a5568', color: '#ffffff' }}>{consultant.name}</td>
                                    <td style={{ padding: '12px', border: '1px solid #4a5568', color: '#ffffff' }}>{consultant.email}</td>
                                    <td style={{ padding: '12px', border: '1px solid #4a5568', color: '#ffffff' }}>{consultant.specialty}</td>
                                    <td style={{ padding: '12px', border: '1px solid #4a5568' }}>
                                        <button onClick={() => handleConsultantAction(consultant.id, 'approve')} style={{ marginRight: '10px', backgroundColor: '#28a745', borderColor: '#28a745', color: 'white', padding: '8px 15px', cursor: 'pointer', border: 'none', borderRadius: '4px', fontSize: '14px' }}>
                                            Approve
                                        </button>
                                        <button onClick={() => handleConsultantAction(consultant.id, 'reject')} style={{ backgroundColor: '#dc3545', borderColor: '#dc3545', color: 'white', padding: '8px 15px', cursor: 'pointer', border: 'none', borderRadius: '4px', fontSize: '14px' }}>
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
                <h2>Approved Consultants</h2>
                <table style={{ width: '100%', borderCollapse: 'collapse', backgroundColor: '#2d3748', border: '1px solid #4a5568' }}>
                    <thead>
                        <tr style={{ backgroundColor: '#1a202c' }}>
                            <th style={{ padding: '12px', textAlign: 'left', border: '1px solid #4a5568', fontWeight: 'bold', color: '#ffffff' }}>ID</th>
                            <th style={{ padding: '12px', textAlign: 'left', border: '1px solid #4a5568', fontWeight: 'bold', color: '#ffffff' }}>Name</th>
                            <th style={{ padding: '12px', textAlign: 'left', border: '1px solid #4a5568', fontWeight: 'bold', color: '#ffffff' }}>Email</th>
                            <th style={{ padding: '12px', textAlign: 'left', border: '1px solid #4a5568', fontWeight: 'bold', color: '#ffffff' }}>Specialty</th>
                            <th style={{ padding: '12px', textAlign: 'left', border: '1px solid #4a5568', fontWeight: 'bold', color: '#ffffff' }}>Status</th>
                        </tr>
                    </thead>
                    <tbody>
                        {approvedConsultants.length === 0 ? (
                            <tr><td colSpan="5" style={{ padding: '12px', textAlign: 'center', border: '1px solid #4a5568', color: '#9da5b0', fontStyle: 'italic' }}>No approved consultants yet.</td></tr>
                        ) : (
                            approvedConsultants.map(consultant => (
                                <tr key={consultant.id} style={{ backgroundColor: '#2d3748' }}>
                                    <td style={{ padding: '12px', border: '1px solid #4a5568', color: '#ffffff' }}>{consultant.id}</td>
                                    <td style={{ padding: '12px', border: '1px solid #4a5568', color: '#ffffff' }}>{consultant.name}</td>
                                    <td style={{ padding: '12px', border: '1px solid #4a5568', color: '#ffffff' }}>{consultant.email}</td>
                                    <td style={{ padding: '12px', border: '1px solid #4a5568', color: '#ffffff' }}>{consultant.specialty}</td>
                                    <td style={{ padding: '12px', border: '1px solid #dee2e6', color: '#28a745', fontWeight: 'bold' }}>Approved</td>
                                </tr>
                            ))
                        )}
                    </tbody>
                </table>
            </section>

            <section>
                <h2>2. Define System Policies (UC12)</h2>
                <p>Configure system-wide policies for pricing, cancellations, and notifications.</p>
                <form onSubmit={handlePolicySubmit}>
                    <h3>Pricing Strategy</h3>
                    <p>
                        <label>Pricing Strategy:</label><br />
                        <select
                            value={pricingStrategy}
                            onChange={(e) => setPricingStrategy(e.target.value)}
                        >
                            <option value="BasePrice">Base Price (No Tax)</option>
                            <option value="TaxedPrice">Taxed Price (Adds tax)</option>
                        </select>
                    </p>
                    <p>
                        <label>Tax Rate (as decimal, e.g., 0.13 for 13%):</label><br />
                        <input
                            type="number"
                            step="0.01"
                            min="0"
                            value={taxRate}
                            onChange={(e) => setTaxRate(e.target.value)}
                        />
                    </p>

                    <h3>Cancellation Policy</h3>
                    <p>
                        <label>Cancellation Policy:</label><br />
                        <select
                            value={cancellationPolicy}
                            onChange={(e) => setCancellationPolicy(e.target.value)}
                        >
                            <option value="Flexible">Flexible (Full refund up to 24h)</option>
                            <option value="Strict">Strict (50% refund up to 48h)</option>
                            <option value="NoRefund">No Refund (All sales final)</option>
                        </select>
                    </p>
                    <p>
                        <label>Cancellation Fee ($):</label><br />
                        <input
                            type="number"
                            step="0.01"
                            min="0"
                            value={cancellationFee}
                            onChange={(e) => setCancellationFee(e.target.value)}
                        />
                    </p>

                    <h3>Notification & Refund Settings</h3>
                    <p>
                        <label>
                            <input
                                type="checkbox"
                                checked={notificationsEnabled}
                                onChange={(e) => setNotificationsEnabled(e.target.checked)}
                            />
                            Enable Notifications
                        </label>
                    </p>
                    <p>
                        <label>
                            <input
                                type="checkbox"
                                checked={refundsEnabled}
                                onChange={(e) => setRefundsEnabled(e.target.checked)}
                            />
                            Enable Refunds
                        </label>
                    </p>

                    <button type="submit">Update Policies</button>
                </form>
            </section>
        </main>
    );
}
