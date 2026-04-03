import { BrowserRouter, Routes, Route } from 'react-router-dom';
import Home from './pages/Home';
import ClientLogin from './pages/ClientLogin';
import ClientRegister from './pages/ClientRegister';
import ClientDashboard from './pages/ClientDashboard';
import ConsultantLogin from './pages/ConsultantLogin';
import ConsultantDashboard from './pages/ConsultantDashboard';
import ConsultantRegistration from './pages/ConsultantRegistration';
import AdminDashboard from './pages/AdminDashboard';

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/client-login" element={<ClientLogin />} />
        <Route path="/client-register" element={<ClientRegister />} />
        <Route path="/client" element={<ClientDashboard />} />
        <Route path="/consultant-login" element={<ConsultantLogin />} />
        <Route path="/consultant" element={<ConsultantDashboard />} />
        <Route path="/consultant-registration" element={<ConsultantRegistration />} />
        <Route path="/admin" element={<AdminDashboard />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;