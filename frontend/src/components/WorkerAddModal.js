import React, { useState } from 'react';

const WorkerAddModal = ({ isOpen, onClose, onSave }) => {
    const [addForm, setAddForm] = useState({
        name: '',
        department: '',
        occupation: '',
        phone: '',
        bloodType: '',
        jobTitle: '',
        age: '',
        weight: '',
        height: '',
        email: '',
        gender: 'MALE',
        password: ''
    });

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setAddForm(prev => ({ ...prev, [name]: value }));
    };

    const handleSave = () => {
        onSave(addForm);
        // 폼 초기화
        setAddForm({
            name: '',
            department: '',
            occupation: '',
            phone: '',
            bloodType: '',
            jobTitle: '',
            age: '',
            weight: '',
            height: '',
            email: '',
            gender: 'MALE',
            password: ''
        });
    };

    const handleClose = () => {
        // 폼 초기화
        setAddForm({
            name: '',
            department: '',
            occupation: '',
            phone: '',
            bloodType: '',
            jobTitle: '',
            age: '',
            weight: '',
            height: '',
            email: '',
            gender: 'MALE',
            password: ''
        });
        onClose();
    };

    if (!isOpen) return null;

    const inputStyle = {
        width: 'calc(100% - 2px)',
        padding: '14px 16px',
        border: '1px solid #d1d5db',
        borderRadius: '8px',
        fontSize: '14px',
        outline: 'none',
        backgroundColor: '#fafafa',
        boxSizing: 'border-box'
    };

    const labelStyle = {
        display: 'block',
        marginBottom: '8px',
        fontSize: '14px',
        fontWeight: '500',
        color: '#374151'
    };

    return (
        <div style={{
            position: 'fixed',
            top: 0,
            left: 0,
            right: 0,
            bottom: 0,
            backgroundColor: 'rgba(0, 0, 0, 0.5)',
            display: 'flex',
            justifyContent: 'center',
            alignItems: 'center',
            zIndex: 1000
        }}>
            <div style={{
                backgroundColor: 'white',
                borderRadius: '12px',
                width: '550px',
                maxHeight: '90vh',
                overflowY: 'auto',
                boxShadow: '0 10px 40px rgba(0, 0, 0, 0.2)'
            }}>
                {/* 헤더 */}
                <div style={{
                    background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                    padding: '20px 24px',
                    borderRadius: '12px 12px 0 0',
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center'
                }}>
                    <div>
                        <h2 style={{
                            fontSize: '20px',
                            fontWeight: '600',
                            color: 'white',
                            margin: '0 0 4px 0'
                        }}>
                            신규 근로자 등록
                        </h2>
                        <p style={{
                            fontSize: '14px',
                            color: 'rgba(255, 255, 255, 0.8)',
                            margin: 0
                        }}>
                            새로운 근로자의 정보를 입력해주세요
                        </p>
                    </div>
                    <button
                        onClick={handleClose}
                        style={{
                            background: 'rgba(255, 255, 255, 0.2)',
                            border: 'none',
                            borderRadius: '6px',
                            width: '32px',
                            height: '32px',
                            fontSize: '18px',
                            cursor: 'pointer',
                            color: 'white'
                        }}
                    >
                        ×
                    </button>
                </div>

                {/* 폼 내용 */}
                <div style={{ padding: '24px' }}>
                    {/* 기본 정보 */}
                    <div style={{ marginBottom: '32px' }}>
                        <h3 style={{
                            fontSize: '16px',
                            fontWeight: '600',
                            color: '#1f2937',
                            margin: '0 0 20px 0',
                            borderLeft: '3px solid #667eea',
                            paddingLeft: '8px'
                        }}>
                            기본 정보
                        </h3>
                        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '20px' }}>
                            <div>
                                <label style={labelStyle}>이름 *</label>
                                <input
                                    type="text"
                                    name="name"
                                    value={addForm.name}
                                    onChange={handleInputChange}
                                    style={inputStyle}
                                />
                            </div>
                            <div>
                                <label style={labelStyle}>부서 *</label>
                                <input
                                    type="text"
                                    name="department"
                                    value={addForm.department}
                                    onChange={handleInputChange}
                                    style={inputStyle}
                                />
                            </div>
                            <div>
                                <label style={labelStyle}>직종 *</label>
                                <input
                                    type="text"
                                    name="occupation"
                                    value={addForm.occupation}
                                    onChange={handleInputChange}
                                    style={inputStyle}
                                />
                            </div>
                            <div>
                                <label style={labelStyle}>직책</label>
                                <input
                                    type="text"
                                    name="jobTitle"
                                    value={addForm.jobTitle}
                                    onChange={handleInputChange}
                                    style={inputStyle}
                                />
                            </div>
                            <div>
                                <label style={labelStyle}>이메일</label>
                                <input
                                    type="email"
                                    name="email"
                                    value={addForm.email}
                                    onChange={handleInputChange}
                                    placeholder="example@company.com"
                                    style={inputStyle}
                                />
                            </div>
                            <div>
                                <label style={labelStyle}>비밀번호 *</label>
                                <input
                                    type="password"
                                    name="password"
                                    value={addForm.password}
                                    onChange={handleInputChange}
                                    placeholder="비밀번호를 입력하세요"
                                    style={inputStyle}
                                />
                            </div>
                        </div>
                        <div style={{ display: 'grid', gridTemplateColumns: '1fr', gap: '20px' }}>
                            <div>
                                <label style={labelStyle}>성별</label>
                                <select
                                    name="gender"
                                    value={addForm.gender}
                                    onChange={handleInputChange}
                                    style={{...inputStyle, cursor: 'pointer'}}
                                >
                                    <option value="MALE">남성</option>
                                    <option value="FEMALE">여성</option>
                                </select>
                            </div>
                        </div>
                    </div>

                    {/* 연락 정보 */}
                    <div style={{ marginBottom: '32px' }}>
                        <h3 style={{
                            fontSize: '16px',
                            fontWeight: '600',
                            color: '#1f2937',
                            margin: '0 0 16px 0',
                            borderLeft: '3px solid #667eea',
                            paddingLeft: '8px'
                        }}>
                            연락 정보
                        </h3>
                        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '20px' }}>
                            <div>
                                <label style={labelStyle}>연락처 *</label>
                                <input
                                    type="text"
                                    name="phone"
                                    value={addForm.phone}
                                    onChange={handleInputChange}
                                    placeholder="010-1234-5678"
                                    style={inputStyle}
                                />
                            </div>
                            <div>
                                <label style={labelStyle}>혈액형</label>
                                <select
                                    name="bloodType"
                                    value={addForm.bloodType}
                                    onChange={handleInputChange}
                                    style={{...inputStyle, cursor: 'pointer'}}
                                >
                                    <option value="">혈액형 선택</option>
                                    <option value="A">A형</option>
                                    <option value="B">B형</option>
                                    <option value="O">O형</option>
                                    <option value="AB">AB형</option>
                                </select>
                            </div>
                        </div>
                    </div>

                    {/* 신체 정보 */}
                    <div style={{ marginBottom: '32px' }}>
                        <h3 style={{
                            fontSize: '16px',
                            fontWeight: '600',
                            color: '#1f2937',
                            margin: '0 0 16px 0',
                            borderLeft: '3px solid #667eea',
                            paddingLeft: '8px'
                        }}>
                            신체 정보
                        </h3>
                        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: '20px' }}>
                            <div>
                                <label style={labelStyle}>나이</label>
                                <input
                                    type="number"
                                    name="age"
                                    value={addForm.age}
                                    onChange={handleInputChange}
                                    style={inputStyle}
                                />
                            </div>
                            <div>
                                <label style={labelStyle}>몸무게 (kg)</label>
                                <input
                                    type="number"
                                    name="weight"
                                    value={addForm.weight}
                                    onChange={handleInputChange}
                                    step="0.1"
                                    style={inputStyle}
                                />
                            </div>
                            <div>
                                <label style={labelStyle}>키 (cm)</label>
                                <input
                                    type="number"
                                    name="height"
                                    value={addForm.height}
                                    onChange={handleInputChange}
                                    step="0.1"
                                    style={inputStyle}
                                />
                            </div>
                        </div>
                    </div>

                    {/* 버튼 */}
                    <div style={{
                        display: 'flex',
                        justifyContent: 'flex-end',
                        gap: '12px',
                        paddingTop: '20px',
                        borderTop: '1px solid #e5e7eb'
                    }}>
                        <button
                            onClick={handleClose}
                            style={{
                                padding: '10px 20px',
                                border: '1px solid #d1d5db',
                                borderRadius: '6px',
                                backgroundColor: 'white',
                                color: '#374151',
                                cursor: 'pointer',
                                fontSize: '14px'
                            }}
                        >
                            취소
                        </button>
                        <button
                            onClick={handleSave}
                            style={{
                                padding: '10px 20px',
                                border: 'none',
                                borderRadius: '6px',
                                background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                                color: 'white',
                                cursor: 'pointer',
                                fontSize: '14px'
                            }}
                        >
                            등록하기
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default WorkerAddModal;