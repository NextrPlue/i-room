// 알림 관련 컴포넌트들을 한 곳에서 export
export { default as NotificationBell } from './NotificationBell';
export { default as NotificationDropdown } from './NotificationDropdown';
export { default as NotificationToast, NotificationToastContainer } from './NotificationToast';

// 웹소켓 메시지를 알림 객체로 변환하는 간단한 유틸리티
export const createNotificationFromWebSocket = (data) => {
    const getTitle = (type) => {
        switch (type) {
            case 'PPE_VIOLATION':
                return '안전장비 미착용 감지';
            case 'DANGER_ZONE':
                return '위험구역 진입 감지';
            case 'HEALTH_RISK':
                return '건강 위험 감지';
            default:
                return '안전 알림';
        }
    };

    return {
        id: `notification-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`,
        type: data.incidentType || 'INFO',
        title: getTitle(data.incidentType),
        message: data.incidentDescription || '새로운 알림이 도착했습니다.',
        workerId: data.workerId,
        timestamp: data.occurredAt || new Date().toISOString(),
        isRead: false
    };
};