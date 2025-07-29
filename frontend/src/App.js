import React, { useState } from 'react';
import Header from './components/Header';
import Sidebar from './components/Sidebar';
import MainContent from './components/MainContent';
import './App.css';
import AdminLogin from "./pages/AdminLogin";

function App() {
    const [activeItem, setActiveItem] = useState('dashboard');
    const [showLogin, setShowLogin] = useState(true);

    const appStyle = {
        minHeight: '100vh',
        backgroundColor: '#f3f4f6',
        fontFamily: '-apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif'
    };

    const layoutStyle = {
        display: 'flex'
    };

    if(showLogin){
        return <AdminLogin onLogin={() => setShowLogin(false)} />;
    }

    return (
        <div style={appStyle}>
            <Header />
            <div style={layoutStyle}>
                <Sidebar activeItem={activeItem} setActiveItem={setActiveItem} />
                <MainContent activeItem={activeItem} />
            </div>
        </div>
    );
}

export default App;