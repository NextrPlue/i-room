import React, { useState } from 'react';
import styles from '../styles/WorkerLogin.css';

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
        <div className="worker-login-container">
            <div className="worker-login-box">
                {/* 프로필 아이콘 */}
                <div className="profile-icon">
                    <svg width="60" height="60" viewBox="0 0 24 24" fill="none">
                        <circle cx="12" cy="12" r="10" fill="#e0e0e0"/>
                        <path d="M12 12C13.6569 12 15 10.6569 15 9C15 7.34315 13.6569 6 12 6C10.3431 6 9 7.34315 9 9C9 10.6569 10.3431 12 12 12Z" fill="#999"/>
                        <path d="M12 14C8.68629 14 6 16.6863 6 20H18C18 16.6863 15.3137 14 12 14Z" fill="#999"/>
                    </svg>
                </div>

                {/* 타이틀 */}
                <h1 className="login-title">
                    안녕하세요 "김재환"님,
                </h1>
                <p className="login-subtitle">
                    오늘도 안전하고 활기찬 하루 보내세요!
                </p>

                {/* 로그인 폼 */}
                <form onSubmit={handleSubmit} className="login-form">
                    <div className="form-group">
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

                    <div className="form-group">
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

                    <button type="submit" className="login-button">
                        🔒 로그인
                    </button>
                </form>
            </div>
        </div>
    );
};

export default WorkerLogin;