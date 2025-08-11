import React, { useState, useEffect } from 'react';
import styles from './SafetyGearAlert.module.css';

const SafetyGearAlert = ({ isOpen, onClose }) => {
    const [timeLeft, setTimeLeft] = useState(30);
    const [isClosing, setIsClosing] = useState(false);

    useEffect(() => {
        if (isOpen && timeLeft > 0) {
            const timer = setTimeout(() => {
                setTimeLeft(timeLeft - 1);
            }, 1000);
            return () => clearTimeout(timer);
        } else if (timeLeft === 0) {
            handleClose();
        }
    }, [isOpen, timeLeft]);

    useEffect(() => {
        if (isOpen) {
            setTimeLeft(30);
            setIsClosing(false);
            // 진동 API (모바일에서 지원 시)
            if (navigator.vibrate) {
                navigator.vibrate([200, 100, 200]);
            }
        }
    }, [isOpen]);

    const handleClose = () => {
        setIsClosing(true);
        setTimeout(() => {
            onClose();
            setIsClosing(false);
        }, 300);
    };

    if (!isOpen && !isClosing) return null;

    return (
        <div className={`${styles.overlay} ${isClosing ? styles.fadeOut : styles.fadeIn}`}>
            <div className={`${styles.alertContainer} ${isClosing ? styles.slideOut : styles.slideIn}`}>

                {/* 경고 아이콘 */}
                <div className={styles.iconWrapper}>
                    <div className={styles.warningIcon}>!</div>
                </div>

                {/* 메인 텍스트 */}
                <h1 className={styles.title}>보호구 미착용 경고!</h1>

                <p className={styles.message}>
                    즉시 보호구를 착용하세요
                </p>

                {/* 확인 버튼 */}
                <button
                    className={styles.confirmButton}
                    onClick={handleClose}
                >
                    <span className={styles.checkIcon}>✓</span>
                    <span>확인</span>
                </button>

                {/* 자동 닫힘 타이머 */}
                <p className={styles.timerText}>
                    {timeLeft}초 후 자동으로 사라집니다.
                </p>
            </div>
        </div>
    );
};

export default SafetyGearAlert;