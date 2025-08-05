import React, { useState, useEffect, useRef } from 'react';
import styles from '../styles/RiskZone.module.css';
// import { riskZoneAPI } from '../api/api'; // API 연동 시 사용

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
    const [riskZones, setRiskZones] = useState([
        {
            id: 1,
            floor: '1F',
            name: '3구역 크레인 작업 구역',
            location: 'X: 37.5721, Y: 126.9876',
            width: '6.5 m',
            height: '12 m'
        },
        {
            id: 2,
            floor: '2F',
            name: '1구역 전기함',
            location: 'X: 37.5719, Y: 126.9868',
            width: '6.5 m',
            height: '12 m'
        },
        {
            id: 3,
            floor: '3F',
            name: '2구역 계단 작업 구역',
            location: 'X: 37.5705, Y: 126.9880',
            width: '3.8 m',
            height: '2.2 m'
        }
    ]);

    // 캔버스에 그려진 위험구역들
    const [canvasZones, setCanvasZones] = useState([]);

    // 컴포넌트 마운트 시 데이터 로드
    useEffect(() => {
        fetchRiskZones();
    }, [currentPage, selectedFloor]);

    // 위험구역 데이터 조회
    const fetchRiskZones = async () => {
        try {
            // API 호출 로직
            // const response = await riskZoneAPI.getRiskZones({
            //     page: currentPage,
            //     size: pageSize,
            //     floor: selectedFloor
            // });
            // setRiskZones(response.data.content || []);
            // setTotalPages(response.data.totalPages || 1);

            // 임시 데이터 필터링
            const filteredZones = riskZones.filter(zone => zone.floor === selectedFloor);
            setTotalPages(Math.ceil(filteredZones.length / pageSize));
        } catch (error) {
            console.error('위험구역 데이터 조회 실패:', error);
        }
    };

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

            // 위치 정보 업데이트
            setLocationInfo({
                x: newZone.x.toFixed(2),
                y: newZone.y.toFixed(2),
                name: '',
                width: (newZone.width * 0.1).toFixed(1), // 임시 스케일 변환
                height: (newZone.height * 0.1).toFixed(1)
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
            width: (zone.width * 0.1).toFixed(1),
            height: (zone.height * 0.1).toFixed(1)
        });
    };

    // 위치 정보 저장
    const handleSaveLocation = async () => {
        if (!selectedZone || !locationInfo.name.trim()) {
            alert('구역을 선택하고 이름을 입력해주세요.');
            return;
        }

        try {
            // API 호출 로직
            // const zoneData = {
            //     floor: selectedFloor,
            //     name: locationInfo.name,
            //     x: parseFloat(locationInfo.x),
            //     y: parseFloat(locationInfo.y),
            //     width: parseFloat(locationInfo.width),
            //     height: parseFloat(locationInfo.height)
            // };
            // await riskZoneAPI.createRiskZone(zoneData);

            alert('위험구역이 저장되었습니다.');

            // 캔버스 위험구역에 이름 추가
            setCanvasZones(prev =>
                prev.map(zone =>
                    zone.id === selectedZone.id
                        ? { ...zone, name: locationInfo.name }
                        : zone
                )
            );

            // 목록 새로고침
            await fetchRiskZones();

            // 폼 초기화
            setSelectedZone(null);
            setLocationInfo({
                x: '',
                y: '',
                name: '',
                width: '',
                height: ''
            });

        } catch (error) {
            console.error('위험구역 저장 실패:', error);
            alert('저장에 실패했습니다.');
        }
    };

    // 위험구역 수정
    const handleEdit = (zone) => {
        console.log('위험구역 수정:', zone);
        // 수정 모달 또는 폼 표시
    };

    // 위험구역 삭제
    const handleDelete = async (zoneId) => {
        if (!window.confirm('정말로 이 위험구역을 삭제하시겠습니까?')) {
            return;
        }

        try {
            // API 호출 로직
            // await riskZoneAPI.deleteRiskZone(zoneId);

            setRiskZones(prev => prev.filter(zone => zone.id !== zoneId));
            alert('위험구역이 삭제되었습니다.');
        } catch (error) {
            console.error('위험구역 삭제 실패:', error);
            alert('삭제에 실패했습니다.');
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

    // 현재 페이지 데이터
    const currentZones = riskZones
        .filter(zone => zone.floor === selectedFloor)
        .slice(currentPage * pageSize, (currentPage + 1) * pageSize);

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
                        <div className={styles.canvas}>
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
                            X: {locationInfo.x || '50.35'}, Y: {locationInfo.y || '64.63'}
                        </p>
                    </div>

                    <div className={styles.formGroup}>
                        <label className={styles.formLabel}>Name</label>
                        <input
                            type="text"
                            className={styles.formInput}
                            value={locationInfo.name}
                            onChange={(e) => setLocationInfo(prev => ({ ...prev, name: e.target.value }))}
                            placeholder="위험구역 이름을 입력하세요"
                        />
                    </div>

                    <div className={styles.formGroup}>
                        <label className={styles.formLabel}>Width</label>
                        <input
                            type="text"
                            className={styles.formInput}
                            value={locationInfo.width}
                            onChange={(e) => setLocationInfo(prev => ({ ...prev, width: e.target.value }))}
                            placeholder="너비 (m)"
                            disabled={!selectedZone}
                        />
                    </div>

                    <div className={styles.formGroup}>
                        <label className={styles.formLabel}>Height</label>
                        <input
                            type="text"
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
                            disabled={!selectedZone || !locationInfo.name.trim()}
                        >
                            위험구역 추가등록
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
                        <th>Location</th>
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
                                <td data-label="Location">{zone.location}</td>
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
                            <td colSpan="6" className={styles.emptyState}>
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
        </div>
    );
};

export default RiskZonePage;