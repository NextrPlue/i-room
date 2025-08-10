import React, { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { adminAPI } from '../api/api';
import styles from '../styles/PrivacyConsent.module.css';

const PrivacyConsentPage = () => {
    const navigate = useNavigate();
    const location = useLocation();
    const [agreed, setAgreed] = useState(false);
    const [isLoading, setIsLoading] = useState(false);

    // AdminSignUpPage에서 전달된 회원가입 데이터
    const signUpData = location.state?.signUpData;

    const handleCancel = () => {
        navigate('/admin/signUp');
    };

    const handleAgree = async () => {
        if (!agreed) return;

        // 회원가입 데이터가 없으면 회원가입 페이지로 돌아가기
        if (!signUpData) {
            alert('회원가입 정보가 없습니다. 다시 회원가입을 진행해주세요.');
            navigate('/admin/signup');
            return;
        }

        try {
            setIsLoading(true);

            // 회원가입 API 호출
            await adminAPI.signUp(signUpData);

            alert('회원가입이 완료되었습니다! 로그인 페이지로 이동합니다.');
            navigate('/admin/login');

        } catch (error) {
            console.error('회원가입 실패:', error);
            alert(error.message || '회원가입에 실패했습니다. 다시 시도해주세요.');
        } finally {
            setIsLoading(false);
        }
    };

    const handleCheckboxChange = (e) => {
        setAgreed(e.target.checked);
    };

    return (
        <div className={styles.page}>
            {/* 상단 바 */}
            <header className={styles.topBar}>
                <span className={styles.circle} aria-hidden="true" />
                <span className={styles.logoText}>이룸</span>
            </header>

            {/* 본문 */}
            <div className={styles.mainContent}>
                <h1 className={styles.title}>관리자 회원가입</h1>
                <hr className={styles.divider} />

                <div className={styles.agreementBox}>
                    <h2 className={styles.agreementTitle}>「개인정보 수집 및 이용 동의서」</h2>
                    <p className={styles.agreementSubtitle}>
                        회사는 서비스 제공을 위하여 다음과 같은 개인정보를 수집 및 이용합니다.
                    </p>

                    <div className={styles.agreementContent}>
                        <div className={styles.agreementSection}>
                            <h4>1. 수집 항목</h4>
                            <p>- 필수정보: 이름, 이메일 주소, 비밀번호, 직무</p>
                            <p>- 선택정보: 생년월일, 성별, 주소</p>
                        </div>

                        <div className={styles.agreementSection}>
                            <h4>2. 수집 및 이용 목적</h4>
                            <p>- 회원 식별 및 관리</p>
                            <p>- 서비스 이용에 따른 본인확인</p>
                            <p>- 고객문의 및 불만 처리</p>
                            <p>- 고지사항 전달</p>
                        </div>

                        <div className={styles.agreementSection}>
                            <h4>3. 보유 및 이용기간</h4>
                            <p>- 회원 탈퇴 시 즉시 파기</p>
                            <p>- 관련 법령에 따라 보존이 필요한 경우 해당 기간 동안 보관</p>
                        </div>

                        <div className={styles.agreementSection}>
                            <h4>4. 동의 거부 권리 및 불이익</h4>
                            <p>- 이용자는 개인정보 수집 및 이용에 대한 동의를 거부할 수 있습니다.</p>
                            <p>- 단, 필수 정보 제공을 거부할 경우 서비스 이용이 제한될 수 있습니다.</p>
                        </div>
                    </div>
                </div>

                <div className={styles.checkboxContainer}>
                    <input
                        type="checkbox"
                        id="agreement"
                        checked={agreed}
                        onChange={handleCheckboxChange}
                        className={styles.checkbox}
                    />
                    <label htmlFor="agreement" className={styles.checkboxLabel}>
                        개인정보 수집 및 이용에 동의합니다
                    </label>
                </div>

                <div className={styles.buttonRow}>
                    <button type="button" className={styles.grayBtn} onClick={handleCancel}>
                        취소
                    </button>
                    <button
                        type="button"
                        className={`${styles.blackBtn} ${!agreed || isLoading ? styles.disabled : ''}`}
                        onClick={handleAgree}
                        disabled={!agreed || isLoading}
                    >
                        {isLoading ? '회원가입 중...' : '회원가입'}
                    </button>
                </div>
            </div>
        </div>
    );
};

export default PrivacyConsentPage;
