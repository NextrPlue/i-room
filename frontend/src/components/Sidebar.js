import {Activity, AlertTriangle, BarChart3, FileText, Home, HardHat, Settings} from "lucide-react";
import React from "react";

const Sidebar = ({ activeItem, setActiveItem }) => {
  const sidebarStyle = {
    backgroundColor: '#f9fafb',
    width: '256px',
    minHeight: '100vh',
    borderRight: '1px solid #e5e7eb'
  };

  const navStyle = {
    padding: '16px'
  };

  const navListStyle = {
    display: 'flex',
    flexDirection: 'column',
    gap: '4px'
  };

  const getMenuItemStyle = (isActive) => ({
    width: '100%',
    display: 'flex',
    alignItems: 'center',
    padding: '10px 12px',
    fontSize: '14px',
    fontWeight: '500',
    borderRadius: '8px',
    border: 'none',
    cursor: 'pointer',
    transition: 'all 0.2s',
    backgroundColor: isActive ? '#dbeafe' : 'transparent',
    color: isActive ? '#1d4ed8' : '#374151',
    borderRight: isActive ? '2px solid #3b82f6' : 'none'
  });

  const iconStyle = (isActive) => ({
    marginRight: '12px',
    color: isActive ? '#2563eb' : '#6b7280'
  });

  const menuItems = [
    { id: 'dashboard', name: '대시보드', icon: Home },
    { id: 'monitoring', name: '실시간 모니터링', icon: Activity },
    { id: 'worker', name: '근로자 관리', icon: HardHat },
    { id: 'blueprint', name: '도면 관리', icon: FileText },
    { id: 'risk', name: '위험구역 관리', icon: AlertTriangle },
    { id: 'report', name: '리포트', icon: BarChart3 },
    { id: 'settings', name: '설정', icon: Settings }
  ];

  return (
      <aside style={sidebarStyle}>
        <div style={navStyle}>
          <nav style={navListStyle}>
            {menuItems.map((item) => {
              const Icon = item.icon;
              const isActive = activeItem === item.id;

              return (
                  <button
                      key={item.id}
                      onClick={() => setActiveItem(item.id)}
                      style={getMenuItemStyle(isActive)}
                      onMouseEnter={(e) => {
                        if (!isActive) {
                          e.target.style.backgroundColor = '#f3f4f6';
                          e.target.style.color = '#111827';
                        }
                      }}
                      onMouseLeave={(e) => {
                        if (!isActive) {
                          e.target.style.backgroundColor = 'transparent';
                          e.target.style.color = '#374151';
                        }
                      }}
                  >
                    <Icon
                        size={18}
                        style={iconStyle(isActive)}
                    />
                    {item.name}
                  </button>
              );
            })}
          </nav>
        </div>
      </aside>
  );
};

export default Sidebar;