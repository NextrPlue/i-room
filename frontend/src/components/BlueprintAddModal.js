import React from 'react';
import styles from '../styles/Blueprint.module.css';

const BlueprintAddModal = ({
    showModal,
    onClose,
    onSubmit,
    uploadForm,
    onFormChange,
    onFileSelect,
    uploadPreview,
    uploading,
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
                    <h2>새 도면 업로드</h2>
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
                            id="blueprintFile"
                            accept="image/*"
                            onChange={onFileSelect}
                            className={styles.fileInput}
                        />
                        <label htmlFor="blueprintFile" className={styles.fileLabel}>
                            {uploadPreview ? (
                                <img
                                    src={uploadPreview}
                                    alt="업로드 미리보기"
                                    className={styles.uploadPreview}
                                />
                            ) : (
                                <div className={styles.fileDropArea}>
                                    <div className={styles.uploadIcon}>📁</div>
                                    <p>도면 이미지를 선택하세요</p>
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
                                value={uploadForm.floor}
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
                                value={uploadForm.width}
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
                                value={uploadForm.height}
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
                            disabled={uploading}
                        >
                            취소
                        </button>
                        <button
                            type="submit"
                            className={styles.submitButton}
                            disabled={uploading || !uploadForm.file}
                        >
                            {uploading ? '업로드 중...' : '업로드'}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default BlueprintAddModal;