import React, { useState } from 'react';
import styles from '../styles/WorkerLogin.module.css';

const WorkerLogin = ({ onLogin }) => {
    const [formData, setFormData] = useState({
        email: '',
        password: ''
    });

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: value
        }));
    };

    const handleSubmit = (e) => {
        e.preventDefault();
        if (onLogin) {
            onLogin(formData);
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
