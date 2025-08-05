import React, {useState} from 'react';
import {BrowserRouter as Router, Navigate, Route, Routes, useNavigate} from 'react-router-dom';
import Header from './components/Header';
import Sidebar from './components/Sidebar';
import AdminLogin from './pages/AdminLogin';
import DashboardPage from './pages/DashboardPage';
import MonitoringPage from './pages/MonitoringPage';
import WorkerDetailPage from './pages/WorkerDetailPage';
import WorkerManagementPage from './pages/WorkerManagementPage';
import BlueprintPage from './pages/BlueprintPage';
import SettingsPage from './pages/SettingsPage';
import './App.css';
import RiskZonePage from "./pages/RiskZonePage";
import ReportPage from "./pages/ReportPage";

// 공통 레이아웃 컴포넌트
const CommonLayout = ({ children, currentPage }) => {
    const [activeItem, setActiveItem] = useState(currentPage);
    const navigate = useNavigate();

    // 사이드바 클릭 시 페이지 이동 처리
    const handleSidebarClick = (item) => {
        setActiveItem(item);
        if (item === 'dashboard') {
            navigate('/admin/dashboard');
        } else if (item === 'worker') {
            navigate('/admin/worker');
        } else if (item === 'blueprint') {
            navigate('/admin/blueprint');
        } else if (item === 'monitoring') {
            navigate('/admin/monitoring');
        } else if (item === 'risk') {
            navigate('/admin/risk');
        } else if (item === 'report') {
            navigate('/admin/report');
        } else if (item === 'settings') {
            navigate('/admin/settings');
        }
    };

    return (
        <div className="app-container">
            <Header/>
            <div className="layout-container">
                <Sidebar activeItem={activeItem} setActiveItem={handleSidebarClick}/>
                {children}
            </div>
        </div>
    );
};

// 임시 페이지 컴포넌트들
const PlaceholderPage = ({ title }) => (
    <main style={{ width: '100%', padding: '24px', backgroundColor: '#ffffff' }}>
        <div style={{ maxWidth: '1280px', margin: '0 auto' }}>
            <h2 style={{ fontSize: '24px', fontWeight: 'bold', color: '#111827', marginBottom: '24px' }}>
                {title}
            </h2>
            <div style={{
                backgroundColor: '#f9fafb',
                borderRadius: '8px',
                padding: '32px',
                minHeight: '384px',
                display: 'flex',
                flexDirection: 'column',
                alignItems: 'center',
                justifyContent: 'center',
                textAlign: 'center'
            }}>
                <p style={{ fontSize: '18px', color: '#6b7280', marginBottom: '8px' }}>
                    {title} 페이지
                </p>
                <p style={{ fontSize: '14px', color: '#9ca3af' }}>
                    선택된 메뉴의 콘텐츠가 여기에 표시됩니다.
                </p>
            </div>
        </div>
    </main>
);

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

                {/* 대시보드 */}
                <Route path="/admin/dashboard" element={
                    <CommonLayout currentPage="dashboard">
                        <DashboardPage/>
                    </CommonLayout>
                }/>

                {/* 근로자 관리 */}
                <Route path="/admin/worker" element={
                    <CommonLayout currentPage="worker">
                        <WorkerManagementPage/>
                    </CommonLayout>
                }/>
                <Route path="/admin/worker/:workerId" element={
                    <CommonLayout currentPage="worker">
                        <WorkerDetailPage/>
                    </CommonLayout>
                }/>

                {/* 도면 관리 */}
                <Route path="/admin/blueprint" element={
                    <CommonLayout currentPage="blueprint">
                        <BlueprintPage/>
                    </CommonLayout>
                }/>

                {/* 나머지 페이지들 (임시) */}
                <Route path="/admin/monitoring" element={
                    <CommonLayout currentPage="monitoring">
                        <MonitoringPage/>
                    </CommonLayout>
                }/>
                <Route path="/admin/risk" element={
                    <CommonLayout currentPage="risk">
                        <RiskZonePage/>
                    </CommonLayout>
                }/>
                <Route path="/admin/report" element={
                    <CommonLayout currentPage="report">
                        <ReportPage/>
                    </CommonLayout>
                }/>
                <Route path="/admin/settings" element={
                    <CommonLayout currentPage="settings">
                        <SettingsPage/>
                    </CommonLayout>
                }/>
            </Routes>
        </Router>
    );
};

export default App;