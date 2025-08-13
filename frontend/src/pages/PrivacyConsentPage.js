import React, { useState, useMemo } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { userAPI } from '../api/api';
import styles from '../styles/PrivacyConsent.module.css';

/** 관리자 회원가입 동의서 설정 (관리자 전용) */
const PRIVACY_CONFIG = {
    companyName: '이룸',
    serviceName: '건설 현장 안전 모니터링 시스템 (i-Room)',

    // 관리자 회원가입용 수집항목
    adminFields: {
        required: ['이름', '이메일', '비밀번호', '휴대폰번호'],
        optional: ['부서/직책']
    },

    // 관리자 동의서 목적(요약)
    purposes: [
        '관리자 계정 생성 및 본인확인, 로그인 인증',
        '서비스 운영 및 고객지원',
        '접속 보안 및 시스템 유지보수'
    ],

    // 관리자 보유기간(요약)
    retentionPeriods: [
        '관리자 계정: 회원 탈퇴 시까지 (탈퇴 시 즉시 파기)',
        '관련 법령에 따른 별도 보존기간이 있는 경우 해당 기간 적용'
    ],

    // 법적 근거(요약)
    legalBasis: ['개인정보보호법 제15조, 제17조 (정보주체 동의)'],


    dpo: {
        name: '개인정보보호책임자 김○○',
        email: 'privacy@iroom.com',
        phone: '02-1234-5678'
    },

    policyPath: '/privacy-policy',
    tosPath: '/terms-of-service'
};

