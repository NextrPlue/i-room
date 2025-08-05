import React, {useState} from 'react';
import styles from '../styles/AdminLogin.module.css';
import {adminAPI} from '../api/api';

const AdminLogin = ({onLogin}) => {
    const [formData, setFormData] = useState({
        email: '',
        password: ''
    });
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');

    // 입력 필드 변경 처리
    const handleInputChange = (e) => {
        const {name, value} = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: value
        }));
        // 입력할 때 에러 메시지 초기화
        if (error) setError('');
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

            // 백엔드 API 호출
            const response = await adminAPI.login(formData);
            // 부모 컴포넌트에 로그인 성공 알림
            onLogin(response);

        } catch (err) {
            console.error('로그인 실패:', err);
            setError(err.message || '로그인에 실패했습니다.');
        } finally {
            setLoading(false);
        }
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

                <form className={styles.form} onSubmit={handleLogin}>
                    <label>Email</label>
                    <input
                        type="email"
                        name="email"
                        placeholder="Enter your email"
                        className={styles.input}
                        value={formData.email}
                        onChange={handleInputChange}
                        disabled={loading}
                    />

                    <label>Password</label>
                    <input
                        type="password"
                        name="password"
                        placeholder="Enter your password"
                        className={styles.input}
                        value={formData.password}
                        onChange={handleInputChange}
                        disabled={loading}
                    />

                    {/* 에러 메시지 표시 */}
                    {error && (
                        <div className={styles.errorMessage}>
                            {error}
                        </div>
                    )}

                    <div className={styles.buttonRow}>
                        <button type="button" className={styles.grayBtn}>관리자 회원가입</button>
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
