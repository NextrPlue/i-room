import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
// import { userAPI } from '../api/api'; // 실제 API 연결 시 사용
import styles from '../styles/WorkerDetail.module.css';

const WorkerDetailPage = () => {
    const { workerId } = useParams();
    const navigate = useNavigate();
    const [worker, setWorker] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchWorkerDetail = async () => {
            try {
                // TODO: 실제 API 연결 시 사용
                // const data = await userAPI.getWorkerDetail(workerId);
                // setWorker(data);

                // 임시 데이터 (나중에 제거)
                setWorker({
                    id: workerId,
                    name: '김재한',
                    department: '건설팀',
                    occupation: '철근공',
                    jobTitle: '근무자',
                    phone: '010-1234-5678',
                    bloodType: 'A',
                    currentLocation: '1구역',
                    healthStatus: '정상',
                    profileImage: null,
                    height: '178cm',
                    weight: '72kg'
                });
            } catch (error) {
                console.error('근로자 상세 정보 조회 실패:', error);
            } finally {
                setLoading(false);
            }
        };

        if (workerId) {
            fetchWorkerDetail();
        }
    }, [workerId]);

    const handleBackClick = () => {
        navigate('/admin/worker');
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
                    <div className={styles.educationItem}>
                        <div className={styles.educationHeader}>
                            <div className={styles.educationColorBar} style={{backgroundColor: '#10B981'}}></div>
                            <div className={styles.educationContent}>
                                <span className={styles.educationTitle}>건설현장 기초 안전 교육</span>
                                <span className={styles.educationDate}>교육 일시: 2025.07.01</span>
                                <span className={styles.completeBadge}>이수완료</span>
                            </div>
                            <button className={styles.certificateBtn}>이수증 보기</button>
                        </div>
                    </div>

                    <div className={styles.educationItem}>
                        <div className={styles.educationHeader}>
                            <div className={styles.educationColorBar} style={{backgroundColor: '#F59E0B'}}></div>
                            <div className={styles.educationContent}>
                                <span className={styles.educationTitle}>전기 안전 교육</span>
                                <span className={styles.educationDate}>교육 일시: 2025.07.01</span>
                                <span className={styles.inProgressBadge}>미이수</span>
                            </div>
                            <button className={styles.certificateBtnDisabled}>이수증 보기</button>
                        </div>
                    </div>

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