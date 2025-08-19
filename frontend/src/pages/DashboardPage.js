import React, {useEffect, useState, useCallback} from 'react';
import styles from '../styles/Dashboard.module.css';
import alarmStompService from '../services/alarmStompService';
import {authUtils} from '../utils/auth';
import {alarmAPI, managementAPI, dashboardAPI, sensorAPI} from '../api/api';
import AlarmModal from '../components/AlarmModal';
import {useAlarmData} from '../hooks/useAlarmData';

const DashboardPage = () => {
    const {
        getAlertIcon,
        getAlertTypeFromData,
        convertToDashboardType,
        getAlertTitle,
        getTimeAgo,
        transformAlarmData
    } = useAlarmData();


    // 종합 안전 점수 계산
    const calculateSafetyScore = useCallback((metrics) => {
        const baseScore = 100;
        const penalties = {
            PPE_VIOLATION: 3,     // 보호구 미착용: 건당 3점 감점
            DANGER_ZONE: 5,      // 위험지역 접근: 건당 5점 감점  
            HEALTH_RISK: 4       // 건강 이상: 건당 4점 감점
        };
        
        const totalPenalty = Object.entries(metrics).reduce((sum, [type, count]) => {
            return sum + (count * penalties[type]);
        }, 0);
        
        return Math.max(0, baseScore - totalPenalty); // 최소 0점
    }, []);

    // 안전 점수별 색상 결정
    const getSafetyScoreColor = useCallback((score) => {
        if (score >= 80) return { color: '#10B981', status: '양호' }; // 녹색
        if (score >= 60) return { color: '#F59E0B', status: '주의' }; // 주황색
        return { color: '#EF4444', status: '위험' }; // 붉은색
    }, []);

    // 출입 통계 상태
    const [workerStats, setWorkerStats] = useState({
        total: 0,
        working: 0,
        offWork: 0,
        absent: 0,
        loading: false
    });

    // 근무중인 근로자 상태 (실시간 현장 현황용)
    const [workingWorkers, setWorkingWorkers] = useState([]);

    // 실시간 위험 알림 데이터 (API + 웹소켓)
    const [alerts, setAlerts] = useState([]);
    const [alertsLoading, setAlertsLoading] = useState(true);
    const [isAlarmModalOpen, setIsAlarmModalOpen] = useState(false);
    const alertsPagination = {
        page: 0,
        size: 3, // 대시보드에는 최근 3개만 표시
        hours: 168 // 최근 7일 (168시간)로 범위 확대
    };


    // Dashboard 메트릭 기반 안전 지표 데이터
    const [safetyMetrics, setSafetyMetrics] = useState({
        PPE_VIOLATION: 0,
        DANGER_ZONE: 0,
        HEALTH_RISK: 0
    });
    const [safetyMetricsLoading, setSafetyMetricsLoading] = useState(true);

    // 현재 안전 점수 계산
    const safetyScore = calculateSafetyScore(safetyMetrics);
    const scoreColor = getSafetyScoreColor(safetyScore);

    // 메트릭 데이터 상태 (안전 점수 변동 추이용)
    const [metricsData, setMetricsData] = useState({
        day: [],
        week: [],
        month: []
    });
    const [metricsLoading, setMetricsLoading] = useState({
        day: true,
        week: true,
        month: true
    });
    const [selectedInterval, setSelectedInterval] = useState('day');

    // 차트 데이터 처리 함수
    const processChartData = useCallback((rawData, interval) => {
        // 날짜별로 데이터 그룹화
        const groupedData = rawData.reduce((acc, item) => {
            const date = new Date(item.getWeekStart);
            const dateKey = date.toISOString().split('T')[0]; // YYYY-MM-DD 형식
            if (!acc[dateKey]) {
                acc[dateKey] = {
                    date: dateKey,
                    PPE_VIOLATION: 0,
                    DANGER_ZONE: 0,
                    HEALTH_RISK: 0
                };
            }
            acc[dateKey][item.getMetricType] = item.getTotalValue;
            return acc;
        }, {});

        // 날짜 범위를 생성하여 누락된 날짜에 0 값 추가
        const fillMissingDates = (data, interval) => {
            if (Object.keys(data).length === 0) return [];

            // 기존 데이터에서 최소 날짜 찾기, 최대 날짜는 오늘로 설정
            const existingDates = Object.keys(data).sort();
            const startDate = new Date(existingDates[0]);
            const endDate = new Date(); // 오늘 날짜로 설정

            const filledData = [];
            const currentDate = new Date(startDate);

            while (currentDate <= endDate) {
                const dateKey = currentDate.toISOString().split('T')[0];
                
                if (data[dateKey]) {
                    filledData.push(data[dateKey]);
                } else {
                    // 누락된 날짜에 0 값 추가
                    filledData.push({
                        date: dateKey,
                        PPE_VIOLATION: 0,
                        DANGER_ZONE: 0,
                        HEALTH_RISK: 0
                    });
                }

                // 간격에 따라 날짜 증가
                if (interval === 'day') {
                    currentDate.setDate(currentDate.getDate() + 1);
                } else if (interval === 'week') {
                    currentDate.setDate(currentDate.getDate() + 7);
                } else { // month
                    currentDate.setMonth(currentDate.getMonth() + 1);
                }
            }

            return filledData;
        };

        // 누락된 날짜 채우기
        const filledData = fillMissingDates(groupedData, interval);

        // 날짜순으로 정렬하고 최근 데이터 제한
        const sortedData = filledData
            .sort((a, b) => new Date(a.date) - new Date(b.date))
            .slice(-10); // 최근 10개

        return sortedData;
    }, []);

    // 선 그래프 렌더링 함수
    const renderLineChart = useCallback((data, interval) => {
        if (!data || data.length === 0) return null;

        const chartWidth = 400;
        const chartHeight = 240;
        const padding = 30;
        const innerWidth = chartWidth - 2 * padding;
        const innerHeight = chartHeight - 2 * padding;

        // 최대값 계산 (유효한 숫자만 필터링)
        const validValues = data.flatMap(d => [d.PPE_VIOLATION, d.DANGER_ZONE, d.HEALTH_RISK])
            .filter(val => typeof val === 'number' && !isNaN(val) && isFinite(val));
        const rawMaxValue = Math.max(...validValues, 1); // 최소값 1로 설정
        const maxValue = Math.ceil(rawMaxValue / 5) * 5; // 5의 배수로 올림

        // 점 좌표 계산 함수
        const getPoints = (metricType) => {
            return data.map((d, i) => {
                // 단일 데이터 포인트 처리
                const x = data.length === 1 
                    ? padding + innerWidth / 2 
                    : padding + (i / (data.length - 1)) * innerWidth;
                
                // 유효하지 않은 값 처리
                const value = typeof d[metricType] === 'number' && !isNaN(d[metricType]) ? d[metricType] : 0;
                const y = padding + innerHeight - (value / maxValue) * innerHeight;
                
                return `${x},${y}`;
            }).join(' ');
        };

        // 날짜 레이블 생성
        const dateLabels = data.map((d, i) => {
            const date = new Date(d.date);
            const x = data.length === 1 
                ? padding + innerWidth / 2 
                : padding + (i / (data.length - 1)) * innerWidth;
            const label = interval === 'day' 
                ? `${date.getMonth() + 1}/${date.getDate()}`
                : interval === 'week'
                ? `${date.getMonth() + 1}/${date.getDate()}`
                : `${date.getMonth() + 1}월`;
            
            return (
                <text
                    key={i}
                    x={x}
                    y={chartHeight - 5}
                    textAnchor="middle"
                    fontSize="10"
                    fill="#6B7280"
                >
                    {label}
                </text>
            );
        });

        // Y축 레이블 생성
        const yAxisLabels = [];
        const ySteps = 5; // 5개 눈금
        for (let i = 0; i <= ySteps; i++) {
            const value = Math.round((maxValue / ySteps) * i);
            const y = padding + innerHeight - (i / ySteps) * innerHeight;
            yAxisLabels.push(
                <text
                    key={`y-label-${i}`}
                    x={padding - 10}
                    y={y + 4}
                    textAnchor="end"
                    fontSize="10"
                    fill="#6B7280"
                >
                    {value}
                </text>
            );
        }

        return (
            <svg 
                viewBox={`0 0 ${chartWidth} ${chartHeight}`} 
                className={styles.chartSvg}
                preserveAspectRatio="xMidYMid meet"
            >
                {/* 배경 격자 */}
                <defs>
                    <pattern id={`grid-${interval}`} width="20" height="20" patternUnits="userSpaceOnUse">
                        <path d="M 20 0 L 0 0 0 20" fill="none" stroke="#F3F4F6" strokeWidth="1"/>
                    </pattern>
                </defs>
                <rect width={chartWidth} height={chartHeight} fill={`url(#grid-${interval})`} opacity="0.5"/>

                {/* Y축 눈금선 */}
                {Array.from({length: 6}, (_, i) => {
                    const y = padding + (i / 5) * innerHeight;
                    return (
                        <line
                            key={`y-grid-${i}`}
                            x1={padding}
                            y1={y}
                            x2={padding + innerWidth}
                            y2={y}
                            stroke="#E5E7EB"
                            strokeWidth="1"
                        />
                    );
                })}

                {/* Y축 레이블 */}
                {yAxisLabels}

                {/* 선 그래프 */}
                <polyline
                    points={getPoints('PPE_VIOLATION')}
                    fill="none"
                    stroke="#f59e0b"
                    strokeWidth="3"
                    strokeLinejoin="round"
                />
                <polyline
                    points={getPoints('DANGER_ZONE')}
                    fill="none"
                    stroke="#ef4444"
                    strokeWidth="3"
                    strokeLinejoin="round"
                />
                <polyline
                    points={getPoints('HEALTH_RISK')}
                    fill="none"
                    stroke="#8b5cf6"
                    strokeWidth="3"
                    strokeLinejoin="round"
                />

                {/* 데이터 포인트 */}
                {['PPE_VIOLATION', 'DANGER_ZONE', 'HEALTH_RISK'].map((metricType, typeIndex) => {
                    const colors = ['#f59e0b', '#ef4444', '#8b5cf6'];
                    return data.map((d, i) => {
                        const x = data.length === 1 
                            ? padding + innerWidth / 2 
                            : padding + (i / (data.length - 1)) * innerWidth;
                        
                        const value = typeof d[metricType] === 'number' && !isNaN(d[metricType]) ? d[metricType] : 0;
                        const y = padding + innerHeight - (value / maxValue) * innerHeight;
                        return (
                            <circle
                                key={`${metricType}-${i}`}
                                cx={x}
                                cy={y}
                                r="4"
                                fill={colors[typeIndex]}
                                stroke="white"
                                strokeWidth="2"
                            >
                                <title>{`${d.date}: ${d[metricType]}건`}</title>
                            </circle>
                        );
                    });
                })}

                {/* 날짜 레이블 */}
                {dateLabels}
            </svg>
        );
    }, []);

    // Dashboard API 기반 안전 지표 계산
    const calculateSafetyIndicators = useCallback(() => {
        if (safetyMetricsLoading) {
            return [
                {
                    id: 1,
                    type: 'normal',
                    title: '보호구 미착용 적발 횟수',
                    value: '...',
                    icon: '🦺'
                },
                {
                    id: 2,
                    type: 'normal',
                    title: '위험지역 접근 횟수',
                    value: '...',
                    icon: '⚠️'
                },
                {
                    id: 3,
                    type: 'normal',
                    title: '건강상태 이상 발생 횟수',
                    value: '...',
                    icon: '🏥'
                }
            ];
        }

        // 위험도 결정 함수
        const getRiskType = (count) => {
            if (count === 0) return 'normal';
            if (count <= 2) return 'warning';
            return 'danger';
        };

        return [
            {
                id: 1,
                type: getRiskType(safetyMetrics.PPE_VIOLATION),
                title: '보호구 미착용 적발 횟수',
                value: `${safetyMetrics.PPE_VIOLATION}건`,
                icon: '🦺'
            },
            {
                id: 2,
                type: getRiskType(safetyMetrics.DANGER_ZONE),
                title: '위험지역 접근 횟수',
                value: `${safetyMetrics.DANGER_ZONE}건`,
                icon: '⚠️'
            },
            {
                id: 3,
                type: getRiskType(safetyMetrics.HEALTH_RISK),
                title: '건강상태 이상 발생 횟수',
                value: `${safetyMetrics.HEALTH_RISK}건`,
                icon: '🏥'
            }
        ];
    }, [safetyMetrics, safetyMetricsLoading]);

    // 계산된 안전 지표
    const indicators = calculateSafetyIndicators();


    // 도넛 차트 계산
    const circumference = 2 * Math.PI * 90; // 반지름 90
    const strokeDasharray = circumference;
    const strokeDashoffset = circumference - (safetyScore / 100) * circumference;

    // 출입 통계 조회 함수
    const fetchWorkerStats = useCallback(async () => {
        try {
            setWorkerStats(prev => ({...prev, loading: true}));
            const response = await managementAPI.getWorkerStats();
            setWorkerStats({
                ...response.data,
                loading: false
            });
        } catch (error) {
            console.error('출입 통계 조회 실패:', error);
            setWorkerStats(prev => ({...prev, loading: false}));
        }
    }, []);

    // 근무중인 근로자와 위치 정보를 통합 조회
    const fetchWorkingWorkersWithLocation = useCallback(async () => {
        try {
            // 1. 근무중인 근로자 목록 조회
            const workingResponse = await managementAPI.getWorkingWorkers();
            const workingWorkers = workingResponse.data || [];

            if (workingWorkers.length === 0) {
                setWorkingWorkers([]);
                return;
            }

            // 2. 해당 근로자들의 위치 정보 조회
            const workerIds = workingWorkers.map(w => w.workerId);
            let workersWithLocation = [];

            try {
                const locationResponse = await sensorAPI.getWorkersLocation(workerIds);
                const locations = locationResponse.data || [];

                // 3. 데이터 통합
                workersWithLocation = workingWorkers.map((worker, index) => {
                    const location = locations.find(loc => loc.workerId === worker.workerId);
                    return {
                        ...worker,
                        id: worker.workerId,
                        name: worker.workerName,
                        department: worker.department,
                        occupation: worker.occupation,
                        enterDate: worker.enterDate,
                        latitude: location?.latitude || (37.5665 + (index * 0.0001)),
                        longitude: location?.longitude || (126.9780 + (index * 0.0001)),
                        status: 'safe', // 기본 상태
                        isWorking: true,
                        workStartTime: worker.enterDate
                    };
                });
            } catch (locationError) {
                console.error('위치 정보 조회 실패:', locationError);

                // 위치 정보 조회 실패 시 기본 위치로 설정
                workersWithLocation = workingWorkers.map((worker, index) => ({
                    ...worker,
                    id: worker.workerId,
                    name: worker.workerName,
                    department: worker.department,
                    occupation: worker.occupation,
                    enterDate: worker.enterDate,
                    latitude: 37.5665 + (index * 0.0001),
                    longitude: 126.9780 + (index * 0.0001),
                    status: 'safe', // 기본 상태
                    isWorking: true,
                    workStartTime: worker.enterDate
                }));
            }

            setWorkingWorkers(workersWithLocation);

        } catch (error) {
            console.error('근무중인 근로자 조회 실패:', error);
            setWorkingWorkers([]);
        }
    }, []);

    // API로부터 알람 목록 로드
    const loadAlarms = useCallback(async () => {
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
    }, [alertsPagination.page, alertsPagination.size, alertsPagination.hours, transformAlarmData]);


    // 메트릭 데이터 로드 함수
    const loadMetrics = useCallback(async (interval) => {
        setMetricsLoading(prev => ({ ...prev, [interval]: true }));
        try {
            const response = await dashboardAPI.getMetrics(interval);
            console.log(`${interval} 메트릭 데이터:`, response);
            
            // response 구조 확인 - response.data가 없으면 response 자체를 사용
            const data = response.data || response || [];
            setMetricsData(prev => ({
                ...prev,
                [interval]: data
            }));
        } catch (error) {
            console.error(`${interval} 메트릭 로드 실패:`, error);
            setMetricsData(prev => ({
                ...prev,
                [interval]: []
            }));
        } finally {
            setMetricsLoading(prev => ({ ...prev, [interval]: false }));
        }
    }, []);

    // 모든 간격의 메트릭 데이터 로드
    const loadAllMetrics = useCallback(async () => {
        await Promise.all([
            loadMetrics('day'),
            loadMetrics('week'),
            loadMetrics('month')
        ]);
    }, [loadMetrics]);

    // 개별 메트릭 조회 함수 (안전 지표용)
    const loadSafetyMetrics = useCallback(async () => {
        setSafetyMetricsLoading(true);
        try {
            const [ppeResponse, dangerResponse, healthResponse] = await Promise.all([
                dashboardAPI.getDashboard('PPE_VIOLATION'),
                dashboardAPI.getDashboard('DANGER_ZONE'),
                dashboardAPI.getDashboard('HEALTH_RISK')
            ]);

            setSafetyMetrics({
                PPE_VIOLATION: ppeResponse?.metricValue || 0,
                DANGER_ZONE: dangerResponse?.metricValue || 0,
                HEALTH_RISK: healthResponse?.metricValue || 0
            });
        } catch (error) {
            console.error('안전 메트릭 로드 실패:', error);
            setSafetyMetrics({
                PPE_VIOLATION: 0,
                DANGER_ZONE: 0,
                HEALTH_RISK: 0
            });
        } finally {
            setSafetyMetricsLoading(false);
        }
    }, []);

    // 웹소켓 연결 및 실시간 데이터 처리
    useEffect(() => {
        const token = authUtils.getToken();
        if (!token) return;

        // 웹소켓 연결
        const connectWebSocket = async () => {
            try {
                await alarmStompService.connect(token, 'admin');
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

            // 기존 알림 목록에 추가 (최신 알림을 맨 위에, 최대 2개 유지하여 총 3개)
            setAlerts(prevAlerts => [newAlert, ...prevAlerts.slice(0, 2)]);
            
            
            // 새 알림에 따라 안전 메트릭 업데이트
            if (data.incidentType) {
                loadSafetyMetrics().catch(console.error);
            }

            // 알림 유형에 따른 근로자 상태 업데이트
            if (data.workerId) {
                setWorkingWorkers(prevWorkers => {
                    return prevWorkers.map(worker => {
                        if (worker.workerId.toString() === data.workerId.toString()) {
                            let newStatus = worker.status;

                            switch (data.incidentType) {
                                case 'PPE_VIOLATION':
                                    newStatus = 'warning'; // 보호구 미착용 -> 주의
                                    break;
                                case 'DANGER_ZONE':
                                case 'HEALTH_RISK':
                                    newStatus = 'danger'; // 위험구역 접근, 건강 위험 -> 위험
                                    break;
                                default:
                                    // 기타 알림은 상태 변경 없음
                                    break;
                            }

                            return {
                                ...worker,
                                status: newStatus,
                                lastAlarmType: data.incidentType,
                                lastAlarmTime: new Date().toISOString()
                            };
                        }
                        return worker;
                    });
                });
            }
        };

        // 이벤트 리스너 등록
        alarmStompService.on('alarm', handleNewAlarm);

        // 웹소켓 연결
        if (!alarmStompService.isConnected()) {
            connectWebSocket().catch(console.error);
        }

        // 클린업
        return () => {
            alarmStompService.off('alarm', handleNewAlarm);
        };
    }, [convertToDashboardType, getAlertTitle, getAlertTypeFromData]);

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
    }, [getTimeAgo]);

    // 컴포넌트 마운트 시 데이터 로드
    useEffect(() => {
        loadAlarms().catch(console.error);
        fetchWorkerStats().catch(console.error);
        fetchWorkingWorkersWithLocation().catch(console.error);
        loadAllMetrics().catch(console.error);
        loadSafetyMetrics().catch(console.error);
    }, [loadAlarms, fetchWorkerStats, fetchWorkingWorkersWithLocation, loadAllMetrics, loadSafetyMetrics]);

    // 현장 현황 계산 (실시간 업데이트)
    const fieldStatus = {
        totalWorkers: workerStats.working || 0,
        safeWorkers: workingWorkers.filter(w => w.status === 'safe').length,
        warningWorkers: workingWorkers.filter(w => w.status === 'warning').length,
        dangerWorkers: workingWorkers.filter(w => w.status === 'danger').length
    };

    return (
        <div className={styles.page}>
            {/* 페이지 헤더 */}
            <header className={styles.pageHeader}>
                <h1 className={styles.pageTitle}>대시보드</h1>
            </header>

            {/* 상단 섹션 - 종합 안전 점수 + 위젯들 */}
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
                                style={{ stroke: scoreColor.color }}
                            />
                        </svg>

                        <div className={styles.chartText}>
                            <p className={styles.chartScore} style={{ color: scoreColor.color }}>{safetyScore}점</p>
                            <p className={styles.chartLabel}>안전 점수</p>
                        </div>
                    </div>

                    <button className={styles.safetyStatusBtn} style={{ backgroundColor: scoreColor.color }}>
                        {scoreColor.status}
                    </button>
                </div>

                {/* 우측 위젯 그리드 */}
                <div className={styles.rightWidgetsGrid}>
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
                                            <p className={styles.alertWorker}>작업자: {alert.workerName || "알 수 없음"}</p>
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

                    {/* 근로자 현황 */}
                    <div className={`${styles.widgetCard} ${styles.statusWidget}`}>
                        <h3 className={styles.widgetTitle}>근로자 현황</h3>

                        <div className={styles.statusList}>
                            <div className={styles.statusItem}>
                                <div className={styles.statusItemIcon}>👥</div>
                                <div className={styles.statusItemContent}>
                                    <p className={styles.statusItemLabel}>총근무자</p>
                                    <p className={styles.statusItemValue}>
                                        {workerStats.loading ? '...' : workerStats.total}명
                                    </p>
                                </div>
                            </div>
                            <div className={styles.statusItem}>
                                <div className={styles.statusItemIcon}>💼</div>
                                <div className={styles.statusItemContent}>
                                    <p className={styles.statusItemLabel}>근무중</p>
                                    <p className={styles.statusItemValue}>
                                        {workerStats.loading ? '...' : workerStats.working}명
                                    </p>
                                </div>
                            </div>
                            <div className={styles.statusItem}>
                                <div className={styles.statusItemIcon}>🏠</div>
                                <div className={styles.statusItemContent}>
                                    <p className={styles.statusItemLabel}>퇴근</p>
                                    <p className={styles.statusItemValue}>
                                        {workerStats.loading ? '...' : workerStats.offWork}명
                                    </p>
                                </div>
                            </div>
                            <div className={styles.statusItem}>
                                <div className={styles.statusItemIcon}>⚪</div>
                                <div className={styles.statusItemContent}>
                                    <p className={styles.statusItemLabel}>미출근</p>
                                    <p className={styles.statusItemValue}>
                                        {workerStats.loading ? '...' : workerStats.absent}명
                                    </p>
                                </div>
                            </div>
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
                                <p className={styles.statusValue}>
                                    {workerStats.loading ? '...' : fieldStatus.totalWorkers}명
                                </p>
                            </div>
                        </div>

                        <p className={styles.statusDetails}>
                            안전: {fieldStatus.safeWorkers}명 | 주의: {fieldStatus.warningWorkers}명 | 위험: {fieldStatus.dangerWorkers}명
                        </p>

                        <button className={styles.statusBtn}>
                            정상 운영
                        </button>
                    </div>
                </div>
            </section>

            {/* 하단 섹션 - 안전 점수 변동 추이 */}
            <section className={styles.bottomSection}>
                {/* 안전 점수 변동 추이 */}
                <div className={styles.trendCard}>
                    <h2 className={styles.trendTitle}>안전 점수 변동 추이</h2>

                    {/* 메트릭 타입별 범례 */}
                    <div className={styles.chartLegend}>
                        <div className={styles.legendItem}>
                            <span className={styles.legendColor} style={{backgroundColor: '#f59e0b'}}></span>
                            <span>보호구 미착용</span>
                        </div>
                        <div className={styles.legendItem}>
                            <span className={styles.legendColor} style={{backgroundColor: '#ef4444'}}></span>
                            <span>위험지역 접근</span>
                        </div>
                        <div className={styles.legendItem}>
                            <span className={styles.legendColor} style={{backgroundColor: '#8b5cf6'}}></span>
                            <span>건강 이상</span>
                        </div>
                    </div>

                    {/* 3개 선 그래프 영역 */}
                    <div className={styles.chartsGrid}>
                        {/* 일별 그래프 */}
                        <div className={styles.chartSection}>
                            <h3 className={styles.chartSectionTitle}>일별</h3>
                            {metricsLoading.day ? (
                                <div className={styles.loadingMessage}>
                                    📊 일별 데이터 로딩중...
                                </div>
                            ) : metricsData.day?.length > 0 ? (
                                <div className={styles.lineChart}>
                                    {(() => {
                                        const data = processChartData(metricsData.day, 'day');
                                        return renderLineChart(data, 'day');
                                    })()}
                                </div>
                            ) : (
                                <div className={styles.noDataMessage}>일별 데이터 없음</div>
                            )}
                        </div>

                        {/* 주별 그래프 */}
                        <div className={styles.chartSection}>
                            <h3 className={styles.chartSectionTitle}>주별</h3>
                            {metricsLoading.week ? (
                                <div className={styles.loadingMessage}>
                                    📊 주별 데이터 로딩중...
                                </div>
                            ) : metricsData.week?.length > 0 ? (
                                <div className={styles.lineChart}>
                                    {(() => {
                                        const data = processChartData(metricsData.week, 'week');
                                        return renderLineChart(data, 'week');
                                    })()}
                                </div>
                            ) : (
                                <div className={styles.noDataMessage}>주별 데이터 없음</div>
                            )}
                        </div>

                        {/* 월별 그래프 */}
                        <div className={styles.chartSection}>
                            <h3 className={styles.chartSectionTitle}>월별</h3>
                            {metricsLoading.month ? (
                                <div className={styles.loadingMessage}>
                                    📊 월별 데이터 로딩중...
                                </div>
                            ) : metricsData.month?.length > 0 ? (
                                <div className={styles.lineChart}>
                                    {(() => {
                                        const data = processChartData(metricsData.month, 'month');
                                        return renderLineChart(data, 'month');
                                    })()}
                                </div>
                            ) : (
                                <div className={styles.noDataMessage}>월별 데이터 없음</div>
                            )}
                        </div>
                    </div>
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