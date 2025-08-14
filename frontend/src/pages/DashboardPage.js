import React, {useEffect, useState} from 'react';
import styles from '../styles/Dashboard.module.css';
import stompService from '../services/stompService';
import {authUtils} from '../utils/auth';
import {alarmAPI} from '../api/api';
import AlarmModal from '../components/AlarmModal';
import {useAlarmData} from '../hooks/useAlarmData';

const DashboardPage = () => {
    const { getAlertIcon, getAlertTypeFromData, convertToDashboardType, getAlertTitle, getTimeAgo, transformAlarmData } = useAlarmData();
    
    // 상태 표시 컴포넌트 (중복 코드 제거용)
    const StatusDisplay = ({ icon, label, value, details, classPrefix }) => (
        <>
            <div className={styles[`${classPrefix}Content`]}>
                <div className={styles[`${classPrefix}Icon`]}>{icon}</div>
                <div className={styles[`${classPrefix}Text`]}>
                    <p className={styles[`${classPrefix}Label`]}>{label}</p>
                    <p className={styles[`${classPrefix}Value`]}>{value}</p>
                </div>
            </div>
            <p className={styles[`${classPrefix}Details`]}>
                {details}
            </p>
        </>
    );

    // 종합 안전 점수
    const [safetyScore] = useState(85);

    // 실시간 위험 알림 데이터 (API + 웹소켓)
    const [alerts, setAlerts] = useState([]);
    const [alertsLoading, setAlertsLoading] = useState(true);
    const [isAlarmModalOpen, setIsAlarmModalOpen] = useState(false);
    const alertsPagination = {
        page: 0,
        size: 4, // 대시보드에는 최근 4개만 표시
        hours: 168 // 최근 7일 (168시간)로 범위 확대
    };

    // 주요 안전 지표 데이터
    const [indicators] = useState([
        {
            id: 1,
            type: 'warning',
            title: '보호구 미착용 작업자 수',
            value: '2건',
            icon: '⚠️'
        },
        {
            id: 2,
            type: 'warning',
            title: '작업 안전 경고',
            value: '1건',
            icon: '⚠️'
        },
        {
            id: 3,
            type: 'danger',
            title: '직원 이상 교원 수',
            value: '3명',
            icon: '😷'
        }
    ]);

    // 현장 현황 데이터
    const [fieldStatus] = useState({
        currentWorkers: 24,
        totalCapacity: 18,
        dangerWorkers: 4,
        normalWorkers: 2
    });


    // 긴급 이상 탐지
    const [emergencyStatus] = useState({
        count: 1,
        location: '박영희 - 심박수 이상'
    });

    // 도넛 차트 계산
    const circumference = 2 * Math.PI * 90; // 반지름 90
    const strokeDasharray = circumference;
    const strokeDashoffset = circumference - (safetyScore / 100) * circumference;


    // API로부터 알람 목록 로드
    const loadAlarms = async () => {
        setAlertsLoading(true);
        try {
            const response = await alarmAPI.getAlarmsForAdmin({
                page: alertsPagination.page,
                size: alertsPagination.size,
                hours: alertsPagination.hours
            });

            const apiAlerts = response.data?.content?.map(transformAlarmData) || [];

            setAlerts(apiAlerts);
        } catch (error) {
            console.error('알람 목록 로드 실패:', error);
        } finally {
            setAlertsLoading(false);
        }
    };

    // 웹소켓 연결 및 실시간 데이터 처리
    useEffect(() => {
        const token = authUtils.getToken();
        if (!token) return;

        // 웹소켓 연결
        const connectWebSocket = async () => {
            try {
                await stompService.connect(token, 'admin');
            } catch (error) {
                console.error('Dashboard: 웹소켓 연결 실패:', error);
            }
        };

        // 새로운 알림 처리
        const handleNewAlarm = (data) => {
            const alertType = getAlertTypeFromData(data.incidentType, data.incidentDescription);
            const dashboardType = convertToDashboardType(alertType);
            
            const newAlert = {
                id: data.id || Date.now(), // 웹소켓에서 ID가 오면 사용, 없으면 임시 ID
                type: dashboardType,
                title: getAlertTitle(alertType, data.incidentDescription),
                description: data.incidentDescription || '알림 내용',
                time: '방금 전',
                timestamp: new Date().toISOString(),
                workerId: data.workerId,
                originalData: data
            };

            // 기존 알림 목록에 추가 (최신 알림을 맨 위에, 최대 3개 유지)
            setAlerts(prevAlerts => [newAlert, ...prevAlerts.slice(0, 3)]);
        };

        // 이벤트 리스너 등록
        stompService.on('alarm', handleNewAlarm);

        // 웹소켓 연결
        if (!stompService.isConnected()) {
            connectWebSocket().catch(console.error);
        }

        // 클린업
        return () => {
            stompService.off('alarm', handleNewAlarm);
        };
    }, []);

    // 시간 업데이트 (1분마다 상대시간 갱신)
    useEffect(() => {
        const timer = setInterval(() => {
            setAlerts(prevAlerts => 
                prevAlerts.map(alert => ({
                    ...alert,
                    time: getTimeAgo(alert.timestamp)
                }))
            );
        }, 60000); // 1분마다 업데이트

        return () => clearInterval(timer);
    }, []);

    // 컴포넌트 마운트 시 데이터 로드
    useEffect(() => {
        loadAlarms().catch(console.error);
    }, []);

    return (
        <div className={styles.page}>
            {/* 페이지 헤더 */}
            <header className={styles.pageHeader}>
                <h1 className={styles.pageTitle}>대시보드</h1>
            </header>

            {/* 상단 섹션 - 종합 안전 점수 + 변동 추이 */}
            <section className={styles.topSection}>
                {/* 종합 안전 점수 */}
                <div className={styles.safetyScoreCard}>
                    <h2 className={styles.safetyScoreTitle}>종합 안전 점수</h2>

                    <div className={styles.chartContainer}>
                        <svg className={styles.donutChart} viewBox="0 0 200 200">
                            {/* 배경 원 */}
                            <circle
                                className={styles.chartBackground}
                                cx="100"
                                cy="100"
                                r="90"
                            />
                            {/* 진행률 원 */}
                            <circle
                                className={styles.chartProgress}
                                cx="100"
                                cy="100"
                                r="90"
                                strokeDasharray={strokeDasharray}
                                strokeDashoffset={strokeDashoffset}
                            />
                        </svg>

                        <div className={styles.chartText}>
                            <p className={styles.chartScore}>{safetyScore}점</p>
                            <p className={styles.chartLabel}>안전 점수</p>
                        </div>
                    </div>

                    <button className={styles.safetyStatusBtn}>
                        양호
                    </button>
                </div>

                {/* 안전 점수 변동 추이 */}
                <div className={styles.trendCard}>
                    <h2 className={styles.trendTitle}>안전 점수 변동 추이</h2>

                    <div className={styles.trendCharts}>
                        <div className={styles.trendChartItem}>
                            <p className={styles.trendChartLabel}>일별 그래프</p>
                            <div className={styles.trendChartPlaceholder}>
                                📈 일별 차트
                            </div>
                        </div>

                        <div className={styles.trendChartItem}>
                            <p className={styles.trendChartLabel}>주별 그래프</p>
                            <div className={styles.trendChartPlaceholder}>
                                📊 주별 차트
                            </div>
                        </div>

                        <div className={styles.trendChartItem}>
                            <p className={styles.trendChartLabel}>월별 그래프</p>
                            <div className={styles.trendChartPlaceholder}>
                                📉 월별 차트
                            </div>
                        </div>
                    </div>
                </div>
            </section>

            {/* 하단 위젯 섹션 */}
            <section className={styles.widgetsSection}>
                {/* 실시간 위험 알림 */}
                <div className={`${styles.widgetCard} ${styles.alertWidget}`}>
                    <div className={styles.widgetHeader}>
                        <h3 className={styles.widgetTitle}>실시간 위험 알림</h3>
                        <button 
                            className={styles.moreButton}
                            onClick={() => setIsAlarmModalOpen(true)}
                        >
                            +
                        </button>
                    </div>

                    <div className={styles.alertList}>
                        {alertsLoading ? (
                            <div style={{ 
                                textAlign: 'center', 
                                padding: '40px 20px', 
                                color: '#9CA3AF',
                                fontSize: '14px'
                            }}>
                                📡 알림 목록을 불러오는 중...
                            </div>
                        ) : alerts.length > 0 ? (
                            alerts.map(alert => (
                                <div key={alert.id} className={`${styles.alertItem} ${styles[alert.type]}`}>
                                    <div className={`${styles.alertIcon} ${styles[alert.type]}`}>
                                        {getAlertIcon(alert.type)}
                                    </div>
                                    <div className={styles.alertContent}>
                                        <p className={styles.alertTitle}>{alert.title}</p>
                                        <p className={styles.alertDesc}>{alert.description}</p>
                                    </div>
                                    <span className={styles.alertTime}>{alert.time}</span>
                                </div>
                            ))
                        ) : (
                            <div style={{ 
                                textAlign: 'center', 
                                padding: '40px 20px', 
                                color: '#9CA3AF',
                                fontSize: '14px'
                            }}>
                                📋 최근 {alertsPagination.hours}시간 내 알림이 없습니다.
                            </div>
                        )}
                    </div>
                </div>

                {/* 주요 안전 지표 */}
                <div className={`${styles.widgetCard} ${styles.indicatorWidget}`}>
                    <h3 className={styles.widgetTitle}>주요 안전 지표</h3>

                    <div className={styles.indicatorList}>
                        {indicators.map(indicator => (
                            <div key={indicator.id} className={styles.indicatorItem}>
                                <div className={`${styles.indicatorIcon} ${styles[indicator.type]}`}>
                                    {indicator.icon}
                                </div>
                                <div className={styles.indicatorContent}>
                                    <p className={styles.indicatorTitle}>{indicator.title}</p>
                                    <p className={styles.indicatorValue}>{indicator.value}</p>
                                </div>
                            </div>
                        ))}
                    </div>
                </div>

                {/* 실시간 현장 현황 */}
                <div className={`${styles.widgetCard} ${styles.statusWidget}`}>
                    <h3 className={styles.widgetTitle}>실시간 현장 현황</h3>

                    <div className={styles.statusSummary}>
                        <div className={styles.statusIcon}>👨‍💼</div>
                        <div className={styles.statusText}>
                            <p className={styles.statusLabel}>현재 인원</p>
                            <p className={styles.statusValue}>{fieldStatus.currentWorkers}명</p>
                        </div>
                    </div>

                    <p className={styles.statusDetails}>
                        건설: {fieldStatus.totalCapacity}명 | 안전: {fieldStatus.dangerWorkers}명 | 관리: {fieldStatus.normalWorkers}명
                    </p>

                    <button className={styles.statusBtn}>
                        정상 운영
                    </button>
                </div>


                {/* 긴급 이상 탐지 */}
                <div className={`${styles.widgetCard} ${styles.emergencyWidget}`}>
                    <h3 className={styles.widgetTitle}>긴급 이상 탐지</h3>

                    <StatusDisplay
                        icon="🏥"
                        label="이상 징후"
                        value={`${emergencyStatus.count}명`}
                        details={emergencyStatus.location}
                        classPrefix="emergency"
                    />
                </div>
            </section>

            {/* 알림 모달 */}
            <AlarmModal 
                isOpen={isAlarmModalOpen} 
                onClose={() => setIsAlarmModalOpen(false)} 
            />
        </div>
    );
};

export default DashboardPage;