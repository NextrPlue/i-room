import React, {useState, useEffect, useRef, useCallback} from 'react';
import styles from '../styles/Monitoring.module.css';
import AlarmModal from '../components/AlarmModal';
import alarmStompService from '../services/alarmStompService';
import sensorStompService from '../services/sensorStompService';
import {authUtils} from '../utils/auth';
import {alarmAPI, blueprintAPI, riskZoneAPI, managementAPI, sensorAPI} from '../api/api';
import {useAlarmData} from '../hooks/useAlarmData';

// 상수 정의
const CANVAS_CONFIG = {
    DEFAULT_GPS: {
        LAT: 37.5665,
        LON: 126.9780
    },
    PIXEL_TO_METER_RATIO: 0.05,
    MAX_SIZE_PERCENTAGE: 30,
    MARGIN_PERCENTAGE: 10,
    BLUEPRINT_SCALE: 80
};

const UPDATE_INTERVALS = {
    WORKER_STATS: 5 * 60 * 1000, // 5분
    TIME_DISPLAY: 60000 // 1분
};

const PAGINATION_CONFIG = {
    page: 0,
    size: 3,
    hours: 168
};

// 유틸리티 함수들
const coordinateUtils = {
    // GPS 경계 계산을 별도 함수로 추출
    getBlueprintBounds: (blueprint) => {
        if (!blueprint?.topLeft || !blueprint?.topRight ||
            !blueprint?.bottomLeft || !blueprint?.bottomRight) {
            return null;
        }

        const {topLeft, topRight, bottomLeft, bottomRight} = blueprint;

        return {
            minLat: Math.min(topLeft.lat, topRight.lat, bottomLeft.lat, bottomRight.lat),
            maxLat: Math.max(topLeft.lat, topRight.lat, bottomLeft.lat, bottomRight.lat),
            minLon: Math.min(topLeft.lon, topRight.lon, bottomLeft.lon, bottomRight.lon),
            maxLon: Math.max(topLeft.lon, topRight.lon, bottomLeft.lon, bottomRight.lon)
        };
    },

    convertGPSToCanvas: (lat, lon, blueprint) => {
        const bounds = coordinateUtils.getBlueprintBounds(blueprint);
        if (!bounds) {
            return {x: 50, y: 50};
        }

        const {minLat, maxLat, minLon, maxLon} = bounds;

        const x = ((lon - minLon) / (maxLon - minLon)) * 100;
        const y = ((maxLat - lat) / (maxLat - minLat)) * 100;

        return {
            x: Math.max(0, Math.min(100, x)),
            y: Math.max(0, Math.min(100, y))
        };
    },

    convertMetersToCanvas: (widthMeters, heightMeters, blueprint) => {
        if (!blueprint?.width || !blueprint?.height) {
            return {width: 5, height: 5};
        }

        let realBuildingWidth, realBuildingHeight;

        if (blueprint.width > 100) {
            realBuildingWidth = blueprint.width * CANVAS_CONFIG.PIXEL_TO_METER_RATIO;
            realBuildingHeight = blueprint.height * CANVAS_CONFIG.PIXEL_TO_METER_RATIO;
        } else {
            realBuildingWidth = blueprint.width;
            realBuildingHeight = blueprint.height;
        }

        const widthRatio = widthMeters / realBuildingWidth;
        const heightRatio = heightMeters / realBuildingHeight;

        const canvasWidth = Math.min(widthRatio * CANVAS_CONFIG.BLUEPRINT_SCALE, CANVAS_CONFIG.MAX_SIZE_PERCENTAGE);
        const canvasHeight = Math.min(heightRatio * CANVAS_CONFIG.BLUEPRINT_SCALE, CANVAS_CONFIG.MAX_SIZE_PERCENTAGE);

        return {width: canvasWidth, height: canvasHeight};
    },

    isInsideBlueprint: (canvasX, canvasY) => {
        return canvasX >= CANVAS_CONFIG.MARGIN_PERCENTAGE &&
            canvasX <= (100 - CANVAS_CONFIG.MARGIN_PERCENTAGE) &&
            canvasY >= CANVAS_CONFIG.MARGIN_PERCENTAGE &&
            canvasY <= (100 - CANVAS_CONFIG.MARGIN_PERCENTAGE);
    }
};

