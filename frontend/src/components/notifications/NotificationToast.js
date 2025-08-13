import React, { useState, useEffect } from 'react';
import { X, AlertTriangle, Shield, Activity, Info, CheckCircle } from 'lucide-react';

const NotificationToast = ({ 
    notification, 
    onClose, 
    duration = 5000,
    position = 'top-right' 
}) => {
    const [isVisible, setIsVisible] = useState(false);
    const [isClosing, setIsClosing] = useState(false);

    useEffect(() => {
        if (notification) {
            setIsVisible(true);
            setIsClosing(false);

            // 자동 닫기 타이머
            const timer = setTimeout(() => {
                handleClose();
            }, duration);

            return () => clearTimeout(timer);
        }
    }, [notification, duration]);

    // CSS 애니메이션을 위한 스타일 주입
    useEffect(() => {
        if (!document.getElementById('toast-animations')) {
            const style = document.createElement('style');
            style.id = 'toast-animations';
            style.textContent = `
                @keyframes toast-progress {
                    from { width: 100%; }
                    to { width: 0%; }
                }
            `;
            document.head.appendChild(style);
        }
    }, []);

    const handleClose = () => {
        setIsClosing(true);
        setTimeout(() => {
            setIsVisible(false);
            onClose?.();
        }, 300); // 애니메이션 시간
    };

    if (!notification || !isVisible) return null;

    const getToastStyle = () => {
        const baseStyle = {
            position: 'fixed',
            zIndex: 9999,
            width: '400px',
            maxWidth: '90vw',
            backgroundColor: 'white',
            border: '1px solid #e5e7eb',
            borderRadius: '8px',
            boxShadow: '0 10px 25px rgba(0, 0, 0, 0.15)',
            overflow: 'hidden',
            transition: 'all 0.3s ease-in-out',
            transform: isClosing ? 'translateX(100%)' : 'translateX(0)',
            opacity: isClosing ? 0 : 1
        };

        // 위치 설정
        switch (position) {
            case 'top-right':
                return { ...baseStyle, top: '20px', right: '20px' };
            case 'top-left':
                return { ...baseStyle, top: '20px', left: '20px' };
            case 'bottom-right':
                return { ...baseStyle, bottom: '20px', right: '20px' };
            case 'bottom-left':
                return { ...baseStyle, bottom: '20px', left: '20px' };
            default:
                return { ...baseStyle, top: '20px', right: '20px' };
        }
    };

    const getTypeConfig = (type) => {
        switch (type) {
            case 'SAFETY_GEAR':
            case 'PPE_VIOLATION':
                return {
                    icon: <Shield size={20} />,
                    color: '#ef4444',
                    bgColor: '#fef2f2',
                    borderColor: '#fecaca'
                };
            case 'DANGER_ZONE':
                return {
                    icon: <AlertTriangle size={20} />,
                    color: '#f59e0b',
                    bgColor: '#fffbeb',
                    borderColor: '#fed7aa'
                };
            case 'HEALTH_RISK':
                return {
                    icon: <Activity size={20} />,
                    color: '#8b5cf6',
                    bgColor: '#f5f3ff',
                    borderColor: '#c4b5fd'
                };
            case 'SUCCESS':
                return {
                    icon: <CheckCircle size={20} />,
                    color: '#10b981',
                    bgColor: '#ecfdf5',
                    borderColor: '#a7f3d0'
                };
            default:
                return {
                    icon: <Info size={20} />,
                    color: '#3b82f6',
                    bgColor: '#eff6ff',
                    borderColor: '#bfdbfe'
                };
        }
    };

    const typeConfig = getTypeConfig(notification.type);

    const headerStyle = {
        padding: '16px 20px 12px 20px',
        backgroundColor: typeConfig.bgColor,
        borderBottom: `1px solid ${typeConfig.borderColor}`,
        display: 'flex',
        alignItems: 'center',
        gap: '12px'
    };

    const iconStyle = {
        color: typeConfig.color,
        flexShrink: 0
    };

    const titleStyle = {
        flex: 1,
        fontSize: '14px',
        fontWeight: '600',
        color: '#1f2937',
        margin: 0
    };

    const closeButtonStyle = {
        padding: '4px',
        backgroundColor: 'transparent',
        border: 'none',
        borderRadius: '4px',
        cursor: 'pointer',
        color: '#6b7280',
        flexShrink: 0
    };

    const contentStyle = {
        padding: '16px 20px'
    };

    const messageStyle = {
        fontSize: '13px',
        color: '#4b5563',
        lineHeight: '1.5',
        marginBottom: '8px'
    };

    const metaStyle = {
        fontSize: '12px',
        color: '#9ca3af',
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center'
    };

    const progressBarStyle = {
        position: 'absolute',
        bottom: 0,
        left: 0,
        height: '3px',
        backgroundColor: typeConfig.color,
        animation: `toast-progress ${duration}ms linear`,
        transformOrigin: 'left'
    };

    const formatTime = (timestamp) => {
        return new Date(timestamp).toLocaleTimeString('ko-KR', {
            hour: '2-digit',
            minute: '2-digit'
        });
    };

    return (
        <div style={getToastStyle()}>
            {/* 헤더 */}
            <div style={headerStyle}>
                <div style={iconStyle}>
                    {typeConfig.icon}
                </div>
                <h4 style={titleStyle}>
                    {notification.title || '안전 알림'}
                </h4>
                <button
                    style={closeButtonStyle}
                    onClick={handleClose}
                    onMouseEnter={(e) => e.target.style.backgroundColor = 'rgba(0,0,0,0.05)'}
                    onMouseLeave={(e) => e.target.style.backgroundColor = 'transparent'}
                >
                    <X size={16} />
                </button>
            </div>

            {/* 내용 */}
            <div style={contentStyle}>
                <div style={messageStyle}>
                    {notification.message}
                </div>
                <div style={metaStyle}>
                    <span>
                        {notification.workerId && `작업자: ${notification.workerId}`}
                    </span>
                    <span>
                        {formatTime(notification.timestamp)}
                    </span>
                </div>
            </div>

            {/* 진행바 */}
            <div style={progressBarStyle} />
        </div>
    );
};

// 여러 토스트를 관리하는 컨테이너 컴포넌트
export const NotificationToastContainer = ({ toasts, onRemoveToast }) => {
    return (
        <>
            {toasts.map((toast, index) => (
                <NotificationToast
                    key={toast.id}
                    notification={toast}
                    onClose={() => onRemoveToast(toast.id)}
                    position="top-right"
                    duration={5000}
                />
            ))}
        </>
    );
};

export default NotificationToast;