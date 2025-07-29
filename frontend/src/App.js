import React, { useState } from 'react';
import Header from './components/Header';
import Sidebar from './components/Sidebar';
import MainContent from './components/MainContent';
import './App.css';

function App() {
    const [activeItem, setActiveItem] = useState('dashboard');

    const appStyle = {
        minHeight: '100vh',
        backgroundColor: '#f3f4f6',
        fontFamily: '-apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif'
    };

    const layoutStyle = {
        display: 'flex'
    };

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