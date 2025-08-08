import React, { useState, useEffect } from 'react';
import styles from '../styles/Blueprint.module.css';
import { blueprintAPI } from '../api/api';
import { authUtils } from '../utils/auth';
import BlueprintAddModal from '../components/BlueprintAddModal';

const BlueprintPage = () => {
    const [searchTerm, setSearchTerm] = useState('');
    const [selectedFilter, setSelectedFilter] = useState('all');
    const [blueprints, setBlueprints] = useState([]);
    const [selectedBlueprint, setSelectedBlueprint] = useState(null);
    const [imageError, setImageError] = useState(false);
    const [showUploadForm, setShowUploadForm] = useState(false);
    const [uploadForm, setUploadForm] = useState({
        file: null,
        floor: 1,
        width: 10.0,
        height: 10.0
    });
    const [uploadPreview, setUploadPreview] = useState(null);
    const [uploading, setUploading] = useState(false);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [currentPage, setCurrentPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [blueprintRotation, setBlueprintRotation] = useState(0);
    const [imageBlob, setImageBlob] = useState(null);
    const pageSize = 7;

    // 도면 목록 조회 함수
    const fetchBlueprints = async (page = 0) => {
        try {
            setLoading(true);
            setError(null);

            const response = await blueprintAPI.getBlueprints({
                page: page,
                size: pageSize
            });

            const data = response.data || response;
            
            setBlueprints(data.content || []);
            setCurrentPage(data.page || 0);
            setTotalPages(data.totalPages || 0);

            // 첫 번째 도면을 기본 선택
            if (data.content && data.content.length > 0) {
                handleBlueprintSelect(data.content[0]);
            }

        } catch (err) {
            console.error('도면 목록 조회 실패:', err);
            setError(err.message || '도면 목록을 불러오는데 실패했습니다.');
        } finally {
            setLoading(false);
        }
    };

    // 컴포넌트 마운트 시 도면 목록 조회
    useEffect(() => {
        fetchBlueprints(0).catch(console.error);
    }, []);

    // 컴포넌트 언마운트 시 blob URL 정리
    useEffect(() => {
        return () => {
            if (imageBlob && typeof imageBlob === 'string') {
                URL.revokeObjectURL(imageBlob);
            }
        };
    }, [imageBlob]);

    // 필터 옵션
    const filterOptions = [
        { value: 'active', label: '회전', color: '#3B82F6' },
        { value: 'inactive', label: '다운로드', color: '#10B981' },
        { value: 'maintenance', label: '수정', color: '#F59E0B' },
        { value: 'urgent', label: '삭제', color: '#EF4444' },
    ];

    // 검색 필터링 (클라이언트 사이드) - 층수로 검색
    const filteredBlueprints = blueprints.filter(blueprint => {
        const matchesSearch = !searchTerm ||
            blueprint.floor.toString().includes(searchTerm) ||
            `${blueprint.floor}층`.includes(searchTerm);

        // 필터는 층수 기준으로 단순화 (모든 도면 표시)
        const matchesFilter = selectedFilter === 'all';
        return matchesSearch && matchesFilter;
    });

    // 이미지 로드 함수
    const loadBlueprintImage = async (blueprintId) => {
        try {
            const imageUrl = blueprintAPI.getBlueprintImage(blueprintId);
            const authHeader = authUtils.getAuthHeader();
            
            const response = await fetch(imageUrl, {
                headers: {
                    Authorization: authHeader
                }
            });
            
            if (!response.ok) {
                const errorText = await response.text();
                console.log('- Error Response:', errorText);
            }
            
            const blob = await response.blob();
            const blobUrl = URL.createObjectURL(blob);
            setImageBlob(blobUrl);
            setImageError(false);
        } catch (error) {
            console.error('이미지 로드 실패:', error);
            setImageError(true);
            setImageBlob(null);
        }
    };

    // 도면 선택 핸들러
    const handleBlueprintSelect = (blueprint) => {
        setSelectedBlueprint(blueprint);
        setImageError(false); // 새 도면 선택 시 에러 상태 초기화
        setBlueprintRotation(0); // 새 도면 선택 시 회전 상태 초기화
        
        // 이전 blob URL 정리
        if (imageBlob && typeof imageBlob === 'string') {
            URL.revokeObjectURL(imageBlob);
        }
        
        // 새 이미지 로드
        if (blueprint && blueprint.id) {
            loadBlueprintImage(blueprint.id).catch(console.error);
        }
    };

    // 이미지 에러 핸들러
    const handleImageError = () => {
        setImageError(true);
    };

    // 파일 선택 핸들러
    const handleFileSelect = (e) => {
        const file = e.target.files[0];
        if (file) {
            // 파일 타입 검증
            if (!file.type.startsWith('image/')) {
                setError('이미지 파일만 업로드 가능합니다.');
                return;
            }

            // 파일 크기 검증 (10MB 제한)
            if (file.size > 10 * 1024 * 1024) {
                setError('파일 크기는 10MB 이하여야 합니다.');
                return;
            }

            setUploadForm(prev => ({ ...prev, file }));

            // 미리보기 생성
            const reader = new FileReader();
            reader.onload = (e) => {
                if (e.target?.result) {
                    setUploadPreview(e.target.result);
                }
            };
            reader.readAsDataURL(file);
        }
    };

    // 업로드 폼 입력 핸들러
    const handleUploadFormChange = (field, value) => {
        setUploadForm(prev => ({
            ...prev,
            [field]: field === 'floor' ? parseInt(value, 10) : parseFloat(value)
        }));
    };

    // 도면 업로드 핸들러
    const handleUploadSubmit = async () => {
        if (!uploadForm.file) {
            setError('도면 파일을 선택해주세요.');
            return;
        }

        try {
            setUploading(true);
            setError(null);

            await blueprintAPI.createBlueprint(uploadForm);

            // 업로드 성공 후 목록 새로고침
            await fetchBlueprints(currentPage);

            // 폼 초기화
            setUploadForm({
                file: null,
                floor: 1,
                width: 10.0,
                height: 10.0
            });
            setUploadPreview(null);
            setShowUploadForm(false);

        } catch (err) {
            console.error('도면 업로드 실패:', err);
            setError(err.message || '도면 업로드에 실패했습니다.');
        } finally {
            setUploading(false);
        }
    };

    // 업로드 폼 취소
    const handleUploadCancel = () => {
        setUploadForm({
            file: null,
            floor: 1,
            width: 10.0,
            height: 10.0
        });
        setUploadPreview(null);
        setShowUploadForm(false);
        setError(null);
    };

    // 회전 버튼 클릭 핸들러
    const handleRotateClick = () => {
        if (!selectedBlueprint) return;
        
        const newRotation = (blueprintRotation + 90) % 360;
        setBlueprintRotation(newRotation);
    };

    // 다운로드 버튼 클릭 핸들러
    const handleDownloadClick = async () => {
        if (!selectedBlueprint || !selectedBlueprint.blueprintUrl) {
            setError('다운로드할 도면이 없습니다.');
            return;
        }

        try {
            const imageUrl = blueprintAPI.getBlueprintImage(selectedBlueprint.id);
            const fileName = `${selectedBlueprint.floor}층_도면.jpg`;
            
            // 이미지 다운로드 (JWT 토큰 포함)
            const response = await fetch(imageUrl, {
                headers: {
                    Authorization: authUtils.getAuthHeader()
                }
            });
            const blob = await response.blob();
            
            // 다운로드 링크 생성
            const downloadUrl = window.URL.createObjectURL(blob);
            const link = document.createElement('a');
            link.href = downloadUrl;
            link.download = fileName;
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
            window.URL.revokeObjectURL(downloadUrl);
        } catch (err) {
            console.error('다운로드 실패:', err);
            setError('도면 다운로드에 실패했습니다.');
        }
    };

    // 삭제 버튼 클릭 핸들러
    const handleDeleteClick = async () => {
        if (!selectedBlueprint) {
            setError('삭제할 도면을 선택해주세요.');
            return;
        }

        if (!window.confirm(`${selectedBlueprint.floor}층 도면을 정말 삭제하시겠습니까?`)) {
            return;
        }

        try {
            setLoading(true);
            await blueprintAPI.deleteBlueprint(selectedBlueprint.id);
            
            // 선택 해제
            setSelectedBlueprint(null);
            setImageBlob(null);
            
            // 목록 새로고침
            await fetchBlueprints(currentPage);
            
        } catch (err) {
            console.error('삭제 실패:', err);
            setError(err.message || '도면 삭제에 실패했습니다.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className={styles.page}>
            {/* 페이지 헤더 */}
            <header className={styles.pageHeader}>
                <h1 className={styles.pageTitle}>도면 관리</h1>
                <button
                    className={styles.addButton}
                    onClick={() => setShowUploadForm(true)}
                >
                    + 새로운 도면 업로드
                </button>
            </header>

            {/* 검색 섹션 */}
            <section className={styles.searchSection}>
                <input
                    className={styles.searchInput}
                    placeholder="층수로 검색해보세요 (예: 1, 2층)"
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)}
                />
            </section>

            {/* 컨텐츠 섹션 - 3열 레이아웃 */}
            <div className={styles.contentSection}>
                {/* 왼쪽: 도면 목록 */}
                <section className={styles.listSection}>
                    <h2 className={styles.sectionTitle}>도면 목록</h2>

                    <div className={styles.blueprintList}>
                        {loading && (
                            <div className={styles.loadingState}>
                                도면 목록을 불러오는 중...
                            </div>
                        )}

                        {error && (
                            <div className={styles.errorState}>
                                {error}
                                <button onClick={() => fetchBlueprints(currentPage).catch(console.error)}>
                                    다시 시도
                                </button>
                            </div>
                        )}

                        {!loading && !error && filteredBlueprints.map((blueprint, index) => (
                            <div
                                key={blueprint.id || index}
                                className={`${styles.blueprintItem} ${
                                    selectedBlueprint?.id === blueprint.id ? styles.selected : ''
                                }`}
                                onClick={() => handleBlueprintSelect(blueprint)}
                            >
                                <div className={styles.blueprintIcon}>📋</div>
                                <div className={styles.blueprintInfo}>
                                    <h3 className={styles.blueprintTitle}>
                                        {blueprint.floor}층 도면
                                    </h3>
                                    <div className={styles.blueprintMeta}>
                                        <span>크기: {blueprint.width}m × {blueprint.height}m</span>
                                    </div>
                                </div>
                            </div>
                        ))}

                        {!loading && !error && filteredBlueprints.length === 0 && (
                            <div className={styles.emptyState}>
                                {searchTerm ? '검색 결과가 없습니다.' : '등록된 도면이 없습니다.'}
                            </div>
                        )}
                    </div>

                    {/* 페이지네이션 */}
                    <div className={styles.pagination}>
                        <button
                            className={styles.pageBtn}
                            onClick={() => fetchBlueprints(currentPage - 1).catch(console.error)}
                            disabled={currentPage === 0}
                        >
                            이전
                        </button>

                        {Array.from({ length: totalPages }, (_, index) => (
                            <button
                                key={index}
                                className={`${styles.pageBtn} ${currentPage === index ? styles.active : ''}`}
                                onClick={() => fetchBlueprints(index).catch(console.error)}
                            >
                                {index + 1}
                            </button>
                        ))}

                        <button
                            className={styles.pageBtn}
                            onClick={() => fetchBlueprints(currentPage + 1).catch(console.error)}
                            disabled={currentPage >= totalPages - 1}
                        >
                            다음
                        </button>
                    </div>
                </section>

                {/* 중앙: 도면 미리보기 */}
                <section className={styles.previewSection}>
                    {selectedBlueprint ? (
                        <div className={styles.blueprintPreview}>
                            <h3 className={styles.previewTitle}>{selectedBlueprint.floor}층 도면</h3>

                            {selectedBlueprint.blueprintUrl && !imageError && imageBlob ? (
                                <img
                                    src={typeof imageBlob === 'string' ? imageBlob : ''}
                                    alt={`${selectedBlueprint.floor}층 도면 - 크기: ${selectedBlueprint.width}m × ${selectedBlueprint.height}m`}
                                    className={styles.previewImage}
                                    onError={handleImageError}
                                    style={{ transform: `rotate(${blueprintRotation}deg)` }}
                                />
                            ) : (
                                <div className={styles.previewError}>
                                    <div className={styles.previewIcon}>📄</div>
                                    <p>도면 이미지가 없습니다</p>
                                </div>
                            )}
                        </div>
                    ) : (
                        <div className={styles.previewPlaceholder}>
                            <div className={styles.previewIcon}>📄</div>
                            <h3 className={styles.previewTitle}>도면 미리보기</h3>
                            <p className={styles.previewSubtitle}>도면을 선택하면 미리보기가 표시됩니다</p>
                        </div>
                    )}
                </section>

                {/* 오른쪽: 필터 및 상세 정보 */}
                <section className={styles.detailSection}>
                    {/* 필터 버튼들 */}
                    <div className={styles.filterSection}>
                        {filterOptions.map((option) => (
                            <button
                                key={option.value}
                                className={`${styles.filterBtn} ${
                                    selectedFilter === option.value ? styles.activeFilter : ''
                                }`}
                                style={{
                                    backgroundColor: selectedFilter === option.value ? option.color : '#F3F4F6',
                                    color: selectedFilter === option.value ? '#fff' : '#374151'
                                }}
                                onClick={() => {
                                    if (option.value === 'active') {
                                        handleRotateClick();
                                    } else if (option.value === 'inactive') {
                                        handleDownloadClick().catch(console.error);
                                    } else if (option.value === 'urgent') {
                                        handleDeleteClick();
                                    } else {
                                        setSelectedFilter(option.value);
                                    }
                                }}
                            >
                                {option.label}
                            </button>
                        ))}
                    </div>

                    {/* 도면 정보 */}
                    {selectedBlueprint && (
                        <div className={styles.blueprintDetails}>
                            <h4 className={styles.detailsTitle}>도면 정보</h4>
                            <div className={styles.detailsGrid}>
                                <div className={styles.detailItem}>
                                    <span className={styles.detailLabel}>층수:</span>
                                    <span className={styles.detailValue}>{selectedBlueprint.floor}층</span>
                                </div>
                                <div className={styles.detailItem}>
                                    <span className={styles.detailLabel}>가로:</span>
                                    <span className={styles.detailValue}>{selectedBlueprint.width}m</span>
                                </div>
                                <div className={styles.detailItem}>
                                    <span className={styles.detailLabel}>세로:</span>
                                    <span className={styles.detailValue}>{selectedBlueprint.height}m</span>
                                </div>
                                <div className={styles.detailItem}>
                                    <span className={styles.detailLabel}>면적:</span>
                                    <span className={styles.detailValue}>
                                        {(selectedBlueprint.width * selectedBlueprint.height).toFixed(2)}㎡
                                    </span>
                                </div>
                                {selectedBlueprint.blueprintUrl && (
                                    <div className={styles.detailItem}>
                                        <span className={styles.detailLabel}>도면 URL:</span>
                                        <span className={styles.detailValue}>
                                            <a
                                                href={typeof imageBlob === 'string' ? imageBlob : '#'}
                                                target="_blank"
                                                rel="noopener noreferrer"
                                            >
                                                도면 보기
                                            </a>
                                        </span>
                                    </div>
                                )}
                            </div>
                        </div>
                    )}

                    {/* 위험구역 표시 */}
                    <div className={styles.dangerZoneSection}>
                        <div className={styles.dangerZoneHeader}>
                            <span className={styles.dangerIcon}>⚠️</span>
                            <span>위험구역 오버레이 표시</span>
                        </div>
                    </div>
                </section>
            </div>

            {/* 파일 업로드 모달 */}
            <BlueprintAddModal
                showModal={showUploadForm}
                onClose={handleUploadCancel}
                onSubmit={handleUploadSubmit}
                uploadForm={uploadForm}
                onFormChange={handleUploadFormChange}
                onFileSelect={handleFileSelect}
                uploadPreview={uploadPreview}
                uploading={uploading}
                error={error}
            />
        </div>
    );
};

export default BlueprintPage;