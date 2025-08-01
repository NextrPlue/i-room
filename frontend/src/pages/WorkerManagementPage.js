import React, {useState, useEffect} from 'react';
import {userAPI} from '../api/api';
import styles from '../styles/WorkerManagement.module.css';
import WorkerEditModal from '../components/WorkerEditModal';

const WorkerManagementPage = () => {
    const [searchTerm, setSearchTerm] = useState('');
    const [searchTarget, setSearchTarget] = useState('name'); // 검색 타겟 추가

    /**
     * @typedef {Object} Worker
     * @property {string} id
     * @property {string} name
     * @property {string} email
     * @property {string} department
     * @property {string} occupation
     * @property {string} phone
     * @property {string} bloodType
     */

    /** @type {[Worker[], Function]} */
    const [workers, setWorkers] = useState([]);

    const [selectedWorker, setSelectedWorker] = useState(null);
    const [isModalOpen, setIsModalOpen] = useState(false);

    const [currentPage, setCurrentPage] = useState(0); // 백엔드는 0부터 시작
    const [pageSize] = useState(10);
    const [totalPages, setTotalPages] = useState(1);


    useEffect(() => {
        console.log('[현재 페이지]:', currentPage);

        const fetchWorkers = async () => {
            try {
                // API 명세서에 맞게 파라미터 수정
                const params = {
                    page: currentPage,
                    size: pageSize
                };

                // 검색어가 있을 때만 target과 keyword 추가
                if (searchTerm) {
                    params.target = searchTarget;
                    params.keyword = searchTerm;
                }

                const data = await userAPI.getWorkers(params);
                setWorkers(data.content || []);
                setTotalPages(data.totalPages); // 서버 응답 기반
            } catch (error) {
                console.error('근로자 데이터 조회 실패:', error);
                setWorkers([]);
            }
        };

        fetchWorkers().catch(console.error);
    }, [currentPage, pageSize, searchTerm, searchTarget]);

    // 검색 필터링
    // const filteredWorkers = workers.filter(worker =>
    //     worker.name?.toLowerCase().includes(searchTerm.toLowerCase()) ||
    //     worker.department?.toLowerCase().includes(searchTerm.toLowerCase()) ||
    //     worker.occupation?.toLowerCase().includes(searchTerm.toLowerCase())
    // );

    // 수정 모달 열기
    const handleEditClick = (worker) => {
        setSelectedWorker(worker);
        setIsModalOpen(true);
    };

    // 모달 닫기
    const handleModalClose = () => {
        setIsModalOpen(false);
        setSelectedWorker(null);
    };

    // 저장
    const handleSave = async (editForm) => {
        try {
            // Worker ID가 없으면 에러
            if (!selectedWorker?.id) {
                console.error('근로자 ID가 없습니다.');
                alert('근로자 ID를 찾을 수 없습니다.');
                return;
            }

            // API 호출을 위한 데이터 변환
            const updateData = {
                name: editForm.name,
                email: editForm.email || "",
                phone: editForm.phone,
                bloodType: editForm.bloodType,
                gender: editForm.gender || "MALE",
                age: parseInt(editForm.age) || 0,
                weight: parseFloat(editForm.weight) || 0,
                height: parseFloat(editForm.height) || 0,
                jobTitle: editForm.jobTitle,
                occupation: editForm.occupation,
                department: editForm.department,
                faceImageUrl: editForm.faceImageUrl || ""
            };

            // API 호출
            const response = await userAPI.updateWorker(selectedWorker.id, updateData);

            console.log('API 응답:', response);

            if (response) {
                alert('저장이 완료되었습니다!');
                // 근로자 목록 새로고침
                const data = await userAPI.getWorkers();
                setWorkers(data.content || []);
                handleModalClose();
            }
        } catch (error) {
            console.error('저장 실패:', error);
            alert('저장에 실패했습니다: ' + (error.message || '알 수 없는 오류'));
        }
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
                <div className={styles.searchWrapper}>
                    <select
                        className={styles.searchSelect}
                        value={searchTarget}
                        onChange={(e) => setSearchTarget(e.target.value)}
                    >
                        <option value="name">이름</option>
                        <option value="email">이메일</option>
                    </select>
                    <input
                        className={styles.searchInput}
                        placeholder={searchTarget === 'name' ? '이름으로 검색해보세요' : '이메일로 검색해보세요'}
                        value={searchTerm}
                        onChange={(e) => {setSearchTerm(e.target.value);setCurrentPage(0);}}
                        onKeyPress={(e) => {
                            if (e.key === 'Enter') {
                                e.preventDefault();
                            }
                        }}
                    />
                </div>
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
                            <th>이메일</th>
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
                        {workers.length === 0 ? (
                            <tr>
                                <td colSpan="9" className={styles.emptyState}>
                                    {searchTerm ? '검색 결과가 없습니다.' : '등록된 근로자가 없습니다.'}
                                </td>
                            </tr>
                        ) : (
                            workers.map((worker) => (
                                <tr key={worker.id}>
                                    <td className={styles.nameCell}>{worker.name}</td>
                                    <td>{worker.email}</td>
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
                                        <button className={styles.editBtn} onClick={() => handleEditClick(worker)}>수정
                                        </button>
                                    </td>
                                </tr>
                            ))
                        )}
                        </tbody>
                    </table>
                </div>

                {/* 페이지네이션 */}
                <div className={styles.pagination}>
                    <button
                        className={styles.pageBtn}
                        onClick={() => setCurrentPage((prev) => Math.max(prev - 1, 0))}
                        disabled={currentPage === 0}
                    >
                        이전
                    </button>

                    {Array.from({ length: totalPages }, (_, index) => (
                        <button
                            key={index}
                            className={`${styles.pageBtn} ${currentPage === index ? styles.active : ''}`}
                            onClick={() => {console.log(`[페이지 버튼 클릭] ${index}페이지로 이동`);
                                setCurrentPage(index)}
                            }
                        >
                            {index + 1}
                        </button>
                    ))}

                    <button
                        className={styles.pageBtn}
                        onClick={() => setCurrentPage((prev) => Math.min(prev + 1, totalPages - 1))}
                        disabled={currentPage === totalPages - 1}
                    >
                        다음
                    </button>
                </div>

            </section>

            {/* 수정 모달 */}
            <WorkerEditModal
                isOpen={isModalOpen}
                worker={selectedWorker}
                onClose={handleModalClose}
                onSave={handleSave}
            />
        </div>
    );
};

export default WorkerManagementPage;