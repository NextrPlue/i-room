import React, { useState, useEffect } from 'react';
import styles from '../styles/ConfirmModal.module.css';

const ConfirmModal = ({ 
    isOpen, 
    title, 
    message, 
    targetName, 
    onConfirm, 
    onCancel, 
    loading = false,
    confirmButtonText = '확인',
    cancelButtonText = '취소',
    loadingText = '처리 중...',
    type = 'default', // 'default', 'warning', 'danger'
    icon = null
}) => {
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

    const handleCancel = () => {
        if (!loading) {
            setIsClosing(true);
            setTimeout(() => {
                onCancel();
            }, 100);
        }
    };

    const handleOverlayClick = (e) => {
        if (e.target === e.currentTarget) {
            handleCancel();
        }
    };

    if (!shouldRender) return null;

    // 타입에 따른 기본 아이콘 설정
    const getDefaultIcon = () => {
        switch (type) {
            case 'warning':
                return '⚠️';
            case 'danger':
                return '⚠️';
            default:
                return '❓';
        }
    };

    const displayIcon = icon || getDefaultIcon();

    return (
        <div className={`${styles.modalOverlay} ${isClosing ? styles.closing : ''}`} onClick={handleOverlayClick}>
            <div className={`${styles.modalContent} ${isClosing ? styles.closing : ''}`} onClick={(e) => e.stopPropagation()}>
                <h3 className={styles.modalTitle}>{title}</h3>
                
                <div className={styles.modalBody}>
                    <div className={styles.modalIcon}>
                        {displayIcon}
                    </div>
                    <p className={styles.modalText}>
                        {targetName ? (
                            <>정말로 <strong>{targetName}</strong> {message}</>
                        ) : (
                            message
                        )}
                    </p>
                    {type === 'danger' && (
                        <p className={styles.warningText}>
                            이 작업은 되돌릴 수 없습니다.
                        </p>
                    )}
                </div>
                
                <div className={styles.modalActions}>
                    <button 
                        className={styles.modalCancelButton}
                        onClick={handleCancel}
                        disabled={loading}
                    >
                        {cancelButtonText}
                    </button>
                    <button 
                        className={`${styles.modalConfirmButton} ${styles[type]}`}
                        onClick={onConfirm}
                        disabled={loading}
                    >
                        {loading ? loadingText : confirmButtonText}
                    </button>
                </div>
            </div>
        </div>
    );
};

export default ConfirmModal;