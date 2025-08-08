import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { workerAPI } from '../api/workerAPI';
import styles from '../styles/WorkerLogin.module.css';

const WorkerLogin = ({ onLogin }) => {
    const navigate = useNavigate();
    const [formData, setFormData] = useState({
        email: '',
        password: ''
    });

    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: value
        }));
        // 입력 시 에러 메시지 초기화
        if (error) setError('');
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError('');

        try {
            // API 호출
            const response = await workerAPI.login(formData);

            if (response.status === 'success') {
                console.log('로그인 성공:', response.message);
                // 홈 페이지로 이동
                navigate('/home');
            } else {
                setError('로그인에 실패했습니다.');
            }
        } catch (error) {
            console.error('로그인 오류:', error);
            setError(error.message || '로그인 중 오류가 발생했습니다.');
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
                        <circle cx="12" cy="12" r="10" fill="#e0e0e0" />
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
                <form onSubmit={handleSubmit} className={styles.loginForm}>
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

                    <button type="submit" className={styles.loginButton}>
                        로그인
                    </button>
                </form>
            </div>
        </div>
    );
};

export default WorkerLogin;
