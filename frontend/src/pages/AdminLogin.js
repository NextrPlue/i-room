import React from 'react';
import styles from '../styles/AdminLogin.module.css';

const AdminLogin = ({onLogin}) => {
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

                <form className={styles.form} onSubmit={(e) => e.preventDefault()}>
                    <label>Email</label>
                    <input
                        type="email"
                        name="email"
                        placeholder="Enter your email"
                        className={styles.input}
                    />

                    <label>Password</label>
                    <input
                        type="password"
                        name="password"
                        placeholder="Enter your password"
                        className={styles.input}
                    />

                    <div className={styles.buttonRow}>
                        <button type="button" className={styles.grayBtn}>관리자 회원가입</button>
                        <button type="submit" className={styles.blackBtn} onClick={onLogin}>로그인</button>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default AdminLogin;
