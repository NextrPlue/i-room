import React, { useState, useEffect } from 'react';
import { BrowserRouter as Router, Navigate, Route, Routes, useNavigate } from 'react-router-dom';
import WorkerLogin from './pages/WorkerLogin';
import WorkerHome from "./pages/WorkerHome";
import stompService from './services/stompService';
import { SafetyGearAlert, DangerZoneAlert, HealthRiskAlert } from './pages/WorkerAlertPage';
import {authUtils as workerAuth, authUtils} from './utils/workerAuth';
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
    // 알람 상태
    const [safetyAlert, setSafetyAlert] = useState({ isOpen: false, data: null });
    const [dangerAlert, setDangerAlert] = useState({ isOpen: false, data: null });
    const [healthAlert, setHealthAlert] = useState({ isOpen: false, data: null });

    // 연결 상태
    const [connectionStatus, setConnectionStatus] = useState('disconnected');
    const [isLoggedIn, setIsLoggedIn] = useState(false);

    // 앱 시작 시 토큰 확인 및 WebSocket 연결
    useEffect(() => {
        const token = workerAuth.getToken();
        if (token) {
            setIsLoggedIn(true);
            connectWebSocket(token);
        }
    }, []);

    // WebSocket 연결 함수
    const connectWebSocket = async (token) => {
        try {
            // 근로자는 항상 'worker' 타입으로 연결
            await stompService.connect(token, 'worker');

            // 연결 상태 이벤트
            stompService.on('connected', () => {
                setConnectionStatus('connected');
                console.log('✅ 근로자 WebSocket 연결 성공');
            });

            stompService.on('disconnected', () => {
                setConnectionStatus('disconnected');
                console.log('❌ WebSocket 연결 끊김');
            });

            stompService.on('error', (error) => {
                console.error('WebSocket 에러:', error);
                setConnectionStatus('error');
            });

            // 보호구 미착용 알람 (PPE_VIOLATION)
            stompService.on('safety-gear-alert', (data) => {
                console.log('🦺 보호구 미착용 알람 수신:', data);
                setSafetyAlert({ isOpen: true, data });

                // 진동 알림 (모바일)
                if (navigator.vibrate) {
                    navigator.vibrate([200, 100, 200]);
                }
            });

            // 위험구역 접근 알람 (DANGER_ZONE)
            stompService.on('danger-zone-alert', (data) => {
                console.log('⚠️ 위험구역 접근 알람 수신:', data);
                setDangerAlert({ isOpen: true, data });

                if (navigator.vibrate) {
                    navigator.vibrate([500, 200, 500]);
                }
            });

            // 건강 위험 알람 (HEALTH_RISK)
            stompService.on('health-risk-alert', (data) => {
                console.log('🏥 건강 위험 알람 수신:', data);
                setHealthAlert({ isOpen: true, data });

                if (navigator.vibrate) {
                    navigator.vibrate([300, 100, 300, 100, 300]);
                }
            });

            // 전체 알람 이벤트 (디버깅용)
            stompService.on('alarm', (data) => {
                console.log('📨 알람 전체 데이터:', data);
            });

        } catch (error) {
            console.error('WebSocket 연결 실패:', error);
            setConnectionStatus('error');
        }
    };

    // 로그인 페이지 컴포넌트
    const WorkerLoginPage = () => {
        const navigate = useNavigate();

        const handleLogin = async (loginData) => {
            try {
                console.log('근로자 로그인:', loginData);

                // 로그인 API 호출은 이미 WorkerLogin 컴포넌트에서 처리됨
                // 토큰이 저장되면 WebSocket 연결
                const token = workerAuth.getToken();
                if (token) {
                    setIsLoggedIn(true);
                    await connectWebSocket(token);
                    navigate('/home');
                }
            } catch (error) {
                console.error('로그인 처리 실패:', error);
            }
        };

        return <WorkerLogin onLogin={handleLogin} />;
    };

    // 컴포넌트 언마운트 시 WebSocket 연결 해제
    useEffect(() => {
        return () => {
            if (stompService.isConnected()) {
                stompService.disconnect();
            }
        };
    }, []);

    return (
        <Router>
            <Routes>
                {/* 근로자 로그인 페이지 */}
                <Route path="/" element={<Navigate to="/login" />} />
                <Route path="/login" element={<WorkerLoginPage />} />

                {/* 근로자 홈 페이지 */}
                <Route path="/home" element={
                    <WorkerLayout>
                        <WorkerHome />
                    </WorkerLayout>
                } />
            </Routes>

            {/* 전역 알람 컴포넌트들 - 로그인 후에만 활성화 */}
            {isLoggedIn && (
                <>
                    <SafetyGearAlert
                        isOpen={safetyAlert.isOpen}
                        onClose={() => setSafetyAlert({ isOpen: false, data: null })}
                        data={safetyAlert.data}
                    />

                    <DangerZoneAlert
                        isOpen={dangerAlert.isOpen}
                        onClose={() => setDangerAlert({ isOpen: false, data: null })}
                        data={dangerAlert.data}
                    />

                    <HealthRiskAlert
                        isOpen={healthAlert.isOpen}
                        onClose={() => setHealthAlert({ isOpen: false, data: null })}
                        data={healthAlert.data}
                    />
                </>
            )}
        </Router>
    );
};

export default WorkerApp;