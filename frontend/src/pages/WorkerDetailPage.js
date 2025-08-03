import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { userAPI } from '../api/api';
import EducationAddModal from '../components/EducationAddModal';
import WorkerEditModal from '../components/WorkerEditModal';
import styles from '../styles/WorkerDetail.module.css';

const WorkerDetailPage = () => {
    const { workerId } = useParams();
    const navigate = useNavigate();
    const [worker, setWorker] = useState(null);
    const [loading, setLoading] = useState(true);

    // 교육이력 관련 상태
    const [educations, setEducations] = useState([]);
    const [educationLoading, setEducationLoading] = useState(false);
    const [educationError, setEducationError] = useState(null);
    const [currentPage, setCurrentPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [pageSize] = useState(3); // 교육이력은 3개씩 표시
    
    // 교육등록 모달 관련 상태
    const [isEducationAddModalOpen, setIsEducationAddModalOpen] = useState(false);
    
    // 근로자 수정 모달 관련 상태
    const [isWorkerEditModalOpen, setIsWorkerEditModalOpen] = useState(false);

    // 교육이력 조회 함수
    const fetchWorkerEducation = async (page = 0) => {
        setEducationLoading(true);
        setEducationError(null);

        try {
            console.log('교육이력 조회 시작:', workerId, '페이지:', page);
            const data = await userAPI.getWorkerEducation(workerId, page, pageSize);
            console.log('교육이력 조회 성공:', data);
            console.log('교육이력 content:', data.content);
            console.log('첫 번째 교육이력 항목:', data.content?.[0]);

            setEducations(data.content || []);
            setTotalPages(data.totalPages || 0);
            setCurrentPage(page);
        } catch (error) {
            console.error('교육이력 조회 실패:', error);
            setEducationError(error.message || '교육이력을 불러오는데 실패했습니다.');
            setEducations([]);
        } finally {
            setEducationLoading(false);
        }
    };

    useEffect(() => {
        const fetchWorkerDetail = async () => {
            try {
                console.log('근로자 상세 정보 조회 시작:', workerId);
                const data = await userAPI.getWorkerDetail(workerId);
                console.log('근로자 상세 정보 조회 성공:', data);
                setWorker(data);
            } catch (error) {
                console.error('근로자 상세 정보 조회 실패:', error);
                setWorker(null);
            } finally {
                setLoading(false);
            }
        };

        if (workerId) {
            fetchWorkerDetail();
            fetchWorkerEducation(0); // 교육이력도 함께 조회
        }
    }, [workerId]);

    const handleBackClick = () => {
        navigate('/admin/worker');
    };

    // 교육이력 페이지 변경 핸들러
    const handleEducationPageChange = (page) => {
        fetchWorkerEducation(page);
    };

    // 교육등록 모달 열기
    const handleEducationAddClick = () => {
        setIsEducationAddModalOpen(true);
    };

    // 교육등록 모달 닫기
    const handleEducationAddModalClose = () => {
        setIsEducationAddModalOpen(false);
    };

    // 교육등록 저장
    const handleEducationAddSave = async (educationData) => {
        try {
            console.log('교육등록 시작:', educationData);
            const response = await userAPI.createWorkerEducation(educationData);
            console.log('교육등록 성공:', response);
            
            alert('안전교육이 등록되었습니다!');
            
            // 교육이력 목록 새로고침 (첫 번째 페이지로)
            await fetchWorkerEducation(0);
            
            // 모달 닫기
            handleEducationAddModalClose();
        } catch (error) {
            console.error('교육등록 실패:', error);
            alert('교육등록에 실패했습니다: ' + (error.message || '알 수 없는 오류'));
        }
    };

    // 근로자 수정 모달 열기
    const handleWorkerEditClick = () => {
        setIsWorkerEditModalOpen(true);
    };

    // 근로자 수정 모달 닫기
    const handleWorkerEditModalClose = () => {
        setIsWorkerEditModalOpen(false);
    };

    // 근로자 수정 저장
    const handleWorkerEditSave = async (editForm) => {
        try {
            console.log('근로자 정보 수정 시작:', editForm);
            const response = await userAPI.updateWorker(workerId, editForm);
            console.log('근로자 정보 수정 성공:', response);
            
            alert('근로자 정보가 수정되었습니다!');
            
            // 근로자 상세 정보 새로고침
            const data = await userAPI.getWorkerDetail(workerId);
            setWorker(data);
            
            // 모달 닫기
            handleWorkerEditModalClose();
        } catch (error) {
            console.error('근로자 정보 수정 실패:', error);
            alert('근로자 정보 수정에 실패했습니다: ' + (error.message || '알 수 없는 오류'));
        }
    };

    if (loading) {
        return (
            <div className={styles.loadingContainer}>
                <div className={styles.loading}>로딩 중...</div>
            </div>
        );
    }

    if (!worker) {
        return (
            <div className={styles.errorContainer}>
                <div className={styles.error}>근로자 정보를 찾을 수 없습니다.</div>
            </div>
        );
    }

    return (
        <div className={styles.page}>
            {/* 브레드크럼 */}
            <div className={styles.breadcrumb}>
                <span className={styles.breadcrumbText}>근로자 상세보기</span>
                <span className={styles.breadcrumbDivider}>•</span>
                <span className={styles.breadcrumbCurrent}>근로자 상세보기</span>
            </div>

            {/* 상단 프로필 소개 카드 */}
            <div className={styles.profileIntroCard}>
                <div className={styles.profileImageContainer}>
                    {worker.profileImage ? (
                        <img
                            src={worker.profileImage}
                            alt={worker.name}
                            className={styles.profileImage}
                        />
                    ) : (
                        <div className={styles.profileImagePlaceholder}>
                            <span className={styles.profileInitial}>
                                {worker.name.charAt(0)}
                            </span>
                        </div>
                    )}
                </div>

                <div className={styles.greetingContent}>
                    <h1 className={styles.workerName}>{worker.name}</h1>
                    <p className={styles.workerPosition}>{worker.department} {worker.occupation}</p>
                    <p className={styles.workerJobTitle}>{worker.jobTitle || '직책 미설정'}</p>
                    <div className={styles.workerContactInfo}>
                        <span className={styles.contactText}>📧 {worker.email || '이메일 미등록'}</span>
                        <span className={styles.contactText}>📞 {worker.phone || '연락처 미등록'}</span>
                    </div>
                </div>

                <button 
                    className={styles.editButton}
                    onClick={handleWorkerEditClick}
                >
                    수정
                </button>
            </div>

            {/* 하단 상세 정보 섹션 */}
            <div className={styles.detailSection}>
                {/* 좌측: 개인정보 */}
                <div className={styles.infoCard}>
                    <h3 className={styles.cardTitleCentered}>개인정보</h3>
                    <div className={styles.sectionDivider}></div>
                    <div className={styles.contactSection}>

                        <div className={styles.contactItem}>
                            <div className={styles.contactIcon}>👤</div>
                            <div className={styles.contactInfo}>
                                <span className={styles.contactLabel}>개인정보</span>
                                <div className={styles.combinedValue}>
                                    <span className={styles.valueItem}>
                                        성별: {worker.gender === 'MALE' ? '남성' : worker.gender === 'FEMALE' ? '여성' : '미설정'}
                                    </span>
                                    <span className={styles.valueDivider}>•</span>
                                    <span className={styles.valueItem}>
                                        나이: {worker.age ? `${worker.age}세` : '미설정'}
                                    </span>
                                </div>
                            </div>
                        </div>

                        <div className={styles.contactItem}>
                            <div className={styles.contactIcon}>📏</div>
                            <div className={styles.contactInfo}>
                                <span className={styles.contactLabel}>신체정보</span>
                                <div className={styles.combinedValue}>
                                    <span className={styles.valueItem}>
                                        키: {worker.height ? `${worker.height}cm` : '미설정'}
                                    </span>
                                    <span className={styles.valueDivider}>•</span>
                                    <span className={styles.valueItem}>
                                        몸무게: {worker.weight ? `${worker.weight}kg` : '미설정'}
                                    </span>
                                </div>
                            </div>
                        </div>

                        <div className={styles.contactItem}>
                            <div className={styles.contactIcon}>🩸</div>
                            <div className={styles.contactInfo}>
                                <span className={styles.contactLabel}>혈액형</span>
                                <span className={styles.contactValue}>{worker.bloodType}형</span>
                            </div>
                        </div>

                        <div className={styles.contactItem}>
                            <div className={styles.contactIcon}>📍</div>
                            <div className={styles.contactInfo}>
                                <span className={styles.contactLabel}>현재 위치</span>
                                <span className={styles.contactValue}>{worker.currentLocation}</span>
                            </div>
                        </div>

                        <div className={styles.contactItem}>
                            <div className={styles.contactIcon}>💊</div>
                            <div className={styles.contactInfo}>
                                <span className={styles.contactLabel}>건강 상태</span>
                                <span className={styles.contactValue}>{worker.healthStatus}</span>
                            </div>
                        </div>
                    </div>
                </div>

                {/* 중앙: 안전교육이력 */}
                <div className={styles.infoCard}>
                    <h3 className={styles.cardTitleCentered}>안전교육이력</h3>
                    <div className={styles.sectionDivider}></div>

                    {educationLoading ? (
                        <div className={styles.educationLoading}>
                            <p>교육이력을 불러오는 중...</p>
                        </div>
                    ) : educationError ? (
                        <div className={styles.educationError}>
                            <p>{educationError}</p>
                            <button
                                className={styles.retryBtn}
                                onClick={() => fetchWorkerEducation(currentPage)}
                            >
                                다시 시도
                            </button>
                        </div>
                    ) : educations.length === 0 ? (
                        <div className={styles.educationEmpty}>
                            <p>등록된 교육이력이 없습니다.</p>
                        </div>
                    ) : (
                        <>
                            {educations.map((education) => (
                                <div key={education.id} className={styles.educationItem}>
                                    <div className={styles.educationHeader}>
                                        <div
                                            className={styles.educationColorBar}
                                            style={{
                                                backgroundColor: education.certUrl ? '#10B981' : '#F59E0B'
                                            }}
                                        ></div>
                                        <div className={styles.educationContent}>
                                            <span className={styles.educationTitle}>
                                                {education.name || '교육명 없음'}
                                            </span>
                                            <span className={styles.educationDate}>
                                                교육 일시: {education.eduDate || '날짜 없음'}
                                            </span>
                                            <span className={
                                                education.certUrl
                                                    ? styles.completeBadge
                                                    : styles.inProgressBadge
                                            }>
                                                {education.certUrl ? '이수완료' : '미이수'}
                                            </span>
                                        </div>
                                        <button
                                            className={
                                                education.certUrl
                                                    ? styles.certificateBtn
                                                    : styles.certificateBtnDisabled
                                            }
                                            disabled={!education.certUrl}
                                            onClick={() => education.certUrl && window.open(education.certUrl, '_blank')}
                                        >
                                            이수증 보기
                                        </button>
                                    </div>
                                </div>
                            ))}

                            {/* 페이지네이션 */}
                            {totalPages > 1 && (
                                <div className={styles.educationPagination}>
                                    <button
                                        className={styles.pageBtn}
                                        onClick={() => handleEducationPageChange(currentPage - 1)}
                                        disabled={currentPage === 0}
                                    >
                                        이전
                                    </button>

                                    {Array.from({ length: totalPages }, (_, index) => (
                                        <button
                                            key={index}
                                            className={`${styles.pageBtn} ${
                                                currentPage === index ? styles.active : ''
                                            }`}
                                            onClick={() => handleEducationPageChange(index)}
                                        >
                                            {index + 1}
                                        </button>
                                    ))}

                                    <button
                                        className={styles.pageBtn}
                                        onClick={() => handleEducationPageChange(currentPage + 1)}
                                        disabled={currentPage === totalPages - 1}
                                    >
                                        다음
                                    </button>
                                </div>
                            )}
                        </>
                    )}

                    <button 
                        className={styles.registerCertificateBtn}
                        onClick={handleEducationAddClick}
                    >
                        📋 이수증 등록
                    </button>
                </div>

                {/* 우측: 출입현황 */}
                <div className={styles.infoCard}>
                    <h3 className={styles.cardTitleCentered}>출입현황</h3>
                    <div className={styles.sectionDivider}></div>
                    <div className={styles.statusItem}>
                        <div className={styles.statusRow}>
                            <span className={styles.statusLabel}>출근시간 :</span>
                            <span className={styles.statusTime}>08:20</span>
                            <span className={styles.attendanceBadge}>출근 완료</span>
                        </div>
                    </div>

                    <div className={styles.statusItem}>
                        <div className={styles.statusRow}>
                            <span className={styles.statusLabel}>퇴근시간 :</span>
                            <span className={styles.statusTime}>-</span>
                            <span className={styles.workingBadge}>근무중</span>
                        </div>
                    </div>
                </div>
            </div>

            {/* 뒤로가기 버튼 */}
            <button className={styles.backButton} onClick={handleBackClick}>
                ← 목록으로 돌아가기
            </button>

            {/* 교육등록 모달 */}
            <EducationAddModal
                isOpen={isEducationAddModalOpen}
                onClose={handleEducationAddModalClose}
                onSave={handleEducationAddSave}
                workerId={workerId}
                workerName={worker?.name}
            />

            {/* 근로자 수정 모달 */}
            <WorkerEditModal
                isOpen={isWorkerEditModalOpen}
                worker={worker}
                onClose={handleWorkerEditModalClose}
                onSave={handleWorkerEditSave}
            />
        </div>
    );
};

export default WorkerDetailPage;