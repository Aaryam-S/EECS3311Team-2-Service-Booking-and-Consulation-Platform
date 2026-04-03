import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';

export default function ConsultantLogin() {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const navigate = useNavigate();
    const API_URL = import.meta.env.VITE_BACKEND_URL;

    const handleLogin = async (e) => {
        e.preventDefault();
        try {
            const response = await axios.post(`${API_URL}/auth/consultant/login`, {
                username,
                password
            });
            
            // Store consultant info in localStorage
            localStorage.setItem('consultantId', response.data.consultantId);
            localStorage.setItem('consultantName', response.data.name);
            localStorage.setItem('consultantEmail', response.data.email);
            localStorage.setItem('consultantSpecialty', response.data.specialty);
            localStorage.setItem('userRole', 'consultant');
            
            alert('Login successful!');
            navigate('/consultant');
        } catch (error) {
            if (error.response?.status === 403) {
                alert('Your account is not yet approved by admin.');
            } else {
                alert('Login failed: ' + (error.response?.data?.error || error.message));
            }
        }
    };

    return (
        <main>
            <nav>
                <button onClick={() => navigate('/')} style={{ background: 'none', border: 'none', color: '#007bff', cursor: 'pointer', textDecoration: 'underline' }}>
                    ← Back to Home
                </button>
            </nav>

            <h1>Consultant Login</h1>
            <form onSubmit={handleLogin} style={{ maxWidth: '400px', margin: '20px 0' }}>
                <p>
                    <label>Username:</label><br />
                    <input
                        type="text"
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
                <button type="submit">Login</button>
            </form>

            <p>Don't have an account? <button onClick={() => navigate('/consultant-registration')} style={{ background: 'none', border: 'none', color: '#007bff', cursor: 'pointer', textDecoration: 'underline' }}>Register here</button></p>
        </main>
    );
}