// 데이터 변환 유틸리티
const dataTransformUtils = {
    transformWorkerData: (workers, locations) => {
        return workers.map((worker, index) => {
            const location = locations.find(loc => loc.workerId === worker.workerId);
            return {
                ...worker,
                id: worker.workerId,
                name: worker.workerName,
                department: worker.department,
                occupation: worker.occupation,
                enterDate: worker.enterDate,
                latitude: location?.latitude || (CANVAS_CONFIG.DEFAULT_GPS.LAT + (index * 0.0001)),
                longitude: location?.longitude || (CANVAS_CONFIG.DEFAULT_GPS.LON + (index * 0.0001)),
                status: 'safe',
                isWorking: true,
                workStartTime: worker.enterDate
            };
        });
    },

    transformRiskZoneData: (zones, blueprintId, blueprint) => {
        return zones
            .filter(zone => zone.blueprintId === blueprintId)
            .map(zone => {
                const canvasPosition = coordinateUtils.convertGPSToCanvas(
                    zone.latitude,
                    zone.longitude,
                    blueprint
                );
                const canvasSize = coordinateUtils.convertMetersToCanvas(
                    zone.width,
                    zone.height,
                    blueprint
                );

                const boxX = canvasPosition.x - canvasSize.width / 2;
                const boxY = canvasPosition.y - canvasSize.height / 2;

                return {
                    id: zone.id,
                    x: boxX,
                    y: boxY,
                    width: canvasSize.width,
                    height: canvasSize.height,
                    level: 'high',
                    name: zone.name || `위험구역 ${zone.id}`,
                    isInside: coordinateUtils.isInsideBlueprint(canvasPosition.x, canvasPosition.y)
                };
            })
            .filter(zone => zone.isInside);
    }
};

// 커스텀 훅들
const useWorkerState = () => {
    const [workingWorkers, setWorkingWorkers] = useState([]);
    const [workerStats, setWorkerStats] = useState({
        total: 0,
        working: 0,
        offWork: 0,
        absent: 0,
        loading: false
    });

    const setWorkerStatsLoading = useCallback((loading) => {
        setWorkerStats(prev => ({
            total: prev.total,
            working: prev.working,
            offWork: prev.offWork,
            absent: prev.absent,
            loading
        }));
    }, []);

    const fetchWorkerStats = useCallback(async () => {
        try {
            setWorkerStatsLoading(true);
            const response = await managementAPI.getWorkerStats();
            setWorkerStats({
                ...response.data,
                loading: false
            });
        } catch (error) {
            console.error('출입 통계 조회 실패:', error);
            setWorkerStatsLoading(false);
        }
    }, [setWorkerStatsLoading]);

    const fetchWorkingWorkersWithLocation = useCallback(async () => {
        try {
            const workingResponse = await managementAPI.getWorkingWorkers();
            const workers = workingResponse.data || [];

            if (workers.length === 0) {
                setWorkingWorkers([]);
                return;
            }

            const workerIds = workers.map(w => w.workerId);
            let workersWithLocation;

            try {
                const locationResponse = await sensorAPI.getWorkersLocation(workerIds);
                const locations = locationResponse.data || [];
                workersWithLocation = dataTransformUtils.transformWorkerData(workers, locations);
            } catch (locationError) {
                console.error('위치 정보 조회 실패:', locationError);
                workersWithLocation = dataTransformUtils.transformWorkerData(workers, []);
            }

            setWorkingWorkers(workersWithLocation);
        } catch (error) {
            console.error('근무중인 근로자 조회 실패:', error);
            setWorkingWorkers([]);
        }
    }, []);

    return {
        workingWorkers,
        setWorkingWorkers,
        workerStats,
        fetchWorkerStats,
        fetchWorkingWorkersWithLocation
    };
};

