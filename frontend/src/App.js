import React, {useState} from 'react';
import {BrowserRouter as Router, Navigate, Route, Routes, useNavigate} from 'react-router-dom';
import Header from './components/Header';
import Sidebar from './components/Sidebar';
import MainContent from './components/MainContent';
import AdminLogin from './pages/AdminLogin';
import './App.css';

const AdminLayout = () => {
    const [activeItem, setActiveItem] = useState('dashboard');

    return (
        <div className="app-container">
            <Header/>
            <div className="layout-container">
                <Sidebar activeItem={activeItem} setActiveItem={setActiveItem}/>
                <MainContent activeItem={activeItem}/>
            </div>
        </div>
    );
};


const AdminLoginPage = () => {
    const navigate = useNavigate();

    const handleLogin = () => {
        navigate('/admin/dashboard');
    };

    return <AdminLogin onLogin={handleLogin}/>;
};


const App = () => {
    return (
        <Router>
            <Routes>
                {/* 관리자 화면 */}
                <Route path="/" element={<Navigate to="/admin/login"/>}/>
                <Route path="/admin/login" element={<AdminLoginPage/>}/>
                <Route path="/admin/dashboard" element={<AdminLayout/>}/>
            </Routes>
        </Router>
    );
};

export default App;