import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { workerAPI } from '../api/workerAPI';
import styles from '../styles/WorkerHome.module.css'

const WorkerHome = () => {
    const navigate = useNavigate();
    const [activeTab, setActiveTab] = useState('attendance');
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    // 근로자 상세정보 상태
    const [workerInfo, setWorkerInfo] = useState({
        name: '',
        greeting: '오늘도 안전하고 활기찬 하루 보내세요!',
        email: '',
        phone: '',
        company: '',
        position: '',
        jobTitle: '',
        bloodType: '',
        height: '',
        weight: '',
        gender: '',
        age: ''
    });

    // 근로자 출입내역 상태
    const [attendanceData, setAttendanceData] = useState({
        checkIn: null,
        checkOut: null,
        isCheckedIn: false
    })

    // 안전교육 상태
    const [safetyEducation, setSafetyEducation] = useState([]);

    // 컴포넌트 마운트 시 데이터 로드
    useEffect(() => {
        loadWorkerData();
        loadAttendanceData();
        loadEducationData();
    }, []);

    // 근로자 데이터 로드
    const loadWorkerData = async () => {
        try {
            setLoading(true);

            // 내 정보 조회
            const infoResponse = await workerAPI.getMyInfo();
            if (infoResponse.status === 'success' && infoResponse.data) {
                const data = infoResponse.data;
                setWorkerInfo({
                    name: data.name || '근로자',
                    greeting: '오늘도 안전하고 활기찬 하루 보내세요!',
                    email: data.email || '',
                    phone: data.phone || '',
                    company: data.department || '미지정',
                    position: data.occupation || '미지정',
                    jobTitle: data.jobTitle || '미지정',
                    bloodType: data.bloodType || '미지정',
                    height: data.height ? `${data.height}cm` : '미지정',
                    weight: data.weight ? `${data.weight}kg` : '미지정',
                    gender: data.gender === 'MALE' ? '남성' : '여성',
                    age: data.age || '미지정'
                });
            }
        } catch (error) {
            console.error('데이터 로드 실패:', error);
            setError('정보를 불러오는데 실패했습니다.');

            // 토큰이 없거나 만료된 경우 로그인 페이지로
            if (error.message && error.message.includes('401')) {
                navigate('/login');
            }
        } finally {
            setLoading(false);
        }
    };

    // 출입 내역 로드
    const loadAttendanceData = async () => {
        try {
            const response = await workerAPI.getMyEntry();

            if (response.status === 'success' && response.data) {
                // enterDate에서 시간 추출 (HH:mm 형식)
                const enterTime = response.data.enterDate ?
                    response.data.enterDate.split('T')[1].substring(0, 5) : null;

                const outTime = response.data.outDate ?
                    response.data.outDate.split('T')[1].substring(0, 5) : null;

                setAttendanceData({
                    checkIn: enterTime,
                    checkOut: outTime,
                    isCheckedIn: enterTime && !outTime
                });
            }
        } catch (error) {
            console.error('출입 데이터 로드 실패:', error);
        }
    };

    // 안전교육 로드
    const loadEducationData = async () => {
        try {
            const response = await workerAPI.getMyEducation(0, 100);

            if (response.status === 'success' && response.data) {
                const educationList = response.data.content.map(edu => ({
                    id: edu.id,
                    workerId: edu.workerId,
                    title: edu.name || '안전교육',
                    date: edu.eduDate || '날짜 정보 없음',
                    status: 'completed',
                    buttonText: '이수완료',
                    certUrl: edu.certUrl
                }));

                setSafetyEducation(educationList);
            }
        } catch (error) {
            console.error('안전교육 데이터 로드 실패:', error);
        }
    };

    // 이수증 보기 클릭 핸들러
    const handleViewCertificate = (certUrl) => {
        if (certUrl) {
            window.open(certUrl, '_blank');
        } else {
            alert('이수증을 찾을 수 없습니다.');
        }
    };

    const handleLogout = () => {
        workerAPI.logout();
        navigate('/login');
    };

    const handleCheckOut = () => {
        console.log('퇴근 처리');
        alert('퇴근 처리되었습니다.');
    };

    // 로딩 중일 때 표시
    if (loading) {
        return (
            <div className={styles.loadingContainer}>
                <div>로딩 중...</div>
            </div>
        );
    }

    // 에러 발생 시
    if (error) {
        return (
            <div className={styles.errorContainer}>
                <p>{error}</p>
                <button onClick={loadWorkerData}>다시 시도</button>
            </div>
        );
    }

    return (
        <div className={styles.workerDetailContainer}>
            <div className={styles.profileHeader}>
                <div className={styles.profileCircle}>
                    <svg width="60" height="60" viewBox="0 0 24 24" fill="none">
                        <circle cx="12" cy="12" r="10" fill="#d0d0d0" />
                        </svg>
                </div>
                <h1 className={styles.workerName}> 안녕하세요, {workerInfo?.name}님,</h1>
                <p className={styles.greetingMessage}>오늘도 안전하고 활기찬 하루 보내세요!</p>
            </div>

            <div className={styles.infoSection}>
                <div className={styles.infoGrid}>
                    <div className={styles.infoRow}>
                        <span className={styles.infoLabel}>이메일 :</span>
                        <span className={styles.infoValue}>{workerInfo.email}</span>
                    </div>
                    <div className={styles.infoRow}>
                        <span className={styles.infoLabel}>연락처 :</span>
                        <span className={styles.infoValue}>{workerInfo.phone}</span>
                    </div>
                    <div className={styles.infoRow}>
                        <span className={styles.infoLabel}>소속 / 직종 :</span>
                        <span className={styles.infoValue}>{workerInfo.company} / {workerInfo.position}</span>
                    </div>
                    <div className={styles.infoRow}>
                        <span className={styles.infoLabel}>혈액형 :</span>
                        <span className={styles.infoValue}>{workerInfo.bloodType}</span>
                    </div>
                    <div className={styles.infoRow}>
                        <span className={styles.infoLabel}>키 / 몸무게 :</span>
                        <span className={styles.infoValue}>{workerInfo.height} / {workerInfo.weight}</span>
                    </div>
                </div>
                <p className={styles.infoNotice}>
                    개인정보 변경이 필요하거나 잘못된 정보가 있을 경우,<br />
                    현장 관리자에게 문의해 주시기 바랍니다.
                </p>
            </div>

            <div className={styles.tabMenu}>
                <button
                    className={`${styles.tabButton} ${activeTab === 'attendance' ? styles.tabButtonActive : ''}`}
                    onClick={() => setActiveTab('attendance')}
                >
                    출입현황
                </button>
                <button
                    className={`${styles.tabButton} ${activeTab === 'education' ? styles.tabButtonActive : ''}`}
                    onClick={() => setActiveTab('education')}
                >
                    안전교육이력
                </button>
            </div>

            <div className={styles.tabContent}>
                {activeTab === 'attendance' && (
                    <div className={styles.attendanceSection}>
                        <h2 className={styles.sectionTitle}>출입현황</h2>
                        <div className={styles.attendanceCard}>
                            <div className={styles.attendanceRow}>
                                <span className={styles.attendanceLabel}>출근시간 :</span>
                                <div className={styles.attendanceTime}>
                                    <span className={styles.timeValue}>{attendanceData.checkIn || '-'}</span>
                                    {attendanceData.isCheckedIn && (
                                        <span className={styles.checkInBadge}>출근완료!</span>
                                    )}
                                </div>
                            </div>
                            <div className={styles.attendanceRow}>
                                <span className={styles.attendanceLabel}>퇴근시간 :</span>
                                <div className={styles.attendanceTime}>
                                    {!attendanceData.checkOut && attendanceData.isCheckedIn && (
                                        <button className={styles.checkOutButton} onClick={handleCheckOut}>
                                            퇴근 전
                                        </button>
                                    )}
                                    {attendanceData.checkOut && (
                                        <span className={styles.timeValue}>{attendanceData.checkOut}</span>
                                    )}
                                </div>
                            </div>
                        </div>
                    </div>
                )}

                {activeTab === 'education' && (
                    <div className={styles.educationSection}>
                        <h2 className={styles.sectionTitle}>
                            안전교육이력
                            {safetyEducation.length > 0 && (
                                <span className={styles.totalCount}> (총 {safetyEducation.length}건)</span>
                            )}
                        </h2>

                        {safetyEducation.length > 0 ? (
                            <div className={styles.educationList}>
                                {safetyEducation.map(edu => (
                                    <div key={edu.id} className={styles.educationCard}>
                                        {/* 이수증 보기 버튼 우측 상단 */}
                                        {edu.certUrl && (
                                            <button
                                                className={styles.certButton}
                                                onClick={() => handleViewCertificate(edu.certUrl)}
                                            >
                                                이수증 보기
                                            </button>
                                        )}

                                        <div className={styles.educationContent}>
                                            <h3 className={styles.educationTitle}>{edu.title}</h3>
                                            <p className={styles.educationDate}>교육 일시: {edu.date}</p>
                                        </div>

                                        <button
                                            className={`${styles.educationButton} ${styles.educationButtonCompleted}`}
                                            disabled={true}
                                        >
                                            {edu.buttonText}
                                        </button>
                                    </div>
                                ))}
                            </div>
                        ) : (
                            <p className={styles.noEducation}>
                                안전교육 이력이 없습니다.
                            </p>
                        )}
                    </div>
                )}
            </div>

            <button className={styles.logoutButton} onClick={handleLogout}>
                로그아웃
            </button>
        </div>
    );
};

export default WorkerHome;
