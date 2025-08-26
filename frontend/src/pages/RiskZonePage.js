import React, { useState, useEffect, useRef, useCallback, useMemo } from 'react';
import styles from '../styles/RiskZone.module.css';
import { riskZoneAPI, blueprintAPI } from '../api/api';
import SuccessModal from '../components/SuccessModal';
import ConfirmModal from '../components/ConfirmModal';

const RiskZonePage = () => {
    const canvasRef = useRef(null);

    // 페이지네이션 상태
    const [currentPage, setCurrentPage] = useState(0);
    const [pageSize] = useState(5);
    const [totalPages, setTotalPages] = useState(1);

    // 위험구역 관련 상태
    const [riskZones, setRiskZones] = useState([]);
    const [displayRiskZones, setDisplayRiskZones] = useState([]); // 현재 도면에 표시할 위험구역들
    const [isSelecting, setIsSelecting] = useState(false);
    const [clickedPoint, setClickedPoint] = useState(null);
    const [riskZoneForm, setRiskZoneForm] = useState({
        name: '',
        width: '1.0',
        height: '1.5',
        gpsLat: 0,
        gpsLon: 0
    });

    // 도면 관련 상태
    const [currentBlueprint, setCurrentBlueprint] = useState(null);
    const [blueprintImage, setBlueprintImage] = useState(null);
    const [availableBlueprintsForSelection, setAvailableBlueprintsForSelection] = useState([]);
    const [availableBlueprints, setAvailableBlueprints] = useState([]);

    // 수정 모달 상태
    const [isEditModalOpen, setIsEditModalOpen] = useState(false);
    const [editingZone, setEditingZone] = useState(null);
    const [editFormData, setEditFormData] = useState({
        floor: 1,
        latitude: '',
        longitude: '',
        width: '',
        height: '',
        name: ''
    });
    
    // 성공 모달 상태
    const [successModal, setSuccessModal] = useState({
        isOpen: false,
        title: '',
        message: ''
    });
    
    // 확인 모달 상태
    const [confirmModal, setConfirmModal] = useState({
        isOpen: false,
        targetZoneId: null
    });

    // 위험구역 데이터 조회
    const fetchRiskZones = useCallback(async () => {
        try {
            const response = await riskZoneAPI.getRiskZones({
                page: currentPage,
                size: pageSize
            });

            const data = response.data || response;
            const zones = data.content || [];

            // Helper function to format zone
            const formatZone = (zone, floorInfo = null) => ({
                id: zone.id,
                floor: floorInfo ? `${floorInfo}F` : `${zone.blueprintId}F`,
                name: zone.name || `위험구역 ${zone.id}`,
                latitude: zone.latitude,
                longitude: zone.longitude,
                width: `${zone.width} m`,
                height: `${zone.height} m`,
                blueprintId: zone.blueprintId,
                originalWidth: zone.width,
                originalHeight: zone.height
            });

            // Blueprint 정보 조회해서 floor 매핑
            const formattedZones = await Promise.all(
                zones.map(async (zone) => {
                    try {
                        const blueprintResponse = await blueprintAPI.getBlueprint(zone.blueprintId);
                        const blueprint = blueprintResponse.data || blueprintResponse;
                        return formatZone(zone, blueprint.floor);
                    } catch (error) {
                        console.error(`Blueprint ${zone.blueprintId} 조회 실패:`, error);
                        return formatZone(zone);
                    }
                })
            );

            setRiskZones(formattedZones);
            setTotalPages(data.totalPages || 1);

        } catch (error) {
            console.error('위험구역 데이터 조회 실패:', error);
            setRiskZones([]);
            setTotalPages(1);
            alert('위험구역 데이터를 불러오는데 실패했습니다.');
        }
    }, [currentPage, pageSize]);

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
                setAvailableBlueprintsForSelection(blueprints);
            } else {
                setAvailableBlueprints([]);
                setAvailableBlueprintsForSelection([]);
            }
        } catch (error) {
            console.error('도면 목록 조회 실패:', error);
            setAvailableBlueprints([]);
            setAvailableBlueprintsForSelection([]);
        }
    }, []);

    // 특정 Blueprint 선택 및 로드
    const selectBlueprint = useCallback(async (blueprintId) => {
        if (!blueprintId) {
            setCurrentBlueprint(null);
            setBlueprintImage(null);
            setDisplayRiskZones([]);
            return;
        }

        try {
            const parsedId = parseInt(blueprintId, 10);
            const blueprint = availableBlueprintsForSelection.find(bp => bp.id === parsedId);
            if (blueprint) {
                setCurrentBlueprint(blueprint);

                // 도면 이미지 Blob URL 생성 (인증 헤더 포함)
                try {
                    const blobUrl = await blueprintAPI.getBlueprintImageBlob(blueprint.id);
                    setBlueprintImage(blobUrl);
                } catch (imageError) {
                    console.error('도면 이미지 로드 실패:', imageError);
                    setBlueprintImage(null);
                }

                // 해당 도면의 위험구역 데이터 조회 및 표시
                await fetchDisplayRiskZonesForBlueprint(blueprint.id, blueprint);
            } else {
                console.warn(`Blueprint ID ${blueprintId}를 찾을 수 없습니다.`);
                setCurrentBlueprint(null);
                setBlueprintImage(null);
                setDisplayRiskZones([]);
            }
        } catch (error) {
            console.error('도면 선택 실패:', error);
            setCurrentBlueprint(null);
            setBlueprintImage(null);
            setDisplayRiskZones([]);
            alert('도면을 선택하는데 실패했습니다.');
        }
    }, [availableBlueprintsForSelection]);

    // 특정 도면의 위험구역 데이터를 조회하여 표시용으로 변환
    const fetchDisplayRiskZonesForBlueprint = useCallback(async (blueprintId, blueprint) => {
        // blueprint가 없으면 위험구역 목록 초기화
        if (!blueprint) {
            setDisplayRiskZones([]);
            return;
        }
        
        try {
            const requestParams = {
                page: 0,
                size: 100, // 모든 위험구역 가져오기
                blueprintId: blueprintId // 특정 도면의 위험구역만
            };
            
            const response = await riskZoneAPI.getRiskZones(requestParams);
            const data = response.data || response;
            const zones = data.content || [];
            
            // 위험구역을 화면에 표시하기 위한 형태로 변환 (현재 선택된 도면의 위험구역만)
            const formattedZones = zones
                .filter(zone => zone.blueprintId === blueprintId) // 클라이언트 사이드 필터링
                .map(zone => {
                    // GPS 좌표를 캔버스 좌표로 변환 (blueprint 직접 전달)
                    const canvasPosition = convertGPSToCanvasWithBlueprint(zone.latitude, zone.longitude, blueprint);
                    const canvasSize = convertMetersToCanvasWithBlueprint(zone.width, zone.height, blueprint);

                    // 중심점 기준으로 박스 위치 계산
                    const boxX = canvasPosition.x - canvasSize.width / 2;
                    const boxY = canvasPosition.y - canvasSize.height / 2;

                    return {
                        id: zone.id,
                        x: boxX,
                        y: boxY,
                        width: canvasSize.width,
                        height: canvasSize.height,
                        level: 'high', // 기본값, 필요시 API에서 레벨 정보 추가
                        name: zone.name || `위험구역 ${zone.id}`,
                        isInside: isInsideBlueprintArea(canvasPosition.x, canvasPosition.y), // 도면 영역 내부 여부
                        centerX: canvasPosition.x, // 디버깅용
                        centerY: canvasPosition.y  // 디버깅용
                    };
                })
                .filter(zone => {
                    // 도면 영역 밖의 위험구역은 필터링 (옵션)
                    if (!zone.isInside) {
                        console.warn(`위험구역 ${zone.id}(${zone.name})이 도면 영역을 벗어났습니다.`,
                            {center: {x: zone.centerX, y: zone.centerY}});
                        return false; // 도면 밖 위험구역 제외
                    }
                    return true;
                });

            setDisplayRiskZones(formattedZones);
        } catch (error) {
            console.error('위험구역 데이터 조회 실패:', error);
            setDisplayRiskZones([]);
        }
    }, []);

    // GPS 경계 계산을 별도 함수로 추출
    const getBlueprintBounds = useCallback((blueprint) => {
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
    }, []);

    // Blueprint를 매개변수로 받는 GPS → Canvas 변환 함수
    const convertGPSToCanvasWithBlueprint = useCallback((lat, lon, blueprint) => {
        const bounds = getBlueprintBounds(blueprint);
        if (!bounds) {
            return {x: 50, y: 50};
        }

        const {minLat, maxLat, minLon, maxLon} = bounds;
        
        // 단순 선형 변환 (경계 기반)
        const x = ((lon - minLon) / (maxLon - minLon)) * 100;
        const y = ((maxLat - lat) / (maxLat - minLat)) * 100; // Y축 반전
        
        return { x, y };
    }, [getBlueprintBounds]);

    // Blueprint를 매개변수로 받는 미터 → 캔버스 크기 변환 함수
    const convertMetersToCanvasWithBlueprint = useCallback((widthMeters, heightMeters, blueprint) => {
        if (!blueprint || !blueprint.width || !blueprint.height) {
            return {width: 5, height: 5}; // 기본값
        }

        // Blueprint의 width, height가 픽셀이면 실제 건물 크기로 가정
        let realBuildingWidth, realBuildingHeight;

        if (blueprint.width > 100) {
            // 픽셀로 추정 (1920 같은 큰 값)
            realBuildingWidth = blueprint.width * 0.05; // 1픽셀 = 5cm로 가정
            realBuildingHeight = blueprint.height * 0.05;
        } else {
            // 이미 미터 단위로 추정
            realBuildingWidth = blueprint.width;
            realBuildingHeight = blueprint.height;
        }

        const widthRatio = (widthMeters / realBuildingWidth);
        const heightRatio = (heightMeters / realBuildingHeight);
        
        const canvasWidth = Math.min(widthRatio * 80, 30); // 최대 30%로 제한
        const canvasHeight = Math.min(heightRatio * 80, 30); // 최대 30%로 제한

        return {width: canvasWidth, height: canvasHeight};
    }, []);

    // 도면 영역 내부인지 확인 (표시용)
    const isInsideBlueprintArea = useCallback((canvasX, canvasY) => {
        const margin = 10; // 10% 여백
        return canvasX >= margin && canvasX <= (100 - margin) &&
               canvasY >= margin && canvasY <= (100 - margin);
    }, []);


    // 캔버스 좌표를 GPS 좌표로 변환
    const convertCanvasToGPS = useCallback((canvasX, canvasY) => {
        const bounds = getBlueprintBounds(currentBlueprint);
        if (!bounds) {
            console.warn('Blueprint 좌표 정보가 없습니다');
            return { lat: 0, lon: 0 };
        }

        const {minLat, maxLat, minLon, maxLon} = bounds;
        
        // 🔍 RiskZone 디버그 로그
        console.log('=== RiskZone 좌표 변환 디버그 ===');
        console.log(`클릭 위치: ${canvasX.toFixed(2)}%, ${canvasY.toFixed(2)}%`);
        console.log(`도면 범위: 위도 ${minLat}~${maxLat}, 경도 ${minLon}~${maxLon}`);
        
        // 단순 선형 변환 (MonitoringPage와 동일한 방식)
        const lon = minLon + (canvasX / 100) * (maxLon - minLon);
        const lat = maxLat - (canvasY / 100) * (maxLat - minLat); // Y축 반전
        
        console.log(`변환 결과: 위도 ${lat}, 경도 ${lon}`);
        console.log('===================================');
        
        return { lat, lon };
    }, [currentBlueprint, getBlueprintBounds]);


    // 도면 이미지 영역 내부인지 확인
    const isInsideBlueprint = useCallback((canvasX, canvasY) => {
        const margin = 10;
        return canvasX >= margin && canvasX <= (100 - margin) &&
               canvasY >= margin && canvasY <= (100 - margin);
    }, []);

    // 캔버스 클릭 이벤트 핸들러
    const handleCanvasClick = useCallback((e) => {
        if (!isSelecting || !currentBlueprint) {
            return;
        }

        const rect = canvasRef.current.getBoundingClientRect();
        const canvasX = ((e.clientX - rect.left) / rect.width) * 100;
        const canvasY = ((e.clientY - rect.top) / rect.height) * 100;

        if (!isInsideBlueprint(canvasX, canvasY)) {
            alert('도면 영역 내에서만 위치를 선택할 수 있습니다.');
            return;
        }

        const gpsCoord = convertCanvasToGPS(canvasX, canvasY);

        setClickedPoint({ x: canvasX, y: canvasY });
        setRiskZoneForm(prevForm => ({
            name: prevForm.name,
            width: prevForm.width,
            height: prevForm.height,
            gpsLat: gpsCoord.lat,
            gpsLon: gpsCoord.lon
        }));

        setIsSelecting(false);
    }, [isSelecting, currentBlueprint, isInsideBlueprint, convertCanvasToGPS]);

    // Select Location 버튼 클릭
    const handleSelectLocation = useCallback(() => {
        setIsSelecting(!isSelecting);
        setClickedPoint(null);
    }, [isSelecting]);

    // 미터를 캔버스 크기로 변환 (미리보기용)
    const convertMetersToCanvas = useCallback((widthMeters, heightMeters) => {
        if (!currentBlueprint?.width || !currentBlueprint?.height) {
            return { width: 0, height: 0 };
        }

        let realBuildingWidth, realBuildingHeight;
        
        if (currentBlueprint.width > 100) {
            realBuildingWidth = currentBlueprint.width * 0.05;
            realBuildingHeight = currentBlueprint.height * 0.05;
        } else {
            realBuildingWidth = currentBlueprint.width;
            realBuildingHeight = currentBlueprint.height;
        }

        const widthRatio = (widthMeters / realBuildingWidth);
        const heightRatio = (heightMeters / realBuildingHeight);
        
        const canvasWidth = Math.min(widthRatio * 80, 30); // 최대 30%로 제한
        const canvasHeight = Math.min(heightRatio * 80, 30); // 최대 30%로 제한
        
        return { width: canvasWidth, height: canvasHeight };
    }, [currentBlueprint]);

    // 위험구역 미리보기 박스 계산
    const getPreviewBox = useMemo(() => {
        if (!clickedPoint || !riskZoneForm.width || !riskZoneForm.height) {
            return null;
        }

        const widthNum = parseFloat(riskZoneForm.width);
        const heightNum = parseFloat(riskZoneForm.height);
        
        if (isNaN(widthNum) || isNaN(heightNum) || widthNum <= 0 || heightNum <= 0) {
            return null;
        }

        const boxSize = convertMetersToCanvas(widthNum, heightNum);
        
        return {
            left: `${clickedPoint.x - boxSize.width / 2}%`,
            top: `${clickedPoint.y - boxSize.height / 2}%`,
            width: `${boxSize.width}%`,
            height: `${boxSize.height}%`
        };
    }, [clickedPoint, riskZoneForm.width, riskZoneForm.height, convertMetersToCanvas]);


    // 위험구역 등록
    const handleCreateRiskZone = async () => {
        if (!clickedPoint) {
            alert('위치를 먼저 클릭해주세요.');
            return;
        }

        if (!currentBlueprint || !currentBlueprint.id) {
            alert('도면을 선택해주세요.');
            return;
        }

        if (!riskZoneForm.name.trim()) {
            alert('위험구역 이름을 입력해주세요.');
            return;
        }

        if (!riskZoneForm.width || !riskZoneForm.height ||
            parseFloat(riskZoneForm.width) <= 0 || parseFloat(riskZoneForm.height) <= 0) {
            alert('유효한 크기를 입력해주세요.');
            return;
        }

        try {
            const riskZoneData = {
                blueprintId: currentBlueprint.id,
                latitude: riskZoneForm.gpsLat,
                longitude: riskZoneForm.gpsLon,
                width: parseFloat(riskZoneForm.width),
                height: parseFloat(riskZoneForm.height),
                name: riskZoneForm.name.trim()
            };

            console.log('위험구역 등록 데이터:', riskZoneData);
            const response = await riskZoneAPI.createRiskZone(riskZoneData);
            console.log('위험구역 등록 완료:', response);

            showSuccessModal('등록 완료', '위험구역이 성공적으로 등록되었습니다.');

            // 폼 초기화
            setClickedPoint(null);
            setRiskZoneForm({
                name: '',
                width: '1.0',
                height: '1.5',
                gpsLat: 0,
                gpsLon: 0
            });

            // 목록 새로고침
            await fetchRiskZones();

            // 현재 도면의 위험구역 표시 새로고침
            if (currentBlueprint) {
                await fetchDisplayRiskZonesForBlueprint(currentBlueprint.id, currentBlueprint);
            }

        } catch (error) {
            console.error('위험구역 등록 실패:', error);
            alert(`등록에 실패했습니다: ${error.message}`);
        }
    };

    // 위험구역 수정 모달 열기
    const handleEdit = async (zone) => {
        setEditingZone(zone);

        // blueprintId로 해당 도면 정보 조회해서 floor 가져오기
        try {
            const blueprintResponse = await blueprintAPI.getBlueprint(zone.blueprintId);
            const blueprint = blueprintResponse.data || blueprintResponse;

            setEditFormData({
                floor: blueprint.floor || 1,
                latitude: String(zone.latitude || 0),
                longitude: String(zone.longitude || 0),
                width: String(zone.originalWidth || parseFloat(zone.width) || 0),
                height: String(zone.originalHeight || parseFloat(zone.height) || 0),
                name: zone.name || ''
            });
        } catch (error) {
            console.error('도면 정보 조회 실패:', error);
            setEditFormData({
                floor: 1,
                latitude: String(zone.latitude || 0),
                longitude: String(zone.longitude || 0),
                width: String(zone.originalWidth || parseFloat(zone.width) || 0),
                height: String(zone.originalHeight || parseFloat(zone.height) || 0),
                name: zone.name || ''
            });
        }

        setIsEditModalOpen(true);
    };

    // 수정 모달 닫기
    const handleCloseEditModal = () => {
        setIsEditModalOpen(false);
        setEditingZone(null);
        setEditFormData({
            floor: 1,
            latitude: '',
            longitude: '',
            width: '',
            height: '',
            name: ''
        });
    };

    // 수정 폼 데이터 변경
    const handleEditFormChange = useCallback((field, value) => {
        setEditFormData(prevData => ({
            ...prevData,
            [field]: value
        }));
    }, []);

    // 위험구역 수정 저장
    const handleSaveEdit = async () => {
        if (!editingZone) return;

        try {
            // 선택된 층수에 해당하는 blueprintId 찾기
            const selectedBlueprint = availableBlueprints.find(bp => bp.floor === editFormData.floor);
            if (!selectedBlueprint) {
                alert(`${editFormData.floor}층에 해당하는 도면을 찾을 수 없습니다.`);
                return;
            }

            const updateData = {
                blueprintId: selectedBlueprint.id,
                latitude: parseFloat(editFormData.latitude),
                longitude: parseFloat(editFormData.longitude),
                width: parseFloat(editFormData.width),
                height: parseFloat(editFormData.height),
                name: editFormData.name.trim() || `위험구역 ${editingZone.id}`
            };

            await riskZoneAPI.updateRiskZone(editingZone.id, updateData);

            showSuccessModal('수정 완료', '위험구역이 성공적으로 수정되었습니다.');
            handleCloseEditModal();

            // 목록 새로고침
            await fetchRiskZones();

            // 현재 도면의 위험구역 표시 새로고침
            if (currentBlueprint) {
                await fetchDisplayRiskZonesForBlueprint(currentBlueprint.id, currentBlueprint);
            }

        } catch (error) {
            console.error('위험구역 수정 실패:', error);
            alert(`수정에 실패했습니다: ${error.message}`);
        }
    };

    // 위험구역 삭제 확인 모달 열기
    const handleDelete = (zoneId) => {
        setConfirmModal({
            isOpen: true,
            targetZoneId: zoneId
        });
    };
    
    // 위험구역 삭제 실행
    const handleConfirmDelete = async () => {
        if (!confirmModal.targetZoneId) return;

        try {
            await riskZoneAPI.deleteRiskZone(confirmModal.targetZoneId);
            showSuccessModal('삭제 완료', '위험구역이 성공적으로 삭제되었습니다.');

            // 목록 새로고침
            await fetchRiskZones();

            // 현재 도면의 위험구역 표시 새로고침
            if (currentBlueprint) {
                await fetchDisplayRiskZonesForBlueprint(currentBlueprint.id, currentBlueprint);
            }
        } catch (error) {
            console.error('위험구역 삭제 실패:', error);
            alert(`삭제에 실패했습니다: ${error.message}`);
        } finally {
            handleCloseConfirmModal();
        }
    };

    // 성공 모달 표시
    const showSuccessModal = (title, message) => {
        setSuccessModal({
            isOpen: true,
            title: title,
            message: message
        });
    };

    // 성공 모달 닫기
    const handleCloseSuccessModal = () => {
        setSuccessModal({
            isOpen: false,
            title: '',
            message: ''
        });
    };
    
    // 확인 모달 닫기
    const handleCloseConfirmModal = () => {
        setConfirmModal({
            isOpen: false,
            targetZoneId: null
        });
    };


    // 컴포넌트 마운트 시 위험구역 목록과 도면 목록 조회
    useEffect(() => {
        const loadData = async () => {
            try {
                await Promise.all([
                    fetchRiskZones(),
                    fetchAvailableBlueprints()
                ]);
            } catch (error) {
                console.error('데이터 로드 실패:', error);
            }
        };
        loadData().catch(error => {
            console.error('데이터 로드 실패:', error);
        });
    }, [currentPage, fetchRiskZones, fetchAvailableBlueprints]);

    // 초기 도면 자동 선택 (첫 번째 도면)
    useEffect(() => {
        const selectFirstBlueprint = async () => {
            if (availableBlueprintsForSelection.length > 0 && !currentBlueprint) {
                const firstBlueprint = availableBlueprintsForSelection[0];
                if (firstBlueprint && firstBlueprint.id) {
                    try {
                        await selectBlueprint(String(firstBlueprint.id));
                    } catch (error) {
                        console.error('초기 도면 선택 실패:', error);
                    }
                }
            }
        };
        selectFirstBlueprint().catch(error => {
            console.error('초기 도면 선택 실패:', error);
        });
    }, [availableBlueprintsForSelection, currentBlueprint, selectBlueprint]);

    // 컴포넌트 언마운트 시 blob URL 정리
    useEffect(() => {
        return () => {
            if (blueprintImage && typeof blueprintImage === 'string' && blueprintImage.startsWith('blob:')) {
                URL.revokeObjectURL(blueprintImage);
            }
        };
    }, [blueprintImage]);

    // 현재 페이지 데이터
    const currentZones = useMemo(() => riskZones, [riskZones]);

    return (
        <div className={styles.page}>
            {/* 페이지 헤더 */}
            <header className={styles.pageHeader}>
                <h1 className={styles.pageTitle}>위험구역 설정 및 관리</h1>
            </header>

            {/* 상단 섹션 */}
            <section className={styles.topSection}>
                {/* 좌측: 위험구역 설정 */}
                <div className={styles.riskZoneSection}>
                    <div className={styles.riskZoneHeader}>
                        <h2 className={styles.sectionTitle}>위험 구역</h2>
                        <select
                            className={styles.floorSelect}
                            value={currentBlueprint?.id ? String(currentBlueprint.id) : ''}
                            onChange={(e) => {
                                const value = e.target.value;
                                if (value && value !== '') {
                                    selectBlueprint(value).catch(error => {
                                        console.error('도면 선택 실패:', error);
                                    });
                                } else {
                                    selectBlueprint('').catch(error => {
                                        console.error('도면 선택 실패:', error);
                                    });
                                }
                            }}
                        >
                            <option value="">도면 선택</option>
                            {availableBlueprintsForSelection.map(blueprint => (
                                <option key={String(blueprint.id)} value={String(blueprint.id)}>
                                    {blueprint.name && blueprint.name.trim() ?
                                        `${blueprint.name} (${blueprint.floor}층)` :
                                        `${blueprint.floor}층 도면`
                                    }
                                </option>
                            ))}
                        </select>
                    </div>

                    <div
                        className={`${styles.canvasContainer} ${isSelecting ? styles.drawing : ''}`}
                        ref={canvasRef}
                        onClick={handleCanvasClick}
                    >
                        <div
                            className={styles.canvas}
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

                            {/* 기존 위험구역 박스 렌더링 */}
                            {displayRiskZones.map(zone => (
                                <div
                                    key={`existing-${zone.id}`}
                                    className={styles.existingRiskZone}
                                    style={{
                                        position: 'absolute',
                                        left: `${zone.x}%`,
                                        top: `${zone.y}%`,
                                        width: `${zone.width}%`,
                                        height: `${zone.height}%`,
                                        border: '2px solid #ff8800',
                                        backgroundColor: 'rgba(255, 136, 0, 0.2)',
                                        pointerEvents: 'none',
                                        borderRadius: '4px',
                                        boxSizing: 'border-box'
                                    }}
                                    title={zone.name}
                                />
                            ))}

                            {/* 위험구역 미리보기 박스 */}
                            {getPreviewBox && (
                                <div
                                    className={styles.previewBox}
                                    style={{
                                        position: 'absolute',
                                        left: getPreviewBox.left,
                                        top: getPreviewBox.top,
                                        width: getPreviewBox.width,
                                        height: getPreviewBox.height,
                                        border: '2px solid #ff4444',
                                        backgroundColor: 'rgba(255, 68, 68, 0.3)',
                                        pointerEvents: 'none',
                                        borderRadius: '4px'
                                    }}
                                />
                            )}

                            {/* 클릭한 지점 표시 */}
                            {clickedPoint && (
                                <div
                                    className={styles.clickedPoint}
                                    style={{
                                        position: 'absolute',
                                        left: `${clickedPoint.x}%`,
                                        top: `${clickedPoint.y}%`,
                                        width: '8px',
                                        height: '8px',
                                        backgroundColor: '#ff4444',
                                        borderRadius: '50%',
                                        transform: 'translate(-50%, -50%)',
                                        pointerEvents: 'none',
                                        border: '2px solid white',
                                        boxShadow: '0 0 4px rgba(0,0,0,0.5)'
                                    }}
                                />
                            )}

                        </div>
                    </div>
                </div>

                {/* 우측: 위험구역 생성 폼 */}
                <div className={styles.locationPanel}>
                    <h3 className={styles.panelTitle}>위험구역 생성</h3>

                    <div className={styles.coordinateInfo}>
                        <p className={styles.coordinateText}>
                            GPS 좌표: {clickedPoint ?
                                `위도: ${riskZoneForm.gpsLat.toFixed(6)}, 경도: ${riskZoneForm.gpsLon.toFixed(6)}` :
                                '위치를 클릭해주세요'
                            }
                        </p>
                        <p className={styles.coordinateText}>
                            도면: {currentBlueprint ?
                                (currentBlueprint.name || `${currentBlueprint.floor}층 도면`) :
                                '도면을 선택해주세요'
                            }
                        </p>
                    </div>

                    <div className={styles.formGroup}>
                        <label className={styles.formLabel}>이름</label>
                        <input
                            type="text"
                            className={styles.formInput}
                            value={riskZoneForm.name}
                            onChange={(e) => {
                                setRiskZoneForm(prevForm => ({
                                    name: e.target.value,
                                    width: prevForm.width,
                                    height: prevForm.height,
                                    gpsLat: prevForm.gpsLat,
                                    gpsLon: prevForm.gpsLon
                                }));
                            }}
                            placeholder="위험구역 이름을 입력하세요"
                        />
                    </div>

                    <div className={styles.formGroup}>
                        <label className={styles.formLabel}>너비 (Width)</label>
                        <input
                            type="number"
                            step="0.1"
                            min="0.1"
                            className={styles.formInput}
                            value={riskZoneForm.width}
                            onChange={(e) => {
                                setRiskZoneForm(prevForm => ({
                                    name: prevForm.name,
                                    width: e.target.value,
                                    height: prevForm.height,
                                    gpsLat: prevForm.gpsLat,
                                    gpsLon: prevForm.gpsLon
                                }));
                            }}
                            placeholder="너비 (m)"
                        />
                    </div>

                    <div className={styles.formGroup}>
                        <label className={styles.formLabel}>높이 (Height)</label>
                        <input
                            type="number"
                            step="0.1"
                            min="0.1"
                            className={styles.formInput}
                            value={riskZoneForm.height}
                            onChange={(e) => {
                                setRiskZoneForm(prevForm => ({
                                    name: prevForm.name,
                                    width: prevForm.width,
                                    height: e.target.value,
                                    gpsLat: prevForm.gpsLat,
                                    gpsLon: prevForm.gpsLon
                                }));
                            }}
                            placeholder="높이 (m)"
                        />
                    </div>

                    <div className={styles.buttonGroup}>
                        <button
                            className={`${styles.selectLocationBtn} ${isSelecting ? styles.active : ''}`}
                            onClick={handleSelectLocation}
                            disabled={!currentBlueprint}
                        >
                            {isSelecting ? '위치 선택 중...' : '위치 선택'}
                        </button>
                        <button
                            className={styles.saveLocationBtn}
                            onClick={handleCreateRiskZone}
                            disabled={!clickedPoint || !riskZoneForm.name.trim() || !riskZoneForm.width || !riskZoneForm.height}
                        >
                            위험구역 등록
                        </button>
                    </div>
                </div>
            </section>

            {/* 하단: 전체 위험구역 목록 */}
            <section className={styles.tableSection}>
                <h2 className={styles.tableTitle}>전체 위험구역 목록</h2>

                <table className={styles.dataTable}>
                    <thead>
                    <tr>
                        <th>Floor</th>
                        <th>Name</th>
                        <th>Latitude</th>
                        <th>Longitude</th>
                        <th>Width</th>
                        <th>Height</th>
                        <th>Button</th>
                    </tr>
                    </thead>
                    <tbody>
                    {currentZones.length > 0 ? (
                        currentZones.map(zone => (
                            <tr key={zone.id}>
                                <td data-label="Floor">{zone.floor}</td>
                                <td data-label="Name">{zone.name}</td>
                                <td data-label="Latitude">{zone.latitude}</td>
                                <td data-label="Longitude">{zone.longitude}</td>
                                <td data-label="Width">{zone.width}</td>
                                <td data-label="Height">{zone.height}</td>
                                <td data-label="Button">
                                    <div className={styles.actionButtons}>
                                        <button
                                            className={`${styles.actionButton} ${styles.editButton}`}
                                            onClick={() => handleEdit(zone)}
                                        >
                                            수정
                                        </button>
                                        <button
                                            className={`${styles.actionButton} ${styles.deleteButton}`}
                                            onClick={() => handleDelete(zone.id)}
                                        >
                                            삭제
                                        </button>
                                    </div>
                                </td>
                            </tr>
                        ))
                    ) : (
                        <tr>
                            <td colSpan="7" className={styles.emptyState}>
                                등록된 위험구역이 없습니다.
                            </td>
                        </tr>
                    )}
                    </tbody>
                </table>

                {/* 페이지네이션 */}
                {totalPages > 1 && (
                    <div className={styles.pagination}>
                        <button
                            className={styles.pageBtn}
                            onClick={() => setCurrentPage(prev => Math.max(prev - 1, 0))}
                            disabled={currentPage === 0}
                        >
                            ‹
                        </button>

                        {Array.from({ length: totalPages }, (_, index) => (
                            <button
                                key={index}
                                className={`${styles.pageBtn} ${currentPage === index ? styles.active : ''}`}
                                onClick={() => setCurrentPage(index)}
                            >
                                {index + 1}
                            </button>
                        ))}

                        <button
                            className={styles.pageBtn}
                            onClick={() => setCurrentPage(prev => Math.min(prev + 1, totalPages - 1))}
                            disabled={currentPage >= totalPages - 1}
                        >
                            ›
                        </button>
                    </div>
                )}
            </section>

            {/* 수정 모달 */}
            {isEditModalOpen && (
                <div className={styles.modalOverlay}>
                    <div className={styles.modal}>
                        <div className={styles.modalHeader}>
                            <h3 className={styles.modalTitle}>위험구역 수정</h3>
                            <button
                                className={styles.modalCloseBtn}
                                onClick={handleCloseEditModal}
                            >
                                ×
                            </button>
                        </div>

                        <div className={styles.modalBody}>
                            <div className={styles.formGroup}>
                                <label className={styles.formLabel}>층수</label>
                                <select
                                    className={styles.formInput}
                                    value={editFormData.floor}
                                    onChange={(e) => handleEditFormChange('floor', parseInt(e.target.value, 10))}
                                >
                                    {availableBlueprints.map(blueprint => (
                                        <option key={blueprint.id} value={blueprint.floor}>
                                            {blueprint.floor}층
                                        </option>
                                    ))}
                                </select>
                            </div>

                            <div className={styles.formGroup}>
                                <label className={styles.formLabel}>이름</label>
                                <input
                                    type="text"
                                    className={styles.formInput}
                                    value={editFormData.name}
                                    onChange={(e) => handleEditFormChange('name', e.target.value)}
                                    placeholder="위험구역 이름을 입력하세요"
                                />
                            </div>

                            <div className={styles.formGroup}>
                                <label className={styles.formLabel}>위도 (Latitude)</label>
                                <input
                                    type="number"
                                    step="0.0001"
                                    className={styles.formInput}
                                    value={editFormData.latitude}
                                    onChange={(e) => handleEditFormChange('latitude', e.target.value)}
                                    placeholder="위도를 입력하세요"
                                />
                            </div>

                            <div className={styles.formGroup}>
                                <label className={styles.formLabel}>경도 (Longitude)</label>
                                <input
                                    type="number"
                                    step="0.0001"
                                    className={styles.formInput}
                                    value={editFormData.longitude}
                                    onChange={(e) => handleEditFormChange('longitude', e.target.value)}
                                    placeholder="경도를 입력하세요"
                                />
                            </div>

                            <div className={styles.formGroup}>
                                <label className={styles.formLabel}>너비 (Width)</label>
                                <input
                                    type="number"
                                    step="0.1"
                                    className={styles.formInput}
                                    value={editFormData.width}
                                    onChange={(e) => handleEditFormChange('width', e.target.value)}
                                    placeholder="너비를 입력하세요"
                                />
                            </div>

                            <div className={styles.formGroup}>
                                <label className={styles.formLabel}>높이 (Height)</label>
                                <input
                                    type="number"
                                    step="0.1"
                                    className={styles.formInput}
                                    value={editFormData.height}
                                    onChange={(e) => handleEditFormChange('height', e.target.value)}
                                    placeholder="높이를 입력하세요"
                                />
                            </div>
                        </div>

                        <div className={styles.modalFooter}>
                            <button
                                className={styles.modalCancelBtn}
                                onClick={handleCloseEditModal}
                            >
                                취소
                            </button>
                            <button
                                className={styles.modalSaveBtn}
                                onClick={handleSaveEdit}
                            >
                                수정 완료
                            </button>
                        </div>
                    </div>
                </div>
            )}
            
            {/* 성공 모달 */}
            <SuccessModal
                isOpen={successModal.isOpen}
                title={successModal.title}
                message={successModal.message}
                onClose={handleCloseSuccessModal}
            />
            
            {/* 삭제 확인 모달 */}
            <ConfirmModal
                isOpen={confirmModal.isOpen}
                title="위험구역 삭제 확인"
                message="을 정말 삭제하시겠습니까?"
                targetName="이 위험구역"
                onConfirm={handleConfirmDelete}
                onCancel={handleCloseConfirmModal}
                confirmButtonText="삭제하기"
                type="danger"
            />
        </div>
    );
};

export default RiskZonePage;