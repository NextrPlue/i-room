import React from 'react';
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
    if (!isOpen) return null;

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
        <div className={styles.modalOverlay} onClick={onCancel}>
            <div className={styles.modalContent} onClick={(e) => e.stopPropagation()}>
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
                        onClick={onCancel}
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