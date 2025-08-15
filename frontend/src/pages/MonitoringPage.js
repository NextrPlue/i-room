import React, {useState, useEffect, useRef, useCallback} from 'react';
import styles from '../styles/Monitoring.module.css';
import AlarmModal from '../components/AlarmModal';
import stompService from '../services/stompService';
import { authUtils } from '../utils/auth';
import { alarmAPI, blueprintAPI, riskZoneAPI } from '../api/api';
import { useAlarmData } from '../hooks/useAlarmData';

const MonitoringPage = () => {
    const mapRef = useRef(null);
    const [selectedFilter, setSelectedFilter] = useState({
        attribute: 'all',
        riskLevel: 'all',
        zone: 'all'
    });
    
    const { getAlertIcon, getAlertTypeFromData, convertToDashboardType, getAlertTitle, getTimeAgo } = useAlarmData();

    // 작업자 위치 데이터 (GPS 좌표 기반 - 실제로는 API에서 가져와야 함)
    const [workers] = useState([
        {id: 1, latitude: 37.5665, longitude: 126.9780, status: 'danger', name: '김철수'},
        {id: 2, latitude: 37.5666, longitude: 126.9781, status: 'warning', name: '이영희'},
        {id: 3, latitude: 37.5667, longitude: 126.9782, status: 'safe', name: '박민수'},
        {id: 4, latitude: 37.5668, longitude: 126.9783, status: 'safe', name: '정수진'},
        {id: 5, latitude: 37.5669, longitude: 126.9784, status: 'warning', name: '한지민'},
        {id: 6, latitude: 37.5670, longitude: 126.9785, status: 'safe', name: '조현우'},
        {id: 7, latitude: 37.5671, longitude: 126.9786, status: 'danger', name: '윤서연'},
        {id: 8, latitude: 37.5672, longitude: 126.9787, status: 'safe', name: '장동건'}
    ]);

    // 도면 관련 상태
    const [currentBlueprint, setCurrentBlueprint] = useState(null);
    const [blueprintImage, setBlueprintImage] = useState(null);
    const [availableBlueprints, setAvailableBlueprints] = useState([]);

    // 위험구역 데이터 (실제 API에서 가져옴)
    const [dangerZones, setDangerZones] = useState([]);

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

    // 사용 가능한 도면 목록 조회
    const fetchAvailableBlueprints = useCallback(async () => {
        try {
            const response = await blueprintAPI.getBlueprints({
                page: 0,
                size: 50
            });

            if (response.status === 'success' && response.data) {
                const data = response.data;
                const blueprints = data.content || [];
                setAvailableBlueprints(blueprints);
            } else {
                setAvailableBlueprints([]);
            }
        } catch (error) {
            console.error('도면 목록 조회 실패:', error);
            setAvailableBlueprints([]);
        }
    }, []);

    // 특정 도면 선택 및 로드
    const selectBlueprint = useCallback(async (blueprintId) => {
        if (!blueprintId) {
            setCurrentBlueprint(null);
            setBlueprintImage(null);
            setDangerZones([]);
            return;
        }

        try {
            const blueprint = availableBlueprints.find(bp => bp.id === parseInt(blueprintId));
            if (blueprint) {
                setCurrentBlueprint(blueprint);
                console.log(`도면 선택됨 - ID: ${blueprint.id}, Name: ${blueprint.name || `${blueprint.floor}층`}`);
                console.log('4개 꼭짓점 좌표:', {
                    topLeft: blueprint.topLeft,
                    topRight: blueprint.topRight,
                    bottomRight: blueprint.bottomRight,
                    bottomLeft: blueprint.bottomLeft
                });
                
                // 도면 이미지 Blob URL 생성
                try {
                    const blobUrl = await blueprintAPI.getBlueprintImageBlob(blueprint.id);
                    setBlueprintImage(blobUrl);
                } catch (imageError) {
                    console.error('도면 이미지 로드 실패:', imageError);
                    setBlueprintImage(null);
                }

                // 해당 도면의 위험구역 데이터 조회
                await fetchRiskZonesForBlueprint(blueprint.id);
            } else {
                console.warn(`Blueprint ID ${blueprintId}를 찾을 수 없습니다.`);
                setCurrentBlueprint(null);
                setBlueprintImage(null);
                setDangerZones([]);
            }
        } catch (error) {
            console.error('도면 선택 실패:', error);
            setCurrentBlueprint(null);
            setBlueprintImage(null);
            setDangerZones([]);
        }
    }, [availableBlueprints]);

    // 특정 도면의 위험구역 데이터 조회
    const fetchRiskZonesForBlueprint = useCallback(async (blueprintId) => {
        try {
            const response = await riskZoneAPI.getRiskZones({
                page: 0,
                size: 100, // 모든 위험구역 가져오기
                blueprintId: blueprintId // 특정 도면의 위험구역만
            });

            const data = response.data || response;
            const zones = data.content || [];
            
            console.log(`MonitoringPage - Blueprint ${blueprintId}의 위험구역 조회 결과:`, {
                총개수: zones.length,
                위험구역들: zones.map(z => ({ id: z.id, name: z.name, lat: z.latitude, lon: z.longitude }))
            });
            
            // 위험구역을 화면에 표시하기 위한 형태로 변환
            const formattedZones = zones
                .map(zone => {
                    // GPS 좌표를 캔버스 좌표로 변환
                    const canvasPosition = convertGPSToCanvas(zone.latitude, zone.longitude);
                    const canvasSize = convertMetersToCanvas(zone.width, zone.height);
                    
                    // 중심점 기준으로 박스 위치 계산
                    const boxX = canvasPosition.x - canvasSize.width / 2;
                    const boxY = canvasPosition.y - canvasSize.height / 2;
                    
                    console.log(`위험구역 ${zone.id} 변환:`, {
                        GPS: { lat: zone.latitude, lon: zone.longitude },
                        캔버스중심: canvasPosition,
                        박스크기: canvasSize,
                        최종박스: { x: boxX, y: boxY, width: canvasSize.width, height: canvasSize.height },
                        도면내부여부: isInsideBlueprint(canvasPosition.x, canvasPosition.y)
                    });
                    
                    return {
                        id: zone.id,
                        x: boxX,
                        y: boxY,
                        width: canvasSize.width,
                        height: canvasSize.height,
                        level: 'high', // 기본값, 필요시 API에서 레벨 정보 추가
                        name: zone.name || `위험구역 ${zone.id}`,
                        isInside: isInsideBlueprint(canvasPosition.x, canvasPosition.y), // 도면 영역 내부 여부
                        centerX: canvasPosition.x, // 디버깅용
                        centerY: canvasPosition.y  // 디버깅용
                    };
                })
                .filter(zone => {
                    // 도면 영역 밖의 위험구역은 필터링 (옵션)
                    if (!zone.isInside) {
                        console.warn(`위험구역 ${zone.id}(${zone.name})이 도면 영역을 벗어났습니다.`, 
                            { center: { x: zone.centerX, y: zone.centerY } });
                        return false; // 도면 밖 위험구역 제외
                    }
                    return true;
                });
            
            console.log(`MonitoringPage - 필터링 후 최종 위험구역:`, {
                필터링전개수: zones.length,
                필터링후개수: formattedZones.length,
                제거된개수: zones.length - formattedZones.length,
                최종위험구역들: formattedZones.map(z => ({ id: z.id, name: z.name, x: z.x, y: z.y, isInside: z.isInside }))
            });
            
            setDangerZones(formattedZones);
        } catch (error) {
            console.error('위험구역 데이터 조회 실패:', error);
            setDangerZones([]);
        }
    }, []);

    // GPS 좌표를 캔버스 좌표로 변환 (RiskZonePage convertCanvasToGPS의 역변환)
    const convertGPSToCanvas = (lat, lon) => {
        if (!currentBlueprint || !currentBlueprint.topLeft || !currentBlueprint.topRight || 
            !currentBlueprint.bottomLeft || !currentBlueprint.bottomRight) {
            return { x: 50, y: 50 }; // 기본값
        }

        const { topLeft, topRight, bottomLeft, bottomRight } = currentBlueprint;
        
        // 더 정확한 그리드 서치로 최적의 u, v 찾기 (정밀도 향상: 0.01 → 0.005)
        let bestU = 0.5, bestV = 0.5;
        let minError = Infinity;
        
        // 1차: 거친 그리드 서치 (0.05 간격)
        for (let u = 0; u <= 1; u += 0.05) {
            for (let v = 0; v <= 1; v += 0.05) {
                const expectedLat = (1-u)*(1-v)*topLeft.lat + u*(1-v)*topRight.lat + 
                                   (1-u)*v*bottomLeft.lat + u*v*bottomRight.lat;
                const expectedLon = (1-u)*(1-v)*topLeft.lon + u*(1-v)*topRight.lon + 
                                   (1-u)*v*bottomLeft.lon + u*v*bottomRight.lon;
                
                const error = Math.abs(expectedLat - lat) + Math.abs(expectedLon - lon);
                
                if (error < minError) {
                    minError = error;
                    bestU = u;
                    bestV = v;
                }
            }
        }
        
        // 2차: 세밀한 그리드 서치 (bestU, bestV 주변 0.002 간격)
        const searchRange = 0.05;
        const step = 0.002;
        const minU = Math.max(0, bestU - searchRange);
        const maxU = Math.min(1, bestU + searchRange);
        const minV = Math.max(0, bestV - searchRange);
        const maxV = Math.min(1, bestV + searchRange);
        
        for (let u = minU; u <= maxU; u += step) {
            for (let v = minV; v <= maxV; v += step) {
                const expectedLat = (1-u)*(1-v)*topLeft.lat + u*(1-v)*topRight.lat + 
                                   (1-u)*v*bottomLeft.lat + u*v*bottomRight.lat;
                const expectedLon = (1-u)*(1-v)*topLeft.lon + u*(1-v)*topRight.lon + 
                                   (1-u)*v*bottomLeft.lon + u*v*bottomRight.lon;
                
                const error = Math.abs(expectedLat - lat) + Math.abs(expectedLon - lon);
                
                if (error < minError) {
                    minError = error;
                    bestU = u;
                    bestV = v;
                }
            }
        }
        
        // 정규화된 좌표를 캔버스 좌표(%)로 변환
        const x = bestU * 100;
        const y = bestV * 100;
        
        return { x: Math.max(0, Math.min(100, x)), y: Math.max(0, Math.min(100, y)) };
    };

    // 미터를 캔버스 크기로 변환 (RiskZonePage와 동일한 로직)
    const convertMetersToCanvas = (widthMeters, heightMeters) => {
        if (!currentBlueprint || !currentBlueprint.width || !currentBlueprint.height) {
            return { width: 5, height: 5 }; // 기본값
        }

        console.log('박스 크기 변환:', {
            입력크기: { width: widthMeters, height: heightMeters },
            도면크기: { width: currentBlueprint.width, height: currentBlueprint.height },
            단위: '미터'
        });

        // Blueprint의 width, height가 픽셀이면 실제 건물 크기로 가정
        // 예: 1920x1080 픽셀 → 192m x 108m 건물로 가정 (1픽셀 = 0.1m)
        let realBuildingWidth, realBuildingHeight;
        
        if (currentBlueprint.width > 100) {
            // 픽셀로 추정 (1920 같은 큰 값)
            realBuildingWidth = currentBlueprint.width * 0.05; // 1픽셀 = 5cm로 가정
            realBuildingHeight = currentBlueprint.height * 0.05;
        } else {
            // 이미 미터 단위로 추정
            realBuildingWidth = currentBlueprint.width;
            realBuildingHeight = currentBlueprint.height;
        }

        console.log('실제 건물 크기 (추정):', { width: realBuildingWidth, height: realBuildingHeight });

        // 박스 크기를 각각 독립적으로 계산
        let widthRatio = (widthMeters / realBuildingWidth);
        let heightRatio = (heightMeters / realBuildingHeight);
        
        // 박스가 너무 크면 (30% 이상) 스케일 다운
        if (widthRatio > 0.3) {
            widthRatio = widthRatio * 0.3; // 30% 이하로 제한
        }
        if (heightRatio > 0.3) {
            heightRatio = heightRatio * 0.3; // 30% 이하로 제한
        }
        
        const canvasWidth = widthRatio * 80; // 80% 영역 사용
        const canvasHeight = heightRatio * 80; // 80% 영역 사용
        
        const result = { width: canvasWidth, height: canvasHeight };
        console.log('캔버스 크기 (%):', result);
        
        return result;
    };

    // 도면 이미지 영역 내부인지 확인 (RiskZonePage와 동일)
    const isInsideBlueprint = (canvasX, canvasY) => {
        // 도면 이미지는 contain으로 center에 위치하므로 실제 이미지 영역 계산 필요
        // 간단히 캔버스 중앙 80% 영역으로 제한 (실제로는 이미지 크기에 따라 달라짐)
        const margin = 10; // 10% 여백
        return canvasX >= margin && canvasX <= (100 - margin) && 
               canvasY >= margin && canvasY <= (100 - margin);
    };


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
                    workerName: alarm.workerName,
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
        fetchAvailableBlueprints();
    }, [alertsPagination.page, alertsPagination.size, alertsPagination.hours, fetchAvailableBlueprints]);

    // 초기 도면 자동 선택 (첫 번째 도면)
    useEffect(() => {
        if (availableBlueprints.length > 0 && !currentBlueprint) {
            const firstBlueprint = availableBlueprints[0];
            selectBlueprint(firstBlueprint.id.toString());
        }
    }, [availableBlueprints, currentBlueprint, selectBlueprint]);

    // 컴포넌트 언마운트 시 blob URL 정리
    useEffect(() => {
        return () => {
            if (blueprintImage && blueprintImage.startsWith('blob:')) {
                URL.revokeObjectURL(blueprintImage);
            }
        };
    }, [blueprintImage]);

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

    // 필터된 작업자 목록 (GPS 좌표를 캔버스 좌표로 변환)
    const filteredWorkers = workers
        .filter(worker => {
            if (selectedFilter.attribute === 'all') return true;
            return worker.status === selectedFilter.attribute;
        })
        .map(worker => {
            // GPS 좌표를 캔버스 좌표로 변환
            const canvasPosition = convertGPSToCanvas(worker.latitude, worker.longitude);
            return {
                ...worker,
                x: canvasPosition.x,
                y: canvasPosition.y
            };
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
                    <div className={styles.mapHeader}>
                        <h2 className={styles.mapTitle}>현장 도면 - 실시간 위치</h2>
                        <select
                            className={styles.blueprintSelect}
                            value={currentBlueprint?.id || ''}
                            onChange={(e) => selectBlueprint(e.target.value)}
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
                            {/* 도면이 없는 경우 안내 메시지 */}
                            {!blueprintImage && (
                                <div className={styles.noBlueprintMessage}>
                                    {currentBlueprint ? 
                                        `${currentBlueprint.name || `${currentBlueprint.floor}층`} 도면을 로드하는 중...` : 
                                        '도면을 선택해주세요'
                                    }
                                </div>
                            )}
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
                                            <p className={styles.alertWorker}>작업자: {alert.workerName || "알 수 없음"}</p>
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