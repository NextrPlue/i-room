import React, { useState } from 'react';
import styles from '../styles/Blueprint.module.css';
import {useNavigate} from "react-router-dom";

const BlueprintPage = () => {
    const navigate = useNavigate();
    const [searchTerm, setSearchTerm] = useState('');
    const [selectedFilter, setSelectedFilter] = useState('all');

    // 더미 데이터 (실제로는 props나 API에서 받아올 예정)
    const blueprints = [
        { id: 1, title: '1층 건설현장 도면', date: '2024.01.10', size: '2.5MB', status: 'active' },
        { id: 2, title: '2층 건설현장 도면', date: '2023.12.15', size: '3.2MB', status: 'inactive' },
        { id: 3, title: '지하1층건설현장 도면', date: '2023.11.08', size: '1.8MB', status: 'inactive' },
        { id: 4, title: '옥상 구조물 도면', date: '2024.01.22', size: '4.1MB', status: 'active' },
        { id: 5, title: '전기 설비 도면', date: '2023.12.30', size: '2.7MB', status: 'active' },
    ];

    // 필터 옵션
    const filterOptions = [
        { value: 'all', label: '전체', color: '#6B7280' },
        { value: 'active', label: '회전', color: '#3B82F6' },
        { value: 'inactive', label: '다운로드', color: '#10B981' },
        { value: 'maintenance', label: '수정', color: '#F59E0B' },
        { value: 'urgent', label: '삭제', color: '#EF4444' },
    ];

    // 검색 필터링
    const filteredBlueprints = blueprints.filter(blueprint => {
        const matchesSearch = blueprint.title.toLowerCase().includes(searchTerm.toLowerCase());
        const matchesFilter = selectedFilter === 'all' || blueprint.status === selectedFilter;
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
                    placeholder="도면명으로 검색해보세요"
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
                        {filteredBlueprints.map((blueprint, index) => (
                            <div
                                key={blueprint.id}
                                className={`${styles.blueprintItem} ${index === 0 ? styles.selected : ''}`}
                            >
                                <div className={styles.blueprintIcon}>📋</div>
                                <div className={styles.blueprintInfo}>
                                    <h3 className={styles.blueprintTitle}>{blueprint.title}</h3>
                                    <div className={styles.blueprintMeta}>
                                        <span>{blueprint.date}</span>
                                        <span>|</span>
                                        <span>{blueprint.size}</span>
                                    </div>
                                </div>
                            </div>
                        ))}

                        {filteredBlueprints.length === 0 && (
                            <div className={styles.emptyState}>
                                {searchTerm ? '검색 결과가 없습니다.' : '등록된 도면이 없습니다.'}
                            </div>
                        )}
                    </div>
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
                        <div className={styles.previewPlaceholder}>
                            <div className={styles.previewIcon}>📄</div>
                            <h3 className={styles.previewTitle}>도면 미리보기</h3>
                            <p className={styles.previewSubtitle}>(PDF/PNG 뷰어 영역)</p>
                        </div>

                        {/* 도면 정보 */}
                        <div className={styles.blueprintDetails}>
                            <h4 className={styles.detailsTitle}>도면 정보</h4>
                            <div className={styles.detailsGrid}>
                                <div className={styles.detailItem}>
                                    <span className={styles.detailLabel}>파일명:</span>
                                    <span className={styles.detailValue}>floor1_construction.pdf</span>
                                </div>
                                <div className={styles.detailItem}>
                                    <span className={styles.detailLabel}>크기:</span>
                                    <span className={styles.detailValue}>2.5MB</span>
                                </div>
                                <div className={styles.detailItem}>
                                    <span className={styles.detailLabel}>업로드:</span>
                                    <span className={styles.detailValue}>2025.07.15</span>
                                </div>
                                <div className={styles.detailItem}>
                                    <span className={styles.detailLabel}>업로드자:</span>
                                    <span className={styles.detailValue}>김관리자</span>
                                </div>
                                <div className={styles.detailItem}>
                                    <span className={styles.detailLabel}>수정:</span>
                                    <span className={styles.detailValue}>2025.07.17</span>
                                </div>
                            </div>
                        </div>

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