const useBlueprintState = () => {
    const [currentBlueprint, setCurrentBlueprint] = useState(null);
    const [blueprintImage, setBlueprintImage] = useState(null);
    const [availableBlueprints, setAvailableBlueprints] = useState([]);

    const fetchAvailableBlueprints = useCallback(async () => {
        try {
            const response = await blueprintAPI.getBlueprints({
                page: 0,
                size: 50
            });

            if (response.status === 'success' && response.data) {
                setAvailableBlueprints(response.data.content || []);
            } else {
                setAvailableBlueprints([]);
            }
        } catch (error) {
            console.error('도면 목록 조회 실패:', error);
            setAvailableBlueprints([]);
        }
    }, []);

    const selectBlueprint = useCallback(async (blueprintId) => {
        if (!blueprintId) {
            setCurrentBlueprint(null);
            setBlueprintImage(null);
            return null;
        }

        try {
            const blueprint = availableBlueprints.find(bp => bp.id === parseInt(blueprintId));
            if (blueprint) {
                setCurrentBlueprint(blueprint);

                try {
                    const blobUrl = await blueprintAPI.getBlueprintImageBlob(blueprint.id);
                    setBlueprintImage(blobUrl);
                } catch (imageError) {
                    console.error('도면 이미지 로드 실패:', imageError);
                    setBlueprintImage(null);
                }

                return blueprint;
            } else {
                console.warn(`Blueprint ID ${blueprintId}를 찾을 수 없습니다.`);
                setCurrentBlueprint(null);
                setBlueprintImage(null);
                return null;
            }
        } catch (error) {
            console.error('도면 선택 실패:', error);
            setCurrentBlueprint(null);
            setBlueprintImage(null);
            return null;
        }
    }, [availableBlueprints]);

    // Cleanup effect
    useEffect(() => {
        return () => {
            if (blueprintImage?.startsWith('blob:')) {
                URL.revokeObjectURL(blueprintImage);
            }
        };
    }, [blueprintImage]);

    return {
        currentBlueprint,
        blueprintImage,
        availableBlueprints,
        fetchAvailableBlueprints,
        selectBlueprint
    };
};

const useRiskZoneState = () => {
    const [dangerZones, setDangerZones] = useState([]);

    const fetchRiskZonesForBlueprint = useCallback(async (blueprintId, blueprint) => {
        if (!blueprint) {
            setDangerZones([]);
            return;
        }

        try {
            const response = await riskZoneAPI.getRiskZones({
                page: 0,
                size: 100,
                blueprintId: blueprintId
            });

            const data = response.data || response;
            const zones = data.content || [];

            const formattedZones = dataTransformUtils.transformRiskZoneData(zones, blueprintId, blueprint);
            setDangerZones(formattedZones);
        } catch (error) {
            console.error('위험구역 데이터 조회 실패:', error);
            setDangerZones([]);
        }
    }, []);

    return {
        dangerZones,
        fetchRiskZonesForBlueprint
    };
};

const useAlertState = () => {
    const [alerts, setAlerts] = useState([]);
    const [alertsLoading, setAlertsLoading] = useState(true);
    const {getAlertIcon, getAlertTypeFromData, convertToDashboardType, getAlertTitle, getTimeAgo} = useAlarmData();

    const loadAlarms = useCallback(async () => {
        setAlertsLoading(true);
        try {
            const response = await alarmAPI.getAlarmsForAdmin(PAGINATION_CONFIG);

            const apiAlerts = response.data?.content?.map(alarm => {
                const alertType = getAlertTypeFromData(alarm.incidentType, alarm.incidentDescription);
                const dashboardType = convertToDashboardType(alertType);

                return {
                    id: alarm.id,
                    type: dashboardType,
                    title: getAlertTitle(alertType, alarm.incidentDescription),
                    description: alarm.incidentDescription || '알림 내용',
                    time: getTimeAgo(alarm.createdAt || alarm.timestamp || new Date().toISOString()),
                    timestamp: alarm.createdAt || alarm.timestamp || new Date().toISOString(),
                    workerId: alarm.workerId,
                    workerName: alarm.workerName,
                    originalData: alarm
                };
            }) || [];

            setAlerts(apiAlerts);
        } catch (error) {
            console.error('알람 목록 로드 실패:', error);
        } finally {
            setAlertsLoading(false);
        }
    }, [getAlertTypeFromData, convertToDashboardType, getAlertTitle, getTimeAgo]);

    return {
        alerts,
        setAlerts,
        alertsLoading,
        loadAlarms,
        getAlertIcon,
        getAlertTypeFromData,
        convertToDashboardType,
        getAlertTitle,
        getTimeAgo
    };
};

