import React, {useState, useEffect} from 'react';
import {BrowserRouter as Router, Navigate, Route, Routes, useNavigate} from 'react-router-dom';
import Header from './components/Header';
import Sidebar from './components/Sidebar';
import AdminLogin from './pages/AdminLogin';
import AdminSignUpPage from './pages/AdminSignUpPage';
import PrivacyConsentPage from './pages/PrivacyConsentPage';
import DashboardPage from './pages/DashboardPage';
import MonitoringPage from './pages/MonitoringPage';
import WorkerDetailPage from './pages/WorkerDetailPage';
import WorkerManagementPage from './pages/WorkerManagementPage';
import BlueprintPage from './pages/BlueprintPage';
import SettingsPage from './pages/SettingsPage';
import './App.css';
import RiskZonePage from "./pages/RiskZonePage";
import ReportPage from "./pages/ReportPage";
import { NotificationToastContainer } from './components/notifications';

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
                <div className="main-content">
                    {children}
                </div>
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
    // 토스트 알림 상태 관리
    const [toasts, setToasts] = useState([]);

    // 커스텀 이벤트 리스너 등록 (Header에서 발생시키는 토스트 이벤트)
    useEffect(() => {
        const handleShowToast = (event) => {
            const notification = event.detail;
            setToasts(prev => [...prev, notification]);
        };

        window.addEventListener('showNotificationToast', handleShowToast);
        
        return () => {
            window.removeEventListener('showNotificationToast', handleShowToast);
        };
    }, []);

    // 토스트 제거 핸들러
    const handleRemoveToast = (toastId) => {
        setToasts(prev => prev.filter(toast => toast.id !== toastId));
    };

    return (
        <Router>
            {/* 토스트 컨테이너 */}
            <NotificationToastContainer 
                toasts={toasts} 
                onRemoveToast={handleRemoveToast} 
            />
            
            <Routes>
                {/* 관리자 화면 */}
                <Route path="/" element={<Navigate to="/admin/login"/>}/>
                <Route path="/admin/login" element={<AdminLoginPage/>}/>
                <Route path="/admin/signup" element={<AdminSignUpPage/>}/>
                <Route path="/admin/privacy-consent" element={<PrivacyConsentPage/>}/>

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

                {/* 실시간 모니터링 */}
                <Route path="/admin/monitoring" element={
                    <CommonLayout currentPage="monitoring">
                        <MonitoringPage/>
                    </CommonLayout>
                }/>

                {/* 위험구역 관리 */}
                <Route path="/admin/risk" element={
                    <CommonLayout currentPage="risk">
                        <RiskZonePage/>
                    </CommonLayout>
                }/>

                {/* 보고서 */}
                <Route path="/admin/report" element={
                    <CommonLayout currentPage="report">
                        <ReportPage/>
                    </CommonLayout>
                }/>

                {/* 설정 */}
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