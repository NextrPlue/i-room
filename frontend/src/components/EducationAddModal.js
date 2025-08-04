import React, { useState } from 'react';

const EducationAddModal = ({ isOpen, onClose, onSave, workerId, workerName }) => {
    const [addForm, setAddForm] = useState({
        name: '',
        eduDate: '',
        certUrl: ''
    });

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setAddForm(prev => ({ ...prev, [name]: value }));
    };

    const handleSave = () => {
        // workerId를 포함한 전체 데이터 구성
        const educationData = {
            workerId: workerId,
            name: addForm.name,
            eduDate: addForm.eduDate,
            certUrl: addForm.certUrl
        };
        
        onSave(educationData);
        
        // 폼 초기화
        setAddForm({
            name: '',
            eduDate: '',
            certUrl: ''
        });
    };

    const handleClose = () => {
        // 폼 초기화
        setAddForm({
            name: '',
            eduDate: '',
            certUrl: ''
        });
        onClose();
    };

    if (!isOpen) return null;

    const modalStyle = {
        position: 'fixed',
        top: 0,
        left: 0,
        width: '100%',
        height: '100%',
        backgroundColor: 'rgba(0, 0, 0, 0.5)',
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        zIndex: 1000
    };

    const modalContentStyle = {
        backgroundColor: 'white',
        borderRadius: '12px',
        padding: '32px',
        width: '500px',
        maxWidth: '90%',
        maxHeight: '80%',
        overflow: 'auto'
    };

    const headerStyle = {
        fontSize: '20px',
        fontWeight: '700',
        color: '#1F2937',
        marginBottom: '24px',
        textAlign: 'center'
    };

    const workerInfoStyle = {
        backgroundColor: '#F3F4F6',
        padding: '12px 16px',
        borderRadius: '8px',
        marginBottom: '24px',
        fontSize: '14px',
        color: '#6B7280'
    };

    const formRowStyle = {
        marginBottom: '20px'
    };

    const labelStyle = {
        display: 'block',
        fontSize: '14px',
        fontWeight: '600',
        color: '#374151',
        marginBottom: '8px'
    };

    const inputStyle = {
        width: '100%',
        padding: '12px',
        border: '1px solid #D1D5DB',
        borderRadius: '8px',
        fontSize: '14px',
        boxSizing: 'border-box'
    };

    const buttonContainerStyle = {
        display: 'flex',
        justifyContent: 'flex-end',
        gap: '12px',
        marginTop: '32px'
    };

    const cancelButtonStyle = {
        background: '#6B7280',
        color: 'white',
        border: 'none',
        borderRadius: '8px',
        padding: '12px 20px',
        fontSize: '14px',
        fontWeight: '600',
        cursor: 'pointer'
    };

    const saveButtonStyle = {
        background: '#1F2937',
        color: 'white',
        border: 'none',
        borderRadius: '8px',
        padding: '12px 20px',
        fontSize: '14px',
        fontWeight: '600',
        cursor: 'pointer'
    };

    return (
        <div style={modalStyle} onClick={handleClose}>
            <div style={modalContentStyle} onClick={(e) => e.stopPropagation()}>
                <h2 style={headerStyle}>안전교육 등록</h2>
                
                <div style={workerInfoStyle}>
                    <strong>대상 근로자:</strong> {workerName || `근로자 ID: ${workerId}`}
                </div>

                <div style={formRowStyle}>
                    <label style={labelStyle}>교육명 *</label>
                    <input
                        type="text"
                        name="name"
                        value={addForm.name}
                        onChange={handleInputChange}
                        placeholder="예: 건설현장 기초 안전 교육"
                        style={inputStyle}
                        required
                    />
                </div>

                <div style={formRowStyle}>
                    <label style={labelStyle}>교육 일시 *</label>
                    <input
                        type="date"
                        name="eduDate"
                        value={addForm.eduDate}
                        onChange={handleInputChange}
                        style={inputStyle}
                        required
                    />
                </div>

                <div style={formRowStyle}>
                    <label style={labelStyle}>수료증 URL</label>
                    <input
                        type="url"
                        name="certUrl"
                        value={addForm.certUrl}
                        onChange={handleInputChange}
                        placeholder="https://example.com/certificate/123.pdf"
                        style={inputStyle}
                    />
                    <div style={{ fontSize: '12px', color: '#6B7280', marginTop: '4px' }}>
                        수료증 URL이 있으면 이수완료로 표시됩니다.
                    </div>
                </div>

                <div style={buttonContainerStyle}>
                    <button style={cancelButtonStyle} onClick={handleClose}>
                        취소
                    </button>
                    <button 
                        style={saveButtonStyle} 
                        onClick={handleSave}
                        disabled={!addForm.name || !addForm.eduDate}
                    >
                        등록
                    </button>
                </div>
            </div>
        </div>
    );
};

export default EducationAddModal;