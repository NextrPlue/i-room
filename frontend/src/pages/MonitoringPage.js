import React, {useState, useEffect, useRef} from 'react';
import styles from '../styles/Monitoring.module.css';
import AlarmModal from '../components/AlarmModal';
import stompService from '../services/stompService';
import { authUtils } from '../utils/auth';
import { alarmAPI } from '../api/api';
import { useAlarmData } from '../hooks/useAlarmData';
// import { riskZoneAPI } from '../api/api'; // API 연동 시 사용

const MonitoringPage = () => {
    const mapRef = useRef(null);
    const [selectedFilter, setSelectedFilter] = useState({
        attribute: 'all',
        riskLevel: 'all',
        zone: 'all'
    });
    
    const { getAlertIcon, getAlertTypeFromData, convertToDashboardType, getAlertTitle, getTimeAgo } = useAlarmData();

    // 작업자 위치 데이터
    const [workers] = useState([
        {id: 1, x: 15, y: 25, status: 'danger', name: '김철수'},
        {id: 2, x: 45, y: 15, status: 'warning', name: '이영희'},
        {id: 3, x: 75, y: 30, status: 'safe', name: '박민수'},
        {id: 4, x: 85, y: 45, status: 'safe', name: '정수진'},
        {id: 5, x: 25, y: 60, status: 'warning', name: '한지민'},
        {id: 6, x: 65, y: 70, status: 'safe', name: '조현우'},
        {id: 7, x: 55, y: 50, status: 'danger', name: '윤서연'},
        {id: 8, x: 35, y: 80, status: 'safe', name: '장동건'}
    ]);

    // 위험구역 데이터
    const [dangerZones] = useState([
        {id: 1, x: 15, y: 15, width: 25, height: 35, level: 'high', name: '고위험구역'},
        {id: 2, x: 45, y: 35, width: 20, height: 20, level: 'medium', name: '중위험구역'}
    ]);

    // 현장 현황 데이터
    const [fieldStatus] = useState({
        totalWorkers: 24,
        safeWorkers: 18,
        warningWorkers: 4,
        dangerWorkers: 2
    });

    // 실시간 경고 알림 데이터 (API + 웹소켓)
    const [alerts, setAlerts] = useState([]);
    const [alertsLoading, setAlertsLoading] = useState(true);
    const [alertsPagination, setAlertsPagination] = useState({
        page: 0,
        size: 3, // 모니터링에서는 최근 3개만 표시
        hours: 168 // 최근 7일
    });

    // 알림 모달 상태
    const [isAlarmModalOpen, setIsAlarmModalOpen] = useState(false);


    // API로부터 알람 목록 로드
    const loadAlarms = async () => {
        setAlertsLoading(true);
        try {
            const response = await alarmAPI.getAlarmsForAdmin({
                page: alertsPagination.page,
                size: alertsPagination.size,
                hours: alertsPagination.hours
            });

            const apiAlerts = response.data?.content?.map(alarm => {
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
            }) || [];

            setAlerts(apiAlerts);
        } catch (error) {
            console.error(' Monitoring: 알람 목록 로드 실패:', error);
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
                console.error('Monitoring: 웹소켓 연결 실패:', error);
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
            setAlerts(prevAlerts => [newAlert, ...prevAlerts.slice(0, 2)]);
        };

        // 이벤트 리스너 등록
        stompService.on('alarm', handleNewAlarm);

        // 웹소켓 연결
        if (!stompService.isConnected()) {
            connectWebSocket();
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
        loadAlarms();
        // fetchRiskZoneData(); // 추후 필요시 활성화
    }, [alertsPagination.page, alertsPagination.size, alertsPagination.hours]);

    // 필터 변경 핸들러
    const handleFilterChange = (filterType, value) => {
        setSelectedFilter(prev => ({
            ...prev,
            [filterType]: value
        }));
    };

    // 작업자 클릭 핸들러
    const handleWorkerClick = (worker) => {
        alert(`작업자: ${worker.name}\n상태: ${getStatusText(worker.status)}`);
    };

    // 상태 텍스트 변환
    const getStatusText = (status) => {
        switch (status) {
            case 'safe':
                return '정상';
            case 'warning':
                return '주의';
            case 'danger':
                return '위험';
            default:
                return '알 수 없음';
        }
    };

    // 필터된 작업자 목록
    const filteredWorkers = workers.filter(worker => {
        if (selectedFilter.attribute === 'all') return true;
        return worker.status === selectedFilter.attribute;
    });

    return (
        <div className={styles.page}>
            {/* 페이지 헤더 */}
            <header className={styles.pageHeader}>
                <h1 className={styles.pageTitle}>실시간 모니터링</h1>
                <span className={styles.updateInfo}>마지막 업데이트: 2초전</span>
            </header>

            {/* 필터 섹션 */}
            <section className={styles.filterSection}>
                <select
                    className={styles.filterDropdown}
                    value={selectedFilter.attribute}
                    onChange={(e) => handleFilterChange('attribute', e.target.value)}
                >
                    <option value="all">전체 속성</option>
                    <option value="safe">정상</option>
                    <option value="warning">주의</option>
                    <option value="danger">위험</option>
                </select>

                <select
                    className={styles.filterDropdown}
                    value={selectedFilter.riskLevel}
                    onChange={(e) => handleFilterChange('riskLevel', e.target.value)}
                >
                    <option value="all">위험도별</option>
                    <option value="high">고위험</option>
                    <option value="medium">중위험</option>
                    <option value="low">저위험</option>
                </select>

                <select
                    className={styles.filterDropdown}
                    value={selectedFilter.zone}
                    onChange={(e) => handleFilterChange('zone', e.target.value)}
                >
                    <option value="all">구역별</option>
                    <option value="zone1">1구역</option>
                    <option value="zone2">2구역</option>
                    <option value="zone3">3구역</option>
                </select>
            </section>

            {/* 메인 콘텐츠 */}
            <div className={styles.contentSection}>
                {/* 좌측: 도면 섹션 */}
                <section className={styles.mapSection}>
                    <h2 className={styles.mapTitle}>현장 도면 - 실시간 위치</h2>

                    <div className={styles.mapContainer} ref={mapRef}>
                        <div className={styles.mapCanvas}>
                            {/* 위험구역 렌더링 */}
                            {dangerZones.map(zone => (
                                <div
                                    key={zone.id}
                                    className={`${styles.dangerZone} ${styles[zone.level]}`}
                                    style={{
                                        left: `${zone.x}%`,
                                        top: `${zone.y}%`,
                                        width: `${zone.width}%`,
                                        height: `${zone.height}%`
                                    }}
                                    title={zone.name}
                                />
                            ))}

                            {/* 작업자 위치 렌더링 */}
                            {filteredWorkers.map(worker => (
                                <div
                                    key={worker.id}
                                    className={`${styles.workerDot} ${styles[worker.status]}`}
                                    style={{
                                        left: `${worker.x}%`,
                                        top: `${worker.y}%`
                                    }}
                                    onClick={() => handleWorkerClick(worker)}
                                    title={`${worker.name} - ${getStatusText(worker.status)}`}
                                />
                            ))}
                        </div>
                    </div>

                    {/* 범례 */}
                    <div className={styles.mapLegend}>
                        <div className={styles.legendGroup}>
                            <div className={styles.legendItem}>
                                <div className={`${styles.legendColor} ${styles.safe}`}></div>
                                <span>정상(18명)</span>
                            </div>
                            <div className={styles.legendItem}>
                                <div className={`${styles.legendColor} ${styles.warning}`}></div>
                                <span>주의(4명)</span>
                            </div>
                            <div className={styles.legendItem}>
                                <div className={`${styles.legendColor} ${styles.danger}`}></div>
                                <span>위험(2명)</span>
                            </div>
                        </div>

                        <div className={styles.legendGroup}>
                            <div className={styles.legendItem}>
                                <div className={`${styles.legendZone} ${styles.high}`}></div>
                                <span>중점관리 위험구역</span>
                            </div>
                            <div className={styles.legendItem}>
                                <div className={`${styles.legendZone} ${styles.medium}`}></div>
                                <span>고요 위험구역</span>
                            </div>
                        </div>
                    </div>
                </section>

                {/* 우측: 정보 패널 */}
                <aside className={styles.infoPanel}>
                    {/* 실시간 현장 현황 */}
                    <div className={styles.statusWidget}>
                        <h3 className={styles.widgetTitle}>실시간 현장 현황</h3>

                        <div className={styles.statusSummary}>
                            <div className={styles.statusIcon}>👨‍💼</div>
                            <div className={styles.statusText}>
                                <p className={styles.statusLabel}>현재 인원</p>
                                <p className={styles.statusValue}>{fieldStatus.totalWorkers}명</p>
                            </div>
                        </div>

                        <p className={styles.statusDetails}>
                            건설: {fieldStatus.safeWorkers}명 | 안전: {fieldStatus.warningWorkers}명 |
                            관리: {fieldStatus.dangerWorkers}명
                        </p>

                        <button className={styles.statusButton}>
                            정상 운영
                        </button>
                    </div>

                    {/* 실시간 경고 알림 */}
                    <div className={styles.alertWidget}>
                        <div className={styles.widgetHeader}>
                            <h3 className={styles.widgetTitle}>실시간 경고 알림</h3>
                            <button 
                                className={styles.moreButton}
                                onClick={() => setIsAlarmModalOpen(true)}
                            >
                                +
                            </button>
                        </div>

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
                            <div className={styles.alertList}>
                                {alerts.slice(0, 3).map(alert => (
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
                                ))}
                            </div>
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
                </aside>
            </div>

            {/* 알림 모달 */}
            <AlarmModal 
                isOpen={isAlarmModalOpen} 
                onClose={() => setIsAlarmModalOpen(false)} 
            />
        </div>
    );
};

export default MonitoringPage;