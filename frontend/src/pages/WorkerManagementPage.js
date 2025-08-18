import React, {useState, useEffect, useCallback} from 'react';
import {managementAPI, userAPI} from '../api/api';
import {useNavigate} from 'react-router-dom';
import styles from '../styles/WorkerManagement.module.css';
import WorkerEditModal from '../components/WorkerEditModal';
import WorkerAddModal from '../components/WorkerAddModal';
import SuccessModal from '../components/SuccessModal';
import ConfirmModal from '../components/ConfirmModal';

/** @typedef {{
 *  total: number,
 *  working: number,
 *  offWork: number,
 *  absent: number,
 *  loading: boolean
 * }} WorkerStats */

const WorkerManagementPage = () => {
    const navigate = useNavigate();
    const [searchTerm, setSearchTerm] = useState('');
    const [searchTarget, setSearchTarget] = useState('name');

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
    const [isAddModalOpen, setIsAddModalOpen] = useState(false);
    
    // 성공 모달 상태
    const [successModal, setSuccessModal] = useState({
        isOpen: false,
        title: '',
        message: ''
    });
    
    // 삭제 확인 모달 상태
    const [confirmModal, setConfirmModal] = useState({
        isOpen: false,
        targetWorker: null
    });

    const [currentPage, setCurrentPage] = useState(0);
    const [pageSize] = useState(10);
    const [totalPages, setTotalPages] = useState(1);

    // 출입 통계 상태
    /** @type {[WorkerStats, import('react').Dispatch<import('react').SetStateAction<WorkerStats>>]} */
    const [workerStats, setWorkerStats] = useState({
        total: 0,
        working: 0,
        offWork: 0,
        absent: 0,
        loading: false
    });

    const refreshWorkersList = useCallback(async () => {
        try {
            const params = {
                page: currentPage,
                size: pageSize
            };

            // 검색어가 있을 때만 target과 keyword 추가
            if (searchTerm) {
                params.target = searchTarget;
                params.keyword = searchTerm;
            }

            const response = await userAPI.getWorkers(params);
            const workerList = response.data.content || [];
            setWorkers(workerList);
            setTotalPages(response.data.totalPages);

            // 근로자 목록이 업데이트되면 통계 조회
            await fetchWorkerStats();
        } catch (error) {
            console.error('근로자 데이터 조회 실패:', error);
            setWorkers([]);
        }
    }, [currentPage, pageSize, searchTerm, searchTarget]);

    useEffect(() => {
        refreshWorkersList().catch(console.error);
    }, [refreshWorkersList]);


    // 출입 통계 조회 함수
    const fetchWorkerStats = async () => {
        try {
            setWorkerStats(prev => ({...prev, loading: true}));
            const response = await managementAPI.getWorkerStats();
            setWorkerStats({
                ...response.data,
                loading: false
            });
        } catch (error) {
            console.error('출입 통계 조회 실패:', error);
            setWorkerStats(prev => ({...prev, loading: false}));
        }
    };

    const handleDetailClick = (worker) => {
        navigate(`/worker/${worker.id}`);
    };

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

    // 등록 모달 열기
    const handleAddClick = () => {
        setIsAddModalOpen(true);
    };

    // 등록 모달 닫기
    const handleAddModalClose = () => {
        setIsAddModalOpen(false);
    };

    // 등록 저장
    const handleAddSave = async (addForm) => {
        try {
            // API 호출을 위한 데이터 변환
            const createData = {
                name: addForm.name,
                email: addForm.email || "",
                password: addForm.password,
                phone: addForm.phone,
                bloodType: addForm.bloodType,
                gender: addForm.gender || "MALE",
                age: parseInt(addForm.age) || 0,
                weight: parseFloat(addForm.weight) || 0,
                height: parseFloat(addForm.height) || 0,
                jobTitle: addForm.jobTitle,
                occupation: addForm.occupation,
                department: addForm.department,
                faceImageUrl: addForm.faceImageUrl || ""
            };

            // API 호출
            const response = await userAPI.createWorker(createData);

            if (response) {
                showSuccessModal('등록 완료', '근로자 등록이 완료되었습니다!');
                await refreshWorkersList();
                handleAddModalClose();
            }
        } catch (error) {
            console.error('등록 실패:', error);
            alert('등록에 실패했습니다: ' + (error.message || '알 수 없는 오류'));
        }
    };


    // 수정 저장
    const handleSave = async (editForm) => {
        try {
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

            const response = await userAPI.updateWorker(selectedWorker.id, updateData);

            if (response) {
                showSuccessModal('수정 완료', '저장이 완료되었습니다!');
                // 근로자 목록 새로고침
                const listResponse = await userAPI.getWorkers();
                setWorkers(listResponse.data.content || []);
                handleModalClose();
            }
        } catch (error) {
            console.error('저장 실패:', error);
            alert('저장에 실패했습니다: ' + (error.message || '알 수 없는 오류'));
        }
    };

    // 삭제 버튼 클릭 핸들러
    const handleDeleteClick = (worker) => {
        setConfirmModal({
            isOpen: true,
            targetWorker: worker
        });
    };
    
    // 삭제 확인 실행
    const handleConfirmDelete = async () => {
        if (!confirmModal.targetWorker) return;
        
        try {
            await userAPI.deleteWorker(confirmModal.targetWorker.id);
            
            // 목록 새로고침
            await refreshWorkersList();
            
            showSuccessModal('삭제 완료', '근로자가 성공적으로 삭제되었습니다.');
        } catch (error) {
            console.error('근로자 삭제 실패:', error);
            alert('근로자 삭제에 실패했습니다: ' + (error.message || '알 수 없는 오류'));
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
            targetWorker: null
        });
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
                        <p className={styles.statValue}>
                            {workerStats.loading ? '...' : workerStats.total}
                        </p>
                    </div>
                </div>
                <div className={styles.statCard}>
                    <div className={styles.statIcon}/>
                    <div className={styles.statContent}>
                        <p className={styles.statLabel}>근무중</p>
                        <p className={styles.statValue}>
                            {workerStats.loading ? '...' : workerStats.working}
                        </p>
                    </div>
                </div>
                <div className={styles.statCard}>
                    <div className={styles.statIcon}/>
                    <div className={styles.statContent}>
                        <p className={styles.statLabel}>퇴근</p>
                        <p className={styles.statValue}>
                            {workerStats.loading ? '...' : workerStats.offWork}
                        </p>
                    </div>
                </div>
                <div className={styles.statCard}>
                    <div className={styles.statIcon}/>
                    <div className={styles.statContent}>
                        <p className={styles.statLabel}>미출근</p>
                        <p className={styles.statValue}>
                            {workerStats.loading ? '...' : workerStats.absent}
                        </p>
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
                        onChange={(e) => {
                            setSearchTerm(e.target.value);
                            setCurrentPage(0);
                        }}
                        onKeyPress={(e) => {
                            if (e.key === 'Enter') {
                                e.preventDefault();
                            }
                        }}
                    />
                </div>
                <button className={styles.addButton} onClick={handleAddClick}>
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
                                        <button className={styles.detailBtn}
                                                onClick={() => handleDetailClick(worker)}>상세
                                        </button>
                                        <button className={styles.editBtn}
                                                onClick={() => handleEditClick(worker)}>수정
                                        </button>
                                        <button className={styles.deleteBtn}
                                                onClick={() => handleDeleteClick(worker)}>삭제
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

                    {Array.from({length: totalPages}, (_, index) => (
                        <button
                            key={index}
                            className={`${styles.pageBtn} ${currentPage === index ? styles.active : ''}`}
                            onClick={() => {
                                setCurrentPage(index)
                            }
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

            {/* 등록 모달 */}
            <WorkerAddModal
                isOpen={isAddModalOpen}
                onClose={handleAddModalClose}
                onSave={handleAddSave}
            />
            
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
                title="근로자 삭제 확인"
                message="을 정말 삭제하시겠습니까?"
                targetName={confirmModal.targetWorker ? confirmModal.targetWorker.name : ''}
                onConfirm={handleConfirmDelete}
                onCancel={handleCloseConfirmModal}
                confirmButtonText="삭제하기"
                type="danger"
            />
        </div>
    );
};

export default WorkerManagementPage;
