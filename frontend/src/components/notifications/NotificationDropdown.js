import React, {useEffect, useRef} from 'react';
import {Activity, AlertTriangle, Bell, Clock, Info, Shield, X} from 'lucide-react';

const NotificationDropdown = ({ 
    isOpen, 
    onClose, 
    notifications = [],
    onMarkAllAsRead 
}) => {
    const dropdownRef = useRef(null);

    // 외부 클릭 시 드롭다운 닫기
    useEffect(() => {
        const handleClickOutside = (event) => {
            if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
                onClose();
            }
        };

        if (isOpen) {
            document.addEventListener('mousedown', handleClickOutside);
        }

        return () => {
            document.removeEventListener('mousedown', handleClickOutside);
        };
    }, [isOpen, onClose]);

    if (!isOpen) return null;

    const dropdownStyle = {
        position: 'absolute',
        top: '100%',
        right: '0',
        width: '380px',
        maxHeight: '500px',
        backgroundColor: 'white',
        border: '1px solid #e5e7eb',
        borderRadius: '8px',
        boxShadow: '0 10px 25px rgba(0, 0, 0, 0.1)',
        zIndex: 1000,
        overflow: 'hidden'
    };

    const headerStyle = {
        padding: '16px 20px',
        borderBottom: '1px solid #f3f4f6',
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        backgroundColor: '#fafafa'
    };

    const titleStyle = {
        fontSize: '16px',
        fontWeight: '600',
        color: '#1f2937'
    };

    const closeButtonStyle = {
        padding: '4px',
        backgroundColor: 'transparent',
        border: 'none',
        borderRadius: '4px',
        cursor: 'pointer',
        color: '#6b7280'
    };

    const contentStyle = {
        maxHeight: '400px',
        overflowY: 'auto'
    };

    const emptyStyle = {
        padding: '40px 20px',
        textAlign: 'center',
        color: '#9ca3af',
        fontSize: '14px'
    };

    const footerStyle = {
        padding: '12px 20px',
        borderTop: '1px solid #f3f4f6',
        backgroundColor: '#fafafa',
        textAlign: 'center'
    };

    const markAllButtonStyle = {
        fontSize: '12px',
        color: '#3b82f6',
        backgroundColor: 'transparent',
        border: 'none',
        cursor: 'pointer',
        padding: '4px 8px',
        borderRadius: '4px'
    };

    const getNotificationIcon = (type) => {
        const iconProps = { size: 16 };
        switch (type) {
            case 'SAFETY_GEAR':
            case 'PPE_VIOLATION':
                return <Shield {...iconProps} color="#ef4444" />;
            case 'DANGER_ZONE':
                return <AlertTriangle {...iconProps} color="#f59e0b" />;
            case 'HEALTH_RISK':
                return <Activity {...iconProps} color="#8b5cf6" />;
            default:
                return <Info {...iconProps} color="#3b82f6" />;
        }
    };

    const formatTime = (timestamp) => {
        const now = new Date();
        const time = new Date(timestamp);
        const diffMs = now - time;
        const diffMins = Math.floor(diffMs / (1000 * 60));
        const diffHours = Math.floor(diffMs / (1000 * 60 * 60));
        const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24));

        if (diffMins < 1) return '방금 전';
        if (diffMins < 60) return `${diffMins}분 전`;
        if (diffHours < 24) return `${diffHours}시간 전`;
        if (diffDays < 7) return `${diffDays}일 전`;
        return time.toLocaleDateString('ko-KR');
    };

    return (
        <div ref={dropdownRef} style={dropdownStyle}>
            {/* 헤더 */}
            <div style={headerStyle}>
                <h3 style={titleStyle}>알림</h3>
                <button 
                    style={closeButtonStyle}
                    onClick={onClose}
                    onMouseEnter={(e) => e.target.style.backgroundColor = '#f3f4f6'}
                    onMouseLeave={(e) => e.target.style.backgroundColor = 'transparent'}
                >
                    <X size={16} />
                </button>
            </div>

            {/* 내용 */}
            <div style={contentStyle}>
                {notifications.length === 0 ? (
                    <div style={emptyStyle}>
                        <Bell size={32} color="#d1d5db" style={{ marginBottom: '8px' }} />
                        <p>새로운 알림이 없습니다</p>
                    </div>
                ) : (
                    notifications.map((notification) => (
                        <NotificationItem
                            key={notification.id}
                            notification={notification}
                            getIcon={getNotificationIcon}
                            formatTime={formatTime}
                        />
                    ))
                )}
            </div>

            {/* 푸터 */}
            {notifications.length > 0 && (
                <div style={footerStyle}>
                    <button 
                        style={markAllButtonStyle}
                        onClick={onMarkAllAsRead}
                        onMouseEnter={(e) => e.target.style.backgroundColor = '#eff6ff'}
                        onMouseLeave={(e) => e.target.style.backgroundColor = 'transparent'}
                    >
                        모든 알림 읽음 처리
                    </button>
                </div>
            )}
        </div>
    );
};

// 개별 알림 아이템 컴포넌트
const NotificationItem = ({ notification, getIcon, formatTime }) => {
    const itemStyle = {
        padding: '16px 20px',
        borderBottom: '1px solid #f9fafb',
        backgroundColor: notification.isRead ? 'white' : '#f0f9ff',
        position: 'relative'
    };

    const contentStyle = {
        display: 'flex',
        gap: '12px',
        alignItems: 'flex-start'
    };

    const textStyle = {
        flex: 1,
        fontSize: '14px'
    };

    const titleStyle = {
        fontWeight: notification.isRead ? '500' : '600',
        color: '#1f2937',
        marginBottom: '4px',
        lineHeight: '1.4'
    };

    const messageStyle = {
        color: '#6b7280',
        marginBottom: '6px',
        lineHeight: '1.4'
    };

    const timeStyle = {
        fontSize: '12px',
        color: '#9ca3af',
        display: 'flex',
        alignItems: 'center',
        gap: '4px'
    };

    const unreadDotStyle = {
        position: 'absolute',
        top: '20px',
        right: '16px',
        width: '8px',
        height: '8px',
        backgroundColor: '#3b82f6',
        borderRadius: '50%'
    };

    return (
        <div style={itemStyle}>
            <div style={contentStyle}>
                {getIcon(notification.type)}
                <div style={textStyle}>
                    <div style={titleStyle}>{notification.title || '안전 알림'}</div>
                    <div style={messageStyle}>{notification.message}</div>
                    <div style={timeStyle}>
                        <Clock size={12} />
                        {formatTime(notification.timestamp)}
                    </div>
                </div>
            </div>
            {!notification.isRead && <div style={unreadDotStyle} />}
        </div>
    );
};

export default NotificationDropdown;