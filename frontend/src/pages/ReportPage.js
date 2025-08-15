import React, { useState, useEffect, useCallback } from 'react';
import styles from '../styles/Report.module.css';
// import { reportAPI } from '../api/api'; // API 연동 시 사용

const ReportPage = () => {
    // 새 리포트 생성 폼 데이터
    const [reportForm, setReportForm] = useState({
        startDate: '',
        endDate: '',
        reportType: 'safety'
    });

    // 현재 페이지 상태
    const [currentPage, setCurrentPage] = useState(0);
    const [pageSize] = useState(5);
    const [totalPages, setTotalPages] = useState(2);

    // 리포트 목록 데이터
    const [reports, setReports] = useState([
        {
            id: 1,
            title: '6월 보호구 착용 분석',
            type: '보호구 착용 분석',
            period: '2025.06.01~06.30',
            createdDate: '2025.07.01',
            status: 'completed'
        },
        {
            id: 2,
            title: '6월 위험구역 분석',
            type: '위험구역 분석',
            period: '2025.06.01~06.30',
            createdDate: '2025.07.01',
            status: 'completed'
        },
        {
            id: 3,
            title: '6월 주간 안전 현황',
            type: '종합 안전 현황',
            period: '2025.06.01~06.08',
            createdDate: '2025.06.09',
            status: 'processing'
        },
        {
            id: 4,
            title: '6월 주간 안전 현황',
            type: '종합 안전 현황',
            period: '2025.06.01~06.08',
            createdDate: '2025.06.09',
            status: 'processing'
        },
        {
            id: 5,
            title: '6월 주간 안전 현황',
            type: '종합 안전 현황',
            period: '2025.06.01~06.08',
            createdDate: '2025.06.09',
            status: 'processing'
        }
    ]);

    // AI 추천사항 데이터
    const [recommendations] = useState([
        {
            id: 1,
            icon: '⚠️',
            title: '위험구역 접근 감소 방안',
            description: '3구역 크레인 작업 시 주의 안전 클래스 설치로 경고합니다.'
        },
        {
            id: 2,
            icon: '🔷',
            title: '보호구 착용 개선',
            description: '오전 시간대 보호구 미착용 발견률이 높습니다. 안전 장갑 감지기 운용 개선.'
        },
        {
            id: 3,
            icon: '📊',
            title: '근로자 건강 관리',
            description: '고온 작업 시간대 휴식 시간을 10분 증가시킵니다. 건강 관리 보완.'
        }
    ]);

    // 미리보기 통계 데이터
    const [previewStats] = useState({
        safetyScore: 87,
        riskEvents: 12,
        complianceRate: 98
    });

    // 리포트 목록 조회
    const fetchReports = useCallback(async () => {
        try {
            // API 호출 로직
            // const response = await reportAPI.getReports({
            //     page: currentPage,
            //     size: pageSize
            // });
            // setReports(response.data.content || []);
            // setTotalPages(response.data.totalPages || 1);

            console.log('리포트 목록 조회 - 페이지:', currentPage);
        } catch (error) {
            console.error('리포트 데이터 조회 실패:', error);
        }
    }, [currentPage, pageSize]);

    // 컴포넌트 마운트 시 데이터 로드
    useEffect(() => {
        fetchReports();
    }, [fetchReports]);

    // 폼 입력 핸들러
    const handleFormChange = (field, value) => {
        setReportForm(prev => ({
            ...prev,
            [field]: value
        }));
    };

    // 리포트 생성
    const handleGenerateReport = async () => {
        if (!reportForm.startDate || !reportForm.endDate) {
            alert('보고서 기간을 선택해주세요.');
            return;
        }

        try {
            // API 호출 로직
            // const reportData = {
            //     startDate: reportForm.startDate,
            //     endDate: reportForm.endDate,
            //     type: reportForm.reportType
            // };
            // await reportAPI.generateReport(reportData);

            alert('리포트 생성이 시작되었습니다.');

            // 새로운 리포트를 목록에 추가 (임시)
            const newReport = {
                id: Date.now(),
                title: `${getReportTypeLabel(reportForm.reportType)} 분석`,
                type: getReportTypeLabel(reportForm.reportType),
                period: `${reportForm.startDate}~${reportForm.endDate}`,
                createdDate: new Date().toISOString().split('T')[0].replace(/-/g, '.'),
                status: 'processing'
            };

            setReports(prev => [newReport, ...prev]);

            // 폼 초기화
            setReportForm({
                startDate: '',
                endDate: '',
                reportType: 'safety'
            });

        } catch (error) {
            console.error('리포트 생성 실패:', error);
            alert('리포트 생성에 실패했습니다.');
        }
    };

    // 리포트 타입 라벨 변환
    const getReportTypeLabel = (type) => {
        switch (type) {
            case 'safety': return '종합 안전 현황';
            case 'equipment': return '보호구 착용 분석';
            case 'risk': return '위험구역 분석';
            default: return '안전 분석';
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

    // PDF 다운로드
    const handlePdfDownload = () => {
        alert('PDF 다운로드가 시작됩니다.');
    };

    // 엑셀 내보내기
    const handleExcelExport = () => {
        alert('엑셀 내보내기가 시작됩니다.');
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

    // 더 많은 추천사항 보기
    const handleMoreRecommendations = () => {
        alert('AI 추천사항 상세 페이지로 이동합니다.');
    };

    // 현재 페이지 데이터
    const currentReports = reports.slice(
        currentPage * pageSize,
        (currentPage + 1) * pageSize
    );

    return (
        <div className={styles.page}>
            {/* 페이지 헤더 */}
            <header className={styles.pageHeader}>
                <h1 className={styles.pageTitle}>안전 보고서 생성 및 관리</h1>
            </header>

            {/* 상단 섹션 - 새 리포트 생성 + 미리보기 */}
            <section className={styles.topSection}>
                {/* 좌측: 새 리포트 생성 */}
                <div className={styles.reportCreateSection}>
                    <h2 className={styles.sectionTitle}>새 리포트 생성</h2>

                    <div className={styles.formGroup}>
                        <label className={styles.formLabel}>보고서 기간</label>
                        <input
                            type="date"
                            className={styles.formInput}
                            value={reportForm.startDate}
                            onChange={(e) => handleFormChange('startDate', e.target.value)}
                        />
                    </div>

                    <div className={styles.formGroup}>
                        <label className={styles.formLabel}>리포트 타입</label>
                        <select
                            className={styles.formSelect}
                            value={reportForm.reportType}
                            onChange={(e) => handleFormChange('reportType', e.target.value)}
                        >
                            <option value="safety">종합 안전 현황</option>
                            <option value="equipment">보호구 착용 분석</option>
                            <option value="risk">위험구역 분석</option>
                        </select>
                    </div>

                    <button
                        className={styles.generateButton}
                        onClick={handleGenerateReport}
                        disabled={!reportForm.startDate || !reportForm.endDate}
                    >
                        리포트 생성
                    </button>
                </div>

                {/* 우측: 리포트 미리보기 */}
                <div className={styles.previewSection}>
                    <div className={styles.previewHeader}>
                        <h2 className={styles.previewTitle}>리포트 미리보기</h2>
                    </div>

                    <div className={styles.chartsContainer}>
                        <div className={styles.chartItem}>
                            <p className={styles.chartTitle}>안전 점수 추이</p>
                            <div className={styles.chartContainer}>
                                <div className={styles.chartPlaceholder}>📊 안전 점수 차트</div>
                                <div className={styles.chartStats}>
                                    <p className={`${styles.chartValue} ${styles.safe}`}>{previewStats.safetyScore}점</p>
                                    <p className={styles.chartLabel}>평균 안전 점수</p>
                                </div>
                            </div>
                        </div>

                        <div className={styles.chartItem}>
                            <p className={styles.chartTitle}>위험 시간 발생 현황</p>
                            <div className={styles.chartContainer}>
                                <div className={styles.chartPlaceholder}>📈 위험 발생 차트</div>
                                <div className={styles.chartStats}>
                                    <p className={`${styles.chartValue} ${styles.danger}`}>{previewStats.riskEvents}건</p>
                                    <p className={styles.chartLabel}>위험 시간</p>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div className={styles.previewActions}>
                        <button className={`${styles.previewButton} ${styles.pdfButton}`} onClick={handlePdfDownload}>
                            PDF 다운로드
                        </button>
                        <button className={`${styles.previewButton} ${styles.exportButton}`} onClick={handleExcelExport}>
                            엑셀 내보내기
                        </button>
                    </div>
                </div>
            </section>

            {/* AI 개선사항 추천 섹션 */}
            <section className={styles.aiRecommendationSection}>
                <div className={styles.aiHeader}>
                    <h2 className={styles.aiTitle}>AI 개선사항 추천</h2>
                    <button className={styles.moreRecommendationsButton} onClick={handleMoreRecommendations}>
                        더 많은 추천사항 보기
                    </button>
                </div>

                <div className={styles.aiRecommendations}>
                    {recommendations.map(recommendation => (
                        <div key={recommendation.id} className={styles.recommendationCard}>
                            <div className={styles.recommendationHeader}>
                                <div className={styles.recommendationIcon}>
                                    {recommendation.icon}
                                </div>
                                <h3 className={styles.recommendationTitle}>
                                    {recommendation.title}
                                </h3>
                            </div>
                            <p className={styles.recommendationDesc}>
                                {recommendation.description}
                            </p>
                        </div>
                    ))}
                </div>
            </section>

            {/* 하단: 리포트 목록 테이블 */}
            <section className={styles.tableSection}>
                <h2 className={styles.tableTitle}>리포트 목록</h2>

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
                                생성된 리포트가 없습니다.
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
                            onClick={() => setCurrentPage(prev => Math.max(prev - 1, 0))}
                            disabled={currentPage === 0}
                        >
                            ‹
                        </button>

                        {Array.from({ length: totalPages }, (_, index) => (
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
                            onClick={() => setCurrentPage(prev => Math.min(prev + 1, totalPages - 1))}
                            disabled={currentPage >= totalPages - 1}
                        >
                            ›
                        </button>

                        <span style={{ margin: '0 16px', color: '#6B7280', fontSize: '14px' }}>...</span>

                        <button className={styles.pageBtn}>
                            40
                        </button>

                        <button
                            className={styles.pageBtn}
                            onClick={() => setCurrentPage(totalPages - 1)}
                        >
                            ››
                        </button>
                    </div>
                )}
            </section>
        </div>
    );
};

export default ReportPage;