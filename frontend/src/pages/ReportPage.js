import React, { useState } from 'react';
import styles from '../styles/Report.module.css';
import { reportAPI } from '../api/api';

const ReportPage = () => {
    // 일일 리포트 폼 데이터
    const [dailyReportForm, setDailyReportForm] = useState({
        date: ''
    });

    // 개선안 리포트 폼 데이터
    const [improvementReportForm, setImprovementReportForm] = useState({
        interval: 'week'
    });

    // 로딩 상태
    const [loadingStates, setLoadingStates] = useState({
        dailyReport: false,
        improvementReport: false
    });

    // 에러 상태
    const [error, setError] = useState('');

    // 현재 페이지 상태
    const [currentPage, setCurrentPage] = useState(0);
    const [pageSize] = useState(5);
    const [totalPages, setTotalPages] = useState(1);

    // 리포트 생성 이력 데이터
    const [reportHistory, setReportHistory] = useState([]);

    // 각 기간별 설명
    const INTERVAL_DESCRIPTIONS = {
        day: '어제 하루 데이터 분석',
        week: '최근 1주일 데이터 분석',
        month: '최근 1개월 데이터 분석'
    };

    // 에러 컨럤 초기화
    const clearError = () => {
        setError('');
    };

    // 오늘 날짜 가져오기
    const getTodayDate = () => {
        const today = new Date();
        return today.toISOString().split('T')[0];
    };

    // 일일 리포트 폼 입력 핸들러
    const handleDailyFormChange = (field, value) => {
        setDailyReportForm(prev => ({
            ...prev,
            [field]: value
        }));
        clearError();
    };

    // 개선안 리포트 폼 입력 핸들러
    const handleImprovementFormChange = (field, value) => {
        setImprovementReportForm(prev => ({
            ...prev,
            [field]: value
        }));
        clearError();
    };

    // 일일 리포트 생성
    const handleGenerateDailyReport = async () => {
        if (!dailyReportForm.date) {
            setError('날짜를 선택해주세요.');
            return;
        }

        setLoadingStates(prev => ({...prev, dailyReport: true}));
        clearError();

        try {
            const blob = await reportAPI.generateDailyReport(dailyReportForm.date);
            reportAPI.downloadFile(blob, `daily_report_${dailyReportForm.date}.pdf`);
            
            // 생성 이력에 추가
            const newReport = {
                id: Date.now(),
                title: `일일 안전 리포트`,
                type: '일일 리포트',
                period: dailyReportForm.date,
                createdDate: new Date().toISOString().split('T')[0],
                status: 'completed'
            };
            setReportHistory(prev => [newReport, ...prev]);
            
            alert('리포트 다운로드가 시작되었습니다.');
            
        } catch (error) {
            console.error('일일 리포트 생성 실패:', error);
            setError('리포트 생성에 실패했습니다: ' + error.message);
        } finally {
            setLoadingStates(prev => ({...prev, dailyReport: false}));
        }
    };

    // 개선안 리포트 생성
    const handleGenerateImprovementReport = async () => {
        setLoadingStates(prev => ({...prev, improvementReport: true}));
        clearError();

        try {
            const blob = await reportAPI.generateImprovementReport(improvementReportForm.interval);
            const intervalLabels = { day: '일간', week: '주간', month: '월간' };
            reportAPI.downloadFile(blob, `${improvementReportForm.interval}_improvement_report.pdf`);
            
            // 생성 이력에 추가
            const newReport = {
                id: Date.now(),
                title: `${intervalLabels[improvementReportForm.interval]} AI 개선안 리포트`,
                type: 'AI 개선안 리포트',
                period: `${intervalLabels[improvementReportForm.interval]} 분석`,
                createdDate: new Date().toISOString().split('T')[0],
                status: 'completed'
            };
            setReportHistory(prev => [newReport, ...prev]);
            
            alert('개선안 리포트 다운로드가 시작되었습니다.');
            
        } catch (error) {
            console.error('개선안 리포트 생성 실패:', error);
            setError('개선안 리포트 생성에 실패했습니다: ' + error.message);
        } finally {
            setLoadingStates(prev => ({...prev, improvementReport: false}));
        }
    };

    // 간격 라벨 변환
    const getIntervalLabel = (interval) => {
        switch (interval) {
            case 'day': return '일간';
            case 'week': return '주간';
            case 'month': return '월간';
            default: return '기간';
        }
    };

    // 상태 라벨 변환
    const getStatusLabel = (status) => {
        switch (status) {
            case 'completed': return '완료';
            case 'processing': return '진행중';
            case 'pending': return '대기중';
            default: return '알 수 없음';
        }
    };


    // 리포트 다운로드
    const handleDownload = (reportId) => {
        console.log('리포트 다운로드:', reportId);
        alert('리포트 다운로드가 시작됩니다.');
    };

    // 리포트 공유
    const handleShare = (reportId) => {
        console.log('리포트 공유:', reportId);
        alert('공유 링크가 클립보드에 복사되었습니다.');
    };


    // 현재 페이지 데이터
    const currentReports = reportHistory.slice(
        currentPage * pageSize,
        (currentPage + 1) * pageSize
    );

    // 전체 페이지 수 계산
    const totalPagesCalculated = Math.ceil(reportHistory.length / pageSize) || 1;

    return (
        <div className={styles.page}>
            {/* 페이지 헤더 */}
            <header className={styles.pageHeader}>
                <h1 className={styles.pageTitle}>안전 보고서 생성 및 관리</h1>
            </header>

            {/* 에러 메시지 */}
            {error && (
                <div className={styles.errorMessage}>
                    <span>⚠️ {error}</span>
                    <button onClick={clearError} className={styles.closeError}>×</button>
                </div>
            )}

            {/* 상단 섹션 - 리포트 생성 카드들 */}
            <section className={styles.topSection}>
                {/* 좌측: 일일 리포트 생성 */}
                <div className={styles.reportCreateSection}>
                    <h2 className={styles.sectionTitle}>일일 안전 리포트 생성</h2>
                    <p className={styles.sectionDescription}>특정 날짜의 안전 현황을 종합하여 보고서를 생성합니다.</p>

                    <div className={styles.formGroup}>
                        <label className={styles.formLabel}>날짜 선택</label>
                        <input
                            type="date"
                            className={styles.formInput}
                            value={dailyReportForm.date}
                            max={getTodayDate()}
                            onChange={(e) => handleDailyFormChange('date', e.target.value)}
                        />
                    </div>

                    <button
                        className={`${styles.generateButton} ${styles.dailyButton}`}
                        onClick={handleGenerateDailyReport}
                        disabled={!dailyReportForm.date || loadingStates.dailyReport}
                    >
                        {loadingStates.dailyReport ? (
                            <>
                                <span className={styles.spinner}></span>
                                리포트 생성 중...
                            </>
                        ) : (
                            '일일 리포트 생성'
                        )}
                    </button>
                </div>

                {/* 우측: AI 개선안 리포트 생성 */}
                <div className={styles.improvementSection}>
                    <h2 className={styles.sectionTitle}>AI 개선안 리포트 생성</h2>
                    <p className={styles.sectionDescription}>AI가 안전 데이터를 분석하여 맞춤형 개선 방안을 제시합니다.</p>

                    <div className={styles.formGroup}>
                        <label className={styles.formLabel}>분석 기간</label>
                        <select
                            className={styles.formSelect}
                            value={improvementReportForm.interval}
                            onChange={(e) => handleImprovementFormChange('interval', e.target.value)}
                        >
                            <option value="day">일간 분석</option>
                            <option value="week">주간 분석</option>
                            <option value="month">월간 분석</option>
                        </select>
                        <p className={styles.intervalDescription}>
                            {INTERVAL_DESCRIPTIONS[improvementReportForm.interval]}
                        </p>
                    </div>

                    <button
                        className={`${styles.generateButton} ${styles.improvementButton}`}
                        onClick={handleGenerateImprovementReport}
                        disabled={loadingStates.improvementReport}
                    >
                        {loadingStates.improvementReport ? (
                            <>
                                <span className={styles.spinner}></span>
                                AI 분석 중... (약 30초 소요)
                            </>
                        ) : (
                            <>
                                🤖 AI 개선안 리포트 생성
                            </>
                        )}
                    </button>
                </div>
            </section>


            {/* 리포트 생성 이력 */}
            <section className={styles.tableSection}>
                <h2 className={styles.tableTitle}>리포트 생성 이력</h2>

                <table className={styles.dataTable}>
                    <thead>
                    <tr>
                        <th>리포트명</th>
                        <th>타입</th>
                        <th>기간</th>
                        <th>생성일</th>
                        <th>상태</th>
                        <th>액션</th>
                    </tr>
                    </thead>
                    <tbody>
                    {currentReports.length > 0 ? (
                        currentReports.map(report => (
                            <tr key={report.id}>
                                <td data-label="리포트명">{report.title}</td>
                                <td data-label="타입">{report.type}</td>
                                <td data-label="기간">{report.period}</td>
                                <td data-label="생성일">{report.createdDate}</td>
                                <td data-label="상태">
                                        <span className={`${styles.statusBadge} ${styles[report.status]}`}>
                                            {getStatusLabel(report.status)}
                                        </span>
                                </td>
                                <td data-label="액션">
                                    <div className={styles.actionButtons}>
                                        <button
                                            className={`${styles.actionButton} ${styles.downloadButton}`}
                                            onClick={() => handleDownload(report.id)}
                                            disabled={report.status !== 'completed'}
                                        >
                                            다운로드
                                        </button>
                                        <button
                                            className={`${styles.actionButton} ${styles.shareButton}`}
                                            onClick={() => handleShare(report.id)}
                                            disabled={report.status !== 'completed'}
                                        >
                                            공유
                                        </button>
                                    </div>
                                </td>
                            </tr>
                        ))
                    ) : (
                        <tr>
                            <td colSpan="6" className={styles.emptyState}>
                                아직 생성된 리포트가 없습니다.
                            </td>
                        </tr>
                    )}
                    </tbody>
                </table>

                {/* 페이지네이션 */}
                {totalPagesCalculated > 1 && (
                    <div className={styles.pagination}>
                        <button
                            className={styles.pageBtn}
                            onClick={() => setCurrentPage(prev => Math.max(prev - 1, 0))}
                            disabled={currentPage === 0}
                        >
                            ‹
                        </button>

                        {Array.from({ length: totalPagesCalculated }, (_, index) => (
                            <button
                                key={index}
                                className={`${styles.pageBtn} ${currentPage === index ? styles.active : ''}`}
                                onClick={() => setCurrentPage(index)}
                            >
                                {index + 1}
                            </button>
                        ))}

                        <button
                            className={styles.pageBtn}
                            onClick={() => setCurrentPage(prev => Math.min(prev + 1, totalPagesCalculated - 1))}
                            disabled={currentPage >= totalPagesCalculated - 1}
                        >
                            ›
                        </button>
                    </div>
                )}
            </section>
        </div>
    );
};

export default ReportPage;