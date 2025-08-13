import React, { useState, useEffect } from 'react';
import styles from '../styles/WorkerAlert.module.css'

// 1. 보호구 미착용 경고 (PPE_VIOLATION)
export const SafetyGearAlert = ({ isOpen, onClose, data }) => {
    const [timeLeft, setTimeLeft] = useState(30);

    useEffect(() => {
        if (isOpen) {
            setTimeLeft(30);
            if (navigator.vibrate) {
                navigator.vibrate([200, 100, 200]);
            }
        }
    }, [isOpen]);

    useEffect(() => {
        if (isOpen && timeLeft > 0) {
            const timer = setTimeout(() => setTimeLeft(timeLeft - 1), 1000);
            return () => clearTimeout(timer);
        } else if (timeLeft === 0) {
            onClose();
        }
    }, [isOpen, timeLeft, onClose]);

    if (!isOpen) return null;

    return (
        <div className={styles.overlay}>
            <div className={`${styles.alertContainer} ${styles.safetyAlert}`}>
                <div className={styles.iconWrapper}>
                    <div className={styles.safetyIcon}>🦺</div>
                </div>

                <h1 className={styles.title}>보호구 미착용 경고!</h1>
                <p className={styles.message}>
                    {"즉시 보호구를 착용하세요"}
                </p>

                {data?.workerImageUrl && (
                    <img
                        src={data.workerImageUrl}
                        alt="위반 이미지"
                        className={styles.violationImage}
                    />
                )}

                <button className={styles.confirmButton} onClick={onClose}>
                    <span>✓ 확인</span>
                </button>

                <p className={styles.timerText}>
                    {timeLeft}초 후 자동으로 사라집니다
                </p>
            </div>
        </div>
    );
};

// 2. 위험구역 접근 경고 (DANGER_ZONE)
export const DangerZoneAlert = ({ isOpen, onClose, data }) => {
    const [timeLeft, setTimeLeft] = useState(30);

    useEffect(() => {
        if (isOpen) {
            setTimeLeft(30);
            if (navigator.vibrate) {
                navigator.vibrate([500, 200, 500]);
            }
        }
    }, [isOpen]);

    useEffect(() => {
        if (isOpen && timeLeft > 0) {
            const timer = setTimeout(() => setTimeLeft(timeLeft - 1), 1000);
            return () => clearTimeout(timer);
        } else if (timeLeft === 0) {
            onClose();
        }
    }, [isOpen, timeLeft, onClose]);

    if (!isOpen) return null;

    return (
        <div className={styles.overlay}>
            <div className={`${styles.alertContainer} ${styles.dangerAlert}`}>
                <div className={styles.iconWrapper}>
                    <div className={styles.dangerIcon}>⚠️</div>
                </div>

                <h1 className={styles.title}>위험구역 접근 경고!</h1>
                <p className={styles.message}>
                    {"즉시 안전구역으로 대피하세요"}
                </p>

                <button className={styles.confirmButton} onClick={onClose}>
                    <span>✓ 확인</span>
                </button>

                <p className={styles.timerText}>
                    {timeLeft}초 후 자동으로 사라집니다
                </p>
            </div>
        </div>
    );
};

// 3. 건강 위험 경고 (HEALTH_RISK)
export const HealthRiskAlert = ({ isOpen, onClose, data }) => {
    const [timeLeft, setTimeLeft] = useState(30);

    useEffect(() => {
        if (isOpen) {
            setTimeLeft(30);
            if (navigator.vibrate) {
                navigator.vibrate([300, 100, 300, 100, 300]);
            }
        }
    }, [isOpen]);

    useEffect(() => {
        if (isOpen && timeLeft > 0) {
            const timer = setTimeout(() => setTimeLeft(timeLeft - 1), 1000);
            return () => clearTimeout(timer);
        } else if (timeLeft === 0) {
            onClose();
        }
    }, [isOpen, timeLeft, onClose]);

    if (!isOpen) return null;

    return (
        <div className={styles.overlay}>
            <div className={`${styles.alertContainer} ${styles.healthAlert}`}>
                <div className={styles.iconWrapper}>
                    <div className={styles.healthIcon}>🏥</div>
                </div>

                <h1 className={styles.title}>건강 위험 경고!</h1>

                <div className={styles.healthInfo}>
                    <p>건강 이상이 감지되었습니다</p>
                    <p>안전한 곳으로 이동하여 휴식을 취해주세요</p>
                </div>

                <button className={styles.confirmButton} onClick={onClose}>
                    <span>✓ 확인</span>
                </button>

                <p className={styles.timerText}>
                    {timeLeft}초 후 자동으로 사라집니다
                </p>
            </div>
        </div>
    );
};