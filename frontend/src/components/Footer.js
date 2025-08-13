import React from 'react';

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
                        <button 
                            type="button"
                            style={{
                                color: '#6b7280', 
                                textDecoration: 'none',
                                background: 'none',
                                border: 'none',
                                cursor: 'pointer',
                                fontSize: '14px'
                            }}
                            onMouseEnter={(e) => e.target.style.color = '#374151'}
                            onMouseLeave={(e) => e.target.style.color = '#6b7280'}
                        >
                            가이드
                        </button>
                        <button 
                            type="button"
                            style={{
                                color: '#6b7280', 
                                textDecoration: 'none',
                                background: 'none',
                                border: 'none',
                                cursor: 'pointer',
                                fontSize: '14px'
                            }}
                            onMouseEnter={(e) => e.target.style.color = '#374151'}
                            onMouseLeave={(e) => e.target.style.color = '#6b7280'}
                        >
                            개인정보
                        </button>
                        <button 
                            type="button"
                            style={{
                                color: '#6b7280', 
                                textDecoration: 'none',
                                background: 'none',
                                border: 'none',
                                cursor: 'pointer',
                                fontSize: '14px'
                            }}
                            onMouseEnter={(e) => e.target.style.color = '#374151'}
                            onMouseLeave={(e) => e.target.style.color = '#6b7280'}
                        >
                            문의
                        </button>
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