import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';

export default function ClientLogin() {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const navigate = useNavigate();
    const API_URL = import.meta.env.VITE_BACKEND_URL;

    const handleLogin = async (e) => {
        e.preventDefault();
        try {
            const response = await axios.post(`${API_URL}/auth/client/login`, {
                username,
                password
            });
            
            // Store client info in localStorage
            localStorage.setItem('clientId', response.data.clientId);
            localStorage.setItem('clientName', response.data.name);
            localStorage.setItem('clientEmail', response.data.email);
            localStorage.setItem('userRole', 'client');
            
            alert('Login successful!');
            navigate('/client');
        } catch (error) {
            alert('Login failed: ' + (error.response?.data?.error || error.message));
        }
    };

    return (
        <main>
            <nav>
                <button onClick={() => navigate('/')} style={{ background: 'none', border: 'none', color: '#007bff', cursor: 'pointer', textDecoration: 'underline' }}>
                    ← Back to Home
                </button>
            </nav>

            <h1>Client Login</h1>
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

            <p>Don't have an account? <button onClick={() => navigate('/client-register')} style={{ background: 'none', border: 'none', color: '#007bff', cursor: 'pointer', textDecoration: 'underline' }}>Register here</button></p>
        </main>
    );
}
