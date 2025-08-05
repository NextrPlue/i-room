import React, { useState, useEffect } from 'react';
import styles from '../styles/Dashboard.module.css';
// import { dashboardAPI } from '../api/api'; // API 연동 시 사용

const DashboardPage = () => {
    // 상태 표시 컴포넌트 (중복 코드 제거용)
    const StatusDisplay = ({ icon, label, value, details, classPrefix }) => (
        <>
            <div className={styles[`${classPrefix}Content`]}>
                <div className={styles[`${classPrefix}Icon`]}>{icon}</div>
                <div className={styles[`${classPrefix}Text`]}>
                    <p className={styles[`${classPrefix}Label`]}>{label}</p>
                    <p className={styles[`${classPrefix}Value`]}>{value}</p>
                </div>
            </div>
            <p className={styles[`${classPrefix}Details`]}>
                {details}
            </p>
        </>
    );

    // 알림 아이콘 가져오기 함수
    const getAlertIcon = (type) => type === 'danger' ? '🚨' : '⚠️';
    // 종합 안전 점수
    const [safetyScore] = useState(85);

    // 실시간 위험 알림 데이터
    const [alerts] = useState([
        {
            id: 1,
            type: 'danger',
            title: '위험구역 접근 경고',
            description: '3층 공사 현장',
            time: '5분 전'
        },
        {
            id: 2,
            type: 'warning',
            title: '보호구 미착용',
            description: '2층 작업 현장',
            time: '10분 전'
        },
        {
            id: 3,
            type: 'warning',
            title: '피로도 위험',
            description: '건설 작업 영역',
            time: '12분 전'
        }
    ]);

    // 주요 안전 지표 데이터
    const [indicators] = useState([
        {
            id: 1,
            type: 'warning',
            title: '보호구 미착용 작업자 수',
            value: '2건',
            icon: '⚠️'
        },
        {
            id: 2,
            type: 'warning',
            title: '작업 안전 경고',
            value: '1건',
            icon: '⚠️'
        },
        {
            id: 3,
            type: 'danger',
            title: '직원 이상 교원 수',
            value: '3명',
            icon: '😷'
        }
    ]);

    // 현장 현황 데이터
    const [fieldStatus] = useState({
        currentWorkers: 24,
        totalCapacity: 18,
        dangerWorkers: 4,
        normalWorkers: 2
    });

    // 유휴 인력 현황
    const [idleStatus] = useState({
        count: 3,
        duration: '15분'
    });

    // 긴급 이상 탐지
    const [emergencyStatus] = useState({
        count: 1,
        location: '박영희 - 심박수 이상'
    });

    // 도넛 차트 계산
    const circumference = 2 * Math.PI * 90; // 반지름 90
    const strokeDasharray = circumference;
    const strokeDashoffset = circumference - (safetyScore / 100) * circumference;

    // 컴포넌트 마운트 시 데이터 로드
    useEffect(() => {
        // API 호출 로직
        // fetchDashboardData();
    }, []);

    return (
        <div className={styles.page}>
            {/* 페이지 헤더 */}
            <header className={styles.pageHeader}>
                <h1 className={styles.pageTitle}>대시보드</h1>
            </header>

            {/* 상단 섹션 - 종합 안전 점수 + 변동 추이 */}
            <section className={styles.topSection}>
                {/* 종합 안전 점수 */}
                <div className={styles.safetyScoreCard}>
                    <h2 className={styles.safetyScoreTitle}>종합 안전 점수</h2>

                    <div className={styles.chartContainer}>
                        <svg className={styles.donutChart} viewBox="0 0 200 200">
                            {/* 배경 원 */}
                            <circle
                                className={styles.chartBackground}
                                cx="100"
                                cy="100"
                                r="90"
                            />
                            {/* 진행률 원 */}
                            <circle
                                className={styles.chartProgress}
                                cx="100"
                                cy="100"
                                r="90"
                                strokeDasharray={strokeDasharray}
                                strokeDashoffset={strokeDashoffset}
                            />
                        </svg>

                        <div className={styles.chartText}>
                            <p className={styles.chartScore}>{safetyScore}점</p>
                            <p className={styles.chartLabel}>안전 점수</p>
                        </div>
                    </div>

                    <button className={styles.safetyStatusBtn}>
                        양호
                    </button>
                </div>

                {/* 안전 점수 변동 추이 */}
                <div className={styles.trendCard}>
                    <h2 className={styles.trendTitle}>안전 점수 변동 추이</h2>

                    <div className={styles.trendCharts}>
                        <div className={styles.trendChartItem}>
                            <p className={styles.trendChartLabel}>일별 그래프</p>
                            <div className={styles.trendChartPlaceholder}>
                                📈 일별 차트
                            </div>
                        </div>

                        <div className={styles.trendChartItem}>
                            <p className={styles.trendChartLabel}>주별 그래프</p>
                            <div className={styles.trendChartPlaceholder}>
                                📊 주별 차트
                            </div>
                        </div>

                        <div className={styles.trendChartItem}>
                            <p className={styles.trendChartLabel}>월별 그래프</p>
                            <div className={styles.trendChartPlaceholder}>
                                📉 월별 차트
                            </div>
                        </div>
                    </div>
                </div>
            </section>

            {/* 하단 위젯 섹션 */}
            <section className={styles.widgetsSection}>
                {/* 실시간 위험 알림 */}
                <div className={`${styles.widgetCard} ${styles.alertWidget}`}>
                    <h3 className={styles.widgetTitle}>실시간 위험 알림</h3>

                    <div className={styles.alertList}>
                        {alerts.map(alert => (
                            <div key={alert.id} className={`${styles.alertItem} ${styles[alert.type]}`}>
                                <div className={`${styles.alertIcon} ${styles[alert.type]}`}>
                                    {getAlertIcon(alert.type)}
                                </div>
                                <div className={styles.alertContent}>
                                    <p className={styles.alertTitle}>{alert.title}</p>
                                    <p className={styles.alertDesc}>{alert.description}</p>
                                </div>
                                <span className={styles.alertTime}>{alert.time}</span>
                            </div>
                        ))}
                    </div>
                </div>

                {/* 주요 안전 지표 */}
                <div className={`${styles.widgetCard} ${styles.indicatorWidget}`}>
                    <h3 className={styles.widgetTitle}>주요 안전 지표</h3>

                    <div className={styles.indicatorList}>
                        {indicators.map(indicator => (
                            <div key={indicator.id} className={styles.indicatorItem}>
                                <div className={`${styles.indicatorIcon} ${styles[indicator.type]}`}>
                                    {indicator.icon}
                                </div>
                                <div className={styles.indicatorContent}>
                                    <p className={styles.indicatorTitle}>{indicator.title}</p>
                                    <p className={styles.indicatorValue}>{indicator.value}</p>
                                </div>
                            </div>
                        ))}
                    </div>
                </div>

                {/* 실시간 현장 현황 */}
                <div className={`${styles.widgetCard} ${styles.statusWidget}`}>
                    <h3 className={styles.widgetTitle}>실시간 현장 현황</h3>

                    <div className={styles.statusSummary}>
                        <div className={styles.statusIcon}>👨‍💼</div>
                        <div className={styles.statusText}>
                            <p className={styles.statusLabel}>현재 인원</p>
                            <p className={styles.statusValue}>{fieldStatus.currentWorkers}명</p>
                        </div>
                    </div>

                    <p className={styles.statusDetails}>
                        건설: {fieldStatus.totalCapacity}명 | 안전: {fieldStatus.dangerWorkers}명 | 관리: {fieldStatus.normalWorkers}명
                    </p>

                    <button className={styles.statusBtn}>
                        정상 운영
                    </button>
                </div>

                {/* 유휴 인력 현황 */}
                <div className={`${styles.widgetCard} ${styles.idleWidget}`}>
                    <h3 className={styles.widgetTitle}>유휴 인력 현황</h3>

                    <StatusDisplay
                        icon="😴"
                        label="유휴 상태"
                        value={`${idleStatus.count}명`}
                        details={`평균 유휴 시간: ${idleStatus.duration}`}
                        classPrefix="idle"
                    />
                </div>

                {/* 긴급 이상 탐지 */}
                <div className={`${styles.widgetCard} ${styles.emergencyWidget}`}>
                    <h3 className={styles.widgetTitle}>긴급 이상 탐지</h3>

                    <StatusDisplay
                        icon="🏥"
                        label="이상 징후"
                        value={`${emergencyStatus.count}명`}
                        details={emergencyStatus.location}
                        classPrefix="emergency"
                    />
                </div>
            </section>
        </div>
    );
};

export default DashboardPage;