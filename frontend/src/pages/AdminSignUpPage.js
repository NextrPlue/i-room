import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import styles from '../styles/AdminSignUp.module.css';
// import { workerAPI } from '../api/api'; // API 연동 시 사용

const AdminSignUpPage = () => {
    const navigate = useNavigate();
    const [isLoading, setIsLoading] = useState(false);

    // 회원가입 폼 데이터 (필수 필드만)
    const [formData, setFormData] = useState({
        name: '',
        email: '',
        password: '',
        passwordConfirm: ''
    });

    // 폼 검증 상태
    const [validation, setValidation] = useState({
        name: { isValid: null, message: '' },
        email: { isValid: null, message: '' },
        password: { isValid: null, message: '' },
        passwordConfirm: { isValid: null, message: '' }
    });

    // 입력 핸들러
    const handleInputChange = (field, value) => {
        setFormData(prev => ({
            ...prev,
            [field]: value
        }));

        // 실시간 검증
        validateField(field, value);
    };

    // 필드별 검증 (최소 검증: 이름/이메일 trim, 비번 8–16자, 비번확인 일치)
    const validateField = (field, value) => {
        let isValid = null;
        let message = '';

        switch (field) {
            case 'name': {
                const v = String(value).trim();
                if (v.length === 0) {
                    isValid = null;
                } else {
                    isValid = true;
                    message = '사용 가능한 이름입니다.';
                }
                break;
            }

            case 'email': {
                const v = String(value).trim();
                if (v.length === 0) {
                    isValid = null;
                } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(v)) {
                    isValid = false;
                    message = '올바른 이메일 형식을 입력해주세요.';
                } else {
                    isValid = true;
                    message = '사용 가능한 이메일입니다.';
                }
                break;
            }

            case 'password': {
                const v = String(value); // 비밀번호는 trim 안 함
                if (v.length === 0) {
                    isValid = null;
                } else if (v.length < 8 || v.length > 16) {
                    isValid = false;
                    message = '비밀번호는 8-16자여야 합니다.';
                } else {
                    isValid = true;
                    message = '사용 가능한 비밀번호입니다.';
                }
                break;
            }

            case 'passwordConfirm': {
                const v = String(value);
                if (v.length === 0) {
                    isValid = null;
                } else if (v !== formData.password) {
                    isValid = false;
                    message = '비밀번호가 일치하지 않습니다.';
                } else {
                    isValid = true;
                    message = '비밀번호가 일치합니다.';
                }
                break;
            }

            default:
                break;
        }

        setValidation(prev => ({
            ...prev,
            [field]: { isValid, message }
        }));
    };

    // 폼 검증 확인
    const isFormValid = () => {
        const requiredFields = ['name', 'email', 'password', 'passwordConfirm'];

        for (let field of requiredFields) {
            if (!formData[field] || validation[field]?.isValid !== true) {
                return false;
            }
        }

        return true;
    };

    // 회원가입 제출
    const handleSubmit = async (e) => {
        e.preventDefault();

        if (!isFormValid()) {
            alert('모든 필수 정보를 올바르게 입력해주세요.');
            return;
        }

        setIsLoading(true);

        try {
            // API 호출 로직
            // const registerData = {
            //     name: formData.name,
            //     email: formData.email,
            //     password: formData.password
            // };

            // await workerAPI.register(registerData);

            // 임시 지연 (실제 API 호출 시뮬레이션)
            await new Promise(resolve => setTimeout(resolve, 2000));

            alert('회원가입이 완료되었습니다! 관리자 승인 후 로그인 가능합니다.');
            navigate('/admin/login'); // 관리자 로그인 페이지로 이동

        } catch (error) {
            console.error('회원가입 실패:', error);
            alert('회원가입에 실패했습니다. 다시 시도해주세요.');
        } finally {
            setIsLoading(false);
        }
    };

    // 취소 버튼 클릭
    const handleCancel = () => {
        if (window.confirm('회원가입을 취소하시겠습니까? 입력한 정보가 모두 삭제됩니다.')) {
            navigate('/admin/login');
        }
    };


    return (
        <div className={styles.page}>
            <div className={styles.container}>
                {/* 좌측: 일러스트 섹션 */}
                <div className={styles.illustrationSection}>
                    <div className={styles.illustrationContainer}>
                        <img
                            src="/i-room.png"
                            alt="이룸(i-Room) 일러스트"
                            className={styles.illustrationImage}
                        />
                    </div>
                    <h1 className={styles.brandTitle}>이룸 (i-Room)</h1>
                    <p className={styles.brandSubtitle}>: 안전을 '이룬다'</p>
                    <p className={styles.brandDescription}>: 지능형(intelligent) 공간(Room)</p>
                </div>

                {/* 우측: 회원가입 폼 */}
                <div className={styles.formSection}>
                    <h2 className={styles.formTitle}>관리자 회원가입</h2>

                    <form className={styles.form} onSubmit={handleSubmit}>
                        {/* 관리자 이름 */}
                        <div className={styles.formRow}>
                            <div className={styles.formGroup}>
                                <label className={styles.formLabel}>관리자 이름</label>
                                <input
                                    type="text"
                                    className={`${styles.formInput} ${validation.name.isValid === false ? styles.error : validation.name.isValid === true ? styles.success : ''}`}
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
                                    className={`${styles.formInput} ${validation.password.isValid === false ? styles.error : validation.password.isValid === true ? styles.success : ''}`}
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

                        {/* E-mail */}
                        <div className={styles.formRow}>
                            <div className={styles.formGroup}>
                                <label className={styles.formLabel}>E-mail</label>
                                <input
                                    type="email"
                                    className={`${styles.formInput} ${validation.email.isValid === false ? styles.error : validation.email.isValid === true ? styles.success : ''}`}
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
                                    className={`${styles.formInput} ${validation.passwordConfirm.isValid === false ? styles.error : validation.passwordConfirm.isValid === true ? styles.success : ''}`}
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

                        {/* 버튼 그룹 */}
                        <div className={styles.buttonGroup}>
                            <button
                                type="button"
                                className={styles.cancelButton}
                                onClick={handleCancel}
                                disabled={isLoading}
                            >
                                취소
                            </button>
                            <button
                                type="submit"
                                className={styles.submitButton}
                                disabled={!isFormValid() || isLoading}
                            >
                                {isLoading ? (
                                    <span className={styles.loading}>
                                        <span className={styles.loadingSpinner}></span>
                                        가입 중...
                                    </span>
                                ) : (
                                    '가입'
                                )}
                            </button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    );
};

export default AdminSignUpPage;