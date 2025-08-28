import React, {useState, useEffect} from 'react';
import {useNavigate} from 'react-router-dom';
import {workerAPI} from '../api/workerAPI';
import {loginLimiter} from '../utils/loginLimiter';
import styles from '../styles/WorkerLogin.module.css';

const WorkerLogin = ({onLogin}) => {
    const navigate = useNavigate();
    const [formData, setFormData] = useState({
        email: 'test@example.com',
        password: '!test123'
    });

    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const [isLocked, setIsLocked] = useState(false);
    const [remainingTime, setRemainingTime] = useState(0);
    const [remainingAttempts, setRemainingAttempts] = useState(loginLimiter.MAX_ATTEMPTS);

    const LOGIN_IDENTIFIER = 'worker_login';

    // 컴포넌트 마운트 시 로그인 제한 상태 확인
    useEffect(() => {
        checkLoginStatus();
        
        // 1초마다 남은 시간 업데이트
        const timer = setInterval(() => {
            if (loginLimiter.isLocked(LOGIN_IDENTIFIER)) {
                const remaining = loginLimiter.getRemainingLockoutTime(LOGIN_IDENTIFIER);
                setRemainingTime(remaining);
                if (remaining <= 0) {
                    checkLoginStatus();
                }
            }
        }, 1000);

        return () => clearInterval(timer);
    }, []);

    const checkLoginStatus = () => {
        const locked = loginLimiter.isLocked(LOGIN_IDENTIFIER);
        const remaining = loginLimiter.getRemainingLockoutTime(LOGIN_IDENTIFIER);
        const attempts = loginLimiter.getRemainingAttempts(LOGIN_IDENTIFIER);
        
        setIsLocked(locked);
        setRemainingTime(remaining);
        setRemainingAttempts(attempts);
        
        if (locked) {
            setError(`로그인이 제한되었습니다. ${loginLimiter.formatRemainingTime(remaining)} 후에 다시 시도해주세요.`);
        }
    };

    const handleChange = (e) => {
        const {name, value} = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: value
        }));
        // 입력 시 에러 메시지 초기화
        if (error) setError('');
    };

    const handleLogin = async (e) => {
        e.preventDefault();

        // 로그인 제한 확인
        if (isLocked) {
            setError(`로그인이 제한되었습니다. ${loginLimiter.formatRemainingTime(remainingTime)} 후에 다시 시도해주세요.`);
            return;
        }

        if (!formData.email || !formData.password) {
            setError('이메일과 비밀번호를 모두 입력해주세요.');
            return;
        }

        try {
            setLoading(true);
            setError('');

            const response = await workerAPI.login(formData);

            if (response.status === 'success') {
                // 로그인 성공 시 실패 횟수 초기화
                loginLimiter.clearFailedAttempts(LOGIN_IDENTIFIER);
                onLogin?.(response);
                navigate('/home');
            } else {
                // 로그인 실패 처리
                const attempts = loginLimiter.recordFailedAttempt(LOGIN_IDENTIFIER);
                checkLoginStatus();
                
                if (attempts >= loginLimiter.MAX_ATTEMPTS) {
                    setError(`로그인에 ${loginLimiter.MAX_ATTEMPTS}회 실패했습니다. 5분간 로그인이 제한됩니다.`);
                } else {
                    const remaining = loginLimiter.MAX_ATTEMPTS - attempts;
                    setError(`${response.message || '로그인에 실패했습니다.'} (남은 시도 횟수: ${remaining}회)`);
                }
            }
        } catch (err) {
            console.error('로그인 실패:', err);
            
            // 로그인 실패 처리
            const attempts = loginLimiter.recordFailedAttempt(LOGIN_IDENTIFIER);
            checkLoginStatus();
            
            const msg = err?.response?.data?.message || err?.message || '로그인 중 오류가 발생했습니다.';
            
            if (attempts >= loginLimiter.MAX_ATTEMPTS) {
                setError(`로그인에 ${loginLimiter.MAX_ATTEMPTS}회 실패했습니다. 5분간 로그인이 제한됩니다.`);
            } else {
                const remaining = loginLimiter.MAX_ATTEMPTS - attempts;
                setError(`${msg} (남은 시도 횟수: ${remaining}회)`);
            }
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className={styles.workerLoginContainer}>
            <div className={styles.workerLoginBox}>
                {/* 프로필 아이콘 */}
                <div className={styles.profileIcon}>
                    <svg width="60" height="60" viewBox="0 0 24 24" fill="none">
                        <circle cx="12" cy="12" r="10" fill="#e0e0e0"/>
                    </svg>
                </div>

                {/* 타이틀 */}
                <h1 className={styles.loginTitle}>
                    안녕하세요!
                </h1>
                <p className={styles.loginSubtitle}>
                    오늘도 안전하고 활기찬 하루 보내세요!
                </p>

                {/* 로그인 폼 */}
                <form onSubmit={handleLogin} className={styles.loginForm}>
                    <div className={styles.formGroup}>
                        <label htmlFor="email">이메일</label>
                        <input
                            type="email"
                            id="email"
                            name="email"
                            value={formData.email}
                            onChange={handleChange}
                            placeholder="이메일을 입력하세요"
                            required
                        />
                    </div>

                    <div className={styles.formGroup}>
                        <label htmlFor="password">비밀번호</label>
                        <input
                            type="password"
                            id="password"
                            name="password"
                            value={formData.password}
                            onChange={handleChange}
                            placeholder="비밀번호를 입력하세요"
                            required
                        />
                    </div>

                    {error && (
                        <div className={styles.errorMsg}>
                            {error}
                        </div>
                    )}

                    <button
                        type="submit"
                        className={styles.loginButton}
                        disabled={loading || isLocked}
                    >
                        {loading ? '로그인 중...' : '로그인'}
                    </button>
                    
                    {!isLocked && remainingAttempts < loginLimiter.MAX_ATTEMPTS && (
                        <div className={styles.attemptsWarning}>
                            남은 시도 횟수: {remainingAttempts}회
                        </div>
                    )}
                </form>
            </div>
        </div>
    );
};

export default WorkerLogin;
