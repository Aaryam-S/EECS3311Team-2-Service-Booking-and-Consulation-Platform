import { useState } from 'react';
import axios from 'axios';

export default function Chatbot() {
    const [messages, setMessages] = useState([
        { sender: 'bot', text: 'Hello! I am the AI assistant. How can I help you today?' }
    ]);
    const [input, setInput] = useState('');
    const [isLoading, setIsLoading] = useState(false);

    const API_URL = import.meta.env.VITE_BACKEND_URL;

    const handleSend = async (e) => {
        e.preventDefault();
        if (!input.trim()) return;

        // 1. Add user's message to the chat
        const userMsg = input;
        setMessages(prev => [...prev, { sender: 'user', text: userMsg }]);
        setInput('');
        setIsLoading(true);

        try {
            // 2. Send the message to the backend
            const res = await axios.post(`${API_URL}/chat`, { message: userMsg });

            // 3. Add the bot's reply to the chat (handling a few common JSON formats)
            const botReply = res.data.reply || res.data || "I received your message!";
            setMessages(prev => [...prev, { sender: 'bot', text: botReply }]);

        } catch (error) {
            console.error("Chatbot error:", error);
            setMessages(prev => [...prev, { sender: 'bot', text: "Sorry, I'm having trouble connecting to the AI server right now." }]);
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div style={{ border: '1px solid #4a5568', padding: '1rem', borderRadius: '8px', marginTop: '2rem', backgroundColor: '#2d3748' }}>
            <h3 style={{ color: '#ffffff' }}> AI Assistant</h3>

            <div style={{ height: '300px', overflowY: 'auto', marginBottom: '1rem', padding: '10px', backgroundColor: '#2d3748', borderRadius: '4px', border: '1px solid #4a5568' }}>
                {messages.map((msg, index) => (
                    <div key={index} style={{ textAlign: msg.sender === 'user' ? 'right' : 'left', margin: '10px 0' }}>
                        <span style={{
                            display: 'inline-block',
                            padding: '8px 12px',
                            borderRadius: '15px',
                            backgroundColor: msg.sender === 'user' ? '#007bff' : '#4a5568',
                            color: '#ffffff'
                        }}>
                            {msg.text}
                        </span>
                    </div>
                ))}
                {isLoading && <div style={{ textAlign: 'left', color: '#9da5b0' }}><em>Typing...</em></div>}
            </div>

            <form onSubmit={handleSend} style={{ display: 'flex', gap: '10px' }}>
                <input
                    type="text"
                    value={input}
                    onChange={(e) => setInput(e.target.value)}
                    placeholder="Ask a question about our services..."
                    style={{ flexGrow: 1, margin: 0 }}
                />
                <button type="submit" disabled={isLoading} style={{ margin: 0 }}>
                    Send
                </button>
            </form>
        </div>
    );
}