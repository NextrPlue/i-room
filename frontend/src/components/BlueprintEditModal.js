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
                        <div className={styles.formGroup} style={{gridColumn: '1 / -1'}}>
                            <label>도면 이름</label>
                            <input
                                type="text"
                                value={editForm.name || ''}
                                onChange={(e) => onFormChange('name', e.target.value)}
                                placeholder="도면 이름을 입력하세요"
                                required
                            />
                        </div>
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

                    {/* GPS 좌표 입력 */}
                    <div className={styles.coordinateSection}>
                        <h3>도면 GPS 좌표</h3>
                        <div className={styles.coordinateRows}>
                            <div className={styles.coordinateRow}>
                                <label>왼쪽 위</label>
                                <input
                                    type="number"
                                    step="0.000001"
                                    value={editForm.topLeft?.lat || 37.5675}
                                    onChange={(e) => onFormChange('topLeft.lat', e.target.value)}
                                    placeholder="위도"
                                    required
                                />
                                <input
                                    type="number"
                                    step="0.000001"
                                    value={editForm.topLeft?.lon || 126.9770}
                                    onChange={(e) => onFormChange('topLeft.lon', e.target.value)}
                                    placeholder="경도"
                                    required
                                />
                            </div>
                            <div className={styles.coordinateRow}>
                                <label>오른쪽 위</label>
                                <input
                                    type="number"
                                    step="0.000001"
                                    value={editForm.topRight?.lat || 37.5675}
                                    onChange={(e) => onFormChange('topRight.lat', e.target.value)}
                                    placeholder="위도"
                                    required
                                />
                                <input
                                    type="number"
                                    step="0.000001"
                                    value={editForm.topRight?.lon || 126.9780}
                                    onChange={(e) => onFormChange('topRight.lon', e.target.value)}
                                    placeholder="경도"
                                    required
                                />
                            </div>
                            <div className={styles.coordinateRow}>
                                <label>오른쪽 아래</label>
                                <input
                                    type="number"
                                    step="0.000001"
                                    value={editForm.bottomRight?.lat || 37.5665}
                                    onChange={(e) => onFormChange('bottomRight.lat', e.target.value)}
                                    placeholder="위도"
                                    required
                                />
                                <input
                                    type="number"
                                    step="0.000001"
                                    value={editForm.bottomRight?.lon || 126.9780}
                                    onChange={(e) => onFormChange('bottomRight.lon', e.target.value)}
                                    placeholder="경도"
                                    required
                                />
                            </div>
                            <div className={styles.coordinateRow}>
                                <label>왼쪽 아래</label>
                                <input
                                    type="number"
                                    step="0.000001"
                                    value={editForm.bottomLeft?.lat || 37.5665}
                                    onChange={(e) => onFormChange('bottomLeft.lat', e.target.value)}
                                    placeholder="위도"
                                    required
                                />
                                <input
                                    type="number"
                                    step="0.000001"
                                    value={editForm.bottomLeft?.lon || 126.9770}
                                    onChange={(e) => onFormChange('bottomLeft.lon', e.target.value)}
                                    placeholder="경도"
                                    required
                                />
                            </div>
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
                            disabled={editing || !editForm.name?.trim()}
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