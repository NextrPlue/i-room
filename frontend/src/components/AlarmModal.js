import React, { useState, useEffect } from 'react';
import styles from '../styles/AlarmModal.module.css';
import { alarmAPI } from '../api/api';
import { useAlarmData } from '../hooks/useAlarmData';

const AlarmModal = ({ isOpen, onClose }) => {
    const [alarms, setAlarms] = useState([]);
    const [loading, setLoading] = useState(false);
    const [pagination, setPagination] = useState({
        page: 0,
        size: 10,
        hours: 168,
        totalPages: 0,
        totalElements: 0
    });
    
    const { getAlertIcon, transformAlarmData } = useAlarmData();


    // 알림 목록 로드
    const loadAlarms = async (page = 0, hours = pagination.hours) => {
        setLoading(true);
        try {
            const response = await alarmAPI.getAlarmsForAdmin({
                page: page,
                size: pagination.size,
                hours: hours
            });

            const apiAlarms = response.data?.content?.map(transformAlarmData) || [];

            setAlarms(apiAlarms);
            setPagination(prev => ({
                ...prev,
                page: page,
                size: prev.size,
                hours: hours,
                totalPages: response.data?.totalPages || 0,
                totalElements: response.data?.totalElements || 0
            }));
        } catch (error) {
            console.error('❌ 알람 목록 로드 실패:', error);
        } finally {
            setLoading(false);
        }
    };

    // 모달이 열릴 때 데이터 로드
    useEffect(() => {
        if (isOpen) {
            loadAlarms(0).catch(console.error);
        }
    }, [isOpen]);

    // 페이지 변경
    const handlePageChange = (newPage) => {
        if (newPage >= 0 && newPage < pagination.totalPages) {
            loadAlarms(newPage).catch(console.error);
        }
    };

    // 모달이 닫혀있으면 렌더링하지 않음
    if (!isOpen) return null;

    return (
        <div className={styles.modalOverlay} onClick={onClose}>
            <div className={styles.modalContent} onClick={(e) => e.stopPropagation()}>
                {/* 모달 헤더 */}
                <div className={styles.modalHeader}>
                    <div className={styles.headerLeft}>
                        <h2 className={styles.modalTitle}>실시간 위험 알림</h2>
                        <select 
                            className={styles.hoursSelect}
                            value={pagination.hours}
                            onChange={(e) => {
                                const newHours = parseInt(e.target.value);
                                setPagination(prev => ({
                                    ...prev,
                                    page: 0,
                                    size: prev.size,
                                    hours: newHours,
                                    totalPages: prev.totalPages,
                                    totalElements: prev.totalElements
                                }));
                                loadAlarms(0, newHours).catch(console.error);
                            }}
                        >
                            <option value={1}>최근 1시간</option>
                            <option value={3}>최근 3시간</option>
                            <option value={6}>최근 6시간</option>
                            <option value={12}>최근 12시간</option>
                            <option value={24}>최근 24시간</option>
                            <option value={72}>최근 3일</option>
                            <option value={168}>최근 7일</option>
                        </select>
                    </div>
                    <button className={styles.closeButton} onClick={onClose}>
                        ×
                    </button>
                </div>

                {/* 알림 목록 */}
                <div className={styles.modalBody}>
                    {loading ? (
                        <div className={styles.loadingState}>
                            📡 알림 목록을 불러오는 중...
                        </div>
                    ) : alarms.length > 0 ? (
                        <>
                            <div className={styles.alarmList}>
                                {alarms.map(alarm => (
                                    <div key={alarm.id} className={`${styles.alarmItem} ${styles[alarm.type]}`}>
                                        <div className={`${styles.alarmIcon} ${styles[alarm.type]}`}>
                                            {getAlertIcon(alarm.type)}
                                        </div>
                                        <div className={styles.alarmContent}>
                                            <p className={styles.alarmTitle}>{alarm.title}</p>
                                            <p className={styles.alarmDesc}>{alarm.description}</p>
                                            <p className={styles.alarmMeta}>작업자: {alarm.workerName || "알 수 없음"}</p>
                                        </div>
                                        <span className={styles.alarmTime}>{alarm.time}</span>
                                    </div>
                                ))}
                            </div>

                            {/* 페이지네이션 */}
                            <div className={styles.pagination}>
                                <button 
                                    className={styles.pageButton}
                                    disabled={pagination.page === 0}
                                    onClick={() => handlePageChange(pagination.page - 1)}
                                >
                                    이전
                                </button>
                                
                                <span className={styles.pageInfo}>
                                    {pagination.page + 1} / {pagination.totalPages} 페이지
                                    (총 {pagination.totalElements}건)
                                </span>
                                
                                <button 
                                    className={styles.pageButton}
                                    disabled={pagination.page >= pagination.totalPages - 1}
                                    onClick={() => handlePageChange(pagination.page + 1)}
                                >
                                    다음
                                </button>
                            </div>
                        </>
                    ) : (
                        <div className={styles.emptyState}>
                            📋 최근 {pagination.hours}시간 내 알림이 없습니다.
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
};

export default AlarmModal;