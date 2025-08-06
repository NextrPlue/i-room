import React, { useState, useEffect } from 'react';
import styles from '../styles/Blueprint.module.css';
import { blueprintAPI } from '../api/api';

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
    const [totalElements, setTotalElements] = useState(0);
    const pageSize = 10;

    // 도면 목록 조회 함수
    const fetchBlueprints = async (page = 0) => {
        try {
            setLoading(true);
            setError(null);

            const response = await blueprintAPI.getBlueprints({
                page: page,
                size: pageSize
            });

            setBlueprints(response.content || []);
            setCurrentPage(response.page || 0);
            setTotalPages(response.totalPages || 0);
            setTotalElements(response.totalElements || 0);

            // 첫 번째 도면을 기본 선택
            if (response.content && response.content.length > 0) {
                setSelectedBlueprint(response.content[0]);
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
        fetchBlueprints(0);
    }, []);

    // 필터 옵션
    const filterOptions = [
        { value: 'all', label: '전체', color: '#6B7280' },
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

    // 도면 선택 핸들러
    const handleBlueprintSelect = (blueprint) => {
        setSelectedBlueprint(blueprint);
        setImageError(false); // 새 도면 선택 시 에러 상태 초기화
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
            reader.onload = (e) => setUploadPreview(e.target.result);
            reader.readAsDataURL(file);
        }
    };

    // 업로드 폼 입력 핸들러
    const handleUploadFormChange = (field, value) => {
        setUploadForm(prev => ({
            ...prev,
            [field]: field === 'floor' ? parseInt(value) : parseFloat(value)
        }));
    };

    // 도면 업로드 핸들러
    const handleUploadSubmit = async (e) => {
        e.preventDefault();

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
                                <button onClick={() => fetchBlueprints(currentPage)}>
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
                    {!loading && !error && totalPages > 1 && (
                        <div className={styles.pagination}>
                            <button
                                onClick={() => fetchBlueprints(currentPage - 1)}
                                disabled={currentPage === 0}
                                className={styles.pageButton}
                            >
                                이전
                            </button>

                            <span className={styles.pageInfo}>
                                {currentPage + 1} / {totalPages}
                                (총 {totalElements}개)
                            </span>

                            <button
                                onClick={() => fetchBlueprints(currentPage + 1)}
                                disabled={currentPage >= totalPages - 1}
                                className={styles.pageButton}
                            >
                                다음
                            </button>
                        </div>
                    )}
                </section>

                {/* 중앙: 도면 미리보기 */}
                <section className={styles.previewSection}>
                    {selectedBlueprint ? (
                        <div className={styles.blueprintPreview}>
                            <h3 className={styles.previewTitle}>{selectedBlueprint.floor}층 도면</h3>

                            {selectedBlueprint.blueprintUrl && !imageError ? (
                                <img
                                    src={`http://localhost:8080${selectedBlueprint.blueprintUrl}`}
                                    alt={`${selectedBlueprint.floor}층 도면 - 크기: ${selectedBlueprint.width}m × ${selectedBlueprint.height}m`}
                                    className={styles.previewImage}
                                    onError={handleImageError}
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
                                onClick={() => setSelectedFilter(option.value)}
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
                                                href={selectedBlueprint.blueprintUrl}
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
            {showUploadForm && (
                <div className={styles.uploadModal}>
                    <div className={styles.uploadModalContent}>
                        <div className={styles.uploadModalHeader}>
                            <h2>새 도면 업로드</h2>
                            <button
                                className={styles.closeButton}
                                onClick={handleUploadCancel}
                            >
                                ✕
                            </button>
                        </div>

                        <form onSubmit={handleUploadSubmit} className={styles.uploadForm}>
                            {/* 파일 선택 영역 */}
                            <div className={styles.fileUploadArea}>
                                <input
                                    type="file"
                                    id="blueprintFile"
                                    accept="image/*"
                                    onChange={handleFileSelect}
                                    className={styles.fileInput}
                                />
                                <label htmlFor="blueprintFile" className={styles.fileLabel}>
                                    {uploadPreview ? (
                                        <img
                                            src={uploadPreview}
                                            alt="업로드 미리보기"
                                            className={styles.uploadPreview}
                                        />
                                    ) : (
                                        <div className={styles.fileDropArea}>
                                            <div className={styles.uploadIcon}>📁</div>
                                            <p>도면 이미지를 선택하세요</p>
                                            <span>PNG, JPG, GIF 형식 (최대 10MB)</span>
                                        </div>
                                    )}
                                </label>
                            </div>

                            {/* 도면 정보 입력 */}
                            <div className={styles.formGrid}>
                                <div className={styles.formGroup}>
                                    <label>층수</label>
                                    <input
                                        type="number"
                                        value={uploadForm.floor}
                                        onChange={(e) => handleUploadFormChange('floor', e.target.value)}
                                        min="1"
                                        required
                                    />
                                </div>
                                <div className={styles.formGroup}>
                                    <label>가로 (m)</label>
                                    <input
                                        type="number"
                                        step="0.1"
                                        value={uploadForm.width}
                                        onChange={(e) => handleUploadFormChange('width', e.target.value)}
                                        min="0.1"
                                        required
                                    />
                                </div>
                                <div className={styles.formGroup}>
                                    <label>세로 (m)</label>
                                    <input
                                        type="number"
                                        step="0.1"
                                        value={uploadForm.height}
                                        onChange={(e) => handleUploadFormChange('height', e.target.value)}
                                        min="0.1"
                                        required
                                    />
                                </div>
                            </div>

                            {/* 에러 메시지 */}
                            {error && (
                                <div className={styles.errorMessage}>
                                    {error}
                                </div>
                            )}

                            {/* 버튼 영역 */}
                            <div className={styles.formButtons}>
                                <button
                                    type="button"
                                    onClick={handleUploadCancel}
                                    className={styles.cancelButton}
                                    disabled={uploading}
                                >
                                    취소
                                </button>
                                <button
                                    type="submit"
                                    className={styles.submitButton}
                                    disabled={uploading || !uploadForm.file}
                                >
                                    {uploading ? '업로드 중...' : '업로드'}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
};

export default BlueprintPage;