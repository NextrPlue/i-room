import React, {useState} from 'react';

const WorkerAddModal = ({isOpen, onClose, onSave}) => {
    // 테스트용 초기값 정의
    const initialFormData = {
        name: '김철수',
        department: '건설부',
        occupation: '현장 작업자',
        phone: '010-1234-5678',
        bloodType: 'A',
        jobTitle: '팀장',
        age: '35',
        weight: '75.5',
        height: '175.0',
        email: 'test@example.com',
        gender: 'MALE',
        password: 'password123!'
    };

    const [addForm, setAddForm] = useState(initialFormData);
    const [loading, setLoading] = useState(false);

    const handleInputChange = (e) => {
        const {name, value} = e.target;
        setAddForm(prev => ({...prev, [name]: value}));
    };

    const handleSave = async () => {
        if (loading) return;
        
        // 필수 필드 검증
        if (!addForm.name || !addForm.name.trim()) {
            alert('이름을 입력해주세요.');
            return;
        }

        if (!addForm.department || !addForm.department.trim()) {
            alert('부서를 입력해주세요.');
            return;
        }

        if (!addForm.occupation || !addForm.occupation.trim()) {
            alert('직종을 입력해주세요.');
            return;
        }

        if (!addForm.phone || !addForm.phone.trim()) {
            alert('연락처를 입력해주세요.');
            return;
        }

        // 연락처 형식 검증
        const phoneRegex = /^010-\d{4}-\d{4}$/;
        if (!phoneRegex.test(addForm.phone.trim())) {
            alert('연락처는 010-0000-0000 형식으로 입력해주세요.');
            return;
        }

        // 이메일 유효성 검사 (선택사항이지만 입력했다면 형식 검증)
        if (addForm.email && addForm.email.trim()) {
            const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
            if (!emailRegex.test(addForm.email.trim())) {
                alert('올바른 형식의 이메일 주소를 입력해주세요.');
                return;
            }
        }

        // 비밀번호 검증
        if (!addForm.password || !addForm.password.trim()) {
            alert('비밀번호를 입력해주세요.');
            return;
        }

        const pwd = addForm.password;
        if (pwd.length < 8 || pwd.length > 16) {
            alert('비밀번호는 8-16자여야 합니다.');
            return;
        }

        if (!/.*[a-zA-Z].*/.test(pwd)) {
            alert('영문자를 포함해야 합니다.');
            return;
        }

        if (!/.*\d.*/.test(pwd)) {
            alert('숫자를 포함해야 합니다.');
            return;
        }

        if (!/[#$%&*+,./:=?@[\\\]^_`{|}~!-]/.test(pwd)) {
            alert('특수문자를 포함해야 합니다.');
            return;
        }

        if (/[()<>'";}]/.test(pwd)) {
            alert('특수문자 ()<>"\';는 사용할 수 없습니다.');
            return;
        }

        // 나이 검증
        if (addForm.age && (isNaN(addForm.age))) {
            alert('나이는 숫자로 입력해주세요.');
            return;
        }

        // 키 검증
        if (addForm.height && (isNaN(addForm.height))) {
            alert('키는 숫자로 입력해주세요.');
            return;
        }

        // 몸무게 검증
        if (addForm.weight && (isNaN(addForm.weight))) {
            alert('몸무게는 숫자로 입력해주세요.');
            return;
        }

        try {
            setLoading(true);
            await onSave(addForm);
            // 폼을 예시값으로 초기화
            setAddForm(initialFormData);
        } catch (error) {
            console.error('근로자 등록 실패:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleClose = () => {
        // 폼을 예시값으로 초기화
        setAddForm(initialFormData);
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
        backgroundColor: loading ? '#f3f4f6' : '#fafafa',
        boxSizing: 'border-box',
        opacity: loading ? 0.6 : 1
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
                <div style={{padding: '24px'}}>
                    {/* 기본 정보 */}
                    <div style={{marginBottom: '32px'}}>
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
                        <div style={{display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '20px'}}>
                            <div>
                                <label style={labelStyle}>이름 *</label>
                                <input
                                    type="text"
                                    name="name"
                                    value={addForm.name}
                                    onChange={handleInputChange}
                                    disabled={loading}
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
                                    disabled={loading}
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
                                    disabled={loading}
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
                                    disabled={loading}
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
                        <div style={{display: 'grid', gridTemplateColumns: '1fr', gap: '20px'}}>
                            <div>
                                <label style={labelStyle}>성별</label>
                                <select
                                    name="gender"
                                    value={addForm.gender}
                                    onChange={handleInputChange}
                                    disabled={loading}
                                    style={{...inputStyle, cursor: loading ? 'not-allowed' : 'pointer'}}
                                >
                                    <option value="MALE">남성</option>
                                    <option value="FEMALE">여성</option>
                                </select>
                            </div>
                        </div>
                    </div>

                    {/* 연락 정보 */}
                    <div style={{marginBottom: '32px'}}>
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
                        <div style={{display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '20px'}}>
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
                                    disabled={loading}
                                    style={{...inputStyle, cursor: loading ? 'not-allowed' : 'pointer'}}
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
                    <div style={{marginBottom: '32px'}}>
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
                        <div style={{display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: '20px'}}>
                            <div>
                                <label style={labelStyle}>나이</label>
                                <input
                                    type="number"
                                    name="age"
                                    value={addForm.age}
                                    onChange={handleInputChange}
                                    disabled={loading}
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
                            disabled={loading}
                            style={{
                                padding: '10px 20px',
                                border: 'none',
                                borderRadius: '6px',
                                background: loading ? '#9ca3af' : 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                                color: 'white',
                                cursor: loading ? 'not-allowed' : 'pointer',
                                fontSize: '14px'
                            }}
                        >
                            {loading ? '등록 중...' : '등록하기'}
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default WorkerAddModal;