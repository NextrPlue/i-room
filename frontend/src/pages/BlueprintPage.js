import React, { useState, useEffect } from 'react';
import styles from '../styles/Blueprint.module.css';
import {useNavigate} from "react-router-dom";
import { blueprintAPI } from '../api/api';

const BlueprintPage = () => {
    const navigate = useNavigate();
    const [searchTerm, setSearchTerm] = useState('');
    const [selectedFilter, setSelectedFilter] = useState('all');
    const [blueprints, setBlueprints] = useState([]);
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

    return (
        <div className={styles.page}>
            {/* 페이지 헤더 */}
            <header className={styles.pageHeader}>
                <h1 className={styles.pageTitle}>도면 관리</h1>
                <button className={styles.addButton}>
                    + 새로운 도면 업로드
                </button>
            </header>

            {/* 검색 섹션 (상단으로 이동) */}
            <section className={styles.searchSection}>
                <input
                    className={styles.searchInput}
                    placeholder="층수로 검색해보세요 (예: 1, 2층)"
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)}
                />
            </section>

            {/* 컨텐츠 섹션 */}
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
                                className={`${styles.blueprintItem} ${index === 0 ? styles.selected : ''}`}
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

                {/* 오른쪽: 필터 및 미리보기 */}
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

                    {/* 미리보기 영역 */}
                    <div className={styles.previewSection}>
                        {filteredBlueprints.length > 0 ? (
                            <div className={styles.blueprintPreview}>
                                <h3 className={styles.previewTitle}>{filteredBlueprints[0].floor}층 도면</h3>
                                <img 
                                    src={filteredBlueprints[0].blueprintUrl} 
                                    alt={`${filteredBlueprints[0].floor}층 도면`}
                                    className={styles.previewImage}
                                    onError={(e) => {
                                        e.target.style.display = 'none';
                                        e.target.nextSibling.style.display = 'block';
                                    }}
                                />
                                <div className={styles.previewError} style={{display: 'none'}}>
                                    <div className={styles.previewIcon}>📄</div>
                                    <p>도면 이미지를 불러올 수 없습니다</p>
                                </div>
                            </div>
                        ) : (
                            <div className={styles.previewPlaceholder}>
                                <div className={styles.previewIcon}>📄</div>
                                <h3 className={styles.previewTitle}>도면 미리보기</h3>
                                <p className={styles.previewSubtitle}>도면을 선택하면 미리보기가 표시됩니다</p>
                            </div>
                        )}

                        {/* 도면 정보 */}
                        {filteredBlueprints.length > 0 && (
                            <div className={styles.blueprintDetails}>
                                <h4 className={styles.detailsTitle}>도면 정보</h4>
                                <div className={styles.detailsGrid}>
                                    <div className={styles.detailItem}>
                                        <span className={styles.detailLabel}>층수:</span>
                                        <span className={styles.detailValue}>{filteredBlueprints[0].floor}층</span>
                                    </div>
                                    <div className={styles.detailItem}>
                                        <span className={styles.detailLabel}>가로:</span>
                                        <span className={styles.detailValue}>{filteredBlueprints[0].width}m</span>
                                    </div>
                                    <div className={styles.detailItem}>
                                        <span className={styles.detailLabel}>세로:</span>
                                        <span className={styles.detailValue}>{filteredBlueprints[0].height}m</span>
                                    </div>
                                    <div className={styles.detailItem}>
                                        <span className={styles.detailLabel}>면적:</span>
                                        <span className={styles.detailValue}>{(filteredBlueprints[0].width * filteredBlueprints[0].height).toFixed(2)}㎡</span>
                                    </div>
                                    <div className={styles.detailItem}>
                                        <span className={styles.detailLabel}>도면 URL:</span>
                                        <span className={styles.detailValue}>
                                            <a href={filteredBlueprints[0].blueprintUrl} target="_blank" rel="noopener noreferrer">
                                                도면 보기
                                            </a>
                                        </span>
                                    </div>
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
                    </div>
                </section>
            </div>
        </div>
    );
};

export default BlueprintPage;