const PrivacyConsentPage = () => {
    const navigate = useNavigate();
    const location = useLocation();
    const [isLoading, setIsLoading] = useState(false);

    // 분리 동의(필수 2개)
    const [consent, setConsent] = useState({
        use: false,      // [필수] 수집·이용 동의
        tos: false       // [필수] 이용약관 동의
    });
    const requiredOk = consent.use && consent.tos;

    const signUpData = location.state?.signUpData;


    const legalList = useMemo(
        () => ({
            adminRequired: PRIVACY_CONFIG.adminFields.required.join(', '),
            adminOptional:
                PRIVACY_CONFIG.adminFields.optional.length > 0
                    ? PRIVACY_CONFIG.adminFields.optional.join(', ')
                    : '해당 없음'
        }),
        []
    );

    const handleCancel = () => navigate('/admin/signup');

    const handleAgree = async () => {
        if (!requiredOk) return;

        if (!signUpData) {
            alert('회원가입 정보가 없습니다. 다시 회원가입을 진행해주세요.');
            navigate('/admin/signup');
            return;
        }

        try {
            setIsLoading(true);
            await userAPI.signUp(signUpData);
            alert('회원가입이 완료되었습니다! 로그인 페이지로 이동합니다.');
            navigate('/admin/login');
        } catch (error) {
            console.error('회원가입 실패:', error);
            alert(error?.message || '회원가입에 실패했습니다. 다시 시도해주세요.');
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className={styles.page}>
            {/* 상단 바 */}
            <header className={styles.topBar}>
                <span className={styles.circle} aria-hidden="true" />
                <span className={styles.logoText}>{PRIVACY_CONFIG.companyName}</span>
            </header>

            {/* 본문 */}
            <div className={styles.mainContent}>
                <h1 className={styles.title}>관리자 회원가입</h1>
                <p style={{textAlign: 'center', margin: '4px 0 0 0', color: '#666'}}>
                    관리자 회원가입을 위해 아래 약관 및 개인정보 처리에 대한 동의가 필요합니다.
                </p>
                <hr className={styles.divider} />

                {/* 1. 개인정보 수집·이용 동의 */}
                <div className={styles.agreementBox}>
                    <h2 className={styles.agreementTitle}>1. 개인정보 수집·이용 동의 (필수)</h2>
                    <div className={styles.agreementContent}>
                        <div className={styles.agreementSection}>
                            <h4>수집 항목</h4>
                            <p>- <strong>필수정보</strong>: {legalList.adminRequired}</p>
                            <p>- <strong>선택정보</strong>: {legalList.adminOptional}</p>
                        </div>
                        
                        <div className={styles.agreementSection}>
                            <h4>수집 및 이용 목적</h4>
                            {PRIVACY_CONFIG.purposes.map((p, i) => (
                                <p key={i}>- {p}</p>
                            ))}
                        </div>
                        
                        <div className={styles.agreementSection}>
                            <h4>보유 및 이용기간</h4>
                            {PRIVACY_CONFIG.retentionPeriods.map((r, i) => (
                                <p key={i}>- {r}</p>
                            ))}
                        </div>
                        
                        <div className={styles.agreementSection}>
                            <h4>동의 거부 권리</h4>
                            <p>- 개인정보 수집·이용에 대한 동의를 거부할 권리가 있으나, 동의 거부 시 회원가입이 제한됩니다.</p>
                        </div>
                    </div>
                    
                    {/* 동의 체크박스 */}
                    <div className={styles.checkboxContainer} style={{ marginTop: '16px', padding: '12px', backgroundColor: '#f8f9fa', borderRadius: '8px' }}>
                        <label className={styles.checkboxLabel} style={{ display: 'flex', gap: 8, alignItems: 'flex-start' }}>
                            <input
                                type="checkbox"
                                checked={consent.use}
                                onChange={e => setConsent(s => ({ ...s, use: e.target.checked }))}
                                className={styles.checkbox}
                            />
                            <span>
                                <strong>[필수] 개인정보 수집·이용에 동의합니다</strong>
                            </span>
                        </label>
                    </div>
                </div>


                {/* 2. 서비스 이용약관 동의 */}
                <div className={styles.agreementBox}>
                    <h2 className={styles.agreementTitle}>2. 서비스 이용약관 동의 (필수)</h2>
                    <div className={styles.agreementContent}>
                        <p>서비스 이용을 위한 기본 약관입니다.</p>
                        <div className={styles.agreementSection}>
                            <p>- 서비스 이용 시 준수해야 할 사항과 회사와 이용자의 권리·의무가 포함되어 있습니다.</p>
                            <p>- 서비스 정책 변경, 계정 관리, 서비스 중단 등에 관한 내용이 포함됩니다.</p>
                            {PRIVACY_CONFIG.tosPath && (
                                <p>
                                    ※ <a href={PRIVACY_CONFIG.tosPath} target="_blank" rel="noreferrer">이용약관 전문 보기</a>
                                </p>
                            )}
                        </div>
                    </div>
                    
                    {/* 동의 체크박스 */}
                    <div className={styles.checkboxContainer} style={{ marginTop: '16px', padding: '12px', backgroundColor: '#f8f9fa', borderRadius: '8px' }}>
                        <label className={styles.checkboxLabel} style={{ display: 'flex', gap: 8, alignItems: 'flex-start' }}>
                            <input
                                type="checkbox"
                                checked={consent.tos}
                                onChange={e => setConsent(s => ({ ...s, tos: e.target.checked }))}
                                className={styles.checkbox}
                            />
                            <span>
                                <strong>[필수] 서비스 이용약관에 동의합니다</strong>
                            </span>
                        </label>
                    </div>
                </div>


                {/* 추가 안내사항 */}
                <div className={styles.agreementBox}>
                    <h2 className={styles.agreementTitle}>추가 안내사항</h2>
                    <div className={styles.agreementContent}>
                        <p>- <strong>개인정보보호 책임자</strong>: {PRIVACY_CONFIG.dpo.name}</p>
                        <p>- <strong>연락처</strong>: {PRIVACY_CONFIG.dpo.email}, {PRIVACY_CONFIG.dpo.phone}</p>
                        <p>- 개인정보 처리에 관한 불만이 있으시면 개인정보보호위원회(privacy.go.kr)에 신고하실 수 있습니다.</p>
                        {PRIVACY_CONFIG.policyPath && (
                            <p>
                                ※ <a href={PRIVACY_CONFIG.policyPath} target="_blank" rel="noopener noreferrer">
                                개인정보 처리방침 전문 보기
                            </a>
                            </p>
                        )}
                    </div>
                </div>

                {/* 버튼 */}
                <div className={styles.buttonRow}>
                    <button type="button" className={styles.grayBtn} onClick={handleCancel} disabled={isLoading}>
                        취소
                    </button>
                    <button
                        type="button"
                        className={`${styles.blackBtn} ${!requiredOk || isLoading ? styles.disabled : ''}`}
                        onClick={handleAgree}
                        disabled={!requiredOk || isLoading}
                    >
                        {isLoading ? '회원가입 중...' : '회원가입'}
                    </button>
                </div>
            </div>
        </div>
    );
};

export default PrivacyConsentPage;
