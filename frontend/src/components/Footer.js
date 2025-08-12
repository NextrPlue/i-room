import React from 'react';
import { Mail, Phone, MapPin, FileText, HelpCircle, Shield } from 'lucide-react';

const Footer = () => {
    const footerStyle = {
        backgroundColor: '#ffffff',
        borderTop: '1px solid #e5e7eb',
        padding: '16px 24px',
        marginTop: 'auto',
        width: '100%',
        boxSizing: 'border-box',
        overflow: 'hidden'
    };

    const containerStyle = {
        width: '100%',
        margin: '0 auto',
        maxWidth: '1200px',
        boxSizing: 'border-box'
    };

    const sectionStyle = {
        flex: '1',
        minWidth: '200px'
    };

    const titleStyle = {
        fontSize: '16px',
        fontWeight: '600',
        color: '#1f2937',
        marginBottom: '16px'
    };

    const companyTitleStyle = {
        fontSize: '20px',
        fontWeight: '700',
        color: '#3b82f6',
        marginBottom: '8px'
    };

    const descriptionStyle = {
        fontSize: '14px',
        color: '#6b7280',
        lineHeight: '1.6',
        marginBottom: '16px'
    };

    const contactItemStyle = {
        display: 'flex',
        alignItems: 'center',
        gap: '8px',
        marginBottom: '8px',
        fontSize: '14px',
        color: '#4b5563'
    };

    const linkStyle = {
        display: 'block',
        fontSize: '14px',
        color: '#6b7280',
        textDecoration: 'none',
        marginBottom: '8px',
        padding: '4px 0',
        transition: 'color 0.2s',
        cursor: 'pointer'
    };

    const bottomBarStyle = {
        borderTop: '1px solid #e5e7eb',
        marginTop: '32px',
        paddingTop: '20px',
        textAlign: 'center',
        fontSize: '12px',
        color: '#9ca3af'
    };

    const handleLinkHover = (e) => {
        e.target.style.color = '#3b82f6';
    };

    const handleLinkLeave = (e) => {
        e.target.style.color = '#6b7280';
    };

    return (
        <footer style={footerStyle}>
            <div style={containerStyle}>
                <div style={{display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '16px'}}>
                    {/* 왼쪽: 회사 정보 */}
                    <div style={{display: 'flex', alignItems: 'center', gap: '8px'}}>
                        <span style={{fontSize: '16px', fontWeight: '600', color: '#374151'}}>이룸</span>
                        <span style={{fontSize: '14px', color: '#9ca3af'}}>건설 현장 안전 모니터링</span>
                    </div>

                    {/* 가운데: 링크들 */}
                    <div style={{display: 'flex', alignItems: 'center', gap: '20px', fontSize: '14px'}}>
                        <a 
                            href="#" 
                            style={{color: '#6b7280', textDecoration: 'none'}}
                            onMouseEnter={(e) => e.target.style.color = '#374151'}
                            onMouseLeave={(e) => e.target.style.color = '#6b7280'}
                        >
                            가이드
                        </a>
                        <a 
                            href="#" 
                            style={{color: '#6b7280', textDecoration: 'none'}}
                            onMouseEnter={(e) => e.target.style.color = '#374151'}
                            onMouseLeave={(e) => e.target.style.color = '#6b7280'}
                        >
                            개인정보
                        </a>
                        <a 
                            href="#" 
                            style={{color: '#6b7280', textDecoration: 'none'}}
                            onMouseEnter={(e) => e.target.style.color = '#374151'}
                            onMouseLeave={(e) => e.target.style.color = '#6b7280'}
                        >
                            문의
                        </a>
                    </div>

                    {/* 오른쪽: 저작권 */}
                    <div style={{fontSize: '13px', color: '#9ca3af'}}>
                        © 2025 이룸. All rights reserved.
                    </div>
                </div>
            </div>
        </footer>
    );
};

export default Footer;