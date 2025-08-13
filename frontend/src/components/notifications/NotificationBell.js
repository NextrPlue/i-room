import React from 'react';
import { Bell } from 'lucide-react';

const NotificationBell = ({ 
    unreadCount = 0, 
    onClick, 
    isActive = false,
    size = 20 
}) => {
    const bellButtonStyle = {
        position: 'relative',
        padding: '8px',
        color: isActive ? '#3b82f6' : '#6b7280',
        backgroundColor: isActive ? '#eff6ff' : 'transparent',
        border: 'none',
        borderRadius: '8px',
        cursor: 'pointer',
        transition: 'all 0.2s',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center'
    };

    const badgeStyle = {
        position: 'absolute',
        top: '2px',
        right: '2px',
        backgroundColor: '#ef4444',
        color: 'white',
        fontSize: '10px',
        fontWeight: '600',
        borderRadius: '50%',
        minWidth: '16px',
        height: '16px',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        lineHeight: '1',
        border: '2px solid white',
        boxSizing: 'border-box'
    };

    const handleMouseEnter = (e) => {
        if (!isActive) {
            e.target.style.backgroundColor = '#f3f4f6';
            e.target.style.color = '#374151';
        }
    };

    const handleMouseLeave = (e) => {
        if (!isActive) {
            e.target.style.backgroundColor = 'transparent';
            e.target.style.color = '#6b7280';
        }
    };

    return (
        <button 
            style={bellButtonStyle}
            onClick={onClick}
            onMouseEnter={handleMouseEnter}
            onMouseLeave={handleMouseLeave}
            aria-label={`알림 ${unreadCount > 0 ? `(${unreadCount}개 미읽음)` : ''}`}
        >
            <Bell size={size} />
            {unreadCount > 0 && (
                <span style={badgeStyle}>
                    {unreadCount > 99 ? '99+' : unreadCount}
                </span>
            )}
        </button>
    );
};

export default NotificationBell;