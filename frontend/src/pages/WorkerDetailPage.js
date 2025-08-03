import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { userAPI } from '../api/api';
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
                    <p className={styles.workerSubtitle}>근무자 상세 정보</p>
                </div>

                <button className={styles.editButton}>수정</button>
            </div>

            {/* 하단 상세 정보 섹션 */}
            <div className={styles.detailSection}>
                {/* 좌측: 개인정보 */}
                <div className={styles.infoCard}>
                    <h3 className={styles.cardTitleCentered}>개인정보</h3>
                    <div className={styles.sectionDivider}></div>
                    <div className={styles.contactSection}>
                        <div className={styles.contactItem}>
                            <div className={styles.contactIcon}>📞</div>
                            <div className={styles.contactInfo}>
                                <span className={styles.contactLabel}>연락처</span>
                                <span className={styles.contactValue}>{worker.phone}</span>
                            </div>
                        </div>

                        <div className={styles.contactItem}>
                            <div className={styles.contactIcon}>✉️</div>
                            <div className={styles.contactInfo}>
                                <span className={styles.contactLabel}>이메일</span>
                                <span className={styles.contactValue}>{worker.email || 'test@example.com'}</span>
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

                    <button className={styles.registerCertificateBtn}>
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
        </div>
    );
};

export default WorkerDetailPage;