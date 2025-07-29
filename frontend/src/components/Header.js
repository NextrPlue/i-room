import {Bell, User} from "lucide-react";
import React from "react";

const Header = () => {
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
        gap: '16px'
    };

    const bellButtonStyle = {
        padding: '8px',
        color: '#6b7280',
        backgroundColor: 'transparent',
        border: 'none',
        borderRadius: '8px',
        cursor: 'pointer',
        transition: 'all 0.2s'
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
                <button style={bellButtonStyle}>
                    <Bell size={20} />
                </button>
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