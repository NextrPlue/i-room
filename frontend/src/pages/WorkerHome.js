import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import styles from '../styles/WorkerHome.module.css'

const WorkerHome = () => {
    const navigate = useNavigate();
    const [activeTab, setActiveTab] = useState('attendance');

    const workerInfo = {
        name: '김재환',
        greeting: '오늘도 안전하고 활기찬 하루 보내세요!',
        company: '건설부',
        position: '철근공',
        bloodType: 'B형',
        height: '178cm',
        weight: '72kg'
    };

    const attendanceData = {
        checkIn: '08:20',
        checkOut: null,
        isCheckedIn: true
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
        navigate('/login');
    };

    const handleCheckOut = () => {
        console.log('퇴근 처리');
        alert('퇴근 처리되었습니다.');
    };

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
                <h1 className={styles.workerName}>안녕하세요 "{workerInfo.name}"님,</h1>
                <p className={styles.greetingMessage}>{workerInfo.greeting}</p>
            </div>

            <div className={styles.infoSection}>
                <div className={styles.infoGrid}>
                    <div className={styles.infoRow}>
                        <span className={styles.infoLabel}>소속 :</span>
                        <span className={styles.infoValue}>{workerInfo.company}</span>
                    </div>
                    <div className={styles.infoRow}>
                        <span className={styles.infoLabel}>혈액형 :</span>
                        <span className={styles.infoValue}>{workerInfo.bloodType}</span>
                    </div>
                    <div className={styles.infoRow}>
                        <span className={styles.infoLabel}>직종 :</span>
                        <span className={styles.infoValue}>{workerInfo.position}</span>
                    </div>
                    <div className={styles.infoRow}>
                        <span className={styles.infoLabel}>키 / 몸무게 :</span>
                        <span className={styles.infoValue}>{workerInfo.height} / {workerInfo.weight}</span>
                    </div>
                </div>
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
                                    <span className={styles.timeValue}>{attendanceData.checkIn}</span>
                                    {attendanceData.isCheckedIn && (
                                        <span className={styles.checkInBadge}>출근완료!</span>
                                    )}
                                </div>
                            </div>
                            <div className={styles.attendanceRow}>
                                <span className={styles.attendanceLabel}>퇴근시간 :</span>
                                <div className={styles.attendanceTime}>
                                    {!attendanceData.checkOut && (
                                        <button className={styles.checkOutButton} onClick={handleCheckOut}>
                                            퇴근 전
                                        </button>
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
