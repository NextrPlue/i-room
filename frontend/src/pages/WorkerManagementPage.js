import React, {useState, useEffect} from 'react';
import {userAPI} from '../api/api';
import styles from '../styles/WorkerManagement.module.css';

const WorkerManagementPage = () => {
    const [searchTerm, setSearchTerm] = useState('');

    /**
     * @typedef {Object} Worker
     * @property {string} id
     * @property {string} name
     * @property {string} department
     * @property {string} occupation
     * @property {string} phone
     * @property {string} bloodType
     */

    /** @type {[Worker[], Function]} */
    const [workers, setWorkers] = useState([]);

    useEffect(() => {
        const fetchWorkers = async () => {
            try {
                const data = await userAPI.getWorkers();
                setWorkers(data.content || []);
            } catch (error) {
                console.error('근로자 데이터 조회 실패:', error);
                setWorkers([]);
            }
        };

        fetchWorkers().catch(console.error);
    }, []);

    // 검색 필터링
    const filteredWorkers = workers.filter(worker =>
        worker.name?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        worker.department?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        worker.occupation?.toLowerCase().includes(searchTerm.toLowerCase())
    );

    return (
        <div className={styles.page}>
            {/* 페이지 헤더 */}
            <header className={styles.pageHeader}>
                <h1 className={styles.pageTitle}>근로자 목록</h1>
            </header>

            {/* 통계 섹션 */}
            <section className={styles.statsSection}>
                <div className={styles.statCard}>
                    <div className={styles.statIcon}/>
                    <div className={styles.statContent}>
                        <p className={styles.statLabel}>총근무자</p>
                        <p className={styles.statValue}>2,082</p>
                    </div>
                </div>
                <div className={styles.statCard}>
                    <div className={styles.statIcon}/>
                    <div className={styles.statContent}>
                        <p className={styles.statLabel}>근무중</p>
                        <p className={styles.statValue}>1,893</p>
                    </div>
                </div>
                <div className={styles.statCard}>
                    <div className={styles.statIcon}/>
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
                                    <td>{worker.occupation}</td>
                                    <td>{worker.phone}</td>
                                    <td>{worker.bloodType}형</td>
                                    <td>-</td>
                                    <td>
                                            <span className={`${styles.healthStatus} ${styles.safe}`}>
                                                정상
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