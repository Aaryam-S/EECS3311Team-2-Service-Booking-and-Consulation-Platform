import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';

export default function ConsultantRegistration() {
    const [name, setName] = useState('');
    const [email, setEmail] = useState('');
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [specialty, setSpecialty] = useState('');
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();

    const API_URL = import.meta.env.VITE_BACKEND_URL;

    const handleSubmit = async (e) => {
        e.preventDefault();

        if (password !== confirmPassword) {
            alert('Passwords do not match.');
            return;
        }

        setLoading(true);

        try {
            const response = await axios.post(`${API_URL}/auth/consultant/register`, {
                name,
                email,
                username,
                password,
                specialty
            });
            alert("Registration submitted successfully! Awaiting admin approval.");
            navigate('/consultant-login');
        } catch (error) {
            alert("Registration failed: " + (error.response?.data?.error || error.message));
            console.error(error);
        } finally {
            setLoading(false);
        }
    };

    return (
        <main>
            <nav>
                <button onClick={() => navigate('/')} style={{ background: 'none', border: 'none', color: '#007bff', cursor: 'pointer', textDecoration: 'underline' }}>
                    ← Back to Home
                </button>
            </nav>

            <h1>Consultant Registration</h1>
            <p>Register as a consultant to offer services on our platform.</p>

            <hr />

            <section>
                <h2>Register as Consultant</h2>
                <p>Once you submit your registration, an admin will review and approve your application.</p>
                
                <form onSubmit={handleSubmit} style={{ maxWidth: '400px', margin: '20px 0' }}>
                    <p>
                        <label>Full Name:</label><br />
                        <input
                            type="text"
                            placeholder="John Doe"
                            value={name}
                            onChange={(e) => setName(e.target.value)}
                            required
                        />
                    </p>

                    <p>
                        <label>Email:</label><br />
                        <input
                            type="email"
                            placeholder="john@example.com"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            required
                        />
                    </p>

                    <p>
                        <label>Username:</label><br />
                        <input
                            type="text"
                            placeholder="johndoe"
                            value={username}
                            onChange={(e) => setUsername(e.target.value)}
                            required
                        />
                    </p>

                    <p>
                        <label>Password:</label><br />
                        <input
                            type="password"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            required
                        />
                    </p>

                    <p>
                        <label>Confirm Password:</label><br />
                        <input
                            type="password"
                            value={confirmPassword}
                            onChange={(e) => setConfirmPassword(e.target.value)}
                            required
                        />
                    </p>

                    <p>
                        <label>Area of Expertise/Specialty:</label><br />
                        <input
                            type="text"
                            placeholder="e.g., Software Development, Career Coaching, etc."
                            value={specialty}
                            onChange={(e) => setSpecialty(e.target.value)}
                            required
                        />
                    </p>

                    <button type="submit" disabled={loading}>
                        {loading ? 'Submitting...' : 'Submit Registration'}
                    </button>
                </form>

                <p>Already have an account? <button onClick={() => navigate('/consultant-login')} style={{ background: 'none', border: 'none', color: '#007bff', cursor: 'pointer', textDecoration: 'underline' }}>Login here</button></p>
            </section>
        </main>
    );
}
