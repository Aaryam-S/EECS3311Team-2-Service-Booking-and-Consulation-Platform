import { BrowserRouter, Routes, Route } from 'react-router-dom';
import Home from './pages/Home';
import ClientDashboard from './pages/ClientDashboard';
import ConsultantDashboard from './pages/ConsultantDashboard';
import AdminDashboard from './pages/AdminDashboard';

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/client" element={<ClientDashboard />} />
        <Route path="/consultant" element={<ConsultantDashboard />} />
        <Route path="/admin" element={<AdminDashboard />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;