import React, { useState, useEffect } from 'react';
import styles from '../styles/Report.module.css';
import { reportAPI } from '../api/api';
import SuccessModal from '../components/SuccessModal';
import ConfirmModal from '../components/ConfirmModal';

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

    // 리포트 데이터 상태
    const [reports, setReports] = useState([]);
    const [loading, setLoading] = useState(false);
    
    // 모달 상태
    const [successModal, setSuccessModal] = useState({
        isOpen: false,
        title: '',
        message: ''
    });
    
    const [confirmModal, setConfirmModal] = useState({
        isOpen: false,
        targetId: null
    });

    // 각 기간별 설명
    const INTERVAL_DESCRIPTIONS = {
        day: '어제 하루 데이터 분석',
        week: '최근 1주일 데이터 분석',
        month: '최근 1개월 데이터 분석'
    };

    // 에러 컨트롤 초기화
    const clearError = () => {
        setError('');
    };

    // 리포트 목록 조회
    const fetchReports = async () => {
        setLoading(true);
        try {
            const response = await reportAPI.getReports({
                page: currentPage,
                size: pageSize,
                sortBy: 'createdAt',
                sortDir: 'desc'
            });
            
            setReports(response.data.content || []);
            setTotalPages(response.data.totalPages || 1);
        } catch (error) {
            console.error('리포트 목록 조회 실패:', error);
            setError('리포트 목록을 불러오는데 실패했습니다.');
        } finally {
            setLoading(false);
        }
    };

    // 컴포넌트 마운트 시 및 페이지 변경 시 리포트 목록 조회
    useEffect(() => {
        fetchReports();
    }, [currentPage]);

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
            
            // 리포트 목록 새로고침
            await fetchReports();
            
            showSuccessModal('다운로드 시작', '리포트 다운로드가 시작되었습니다.');
            
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
            
            // 리포트 목록 새로고침
            await fetchReports();
            
            showSuccessModal('다운로드 시작', '개선안 리포트 다운로드가 시작되었습니다.');
            
        } catch (error) {
            console.error('개선안 리포트 생성 실패:', error);
            setError('개선안 리포트 생성에 실패했습니다: ' + error.message);
        } finally {
            setLoadingStates(prev => ({...prev, improvementReport: false}));
        }
    };

    // 리포트 타입 라벨 변환
    const getReportTypeLabel = (reportType) => {
        switch (reportType) {
            case 'DAILY_REPORT': return '일일 리포트';
            case 'IMPROVEMENT_REPORT': return 'AI 개선안 리포트';
            default: return '기타';
        }
    };

    // 날짜 포맷팅
    const formatDate = (dateString) => {
        if (!dateString) return '-';
        return new Date(dateString).toLocaleDateString('ko-KR');
    };


    // 리포트 다운로드
    const handleDownload = async (reportId, reportName) => {
        try {
            const blob = await reportAPI.downloadStoredReport(reportId);
            reportAPI.downloadFile(blob, `${reportName}.pdf`);
        } catch (error) {
            console.error('리포트 다운로드 실패:', error);
            setError('리포트 다운로드에 실패했습니다.');
        }
    };

    // 리포트 삭제 확인 모달 열기
    const handleDelete = (reportId) => {
        setConfirmModal({
            isOpen: true,
            targetId: reportId
        });
    };
    
    // 리포트 삭제 실행
    const handleConfirmDelete = async () => {
        if (!confirmModal.targetId) return;
        
        try {
            await reportAPI.deleteReport(confirmModal.targetId);
            await fetchReports(); // 목록 새로고침
            showSuccessModal('삭제 완료', '리포트가 삭제되었습니다.');
        } catch (error) {
            console.error('리포트 삭제 실패:', error);
            setError('리포트 삭제에 실패했습니다.');
        } finally {
            handleCloseConfirmModal();
        }
    };
    
    // 모달 관련 함수들
    const showSuccessModal = (title, message) => {
        setSuccessModal({
            isOpen: true,
            title: title,
            message: message
        });
    };
    
    const handleCloseSuccessModal = () => {
        setSuccessModal({
            isOpen: false,
            title: '',
            message: ''
        });
    };
    
    const handleCloseConfirmModal = () => {
        setConfirmModal({
            isOpen: false,
            targetId: null
        });
    };


    // 페이지 변경 핸들러
    const handlePageChange = (newPage) => {
        setCurrentPage(newPage);
    };

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
                    {loading ? (
                        <tr>
                            <td colSpan="6" className={styles.emptyState}>
                                로딩 중...
                            </td>
                        </tr>
                    ) : reports.length > 0 ? (
                        reports.map(report => (
                            <tr key={report.id}>
                                <td data-label="리포트명">{report.reportName}</td>
                                <td data-label="타입">{getReportTypeLabel(report.reportType)}</td>
                                <td data-label="기간">{report.period}</td>
                                <td data-label="생성일">{formatDate(report.createdAt)}</td>
                                <td data-label="상태">
                                    <span className={`${styles.statusBadge} ${styles.completed}`}>
                                        완료
                                    </span>
                                </td>
                                <td data-label="액션">
                                    <div className={styles.actionButtons}>
                                        <button
                                            className={`${styles.actionButton} ${styles.downloadButton}`}
                                            onClick={() => handleDownload(report.id, report.reportName)}
                                        >
                                            다운로드
                                        </button>
                                        <button
                                            className={`${styles.actionButton} ${styles.shareButton}`}
                                            onClick={() => handleDelete(report.id)}
                                        >
                                            삭제
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
                {totalPages > 1 && (
                    <div className={styles.pagination}>
                        <button
                            className={styles.pageBtn}
                            onClick={() => handlePageChange(Math.max(currentPage - 1, 0))}
                            disabled={currentPage === 0}
                        >
                            ‹
                        </button>

                        {Array.from({ length: totalPages }, (_, index) => (
                            <button
                                key={index}
                                className={`${styles.pageBtn} ${currentPage === index ? styles.active : ''}`}
                                onClick={() => handlePageChange(index)}
                            >
                                {index + 1}
                            </button>
                        ))}

                        <button
                            className={styles.pageBtn}
                            onClick={() => handlePageChange(Math.min(currentPage + 1, totalPages - 1))}
                            disabled={currentPage >= totalPages - 1}
                        >
                            ›
                        </button>
                    </div>
                )}
            </section>
            
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
                title="리포트 삭제 확인"
                message="을 정말 삭제하시겠습니까?"
                targetName="이 리포트"
                onConfirm={handleConfirmDelete}
                onCancel={handleCloseConfirmModal}
                confirmButtonText="삭제하기"
                type="danger"
            />
        </div>
    );
};

export default ReportPage;