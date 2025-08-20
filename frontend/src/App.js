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
import {NotificationToastContainer} from './components/notifications';
import PrivacyPolicy from "./components/PrivacyPolicy";

// 공통 레이아웃 컴포넌트
const CommonLayout = ({children, currentPage}) => {
    const [activeItem, setActiveItem] = useState(currentPage);
    const navigate = useNavigate();

    // 사이드바 클릭 시 페이지 이동 처리
    const handleSidebarClick = (item) => {
        setActiveItem(item);
        if (item === 'dashboard') {
            navigate('/dashboard');
        } else if (item === 'worker') {
            navigate('/worker');
        } else if (item === 'blueprint') {
            navigate('/blueprint');
        } else if (item === 'monitoring') {
            navigate('/monitoring');
        } else if (item === 'risk') {
            navigate('/risk');
        } else if (item === 'report') {
            navigate('/report');
        } else if (item === 'settings') {
            navigate('/settings');
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
        navigate('/dashboard');
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

    // 환경에 따라 basename 설정
    // 로컬 개발환경에서도 /admin을 지원하되, localhost:3000에서도 작동하도록 설정
    const basename = process.env.REACT_APP_MODE === 'worker' ? '/worker' : '/admin';

    return (
        <Router basename={basename}>
            {/* 토스트 컨테이너 */}
            <NotificationToastContainer
                toasts={toasts}
                onRemoveToast={handleRemoveToast}
            />

            <Routes>
                {/* 관리자 화면 */}
                <Route path="/" element={<Navigate to="/login"/>}/>
                <Route path="/login" element={<AdminLoginPage/>}/>
                <Route path="/signup" element={<AdminSignUpPage/>}/>
                <Route path="/privacy-consent" element={<PrivacyConsentPage/>}/>

                {/* 대시보드 */}
                <Route path="/dashboard" element={
                    <CommonLayout currentPage="dashboard">
                        <DashboardPage/>
                    </CommonLayout>
                }/>

                {/* 근로자 관리 */}
                <Route path="/worker" element={
                    <CommonLayout currentPage="worker">
                        <WorkerManagementPage/>
                    </CommonLayout>
                }/>
                <Route path="/worker/:workerId" element={
                    <CommonLayout currentPage="worker">
                        <WorkerDetailPage/>
                    </CommonLayout>
                }/>

                {/* 도면 관리 */}
                <Route path="/blueprint" element={
                    <CommonLayout currentPage="blueprint">
                        <BlueprintPage/>
                    </CommonLayout>
                }/>

                {/* 실시간 모니터링 */}
                <Route path="/monitoring" element={
                    <CommonLayout currentPage="monitoring">
                        <MonitoringPage/>
                    </CommonLayout>
                }/>

                {/* 위험구역 관리 */}
                <Route path="/risk" element={
                    <CommonLayout currentPage="risk">
                        <RiskZonePage/>
                    </CommonLayout>
                }/>

                {/* 보고서 */}
                <Route path="/report" element={
                    <CommonLayout currentPage="report">
                        <ReportPage/>
                    </CommonLayout>
                }/>

                {/* 설정 */}
                <Route path="/settings" element={
                    <CommonLayout currentPage="settings">
                        <SettingsPage/>
                    </CommonLayout>
                }/>

                <Route path="/privacy-policy" element={<PrivacyPolicy />} />
            </Routes>
        </Router>
    );
};

export default App;