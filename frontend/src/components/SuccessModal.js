import React from 'react';
import styles from '../styles/SuccessModal.module.css';

const SuccessModal = ({ isOpen, title, message, onClose }) => {
    if (!isOpen) return null;

    return (
        <div className={styles.modalOverlay} onClick={onClose}>
            <div className={styles.modalContent} onClick={(e) => e.stopPropagation()}>
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
                        onClick={onClose}
                    >
                        확인
                    </button>
                </div>
            </div>
        </div>
    );
};

export default SuccessModal;