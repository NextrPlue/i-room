import React, { useState, useEffect, useRef, useCallback } from 'react';
import styles from '../styles/RiskZone.module.css';
import { riskZoneAPI, blueprintAPI } from '../api/api';

/**
 * 위험구역 설정 및 관리 페이지
 * 
 * TODO: 향후 구현 예정
 * - GPS 좌표 매핑 시스템 구현
 * - 실제 좌표 기반 위험구역 표시
 */

const RiskZonePage = () => {
    const canvasRef = useRef(null);
    const [selectedFloor, setSelectedFloor] = useState('1F');
    const [isSelecting, setIsSelecting] = useState(false);
    const [dragStart, setDragStart] = useState(null);
    const [dragEnd, setDragEnd] = useState(null);
    const [selectedZone, setSelectedZone] = useState(null);

    // 선택된 위치 정보
    const [locationInfo, setLocationInfo] = useState({
        x: '',
        y: '',
        name: '',
        width: '',
        height: ''
    });

    // 현재 페이지 상태
    const [currentPage, setCurrentPage] = useState(0);
    const [pageSize] = useState(5);
    const [totalPages, setTotalPages] = useState(1);

    // 위험구역 목록 데이터
    const [riskZones, setRiskZones] = useState([]);

    // 캔버스에 그려진 위험구역들
    const [canvasZones, setCanvasZones] = useState([]);

    // 도면 관련 상태
    const [currentBlueprint, setCurrentBlueprint] = useState(null);
    const [blueprintImage, setBlueprintImage] = useState(null);

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
    const [availableBlueprints, setAvailableBlueprints] = useState([]);

    // 위험구역 데이터 조회
    const fetchRiskZones = useCallback(async () => {
        try {
            const response = await riskZoneAPI.getRiskZones({
                page: currentPage,
                size: pageSize
            });

            const data = response.data || response;
            const zones = data.content || [];
            
            // Blueprint 정보 조회해서 floor 매핑
            const formattedZones = await Promise.all(
                zones.map(async (zone) => {
                    try {
                        // blueprintId로 Blueprint 정보 조회
                        const blueprintResponse = await blueprintAPI.getBlueprint(zone.blueprintId);
                        const blueprint = blueprintResponse.data || blueprintResponse;
                        
                        return {
                            id: zone.id,
                            floor: `${blueprint.floor}F`,
                            name: zone.name || `위험구역 ${zone.id}`,
                            latitude: zone.latitude,
                            longitude: zone.longitude,
                            width: `${zone.width} m`,
                            height: `${zone.height} m`,
                            // 원본 데이터도 보관
                            blueprintId: zone.blueprintId,
                            originalWidth: zone.width,
                            originalHeight: zone.height
                        };
                    } catch (error) {
                        console.error(`Blueprint ${zone.blueprintId} 조회 실패:`, error);
                        // Blueprint 조회 실패 시 기본값 사용
                        return {
                            id: zone.id,
                            floor: `${zone.blueprintId}F`, // fallback
                            name: zone.name || `위험구역 ${zone.id}`,
                            latitude: zone.latitude,
                            longitude: zone.longitude,
                            width: `${zone.width} m`,
                            height: `${zone.height} m`,
                            blueprintId: zone.blueprintId,
                            originalWidth: zone.width,
                            originalHeight: zone.height
                        };
                    }
                })
            );
            
            setRiskZones(formattedZones);
            setTotalPages(data.totalPages || 1);

        } catch (error) {
            console.error('위험구역 데이터 조회 실패:', error);
            setRiskZones([]);
            setTotalPages(1);
        }
    }, [currentPage, pageSize]);

    // 사용 가능한 도면 목록 조회 (수정 모달용)
    const fetchAvailableBlueprints = useCallback(async () => {
        try {
            const response = await blueprintAPI.getBlueprints({
                page: 0,
                size: 50 // 충분히 많이 가져오기
            });

            const data = response.data || response;
            setAvailableBlueprints(data.content || []);
        } catch (error) {
            console.error('도면 목록 조회 실패:', error);
            setAvailableBlueprints([]);
        }
    }, []);

    // 도면 데이터 조회
    const fetchBlueprint = useCallback(async () => {
        try {
            const floorNumber = parseInt(selectedFloor.replace('F', '')); // "1F" -> 1
            const response = await blueprintAPI.getBlueprints({
                page: 0,
                size: 1,
                target: 'floor',
                keyword: floorNumber.toString()
            });

            const data = response.data || response;
            
            if (data.content && data.content.length > 0) {
                const blueprint = data.content[0];
                setCurrentBlueprint(blueprint);
                console.log(`${selectedFloor} 도면 로드됨 - ID: ${blueprint.id}, Floor: ${blueprint.floor}`);
                
                // 도면 이미지 Blob URL 생성 (인증 헤더 포함)
                try {
                    const blobUrl = await blueprintAPI.getBlueprintImageBlob(blueprint.id);
                    setBlueprintImage(blobUrl);
                } catch (imageError) {
                    console.error('도면 이미지 로드 실패:', imageError);
                    setBlueprintImage(null);
                }
            } else {
                // 해당 층의 도면이 없는 경우
                console.warn(`${selectedFloor} 층의 도면을 찾을 수 없습니다.`);
                setCurrentBlueprint(null);
                setBlueprintImage(null);
            }
        } catch (error) {
            console.error('도면 데이터 조회 실패:', error);
            setCurrentBlueprint(null);
            setBlueprintImage(null);
        }
    }, [selectedFloor]);

    // 캔버스 마우스 이벤트 핸들러
    const handleMouseDown = (e) => {
        if (!isSelecting) return;

        const rect = canvasRef.current.getBoundingClientRect();
        const x = ((e.clientX - rect.left) / rect.width) * 100;
        const y = ((e.clientY - rect.top) / rect.height) * 100;

        setDragStart({ x, y });
        setDragEnd({ x, y });
    };

    const handleMouseMove = (e) => {
        if (!isSelecting || !dragStart) return;

        const rect = canvasRef.current.getBoundingClientRect();
        const x = ((e.clientX - rect.left) / rect.width) * 100;
        const y = ((e.clientY - rect.top) / rect.height) * 100;

        setDragEnd({ x, y });
    };

    const handleMouseUp = (e) => {
        if (!isSelecting || !dragStart || !dragEnd) return;

        const width = Math.abs(dragEnd.x - dragStart.x);
        const height = Math.abs(dragEnd.y - dragStart.y);

        if (width > 2 && height > 2) { // 최소 크기 체크
            const newZone = {
                id: Date.now(),
                x: Math.min(dragStart.x, dragEnd.x),
                y: Math.min(dragStart.y, dragEnd.y),
                width: width,
                height: height
            };

            setCanvasZones(prev => [...prev, newZone]);
            setSelectedZone(newZone);

            // 위치 정보 업데이트 (GPS 좌표 매핑 구현 후 수정 필요)
            setLocationInfo({
                x: newZone.x.toFixed(2),
                y: newZone.y.toFixed(2),
                name: '',
                width: newZone.width.toFixed(1),
                height: newZone.height.toFixed(1)
            });
        }

        setDragStart(null);
        setDragEnd(null);
        setIsSelecting(false);
    };

    // Select Location 버튼 클릭
    const handleSelectLocation = () => {
        setIsSelecting(!isSelecting);
        setDragStart(null);
        setDragEnd(null);
    };

    // 위험구역 클릭
    const handleZoneClick = (zone) => {
        setSelectedZone(zone);
        setLocationInfo({
            x: zone.x.toFixed(2),
            y: zone.y.toFixed(2),
            name: zone.name || '',
            width: zone.width.toFixed(1),
            height: zone.height.toFixed(1)
        });
    };

    // 위치 정보 저장 (위험구역 등록)
    const handleSaveLocation = async () => {
        if (!selectedZone) {
            alert('구역을 선택해주세요.');
            return;
        }

        if (!currentBlueprint || !currentBlueprint.id) {
            alert('도면 정보가 없습니다. 층수를 선택해주세요.');
            return;
        }

        try {
            console.log('locationInfo:', locationInfo);
            console.log('locationInfo.name:', locationInfo.name);
            
            // 캔버스 좌표를 실제 GPS 좌표로 변환 (여기서는 임시로 그대로 사용)
            const riskZoneData = {
                blueprintId: currentBlueprint.id,
                latitude: parseFloat(locationInfo.x) || selectedZone.x,
                longitude: parseFloat(locationInfo.y) || selectedZone.y,
                width: parseFloat(locationInfo.width) || selectedZone.width,
                height: parseFloat(locationInfo.height) || selectedZone.height,
                name: locationInfo.name.trim() || `위험구역 ${Date.now()}`
            };

            console.log('위험구역 등록 데이터:', riskZoneData);
            const response = await riskZoneAPI.createRiskZone(riskZoneData);
            console.log('위험구역 등록 완료:', response);
            
            alert('위험구역이 성공적으로 등록되었습니다.');
            
            // 캔버스에서 임시 구역 제거
            setCanvasZones(prev => prev.filter(zone => zone.id !== selectedZone.id));
            setSelectedZone(null);
            setLocationInfo({
                x: '',
                y: '',
                name: '',
                width: '',
                height: ''
            });
            
            // 목록 새로고침
            await fetchRiskZones();

        } catch (error) {
            console.error('위험구역 저장 실패:', error);
            alert(`저장에 실패했습니다: ${error.message}`);
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
                latitude: zone.latitude || 0,
                longitude: zone.longitude || 0,
                width: zone.originalWidth || parseFloat(zone.width) || 0,
                height: zone.originalHeight || parseFloat(zone.height) || 0,
                name: zone.name || ''
            });
        } catch (error) {
            console.error('도면 정보 조회 실패:', error);
            setEditFormData({
                floor: 1,
                latitude: zone.latitude || 0,
                longitude: zone.longitude || 0,
                width: zone.originalWidth || parseFloat(zone.width) || 0,
                height: zone.originalHeight || parseFloat(zone.height) || 0,
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
    const handleEditFormChange = (field, value) => {
        setEditFormData(prev => ({
            ...prev,
            [field]: value
        }));
    };

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
            
            alert('위험구역이 성공적으로 수정되었습니다.');
            handleCloseEditModal();
            
            // 목록 새로고침
            await fetchRiskZones();
            
        } catch (error) {
            console.error('위험구역 수정 실패:', error);
            alert(`수정에 실패했습니다: ${error.message}`);
        }
    };

    // 위험구역 삭제
    const handleDelete = async (zoneId) => {
        if (!window.confirm('정말로 이 위험구역을 삭제하시겠습니까?')) {
            return;
        }

        try {
            await riskZoneAPI.deleteRiskZone(zoneId);
            alert('위험구역이 성공적으로 삭제되었습니다.');
            
            // 목록 새로고침
            await fetchRiskZones();
        } catch (error) {
            console.error('위험구역 삭제 실패:', error);
            alert(`삭제에 실패했습니다: ${error.message}`);
        }
    };

    // 드래그 영역 계산
    const getDragArea = () => {
        if (!dragStart || !dragEnd) return null;

        return {
            left: `${Math.min(dragStart.x, dragEnd.x)}%`,
            top: `${Math.min(dragStart.y, dragEnd.y)}%`,
            width: `${Math.abs(dragEnd.x - dragStart.x)}%`,
            height: `${Math.abs(dragEnd.y - dragStart.y)}%`
        };
    };

    // 컴포넌트 마운트 시 위험구역 목록과 도면 목록 조회
    useEffect(() => {
        fetchRiskZones();
        fetchAvailableBlueprints();
    }, [currentPage, fetchRiskZones, fetchAvailableBlueprints]);

    // 초기 로드 + 층 변경 시 도면 조회
    useEffect(() => {
        fetchBlueprint();
    }, [selectedFloor, fetchBlueprint]);

    // 컴포넌트 언마운트 시 blob URL 정리
    useEffect(() => {
        return () => {
            if (blueprintImage && blueprintImage.startsWith('blob:')) {
                URL.revokeObjectURL(blueprintImage);
            }
        };
    }, [blueprintImage]);

    // 현재 페이지 데이터 (API에서 이미 페이지네이션 적용됨)
    const currentZones = riskZones;

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
                            value={selectedFloor}
                            onChange={(e) => setSelectedFloor(e.target.value)}
                        >
                            <option value="1F">1F</option>
                            <option value="2F">2F</option>
                            <option value="3F">3F</option>
                            <option value="4F">4F</option>
                            <option value="5F">5F</option>
                        </select>
                    </div>

                    <div
                        className={`${styles.canvasContainer} ${isSelecting ? styles.drawing : ''}`}
                        ref={canvasRef}
                        onMouseDown={handleMouseDown}
                        onMouseMove={handleMouseMove}
                        onMouseUp={handleMouseUp}
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
                                    {selectedFloor} 층의 도면이 없습니다
                                </div>
                            )}
                            
                            {/* 기존 위험구역들 */}
                            {canvasZones.map(zone => (
                                <div
                                    key={zone.id}
                                    className={`${styles.riskZone} ${selectedZone?.id === zone.id ? styles.selected : ''}`}
                                    style={{
                                        left: `${zone.x}%`,
                                        top: `${zone.y}%`,
                                        width: `${zone.width}%`,
                                        height: `${zone.height}%`
                                    }}
                                    onClick={() => handleZoneClick(zone)}
                                    title={zone.name || '위험구역'}
                                />
                            ))}

                            {/* 드래그 중인 영역 */}
                            {dragStart && dragEnd && (
                                <div
                                    className={styles.dragArea}
                                    style={getDragArea()}
                                />
                            )}
                        </div>
                    </div>
                </div>

                {/* 우측: Selected Location 정보 */}
                <div className={styles.locationPanel}>
                    <h3 className={styles.panelTitle}>Selected Location</h3>

                    <div className={styles.coordinateInfo}>
                        <p className={styles.coordinateText}>
                            위치: {locationInfo.x ? `X: ${locationInfo.x}, Y: ${locationInfo.y}` : '영역을 선택해주세요'}
                        </p>
                        <p className={styles.coordinateText}>
                            층수: {selectedFloor} {currentBlueprint ? '' : '(도면 없음)'}
                        </p>
                    </div>

                    <div className={styles.formGroup}>
                        <label className={styles.formLabel}>이름</label>
                        <input
                            type="text"
                            className={styles.formInput}
                            value={locationInfo.name}
                            onChange={(e) => setLocationInfo(prev => ({ ...prev, name: e.target.value }))}
                            placeholder="위험구역 이름을 입력하세요"
                        />
                    </div>

                    <div className={styles.formGroup}>
                        <label className={styles.formLabel}>너비 (Width)</label>
                        <input
                            type="number"
                            step="0.1"
                            className={styles.formInput}
                            value={locationInfo.width}
                            onChange={(e) => setLocationInfo(prev => ({ ...prev, width: e.target.value }))}
                            placeholder="너비 (m)"
                            disabled={!selectedZone}
                        />
                    </div>

                    <div className={styles.formGroup}>
                        <label className={styles.formLabel}>높이 (Height)</label>
                        <input
                            type="number"
                            step="0.1"
                            className={styles.formInput}
                            value={locationInfo.height}
                            onChange={(e) => setLocationInfo(prev => ({ ...prev, height: e.target.value }))}
                            placeholder="높이 (m)"
                            disabled={!selectedZone}
                        />
                    </div>

                    <div className={styles.buttonGroup}>
                        <button
                            className={`${styles.selectLocationBtn} ${isSelecting ? styles.active : ''}`}
                            onClick={handleSelectLocation}
                        >
                            Select Location
                        </button>
                        <button
                            className={styles.saveLocationBtn}
                            onClick={handleSaveLocation}
                            disabled={!selectedZone}
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
                                    onChange={(e) => handleEditFormChange('floor', parseInt(e.target.value))}
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
        </div>
    );
};

export default RiskZonePage;