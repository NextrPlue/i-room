import React from 'react';
import styles from '../styles/Blueprint.module.css';

const BlueprintEditModal = ({
    showModal,
    onClose,
    onSubmit,
    editForm,
    onFormChange,
    onFileSelect,
    editPreview,
    editing,
    error
}) => {
    if (!showModal) return null;

    const handleSubmit = (e) => {
        e.preventDefault();
        onSubmit();
    };

    return (
        <div className={styles.uploadModal}>
            <div className={styles.uploadModalContent}>
                <div className={styles.uploadModalHeader}>
                    <h2>도면 수정</h2>
                    <button
                        className={styles.closeButton}
                        onClick={onClose}
                        type="button"
                    >
                        ✕
                    </button>
                </div>

                <form onSubmit={handleSubmit} className={styles.uploadForm}>
                    {/* 파일 선택 영역 */}
                    <div className={styles.fileUploadArea}>
                        <input
                            type="file"
                            id="editBlueprintFile"
                            accept="image/*"
                            onChange={onFileSelect}
                            className={styles.fileInput}
                        />
                        <label htmlFor="editBlueprintFile" className={styles.fileLabel}>
                            {editPreview ? (
                                <img
                                    src={typeof editPreview === 'string' ? editPreview : ''}
                                    alt="수정 미리보기"
                                    className={styles.uploadPreview}
                                />
                            ) : (
                                <div className={styles.fileDropArea}>
                                    <div className={styles.uploadIcon}>📁</div>
                                    <p>새 도면 이미지를 선택하세요 (선택사항)</p>
                                    <span>PNG, JPG 형식 (최대 10MB)</span>
                                </div>
                            )}
                        </label>
                    </div>

                    {/* 도면 정보 입력 */}
                    <div className={styles.formGrid}>
                        <div className={styles.formGroup}>
                            <label>층수</label>
                            <input
                                type="number"
                                value={editForm.floor}
                                onChange={(e) => onFormChange('floor', e.target.value)}
                                min="1"
                                required
                            />
                        </div>
                        <div className={styles.formGroup}>
                            <label>가로 (m)</label>
                            <input
                                type="number"
                                step="0.1"
                                value={editForm.width}
                                onChange={(e) => onFormChange('width', e.target.value)}
                                min="0.1"
                                required
                            />
                        </div>
                        <div className={styles.formGroup}>
                            <label>세로 (m)</label>
                            <input
                                type="number"
                                step="0.1"
                                value={editForm.height}
                                onChange={(e) => onFormChange('height', e.target.value)}
                                min="0.1"
                                required
                            />
                        </div>
                    </div>

                    {/* 에러 메시지 */}
                    {error && (
                        <div className={styles.errorMessage}>
                            {error}
                        </div>
                    )}

                    {/* 버튼 영역 */}
                    <div className={styles.formButtons}>
                        <button
                            type="button"
                            onClick={onClose}
                            className={styles.cancelButton}
                            disabled={editing}
                        >
                            취소
                        </button>
                        <button
                            type="submit"
                            className={styles.submitButton}
                            disabled={editing}
                        >
                            {editing ? '수정 중...' : '수정 완료'}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default BlueprintEditModal;