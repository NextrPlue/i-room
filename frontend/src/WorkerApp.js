import React from 'react';
import {BrowserRouter as Router, Navigate, Route, Routes, useNavigate} from 'react-router-dom';
import WorkerLogin from './pages/WorkerLogin';
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
                {/* 근로자 로그인 페이지 */}
                <Route path="/" element={<Navigate to="/login"/>}/>
                <Route path="/login" element={<WorkerLoginPage/>}/>

                {/* 근로자 상세 페이지 */}
                <Route path="/home" element={
                    <WorkerLayout>
                        <WorkerHome/>
                    </WorkerLayout>
                }/>
            </Routes>
        </Router>
    );
};

export default WorkerApp;