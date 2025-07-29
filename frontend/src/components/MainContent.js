import React from "react";

const MainContent = ({ activeItem }) => {
  const mainStyle = {
    flex: '1',
    padding: '24px',
    backgroundColor: '#ffffff'
  };

  const containerStyle = {
    maxWidth: '1280px',
    margin: '0 auto'
  };

  const titleStyle = {
    fontSize: '24px',
    fontWeight: 'bold',
    color: '#111827',
    marginBottom: '24px'
  };

  const contentAreaStyle = {
    backgroundColor: '#f9fafb',
    borderRadius: '8px',
    padding: '32px',
    minHeight: '384px',
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'center',
    justifyContent: 'center',
    textAlign: 'center'
  };

  const contentTitleStyle = {
    fontSize: '18px',
    color: '#6b7280',
    marginBottom: '8px'
  };

  const contentDescStyle = {
    fontSize: '14px',
    color: '#9ca3af'
  };

  const getContentTitle = () => {
    const titles = {
      dashboard: '대시보드',
      monitoring: '실시간 모니터링',
      worker: '근로자 관리',
      blueprint: '도면 관리',
      risk: '위험구역 관리',
      report: '리포트',
      settings: '설정'
    };
    return titles[activeItem] || '대시보드';
  };

  return (
      <main style={mainStyle}>
        <div style={containerStyle}>
          <h2 style={titleStyle}>{getContentTitle()}</h2>

          <div style={contentAreaStyle}>
            <p style={contentTitleStyle}>{getContentTitle()} 페이지</p>
            <p style={contentDescStyle}>선택된 메뉴의 콘텐츠가 여기에 표시됩니다.</p>
          </div>
        </div>
      </main>
  );
};

export default MainContent;