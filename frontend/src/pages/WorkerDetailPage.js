import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { userAPI, managementAPI } from '../api/api';
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

    // 출입현황 관련 상태
    const [attendance, setAttendance] = useState(null);
    const [attendanceLoading, setAttendanceLoading] = useState(false);
    const [attendanceError, setAttendanceError] = useState(null);

    // 출입현황 조회 함수
    const fetchWorkerAttendance = async () => {
        setAttendanceLoading(true);
        setAttendanceError(null);

        try {
            const response = await managementAPI.getWorkerAttendance(workerId);

            // 응답 구조에 따른 안전한 처리
            if (response.data) {
                setAttendance(response.data);
            } else {
                setAttendance(null);
            }
        } catch (error) {
            console.error('출입현황 조회 실패:', error);
            setAttendanceError(error.message || '출입현황을 불러오는데 실패했습니다.');
            setAttendance(null);
        } finally {
            setAttendanceLoading(false);
        }
    };

    // 교육이력 조회 함수
    const fetchWorkerEducation = async (page = 0) => {
        setEducationLoading(true);
        setEducationError(null);

        try {
            const response = await managementAPI.getWorkerEducation(workerId, page, pageSize);
            setEducations(response.data.content || []);
            setTotalPages(response.data.totalPages || 0);
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
                const response = await userAPI.getWorkerDetail(workerId);
                setWorker(response.data);
            } catch (error) {
                console.error('근로자 상세 정보 조회 실패:', error);
                setWorker(null);
            } finally {
                setLoading(false);
            }
        };

        if (workerId) {
            (async () =>{
                await fetchWorkerDetail();
                await fetchWorkerEducation(0);
                await fetchWorkerAttendance();
            })();
        }
    }, [workerId]);

    const handleBackClick = () => {
        navigate('/admin/worker');
    };

    // 교육이력 페이지 변경 핸들러
    const handleEducationPageChange = (page) => {
        (async () => {
            await fetchWorkerEducation(page);
        })();
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
            await managementAPI.createWorkerEducation(educationData);
            alert('안전교육이 등록되었습니다!');

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
            await userAPI.updateWorker(workerId, editForm);
            alert('근로자 정보가 수정되었습니다!');

            // 근로자 상세 정보 새로고침
            const detailResponse = await userAPI.getWorkerDetail(workerId);
            setWorker(detailResponse.data);

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
            <div className={styles.breadcrumb}>
                <span className={styles.breadcrumbText}>근로자 상세보기</span>
                <span className={styles.breadcrumbDivider}>•</span>
                <span className={styles.breadcrumbCurrent}>근로자 상세보기</span>
            </div>

            {/* 상단 프로필 소개 카드 */}
            <div className={styles.profileIntroCard}>
                <div className={styles.profileImageContainer}>
                    <div className={styles.profileImagePlaceholder}>
                      <span className={styles.profileInitial}>
                        {worker.name.charAt(0)}
                      </span>
                    </div>
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
                    <h3>개인정보</h3>
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
                    <h3>안전교육이력</h3>

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
                    <h3>출입현황</h3>

                    {attendanceLoading ? (
                        <div className={styles.educationLoading}>
                            <p>출입현황을 불러오는 중...</p>
                        </div>
                    ) : attendanceError ? (
                        <div className={styles.educationError}>
                            <p>{attendanceError}</p>
                            <button
                                className={styles.retryBtn}
                                onClick={fetchWorkerAttendance}
                            >
                                다시 시도
                            </button>
                        </div>
                    ) : !attendance ? (
                        <div className={styles.educationEmpty}>
                            <p>출입 기록이 없습니다.</p>
                        </div>
                    ) : (() => {
                        const { enterDate, outDate } = attendance;
                        return (
                            <>
                                <div className={styles.statusItem}>
                                    <div className={styles.statusRow}>
                                        <span className={styles.statusLabel}>출근시간 :</span>
                                        <div className={styles.statusTimeContainer}>
                                            {enterDate ? (
                                                <>
                                                    <span className={styles.statusDate}>
                                                        {new Date(enterDate).toLocaleDateString('ko-KR', {
                                                            month: '2-digit',
                                                            day: '2-digit'
                                                        })}
                                                    </span>
                                                    <span className={styles.statusTime}>
                                                        {new Date(enterDate).toLocaleTimeString('ko-KR', {
                                                            hour: '2-digit',
                                                            minute: '2-digit'
                                                        })}
                                                    </span>
                                                </>
                                            ) : (
                                                <span className={styles.statusTime}>-</span>
                                            )}
                                        </div>
                                        <span className={enterDate ? styles.attendanceBadge : styles.workingBadge}>
                                            {enterDate ? '출근 완료' : '미출근'}
                                        </span>
                                    </div>
                                </div>

                                <div className={styles.statusItem}>
                                    <div className={styles.statusRow}>
                                        <span className={styles.statusLabel}>퇴근시간 :</span>
                                        <div className={styles.statusTimeContainer}>
                                            {outDate ? (
                                                <>
                                                    <span className={styles.statusDate}>
                                                        {new Date(outDate).toLocaleDateString('ko-KR', {
                                                            month: '2-digit',
                                                            day: '2-digit'
                                                        })}
                                                    </span>
                                                    <span className={styles.statusTime}>
                                                        {new Date(outDate).toLocaleTimeString('ko-KR', {
                                                            hour: '2-digit',
                                                            minute: '2-digit'
                                                        })}
                                                    </span>
                                                </>
                                            ) : (
                                                <span className={styles.statusTime}>-</span>
                                            )}
                                        </div>
                                        <span className={outDate ? styles.attendanceBadge : styles.workingBadge}>
                                            {outDate ? '퇴근 완료' : '근무중'}
                                        </span>
                                    </div>
                                </div>
                            </>
                        );
                    })()}
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