import React from 'react';
import { useNavigate } from 'react-router-dom';
import styles from '../styles/PrivacyPolicy.module.css';
import Footer from '../components/Footer';

const PrivacyPolicy = () => {
    const navigate = useNavigate();

    const handleBack = () => {
        navigate(-1); // 이전 페이지로 돌아가기
    };

    return (
        <div className={styles.page}>
            {/* 상단 바 */}
            <header className={styles.topBar}>
                <span className={styles.circle}></span>
                <span className={styles.logoText}>이룸</span>
            </header>

            {/* 본문 */}
            <div className={styles.mainContent}>
                <div className={styles.header}>
                    <h1 className={styles.title}>개인정보 처리방침</h1>
                    <p className={styles.subtitle}>
                        이룸 건설 현장 안전 모니터링 시스템(i-Room)의 개인정보 처리방침입니다.
                    </p>
                </div>

                <div className={styles.contentWrapper}>
                    {/* 시행일 */}
                    <div className={styles.effectiveDate}>
                        <strong>시행일: 2025년 1월 1일</strong>
                    </div>

                    {/* 1. 개인정보 처리 목적 */}
                    <section className={styles.section}>
                        <h2 className={styles.sectionTitle}>1. 개인정보의 처리 목적</h2>
                        <div className={styles.sectionContent}>
                            <p>이룸은 다음의 목적을 위하여 개인정보를 처리합니다.</p>
                            <ul>
                                <li>
                                    <strong>회원가입 및 관리</strong>
                                    <p>회원제 서비스 제공에 따른 본인 식별·인증, 회원자격 유지·관리, 서비스 부정이용 방지, 각종 고지·통지 등</p>
                                </li>
                                <li>
                                    <strong>서비스 제공</strong>
                                    <p>건설 현장 안전 모니터링 서비스 제공, 본인인증,  등</p>
                                </li>
                                <li>
                                    <strong>고충처리</strong>
                                    <p>신원 확인, 민원사항 확인, 사실조사를 위한 연락·통지, 처리결과 통보 등</p>
                                </li>
                            </ul>
                        </div>
                    </section>

                    {/* 2. 개인정보 수집 항목 */}
                    <section className={styles.section}>
                        <h2 className={styles.sectionTitle}>2. 수집하는 개인정보 항목</h2>
                        <div className={styles.sectionContent}>
                            <h3>관리자 회원가입 및 등록 시</h3>
                            <ul>
                                <li><strong>필수항목:</strong> 이름, 이메일, 비밀번호, 휴대폰번호, 부서/직책, 혈액형, 나이</li>
                            </ul>

                            <h3>서비스 이용 시</h3>
                            <ul>
                                <li>IP주소, 쿠키, 서비스 이용기록 등</li>
                            </ul>
                        </div>
                    </section>

                    {/* 3. 개인정보 보유 및 이용기간 */}
                    <section className={styles.section}>
                        <h2 className={styles.sectionTitle}>3. 개인정보의 보유 및 이용기간</h2>
                        <div className={styles.sectionContent}>
                            <p>회사는 법령에 따른 개인정보 보유·이용기간 또는 정보주체로부터 개인정보를 수집 시에 동의받은 개인정보 보유·이용기간 내에서 개인정보를 처리·보유합니다.</p>
                            <ul>
                                <li>
                                    <strong>회원정보:</strong> 회원 탈퇴 시까지
                                </li>
                                <li>
                                    <strong>전자상거래 관련 기록</strong>
                                    <ul>
                                        <li>계약 또는 청약철회 등에 관한 기록: 5년</li>
                                        <li>대금결제 및 재화 등의 공급에 관한 기록: 5년</li>
                                        <li>소비자의 불만 또는 분쟁처리에 관한 기록: 3년</li>
                                    </ul>
                                </li>
                                <li>
                                    <strong>통신비밀보호법 관련</strong>
                                    <ul>
                                        <li>웹사이트 방문기록: 3개월</li>
                                    </ul>
                                </li>
                            </ul>
                        </div>
                    </section>


                    {/* 4. 정보주체의 권리·의무 */}
                    <section className={styles.section}>
                        <h2 className={styles.sectionTitle}>6. 정보주체의 권리·의무 및 행사방법</h2>
                        <div className={styles.sectionContent}>
                            <p>정보주체는 다음과 같은 권리를 행사할 수 있습니다.</p>
                            <ul>
                                <li>개인정보 열람요구</li>
                                <li>오류 등이 있을 경우 정정 요구</li>
                                <li>삭제요구</li>
                                <li>처리정지 요구</li>
                            </ul>
                            <p>권리 행사는 개인정보보호법 시행령 제41조제1항에 따라 서면, 전자우편, 모사전송(FAX) 등을 통하여 하실 수 있으며, 회사는 이에 대해 지체없이 조치하겠습니다.</p>
                        </div>
                    </section>

                    {/* 5. 개인정보의 파기 */}
                    <section className={styles.section}>
                        <h2 className={styles.sectionTitle}>7. 개인정보의 파기</h2>
                        <div className={styles.sectionContent}>
                            <p>회사는 개인정보 보유기간의 경과, 처리목적 달성 등 개인정보가 불필요하게 되었을 때에는 지체없이 해당 개인정보를 파기합니다.</p>
                            <h3>파기절차</h3>
                            <ul>
                                <li>이용자가 입력한 정보는 목적 달성 후 별도의 DB에 옮겨져 내부 방침 및 기타 관련 법령에 따라 일정기간 저장된 후 파기됩니다.</li>
                            </ul>
                            <h3>파기방법</h3>
                            <ul>
                                <li>전자적 파일 형태의 정보는 기록을 재생할 수 없는 기술적 방법을 사용합니다.</li>
                                <li>종이에 출력된 개인정보는 분쇄기로 분쇄하거나 소각을 통하여 파기합니다.</li>
                            </ul>
                        </div>
                    </section>

                    {/* 6. 개인정보 보호책임자 */}
                    <section className={styles.section}>
                        <h2 className={styles.sectionTitle}>8. 개인정보 보호책임자</h2>
                        <div className={styles.sectionContent}>
                            <p>회사는 개인정보 처리에 관한 업무를 총괄해서 책임지고, 개인정보 처리와 관련한 정보주체의 불만처리 및 피해구제 등을 위하여 아래와 같이 개인정보 보호책임자를 지정하고 있습니다.</p>
                            <div className={styles.contactBox}>
                                <h3>개인정보 보호책임자</h3>
                                <ul>
                                    <li><strong>성명:</strong> 김철환</li>
                                    <li><strong>직책:</strong> 정보보호팀장</li>
                                    <li><strong>연락처:</strong> 02-1234-5678</li>
                                    <li><strong>이메일:</strong> privacy@iroom.com</li>
                                </ul>
                            </div>
                        </div>
                    </section>

                    {/* 7. 개인정보의 안전성 확보조치 */}
                    <section className={styles.section}>
                        <h2 className={styles.sectionTitle}>10. 개인정보의 안전성 확보조치</h2>
                        <div className={styles.sectionContent}>
                            <p>회사는 개인정보의 안전성 확보를 위해 다음과 같은 조치를 취하고 있습니다.</p>
                            <ul>
                                <li>개인정보 취급 직원의 최소화 및 교육</li>
                                <li>내부관리계획의 수립 및 시행</li>
                                <li>개인정보의 암호화</li>
                                <li>해킹 등에 대비한 기술적 대책</li>
                                <li>개인정보에 대한 접근 제한</li>
                                <li>접속기록의 보관 및 위변조 방지</li>
                                <li>문서보안을 위한 잠금장치 사용</li>
                            </ul>
                        </div>
                    </section>

                    {/* 버튼 */}
                    <div className={styles.buttonContainer}>
                        <button
                            type="button"
                            className={styles.backButton}
                            onClick={handleBack}
                        >
                            돌아가기
                        </button>
                    </div>
                </div>
            </div>

            <Footer />
        </div>
    );
};

export default PrivacyPolicy;