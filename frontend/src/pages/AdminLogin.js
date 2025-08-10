import React, {useState} from 'react';
import {useNavigate} from 'react-router-dom';
import styles from '../styles/AdminLogin.module.css';
import {adminAPI} from '../api/api';

const AdminLogin = ({onLogin}) => {
    const navigate = useNavigate();
    const [formData, setFormData] = useState({
        email: 'admin@iroom.com',
        password: 'admin123!'
    });
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');

    // 폼 검증 상태
    const [validation, setValidation] = useState({
        email: { isValid: null, message: '' },
        password: { isValid: null, message: '' }
    });

    // 입력 필드 변경 처리
    const handleInputChange = (field, value) => {
        setFormData(prev => ({
            ...prev,
            [field]: value
        }));
        // 입력할 때 에러 메시지 초기화
        if (error) setError('');
        
        // 실시간 검증
        validateField(field, value);
    };

    // 필드별 검증
    const validateField = (field, value) => {
        let isValid = null;
        let message = '';

        switch (field) {
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
                const v = String(value);
                if (v.length === 0) {
                    isValid = null;
                } else if (v.length < 8 || v.length > 16) {
                    isValid = false;
                    message = '비밀번호는 8-16자여야 합니다.';
                } else if (!/.*[a-zA-Z].*/.test(v)) {
                    isValid = false;
                    message = '영문자를 포함해야 합니다.';
                } else if (!/.*\d.*/.test(v)) {
                    isValid = false;
                    message = '숫자를 포함해야 합니다.';
                } else if (!/[!#$%&*+,.\/:=?@\[\\\]^_`{|}~-]/.test(v)) {
                    isValid = false;
                    message = '특수문자를 포함해야 합니다.';
                } else if (/[()<>'";}]/.test(v)) {
                    isValid = false;
                    message = '특수문자 ()<>"\';는 사용할 수 없습니다.';
                } else {
                    isValid = true;
                    message = '사용 가능한 비밀번호입니다.';
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

    // 로그인 처리
    const handleLogin = async (e) => {
        e.preventDefault();

        // 입력값 검증
        if (!formData.email || !formData.password) {
            setError('이메일과 비밀번호를 모두 입력해주세요.');
            return;
        }

        try {
            setLoading(true);
            setError('');

            const response = await adminAPI.login(formData);
            onLogin(response);

        } catch (err) {
            console.error('로그인 실패:', err);
            setError(err.message || '로그인에 실패했습니다.');
        } finally {
            setLoading(false);
        }
    };

    // 회원가입 페이지로 이동
    const handleSignUp = () => {
        navigate('/admin/signup');
    };

    return (
        <div className={styles.page}>
            <header className={styles.topBar}>
                <span className={styles.circle}></span>
                <span className={styles.logoText}>이룸</span>
            </header>


            <div className={styles.mainContent}>
                <div className={styles.left}>
                    <h1 className={styles.leftTitle}>이룸 (i-Room)</h1>
                    <hr style={{margin: '16px 0', width: '198px', borderTop: '1.5px solid #a1a1aa'}}/>
                    <div className={styles.leftDesc}>
                        : 안전을 '이룬다'<br/>
                        : 지능형(intelligent) 공간(Room)
                    </div>
                </div>

                <form className={styles.form} onSubmit={handleLogin} noValidate>
                    <div className={styles.formGroup}>
                        <label className={styles.formLabel}>Email</label>
                        <input
                            type="email"
                            placeholder="Enter your email"
                            className={`${styles.formInput} ${validation.email.isValid === false ? styles.error : validation.email.isValid === true ? styles.success : ''}`}
                            value={formData.email}
                            onChange={(e) => handleInputChange('email', e.target.value)}
                            disabled={loading}
                        />
                        {validation.email.message && !error && (
                            <span className={validation.email.isValid ? styles.successMessage : styles.errorMessage}>
                                {validation.email.message}
                            </span>
                        )}
                    </div>

                    <div className={styles.formGroup}>
                        <label className={styles.formLabel}>Password</label>
                        <input
                            type="password"
                            placeholder="Enter your password"
                            className={`${styles.formInput} ${validation.password.isValid === false ? styles.error : validation.password.isValid === true ? styles.success : ''}`}
                            value={formData.password}
                            onChange={(e) => handleInputChange('password', e.target.value)}
                            disabled={loading}
                        />
                        {validation.password.message && !error && (
                            <span className={validation.password.isValid ? styles.successMessage : styles.errorMessage}>
                                {validation.password.message}
                            </span>
                        )}
                    </div>

                    {/* 전체 폼 에러 메시지 표시 */}
                    {error && (
                        <div className={styles.formErrorMessage}>
                            {error}
                        </div>
                    )}

                    <div className={styles.buttonRow}>
                        <button type="button" className={styles.grayBtn} onClick={handleSignUp}>관리자 회원가입</button>
                        <button
                            type="submit"
                            className={styles.blackBtn}
                            disabled={loading}
                        >
                            {loading ? '로그인 중...' : '로그인'}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default AdminLogin;
