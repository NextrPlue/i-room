import React, { useState, useEffect } from 'react';
import styles from '../styles/SuccessModal.module.css';

const SuccessModal = ({ isOpen, title, message, onClose }) => {
    const [isClosing, setIsClosing] = useState(false);
    const [shouldRender, setShouldRender] = useState(false);

    useEffect(() => {
        if (isOpen) {
            setShouldRender(true);
            setIsClosing(false);
        } else if (shouldRender) {
            setIsClosing(true);
            const timer = setTimeout(() => {
                setShouldRender(false);
                setIsClosing(false);
            }, 100); // 애니메이션 지속 시간과 일치
            return () => clearTimeout(timer);
        }
    }, [isOpen, shouldRender]);

    const handleClose = () => {
        setIsClosing(true);
        setTimeout(() => {
            onClose();
        }, 100);
    };

    const handleOverlayClick = (e) => {
        if (e.target === e.currentTarget) {
            handleClose();
        }
    };

    if (!shouldRender) return null;

    return (
        <div className={`${styles.modalOverlay} ${isClosing ? styles.closing : ''}`} onClick={handleOverlayClick}>
            <div className={`${styles.modalContent} ${isClosing ? styles.closing : ''}`} onClick={(e) => e.stopPropagation()}>
                <h3 className={styles.modalTitle}>{title}</h3>
                
                <div className={styles.modalBody}>
                    <div className={styles.successIcon}>
                        ✓
                    </div>
                    <p className={styles.modalText}>
                        {message}
                    </p>
                </div>
                
                <div className={styles.modalActions}>
                    <button 
                        className={styles.modalConfirmButton}
                        onClick={handleClose}
                    >
                        확인
                    </button>
                </div>
            </div>
        </div>
    );
};

export default SuccessModal;