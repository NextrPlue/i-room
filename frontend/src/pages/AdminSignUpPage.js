import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import styles from '../styles/AdminSignUp.module.css';

const AdminSignUpPage = () => {
    const navigate = useNavigate();

    // 폼 데이터
    const [formData, setFormData] = useState({
        name: '관리자1',
        email: 'admin@test1.com',
        password: 'admin1234!',
        passwordConfirm: 'admin1234!',
        phone: '010-1234-5678',
    });

    // 각 필드 검증 상태
    const [validation, setValidation] = useState({
        name: { isValid: null, message: '' },
        email: { isValid: null, message: '' },
        password: { isValid: null, message: '' },
        passwordConfirm: { isValid: null, message: '' },
        phone: { isValid: null, message: '' },
    });

    const handleInputChange = (field, value) => {
        setFormData(prev => ({ ...prev, [field]: value }));
        validateField(field, value);
    };


    const validateField = (field, value) => {
        let isValid = null;
        let message = '';

        switch (field) {
            case 'name': {
                const v = String(value).trim();
                isValid = v.length ? true : null;
                message = v.length ? '사용 가능한 이름입니다.' : '';
                break;
            }
            case 'email': {
                const v = String(value).trim();
                if (!v.length) { isValid = null; break; }
                if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(v)) {
                    isValid = false; message = '올바른 이메일 형식을 입력해주세요.';
                } else {
                    isValid = true; message = '사용 가능한 이메일입니다.';
                }
                break;
            }
            case 'password': {
                const v = String(value);
                if (!v.length) { isValid = null; break; }
                if (v.length < 8 || v.length > 16) {
                    isValid = false; message = '비밀번호는 8-16자여야 합니다.';
                } else if (!/[a-zA-Z]/.test(v)) {
                    isValid = false; message = '영문자를 포함해야 합니다.';
                } else if (!/\d/.test(v)) {
                    isValid = false; message = '숫자를 포함해야 합니다.';
                } else if (!/[#$%&*+,./:=?@[\\\]^_`{|}~!-]/.test(v) || /[()<>'";}]/.test(v)) {
                    isValid = false; message = '허용된 특수문자를 포함해야 합니다.';
                } else {
                    isValid = true; message = '사용 가능한 비밀번호입니다.';
                }
                // 비번이 바뀌면 확인 필드도 재검증
                if (formData.passwordConfirm) {
                    validateField('passwordConfirm', formData.passwordConfirm);
                }
                break;
            }
            case 'passwordConfirm': {
                const v = String(value);
                if (!v.length) { isValid = null; break; }
                if (v !== formData.password) {
                    isValid = false; message = '비밀번호가 일치하지 않습니다.';
                } else {
                    isValid = true; message = '비밀번호가 일치합니다.';
                }
                break;
            }
            case 'phone': {
                const v = String(value).trim();
                if (!v.length) { isValid = null; break; }
                if (!/^010-\d{4}-\d{4}$/.test(v)) {
                    isValid = false; message = '전화번호 형식이 올바르지 않습니다. (예: 010-1234-5678)';
                } else {
                    isValid = true; message = '사용 가능한 전화번호입니다.';
                }
                break;
            }
            default: break;
        }

        setValidation(prev => ({ ...prev, [field]: { isValid, message } }));
    };

    const isFormValid = () => {
        
        // 모든 필드가 채워져 있는지 확인
        const fieldsCheck = !formData.name?.trim() || !formData.email?.trim() || !formData.password?.trim() || 
            !formData.passwordConfirm?.trim() || !formData.phone?.trim();
        if (fieldsCheck) {
            return false;
        }
        
        // 이메일 형식 검증
        const emailCheck = !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email.trim());
        if (emailCheck) {
            return false;
        }
        
        // 비밀번호 길이 및 구성 검증
        const pwd = formData.password;
        const lengthCheck = pwd.length < 8 || pwd.length > 16;
        const letterCheck = !/[A-Za-z]/.test(pwd);
        const numberCheck = !/\d/.test(pwd);
        const specialCheck = !/[#$%&*+,./:=?@[\\\]^_`{|}~!-]/.test(pwd);
        const forbiddenCheck = /[()<>'";}]/.test(pwd);
        
        if (lengthCheck || letterCheck || numberCheck || specialCheck || forbiddenCheck) {
            return false;
        }
        
        // 비밀번호 확인
        const passwordMatchCheck = formData.password !== formData.passwordConfirm;
        if (passwordMatchCheck) {
            return false;
        }
        
        // 전화번호 형식 검증
        const phoneCheck = !/^010-\d{4}-\d{4}$/.test(formData.phone.trim());
        if (phoneCheck) {
            return false;
        }

        return true;
    };

    const handleCancel = () => {
        if (window.confirm('회원가입을 취소하시겠습니까? 입력한 정보가 모두 삭제됩니다.')) {
            navigate('/admin/login');
        }
    };

    const handleSubmit = (e) => {
        e.preventDefault();
        if (!isFormValid()) {
            alert('모든 필수 정보를 올바르게 입력해주세요.');
            return;
        }
        const signUpData = {
            name: formData.name.trim(),
            email: formData.email.trim(),
            password: formData.password,
            phone: formData.phone.trim(),
        };
        navigate('/admin/privacy-consent', { state: { signUpData } });
    };

    return (
        <div className={styles.page}>
            {/* 상단 바 */}
            <header className={styles.topBar}>
                <span className={styles.circle} />
                <span className={styles.logoText}>이룸</span>
            </header>

            {/* 본문 2열 레이아웃 */}
            <div className={styles.mainContent}>
                <div className={styles.container}>
                    {/* 왼쪽: 큰 이미지 */}
                    <section className={styles.illustrationSection}>
                        <div className={styles.illustrationContainer}>
                            <img
                                src="/i-room.png"
                                alt="이룸(i-Room) 일러스트"
                                className={styles.illustrationImage}
                            />
                        </div>
                    </section>

                    {/* 오른쪽: 큰 타이틀 + 언더라인 + 폼 */}
                    <section className={styles.formSection}>
                        <div className={styles.titleWrap}>
                            <h1 className={styles.bigTitle}>이룸 (i-Room)</h1>
                            <span className={styles.titleUnderline} />
                            <ul className={styles.titleDesc}>
                                <li>: 안전을 ‘이룬다’</li>
                                <li>: 지능형(Intelligent) 공간(Room)</li>
                            </ul>
                        </div>

                        <h2 className={styles.formTitle}>관리자 회원가입</h2>

                        <form className={styles.form} onSubmit={handleSubmit} noValidate>
                            {/* 이름 / 비밀번호 */}
                            <div className={styles.formRow}>
                                <div className={styles.formGroup}>
                                    <label className={styles.formLabel}>관리자 이름</label>
                                    <input
                                        type="text"
                                        className={`${styles.formInput} ${
                                            validation.name.isValid === false
                                                ? styles.error
                                                : validation.name.isValid === true
                                                    ? styles.success
                                                    : ''
                                        }`}
                                        placeholder="이름을 입력해주세요"
                                        value={formData.name}
                                        onChange={(e) => handleInputChange('name', e.target.value)}
                                    />
                                    {validation.name.message && (
                                        <span className={validation.name.isValid ? styles.successMessage : styles.errorMessage}>
                      {validation.name.message}
                    </span>
                                    )}
                                </div>

                                <div className={styles.formGroup}>
                                    <label className={styles.formLabel}>password</label>
                                    <input
                                        type="password"
                                        className={`${styles.formInput} ${
                                            validation.password.isValid === false
                                                ? styles.error
                                                : validation.password.isValid === true
                                                    ? styles.success
                                                    : ''
                                        }`}
                                        placeholder="비밀번호를 입력해주세요"
                                        value={formData.password}
                                        onChange={(e) => handleInputChange('password', e.target.value)}
                                    />
                                    {validation.password.message && (
                                        <span className={validation.password.isValid ? styles.successMessage : styles.errorMessage}>
                      {validation.password.message}
                    </span>
                                    )}
                                </div>
                            </div>

                            {/* 이메일 / 비밀번호 확인 */}
                            <div className={styles.formRow}>
                                <div className={styles.formGroup}>
                                    <label className={styles.formLabel}>E-mail</label>
                                    <input
                                        type="email"
                                        className={`${styles.formInput} ${
                                            validation.email.isValid === false
                                                ? styles.error
                                                : validation.email.isValid === true
                                                    ? styles.success
                                                    : ''
                                        }`}
                                        placeholder="예) iroom2025@naver.com"
                                        value={formData.email}
                                        onChange={(e) => handleInputChange('email', e.target.value)}
                                    />
                                    {validation.email.message && (
                                        <span className={validation.email.isValid ? styles.successMessage : styles.errorMessage}>
                      {validation.email.message}
                    </span>
                                    )}
                                </div>

                                <div className={styles.formGroup}>
                                    <label className={styles.formLabel}>password 확인</label>
                                    <input
                                        type="password"
                                        className={`${styles.formInput} ${
                                            validation.passwordConfirm.isValid === false
                                                ? styles.error
                                                : validation.passwordConfirm.isValid === true
                                                    ? styles.success
                                                    : ''
                                        }`}
                                        placeholder="비밀번호를 다시 입력해주세요"
                                        value={formData.passwordConfirm}
                                        onChange={(e) => handleInputChange('passwordConfirm', e.target.value)}
                                    />
                                    {validation.passwordConfirm.message && (
                                        <span className={validation.passwordConfirm.isValid ? styles.successMessage : styles.errorMessage}>
                      {validation.passwordConfirm.message}
                    </span>
                                    )}
                                </div>
                            </div>

                            {/* 전화번호 */}
                            <div className={styles.formRow}>
                                <div className={styles.formGroup}>
                                    <label className={styles.formLabel}>전화번호</label>
                                    <input
                                        type="tel"
                                        className={`${styles.formInput} ${
                                            validation.phone.isValid === false
                                                ? styles.error
                                                : validation.phone.isValid === true
                                                    ? styles.success
                                                    : ''
                                        }`}
                                        placeholder="010-1234-5678"
                                        value={formData.phone}
                                        onChange={(e) => handleInputChange('phone', e.target.value)}
                                    />
                                    {validation.phone.message && (
                                        <span className={validation.phone.isValid ? styles.successMessage : styles.errorMessage}>
                      {validation.phone.message}
                    </span>
                                    )}
                                </div>
                            </div>

                            {/* 버튼 */}
                            <div className={styles.buttonGroup}>
                                <button type="button" className={styles.cancelButton} onClick={handleCancel}>
                                    취소
                                </button>
                                <button type="submit" className={styles.submitButton}>
                                    다음
                                </button>
                            </div>
                        </form>
                    </section>
                </div>
            </div>
        </div>
    );
};

export default AdminSignUpPage;
