import { useCallback } from 'react';

// 알림 데이터 관련 로직을 처리하는 Custom Hook
export const useAlarmData = () => {
    // 알림 타입 결정 함수
    const getAlertTypeFromData = useCallback((incidentType, description) => {
        const normalizedType = incidentType?.replace(/[ -]+/g, '_').toUpperCase() || '';
        
        if (['PPE_VIOLATION', 'DANGER_ZONE', 'HEALTH_RISK'].includes(normalizedType)) {
            return normalizedType;
        }
        
        const lowerDescription = description?.toLowerCase() || '';
        
        if (lowerDescription.includes('보호구') || lowerDescription.includes('미착용')) {
            return 'PPE_VIOLATION';
        } else if (lowerDescription.includes('위험구역') || lowerDescription.includes('위험')) {
            return 'DANGER_ZONE';
        } else if (lowerDescription.includes('건강') || lowerDescription.includes('심박수')) {
            return 'HEALTH_RISK';
        }
        
        return 'PPE_VIOLATION';
    }, []);

    // 알림 타입을 대시보드 타입으로 변환
    const convertToDashboardType = useCallback((alertType) => {
        switch (alertType) {
            case 'PPE_VIOLATION':
                return 'warning'; // 노란색/주황 - 보호구 미착용
            case 'DANGER_ZONE':
                return 'danger';  // 빨간색 - 위험구역 접근
            case 'HEALTH_RISK':
                return 'health';  // 파란색 - 건강 위험
            default:
                return 'warning';
        }
    }, []);

    // 알림 제목 생성
    const getAlertTitle = useCallback((alertType, description) => {
        switch (alertType) {
            case 'PPE_VIOLATION':
                return '보호구 미착용';
            case 'DANGER_ZONE':
                return '위험구역 접근 경고';
            case 'HEALTH_RISK':
                return '피로도 위험';
            default:
                return description || '안전 알림';
        }
    }, []);

    // 알림 아이콘 가져오기
    const getAlertIcon = useCallback((type) => {
        switch (type) {
            case 'warning': return '🦺';  // PPE_VIOLATION - 보호구 미착용
            case 'danger': return '⚠️';   // DANGER_ZONE - 위험구역 접근
            case 'health': return '🏥';   // HEALTH_RISK - 건강 위험
            default: return '⚠️';
        }
    }, []);

    // 시간 포맷팅 (상대시간으로 변환)
    const getTimeAgo = useCallback((timestamp) => {
        if (!timestamp) return '방금 전';
        
        const now = new Date();
        const time = new Date(timestamp);
        const diffInMinutes = Math.floor((now - time) / (1000 * 60));
        
        if (diffInMinutes < 1) return '방금 전';
        if (diffInMinutes < 60) return `${diffInMinutes}분 전`;
        
        const diffInHours = Math.floor(diffInMinutes / 60);
        if (diffInHours < 24) return `${diffInHours}시간 전`;
        
        const diffInDays = Math.floor(diffInHours / 24);
        return `${diffInDays}일 전`;
    }, []);

    // 알림 데이터 변환 (API 응답을 대시보드용 포맷으로 변환)
    const transformAlarmData = useCallback((alarm) => {
        const alertType = getAlertTypeFromData(alarm.incidentType, alarm.incidentDescription);
        const dashboardType = convertToDashboardType(alertType);
        
        return {
            id: alarm.id,
            type: dashboardType,
            title: getAlertTitle(alertType, alarm.incidentDescription),
            description: alarm.incidentDescription || '알림 내용',
            time: getTimeAgo(alarm.createdAt),
            timestamp: alarm.createdAt,
            workerId: alarm.workerId,
            originalData: alarm
        };
    }, [getAlertTypeFromData, convertToDashboardType, getAlertTitle, getTimeAgo]);

    return {
        getAlertTypeFromData,
        convertToDashboardType,
        getAlertTitle,
        getAlertIcon,
        getTimeAgo,
        transformAlarmData
    };
};