import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import axios from 'axios';

export default function AdminDashboard() {
    // State for Consultants
    const [pendingConsultants, setPendingConsultants] = useState([]);

    // State for System Policies
    const [pricingStrategy, setPricingStrategy] = useState('BasePrice');
    const [cancellationPolicy, setCancellationPolicy] = useState('Flexible');

    const API_URL = import.meta.env.VITE_BACKEND_URL;

    // Fetch data when the page loads
    useEffect(() => {
        fetchAdminData();
    }, []);

    const fetchAdminData = async () => {
        try {
            // Fetch pending consultants waiting for approval
            const consultantsRes = await axios.get(`${API_URL}/admin/consultants/pending`);
            setPendingConsultants(consultantsRes.data || []);

            // Fetch current system policies
            const policiesRes = await axios.get(`${API_URL}/admin/policies`);
            if (policiesRes.data) {
                setPricingStrategy(policiesRes.data.pricingStrategy || 'BasePrice');
                setCancellationPolicy(policiesRes.data.cancellationPolicy || 'Flexible');
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
                cancellationPolicy: cancellationPolicy
            });
            alert("System policies updated successfully!");
        } catch (error) {
            alert("Failed to update policies.");
            console.error(error);
        }
    };

    return (
        <main>
            <nav>
                <Link to="/">← Back to Home</Link>
            </nav>

            <h1>Admin Dashboard</h1>
            <p>Welcome, Admin. Manage system policies and approve new consultants here.</p>

            <hr />

            <section>
                <h2>1. Pending Consultant Approvals</h2>
                <table>
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Name</th>
                            <th>Email</th>
                            <th>Specialty</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        {pendingConsultants.length === 0 ? (
                            <tr><td colSpan="5">No pending consultant registrations.</td></tr>
                        ) : (
                            pendingConsultants.map(consultant => (
                                <tr key={consultant.id}>
                                    <td>{consultant.id}</td>
                                    <td>{consultant.name}</td>
                                    <td>{consultant.email}</td>
                                    <td>{consultant.specialty}</td>
                                    <td>
                                        <button onClick={() => handleConsultantAction(consultant.id, 'approve')} style={{ marginRight: '10px', backgroundColor: '#28a745', borderColor: '#28a745' }}>Approve</button>
                                        <button onClick={() => handleConsultantAction(consultant.id, 'reject')} style={{ backgroundColor: '#dc3545', borderColor: '#dc3545' }}>Reject</button>
                                    </td>
                                </tr>
                            ))
                        )}
                    </tbody>
                </table>
            </section>

            <section>
                <h2>2. Manage System Policies</h2>
                <form onSubmit={handlePolicySubmit}>
                    <p>
                        <label>Pricing Strategy:</label><br />
                        <select
                            value={pricingStrategy}
                            onChange={(e) => setPricingStrategy(e.target.value)}
                        >
                            <option value="BasePrice">Base Price (No Tax)</option>
                            <option value="TaxedPrice">Taxed Price (Adds standard tax)</option>
                        </select>
                    </p>
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
                    <button type="submit">Update Policies</button>
                </form>
            </section>

        </main>
    );
}