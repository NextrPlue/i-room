import React from 'react';
import {BrowserRouter as Router, Navigate, Route, Routes, useNavigate} from 'react-router-dom';
import './WorkerApp.css';

// 근로자 레이아웃 컴포넌트
const WorkerLayout = ({ children }) => {
    return (
        <div className="worker-app-container">
            <div className="worker-layout">
                {children}
            </div>
        </div>
    );
};

const WorkerApp = () => {
    const WorkerLoginPage = () => {
        const navigate = useNavigate();

        const handleLogin = (loginData) => {
            // 로그인 처리 로직
            console.log('근로자 로그인:', loginData);
            navigate('/home');
        };

        return <WorkerLogin onLogin={handleLogin}/>;
    };

    return (
        <Router>
            <Routes>
                {/* 기본 경로는 로그인으로 */}
                <Route path="/" element={<Navigate to="/login"/>}/>
                <Route path="/login" element={<WorkerLoginPage/>}/>

                {/* 로그인 후 페이지들 */}
                <Route path="/home" element={
                    <WorkerLayout>
                        <WorkerHome/>
                    </WorkerLayout>
                }/>

                <Route path="/safety" element={
                    <WorkerLayout>
                        <WorkerSafety/>
                    </WorkerLayout>
                }/>

                <Route path="/schedule" element={
                    <WorkerLayout>
                        <WorkerSchedule/>
                    </WorkerLayout>
                }/>
            </Routes>
        </Router>
    );
};

export default WorkerApp;