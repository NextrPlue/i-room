import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { workerAPI } from '../api/workerAPI';
import styles from '../styles/WorkerHome.module.css'

const WorkerHome = () => {
    const navigate = useNavigate();
    const [activeTab, setActiveTab] = useState('attendance');
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    // 상태 관리
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

    const [attendanceData, setAttendanceData] = useState({
        checkIn: null,
        checkOut: null,
        isCheckedIn: false
    })

    // 컴포넌트 마운트 시 데이터 로드
    useEffect(() => {
        loadWorkerData();
        loadAttendanceData();
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
            const response = await fetch('/api/management/entries', {
                headers: {
                    'Authorization': `Bearer ${localStorage.getItem('authToken')}`
                }
            });

            const data = await response.json();

            if (data.status === 'success' && data.data) {
                // enterDate에서 시간 추출 (HH:mm 형식)
                const enterTime = data.data.enterDate ?
                    data.data.enterDate.split('T')[1].substring(0, 5) : null;

                const outTime = data.data.outDate ?
                    data.data.outDate.split('T')[1].substring(0, 5) : null;

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

    const safetyEducation = [
        {
            id: 1,
            title: '건설현장 기초 안전 교육',
            date: '2025.07.01',
            status: 'completed',
            buttonText: '이수완료'
        }
    ];

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
                        <path d="M12 12C13.6569 12 15 10.6569 15 9C15 7.34315 13.6569 6 12 6C10.3431 6 9 7.34315 9 9C9 10.6569 10.3431 12 12 12Z" fill="#888" />
                        <path d="M12 14C8.68629 14 6 16.6863 6 20H18C18 16.6863 15.3137 14 12 14Z" fill="#888" />
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
                        <h2 className={styles.sectionTitle}>안전교육이력</h2>
                        {safetyEducation.map(edu => (
                            <div key={edu.id} className={styles.educationCard}>
                                {/* 이수증 보기 버튼 우측 상단 */}
                                {edu.status === 'completed' && (
                                    <button className={styles.certButton}>
                                        이수증 보기
                                    </button>
                                )}

                                <div className={styles.educationContent}>
                                    <h3 className={styles.educationTitle}>{edu.title}</h3>
                                    <p className={styles.educationDate}>교육 일시: {edu.date}</p>
                                </div>

                                <button
                                    className={`${styles.educationButton} ${edu.status === 'completed' ? styles.educationButtonCompleted : ''}`}
                                    disabled={edu.status === 'completed'}
                                >
                                    {edu.buttonText}
                                </button>
                            </div>
                        ))}
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