// 메인 컴포넌트
const MonitoringPage = () => {
    const mapRef = useRef(null);
    const [selectedFilter, setSelectedFilter] = useState({
        attribute: 'all',
        riskLevel: 'all',
        zone: 'all'
    });
    const [isAlarmModalOpen, setIsAlarmModalOpen] = useState(false);

    // 커스텀 훅 사용
    const {
        workingWorkers,
        setWorkingWorkers,
        workerStats,
        fetchWorkerStats,
        fetchWorkingWorkersWithLocation
    } = useWorkerState();

    const {
        currentBlueprint,
        blueprintImage,
        availableBlueprints,
        fetchAvailableBlueprints,
        selectBlueprint
    } = useBlueprintState();

    const {
        dangerZones,
        fetchRiskZonesForBlueprint
    } = useRiskZoneState();

    const {
        alerts,
        setAlerts,
        alertsLoading,
        loadAlarms,
        getAlertIcon,
        getAlertTypeFromData,
        convertToDashboardType,
        getAlertTitle,
        getTimeAgo
    } = useAlertState();

    // 현장 현황 계산
    const fieldStatus = {
        totalWorkers: workerStats.working || 0,
        safeWorkers: workingWorkers.filter(w => w.status === 'safe').length,
        warningWorkers: workingWorkers.filter(w => w.status === 'warning').length,
        dangerWorkers: workingWorkers.filter(w => w.status === 'danger').length
    };

    // 근로자 상태 업데이트 공통 함수
    const updateWorkerStatus = useCallback((workerId, incidentType, additionalData = {}) => {
        setWorkingWorkers(prevWorkers => {
            return prevWorkers.map(worker => {
                if (worker.workerId.toString() === workerId.toString()) {
                    let newStatus = worker.status;

                    switch (incidentType) {
                        case 'PPE_VIOLATION':
                            newStatus = 'warning';
                            break;
                        case 'DANGER_ZONE':
                        case 'HEALTH_RISK':
                            newStatus = 'danger';
                            break;
                        case 'sensor_update':
                            // 센서 업데이트는 상태 변경 없음
                            break;
                        default:
                            break;
                    }

                    return {
                        ...worker,
                        ...additionalData,
                        status: newStatus,
                        ...(incidentType !== 'sensor_update' && {
                            lastAlarmType: incidentType,
                            lastAlarmTime: new Date().toISOString()
                        })
                    };
                }
                return worker;
            });
        });
    }, [setWorkingWorkers]);

    // 실시간 센서 데이터 처리
    const handleSensorUpdate = useCallback((data) => {
        if (data.type === 'sensor_update') {
            updateWorkerStatus(data.workerId, 'sensor_update', {
                latitude: data.latitude,
                longitude: data.longitude,
                heartRate: data.heartRate,
                steps: data.steps,
                lastUpdate: new Date().toISOString()
            });
        }
    }, [updateWorkerStatus]);

    // 새로운 알림 처리
    const handleNewAlarm = useCallback((data) => {
        const alertType = getAlertTypeFromData(data.incidentType, data.incidentDescription);
        const dashboardType = convertToDashboardType(alertType);

        let workerName = null;
        let workerId = null;
        if (alertType !== 'PPE_VIOLATION') {
            workerId = data.workerId;
            if (data.workerName) {
                workerName = data.workerName;
            } else if (data.workerId) {
                const worker = workingWorkers.find(w => w.workerId.toString() === data.workerId.toString());
                workerName = worker?.name || worker?.workerName;
            }
        }

        const newAlert = {
            id: data.id || Date.now(),
            type: dashboardType,
            title: getAlertTitle(alertType, data.incidentDescription),
            description: data.incidentDescription || '알림 내용',
            time: '방금 전',
            timestamp: new Date().toISOString(),
            workerId: workerId,
            workerName: workerName,
            originalData: data
        };

        setAlerts(prevAlerts => [newAlert, ...prevAlerts.slice(0, 2)]);

        // 근로자 상태 업데이트
        if (data.workerId) {
            updateWorkerStatus(data.workerId, data.incidentType);
        }
    }, [getAlertTypeFromData, convertToDashboardType, getAlertTitle, workingWorkers, setAlerts, updateWorkerStatus]);

    // 웹소켓 연결 및 이벤트 처리
    useEffect(() => {
        const token = authUtils.getToken();
        if (!token) return;

        const connectWebSockets = async () => {
            try {
                if (!alarmStompService.isConnected()) {
                    await alarmStompService.connect(token, 'admin');
                }
                if (!sensorStompService.isConnected()) {
                    await sensorStompService.connect(token, 'admin');
                }
            } catch (error) {
                console.error('웹소켓 연결 실패:', error);
            }
        };

        alarmStompService.on('alarm', handleNewAlarm);
        sensorStompService.on('sensor-update', handleSensorUpdate);

        connectWebSockets().catch(error => {
            console.error('웹소켓 초기 연결 실패:', error);
        });

        return () => {
            alarmStompService.off('alarm', handleNewAlarm);
            sensorStompService.off('sensor-update', handleSensorUpdate);
        };
    }, [handleNewAlarm, handleSensorUpdate]);

    // 데이터 초기 로드
    useEffect(() => {
        const initializeData = async () => {
            await Promise.all([
                fetchWorkerStats(),
                fetchWorkingWorkersWithLocation(),
                loadAlarms(),
                fetchAvailableBlueprints()
            ]);
        };

        initializeData().catch(error => {
            console.error('데이터 초기화 실패:', error);
        });
    }, [fetchWorkerStats, fetchWorkingWorkersWithLocation, loadAlarms, fetchAvailableBlueprints]);

    // 주기적 업데이트
    useEffect(() => {
        const workerStatsInterval = setInterval(fetchWorkerStats, UPDATE_INTERVALS.WORKER_STATS);
        const timeUpdateInterval = setInterval(() => {
            setAlerts(prevAlerts =>
                prevAlerts.map(alert => ({
                    ...alert,
                    time: getTimeAgo(alert.timestamp)
                }))
            );
        }, UPDATE_INTERVALS.TIME_DISPLAY);

        return () => {
            clearInterval(workerStatsInterval);
            clearInterval(timeUpdateInterval);
        };
    }, [fetchWorkerStats, getTimeAgo, setAlerts]);

    // 초기 도면 자동 선택
    useEffect(() => {
        if (availableBlueprints.length > 0 && !currentBlueprint) {
            const firstBlueprint = availableBlueprints[0];
            selectBlueprint(firstBlueprint.id.toString()).catch(error => {
                console.error('초기 도면 선택 실패:', error);
            });
        }
    }, [availableBlueprints, currentBlueprint, selectBlueprint]);

    // 도면 변경 시 위험구역 로드
    useEffect(() => {
        if (currentBlueprint) {
            fetchRiskZonesForBlueprint(currentBlueprint.id, currentBlueprint).catch(error => {
                console.error('위험구역 로드 실패:', error);
            });
        }
    }, [currentBlueprint, fetchRiskZonesForBlueprint]);

    // 이벤트 핸들러들
    const handleFilterChange = (filterType, value) => {
        setSelectedFilter(prev => ({
            ...prev,
            [filterType]: value
        }));
    };

    const handleWorkerClick = (worker) => {
        const statusText = {
            safe: '정상',
            warning: '주의',
            danger: '위험'
        }[worker.status] || '알 수 없음';

        alert(`작업자: ${worker.name}\n상태: ${statusText}`);
    };

    const handleBlueprintChange = async (e) => {
        const blueprint = await selectBlueprint(e.target.value);
        if (blueprint) {
            await fetchRiskZonesForBlueprint(blueprint.id, blueprint);
        }
    };

    // 필터된 작업자 목록
    const filteredWorkers = workingWorkers
        .filter(worker => {
            if (selectedFilter.attribute === 'all') return true;
            return worker.status === selectedFilter.attribute;
        })
        .map(worker => {
            const canvasPosition = coordinateUtils.convertGPSToCanvas(
                worker.latitude,
                worker.longitude,
                currentBlueprint
            );
            return {
                ...worker,
                x: canvasPosition.x,
                y: canvasPosition.y
            };
        });

    return (
        <div className={styles.page}>
            <header className={styles.pageHeader}>
                <h1 className={styles.pageTitle}>실시간 모니터링</h1>
            </header>

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

            <div className={styles.contentSection}>
                <section className={styles.mapSection}>
                    <div className={styles.mapHeader}>
                        <h2 className={styles.mapTitle}>현장 도면 - 실시간 위치</h2>
                        <select
                            className={styles.blueprintSelect}
                            value={currentBlueprint?.id || ''}
                            onChange={handleBlueprintChange}
                        >
                            <option value="">도면 선택</option>
                            {availableBlueprints.map(blueprint => (
                                <option key={blueprint.id} value={blueprint.id}>
                                    {blueprint.name && blueprint.name.trim() ?
                                        `${blueprint.name} (${blueprint.floor}층)` :
                                        `${blueprint.floor}층 도면`
                                    }
                                </option>
                            ))}
                        </select>
                    </div>

                    <div className={styles.mapContainer} ref={mapRef}>
                        <div
                            className={styles.mapCanvas}
                            style={{
                                backgroundImage: blueprintImage ? `url(${blueprintImage})` : 'none',
                                backgroundSize: 'contain',
                                backgroundRepeat: 'no-repeat',
                                backgroundPosition: 'center'
                            }}
                        >
                            {!blueprintImage && (
                                <div className={styles.noBlueprintMessage}>
                                    {currentBlueprint ?
                                        `${currentBlueprint.name || `${currentBlueprint.floor}층`} 도면을 로드하는 중...` :
                                        '도면을 선택해주세요'
                                    }
                                </div>
                            )}

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

                            {filteredWorkers.map(worker => (
                                <div
                                    key={worker.id}
                                    className={`${styles.workerDot} ${styles[worker.status]}`}
                                    style={{
                                        left: `${worker.x}%`,
                                        top: `${worker.y}%`
                                    }}
                                    onClick={() => handleWorkerClick(worker)}
                                    title={`${worker.name} - ${worker.status === 'safe' ? '정상' : worker.status === 'warning' ? '주의' : '위험'}`}
                                />
                            ))}
                        </div>
                    </div>

                    <div className={styles.mapLegend}>
                        <div className={styles.legendGroup}>
                            <div className={styles.legendItem}>
                                <div className={`${styles.legendColor} ${styles.safe}`}></div>
                                <span>정상({fieldStatus.safeWorkers}명)</span>
                            </div>
                            <div className={styles.legendItem}>
                                <div className={`${styles.legendColor} ${styles.warning}`}></div>
                                <span>주의({fieldStatus.warningWorkers}명)</span>
                            </div>
                            <div className={styles.legendItem}>
                                <div className={`${styles.legendColor} ${styles.danger}`}></div>
                                <span>위험({fieldStatus.dangerWorkers}명)</span>
                            </div>
                        </div>

                        <div className={styles.legendGroup}>
                            <div className={styles.legendItem}>
                                <div className={`${styles.legendZone} ${styles.high}`}></div>
                                <span>위험구역</span>
                            </div>
                        </div>
                    </div>
                </section>

                <aside className={styles.infoPanel}>
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
                            안전: {fieldStatus.safeWorkers}명 | 주의: {fieldStatus.warningWorkers}명 |
                            위험: {fieldStatus.dangerWorkers}명
                        </p>

                        <button className={styles.statusButton}
                                onClick={() => window.open('https://e5f9364d318d.ngrok-free.app/monitor', '_blank')}
                        >
                            모니터링
                        </button>
                    </div>

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
                                            {alert.type !== 'warning' && (
                                                <p className={styles.alertWorker}>작업자: {alert.workerName || "알 수 없음"}</p>
                                            )}
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
                                📋 최근 {PAGINATION_CONFIG.hours}시간 내 알림이 없습니다.
                            </div>
                        )}
                    </div>
                </aside>
            </div>

            <AlarmModal
                isOpen={isAlarmModalOpen}
                onClose={() => setIsAlarmModalOpen(false)}
            />
        </div>
    );
};

export default MonitoringPage;