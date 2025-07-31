import React, { useState } from 'react';
import styles from '../styles/WorkerManagement.module.css';

const WorkerManagementPage = () => {
    const [searchTerm, setSearchTerm] = useState('');

    // 더미 데이터 (실제로는 props나 API에서 받아올 예정)
    const workers = [
        { id: 1, name: '김재완', department: '건설팀', position: '철근공', phone: '010-1234-5678', bloodType: 'A형', location: '1구역', status: 'safe' },
        { id: 2, name: '이수진', department: '건설팀', position: '용접공', phone: '010-2345-6789', bloodType: 'B형', location: '2구역', status: 'safe' },
        { id: 3, name: '박민호', department: '안전팀', position: '안전관리자', phone: '010-3456-7890', bloodType: 'O형', location: '3구역', status: 'safe' },
        { id: 4, name: '최영희', department: '품질팀', position: '품질검사원', phone: '010-4567-8901', bloodType: 'AB형', location: '1구역', status: 'safe' },
        { id: 5, name: '정다은', department: '건설팀', position: '현장관리자', phone: '010-5678-9012', bloodType: 'A형', location: '2구역', status: 'safe' },
        { id: 6, name: '한석진', department: '기술팀', position: '기술자', phone: '010-6789-0123', bloodType: 'B형', location: '3구역', status: 'warning' },
        { id: 7, name: '윤서연', department: '건설팀', position: '철근공', phone: '010-7890-1234', bloodType: 'O형', location: '1구역', status: 'danger' },
        { id: 8, name: '조현우', department: '안전팀', position: '안전요원', phone: '010-8901-2345', bloodType: 'A형', location: '2구역', status: 'safe' },
    ];

    // 검색 필터링
    const filteredWorkers = workers.filter(worker =>
        worker.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
        worker.department.toLowerCase().includes(searchTerm.toLowerCase()) ||
        worker.position.toLowerCase().includes(searchTerm.toLowerCase())
    );

    // 상태별 스타일
    const getStatusClass = (status) => {
        const statusMap = {
            safe: styles.safe,
            warning: styles.warning,
            danger: styles.danger
        };
        return statusMap[status] || styles.safe;
    };

    const getStatusText = (status) => {
        const textMap = {
            safe: '정상',
            warning: '경고',
            danger: '위험'
        };
        return textMap[status] || '정상';
    };

    return (
        <div className={styles.page}>
            {/* 페이지 헤더 */}
            <header className={styles.pageHeader}>
                <h1 className={styles.pageTitle}>근로자 목록</h1>
            </header>

            {/* 통계 섹션 */}
            <section className={styles.statsSection}>
                <div className={styles.statCard}>
                    <div className={styles.statIcon} />
                    <div className={styles.statContent}>
                        <p className={styles.statLabel}>총근무자</p>
                        <p className={styles.statValue}>2,082</p>
                    </div>
                </div>
                <div className={styles.statCard}>
                    <div className={styles.statIcon} />
                    <div className={styles.statContent}>
                        <p className={styles.statLabel}>근무중</p>
                        <p className={styles.statValue}>1,893</p>
                    </div>
                </div>
                <div className={styles.statCard}>
                    <div className={styles.statIcon} />
                    <div className={styles.statContent}>
                        <p className={styles.statLabel}>퇴근</p>
                        <p className={styles.statValue}>189</p>
                    </div>
                </div>
            </section>

            {/* 검색/필터 섹션 */}
            <section className={styles.filterSection}>
                <input
                    className={styles.searchInput}
                    placeholder="이름, 소속, 직종으로 검색해보세요"
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)}
                />
                <button className={styles.addButton}>
                    + 신규 근로자 등록
                </button>
            </section>

            {/* 테이블 섹션 */}
            <section className={styles.tableSection}>
                <div className={styles.tableContainer}>
                    <table className={styles.dataTable}>
                        <thead>
                        <tr>
                            <th>근로자이름</th>
                            <th>소속</th>
                            <th>직종</th>
                            <th>연락처</th>
                            <th>혈액형</th>
                            <th>현재위치</th>
                            <th>건강상태</th>
                            <th>액션</th>
                        </tr>
                        </thead>
                        <tbody>
                        {filteredWorkers.length === 0 ? (
                            <tr>
                                <td colSpan="8" className={styles.emptyState}>
                                    {searchTerm ? '검색 결과가 없습니다.' : '등록된 근로자가 없습니다.'}
                                </td>
                            </tr>
                        ) : (
                            filteredWorkers.map((worker) => (
                                <tr key={worker.id}>
                                    <td className={styles.nameCell}>{worker.name}</td>
                                    <td>{worker.department}</td>
                                    <td>{worker.position}</td>
                                    <td>{worker.phone}</td>
                                    <td>{worker.bloodType}</td>
                                    <td>{worker.location}</td>
                                    <td>
                                            <span className={`${styles.healthStatus} ${getStatusClass(worker.status)}`}>
                                                {getStatusText(worker.status)}
                                            </span>
                                    </td>
                                    <td className={styles.actionCell}>
                                        <button className={styles.detailBtn}>상세</button>
                                        <button className={styles.editBtn}>수정</button>
                                    </td>
                                </tr>
                            ))
                        )}
                        </tbody>
                    </table>
                </div>

                {/* 페이지네이션 */}
                <div className={styles.pagination}>
                    <button className={styles.pageBtn}>이전</button>
                    {[1, 2, 3, 4, '...', 40].map((page, index) => (
                        <button key={index} className={styles.pageBtn}>
                            {page}
                        </button>
                    ))}
                    <button className={styles.pageBtn}>다음</button>
                </div>
            </section>
        </div>
    );
};

export default WorkerManagementPage;