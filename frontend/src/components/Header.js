import { User } from "lucide-react";
import React, { useState, useEffect } from "react";
import { NotificationBell, NotificationDropdown, createNotificationFromWebSocket } from './notifications';
import stompService from '../services/stompService';
import { authUtils } from '../utils/auth';

const Header = () => {
    // 알림 상태 관리
    const [notifications, setNotifications] = useState([]);
    const [isDropdownOpen, setIsDropdownOpen] = useState(false);

    // 웹소켓 연결 및 이벤트 처리
    useEffect(() => {
        const token = authUtils.getToken();
        if (!token) return;

        // 웹소켓 연결
        const connectWebSocket = async () => {
            try {
                await stompService.connect(token, 'admin');
            } catch (error) {
            }
        };

        // 알림 이벤트 리스너 등록
        const handleNewAlarm = (data) => {
            const newNotification = createNotificationFromWebSocket(data);
            
            setNotifications(prev => [newNotification, ...prev.slice(0, 49)]); // 최대 50개 유지
            
            // 토스트 알림 표시
            window.dispatchEvent(new CustomEvent('showNotificationToast', { 
                detail: newNotification 
            }));
        };

        // 이벤트 리스너 등록
        stompService.on('alarm', handleNewAlarm);

        // 웹소켓 연결
        if (!stompService.isConnected()) {
            connectWebSocket().catch(console.error);
        }

        return () => {
            stompService.off('alarm', handleNewAlarm);
        };
    }, []);

    // 알림 관련 핸들러들
    const handleBellClick = () => {
        setIsDropdownOpen(!isDropdownOpen);
    };

    const handleDropdownClose = () => {
        setIsDropdownOpen(false);
    };

    const handleMarkAllAsRead = () => {
        setNotifications(prev => 
            prev.map(notification => ({ ...notification, isRead: true }))
        );
    };

    // 미읽음 알림 개수 계산
    const unreadCount = notifications.filter(n => !n.isRead).length;

    const headerStyle = {
        backgroundColor: '#ffffff',
        borderBottom: '1px solid #e5e7eb',
        height: '64px',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        padding: '0 24px',
        boxShadow: '0 1px 3px 0 rgba(0, 0, 0, 0.1)'
    };

    const titleStyle = {
        fontSize: '20px',
        fontWeight: '600',
        color: '#1f2937'
    };

    const rightSectionStyle = {
        display: 'flex',
        alignItems: 'center',
        gap: '16px',
        position: 'relative' // 드롭다운 위치 기준점
    };

    const userSectionStyle = {
        display: 'flex',
        alignItems: 'center',
        gap: '8px'
    };

    const avatarStyle = {
        width: '32px',
        height: '32px',
        backgroundColor: '#3b82f6',
        borderRadius: '50%',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center'
    };

    const usernameStyle = {
        fontSize: '14px',
        color: '#374151'
    };


    return (
        <header style={headerStyle}>
            <div>
                <h1 style={titleStyle}>이룸</h1>
            </div>
            <div style={rightSectionStyle}>
                {/* 알림 벨 */}
                <NotificationBell
                    unreadCount={unreadCount}
                    onClick={handleBellClick}
                    isActive={isDropdownOpen}
                />
                
                {/* 알림 드롭다운 */}
                <NotificationDropdown
                    isOpen={isDropdownOpen}
                    onClose={handleDropdownClose}
                    notifications={notifications}
                    onMarkAllAsRead={handleMarkAllAsRead}
                />

                {/* 사용자 정보 */}
                <div style={userSectionStyle}>
                    <div style={avatarStyle}>
                        <User size={16} color="white" />
                    </div>
                    <span style={usernameStyle}>관리자님</span>
                </div>
            </div>
        </header>
    );
};

export default Header;