import React, { useState, useEffect } from 'react';
import styles from '../styles/Settings.module.css';
import { userAPI } from '../api/api';

const SettingsPage = () => {
    const [myAccount, setMyAccount] = useState(null);

    const [isEditing, setIsEditing] = useState(false);
    const [editForm, setEditForm] = useState({
        name: '',
        email: '',
        phone: ''
    });

    const [passwordForm, setPasswordForm] = useState({
        currentPassword: '',
        newPassword: '',
        confirmPassword: ''
    });

    // 비밀번호 폼 검증 상태
    const [passwordValidation, setPasswordValidation] = useState({
        currentPassword: { isValid: null, message: '' },
        newPassword: { isValid: null, message: '' },
        confirmPassword: { isValid: null, message: '' }
    });

    // 관리자 목록
    const [admins, setAdmins] = useState([]);

    // 권한 변경 모달 상태
    const [roleChangeModal, setRoleChangeModal] = useState({
        isOpen: false,
        adminId: null,
        adminName: '',
        currentRole: '',
        newRole: ''
    });

    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    const isSuperAdmin = () => {
        return myAccount && (myAccount.role === 'SUPER_ADMIN' || myAccount.role === 'Super Admin');
    };

    // 권한별 CSS 클래스 이름 매핑
    const getRoleClass = (role) => {
        switch(role) {
            case 'SUPER_ADMIN':
                return 'superAdmin';
            case 'ADMIN':
                return 'admin';
            case 'READER':
                return 'reader';
            default:
                return 'reader';
        }
    };

    useEffect(() => {
        const loadData = async () => {
            await fetchMyAccountInfo();
            await fetchAdminData();
        };
        loadData().catch(console.error);
    }, []);

    const fetchMyAccountInfo = async () => {
        try {
            setLoading(true);
            setError(null);

            const response = await userAPI.getMyInfo();
            if (response.data) {
                const accountData = {
                    id: response.data.id,
                    username: response.data.email,
                    name: response.data.name,
                    phone: response.data.phone,
                    role: response.data.role
                };
                setMyAccount(accountData);
                setEditForm({
                    name: response.data.name,
                    email: response.data.email,
                    phone: response.data.phone
                });
            }
        } catch (err) {
            console.error('내 계정 정보 조회 실패:', err);
            setError(err.message || '계정 정보를 불러오는데 실패했습니다.');
        } finally {
            setLoading(false);
        }
    };

    const fetchAdminData = async () => {
        try {
            setLoading(true);
            setError(null);

            const response = await userAPI.getAdmins({
                page: 0,
                size: 5
            });

            if (response.data && response.data.content) {
                const adminList = response.data.content.map(admin => {
                    let displayRole;
                    let roleType;
                    
                    switch(admin.role) {
                        case 'SUPER_ADMIN':
                            displayRole = 'Super Admin';
                            roleType = 'superAdmin';
                            break;
                        case 'ADMIN':
                            displayRole = 'Admin';
                            roleType = 'admin';
                            break;
                        case 'READER':
                            displayRole = 'Reader';
                            roleType = 'reader';
                            break;
                        default:
                            displayRole = admin.role;
                            roleType = admin.role.toLowerCase();
                    }
                    
                    return {
                        id: admin.id,
                        name: admin.name,
                        email: admin.email,
                        phone: admin.phone,
                        role: displayRole,
                        roleType: roleType
                    };
                });
                setAdmins(adminList);
            }
        } catch (err) {
            console.error('관리자 데이터 조회 실패:', err);
            setError(err.message || '관리자 목록을 불러오는데 실패했습니다.');
        } finally {
            setLoading(false);
        }
    };

    // 비밀번호 필드별 검증
    const validatePasswordField = (field, value) => {
        let isValid = null;
        let message = '';

        switch (field) {
            case 'currentPassword': {
                const v = String(value);
                if (v.length === 0) {
                    isValid = null;
                } else if (v.length > 0) {
                    isValid = true;
                    message = '';
                }
                break;
            }

            case 'newPassword': {
                const v = String(value);
                if (v.length === 0) {
                    isValid = null;
                } else if (v.length < 8 || v.length > 16) {
                    isValid = false;
                    message = '비밀번호는 8-16자여야 합니다.';
                } else if (!/.*[a-zA-Z].*/.test(v)) {
                    isValid = false;
                    message = '영문자를 포함해야 합니다.';
                } else if (!/.*\d.*/.test(v)) {
                    isValid = false;
                    message = '숫자를 포함해야 합니다.';
                } else if (!/[#$%&*+,./:=?@[\\\]^_`{|}~!-]/.test(v)) {
                    isValid = false;
                    message = '특수문자를 포함해야 합니다.';
                } else if (/[()<>'";}]/.test(v)) {
                    isValid = false;
                    message = '특수문자 ()<>"\';는 사용할 수 없습니다.';
                } else {
                    isValid = true;
                    message = '사용 가능한 비밀번호입니다.';
                }
                break;
            }

            case 'confirmPassword': {
                const v = String(value);
                const newPassword = passwordForm.newPassword;
                if (v.length === 0) {
                    isValid = null;
                } else if (v !== newPassword) {
                    isValid = false;
                    message = '새 비밀번호와 일치하지 않습니다.';
                } else {
                    isValid = true;
                    message = '비밀번호가 일치합니다.';
                }
                break;
            }

            default:
                break;
        }

        setPasswordValidation(prev => ({
            ...prev,
            [field]: { isValid, message }
        }));
    };

    const handlePasswordInputChange = (field, value) => {
        setPasswordForm(prev => ({
            ...prev,
            [field]: value
        }));
        
        // 실시간 검증
        validatePasswordField(field, value);
        
        // 비밀번호 확인 필드가 있을 때 새 비밀번호 변경 시 비밀번호 확인도 다시 검증
        if (field === 'newPassword' && passwordForm.confirmPassword) {
            setTimeout(() => {
                validatePasswordField('confirmPassword', passwordForm.confirmPassword);
            }, 0);
        }
    };

    const handleEditFormChange = (field, value) => {
        setEditForm(prev => ({
            ...prev,
            [field]: value
        }));
    };

    const handleEditProfile = () => {
        setIsEditing(true);
    };

    const handleCancelEdit = () => {
        setIsEditing(false);
        if (myAccount) {
            setEditForm({
                name: myAccount.name,
                email: myAccount.username,
                phone: myAccount.phone
            });
        }
    };

    const handleSaveEdit = async () => {
        try {
            setLoading(true);
            setError(null);

            const response = await userAPI.updateMyInfo(editForm);
            
            if (response.data) {
                const updatedAccount = {
                    ...myAccount,
                    name: response.data.name,
                    username: response.data.email,
                    phone: response.data.phone
                };
                setMyAccount(updatedAccount);
                setIsEditing(false);
                alert('정보가 성공적으로 수정되었습니다.');
            }
        } catch (err) {
            console.error('정보 수정 실패:', err);
            alert(err.message || '정보 수정에 실패했습니다.');
        } finally {
            setLoading(false);
        }
    };

    const handleChangePassword = async () => {
        if (!passwordForm.currentPassword || !passwordForm.newPassword || !passwordForm.confirmPassword) {
            alert('모든 필드를 입력해주세요.');
            return;
        }

        // 새 비밀번호 유효성 검증
        if (passwordValidation.newPassword.isValid === false) {
            alert(passwordValidation.newPassword.message);
            return;
        }

        if (passwordForm.newPassword !== passwordForm.confirmPassword) {
            alert('새 비밀번호가 일치하지 않습니다.');
            return;
        }

        try {
            setLoading(true);
            setError(null);

            await userAPI.changePassword({
                password: passwordForm.currentPassword,
                newPassword: passwordForm.newPassword
            });

            alert('비밀번호가 성공적으로 변경되었습니다.');

            // 폼 및 검증 상태 초기화
            setPasswordForm({
                currentPassword: '',
                newPassword: '',
                confirmPassword: ''
            });
            setPasswordValidation({
                currentPassword: { isValid: null, message: '' },
                newPassword: { isValid: null, message: '' },
                confirmPassword: { isValid: null, message: '' }
            });
        } catch (err) {
            console.error('비밀번호 변경 실패:', err);
            alert(err.message || '비밀번호 변경에 실패했습니다.');
        } finally {
            setLoading(false);
        }
    };

    // 권한 변경 모달 열기
    const handleEditAdmin = (adminId) => {
        if (!isSuperAdmin()) {
            alert('관리자 권한 변경은 Super Admin만 가능합니다.');
            return;
        }

        const admin = admins.find(a => a.id === adminId);
        if (!admin) return;

        let currentRole;
        switch(admin.roleType) {
            case 'superAdmin':
                currentRole = 'SUPER_ADMIN';
                break;
            case 'admin':
                currentRole = 'ADMIN';
                break;
            case 'reader':
                currentRole = 'READER';
                break;
            default:
                currentRole = 'READER';
        }

        setRoleChangeModal({
            isOpen: true,
            adminId: adminId,
            adminName: admin.name,
            currentRole: currentRole,
            newRole: currentRole
        });
    };

    // 권한 변경 모달 닫기
    const handleCloseRoleModal = () => {
        setRoleChangeModal({
            isOpen: false,
            adminId: null,
            adminName: '',
            currentRole: '',
            newRole: ''
        });
    };

    // 권한 변경 실행
    const handleConfirmRoleChange = async () => {
        const { adminId, adminName, currentRole, newRole } = roleChangeModal;
        
        if (newRole === currentRole) {
            handleCloseRoleModal();
            return;
        }

        try {
            setLoading(true);
            const response = await userAPI.changeAdminRole(adminId, newRole);
            
            if (response.data) {
                let displayRole, roleType;
                switch(newRole) {
                    case 'SUPER_ADMIN':
                        displayRole = 'Super Admin';
                        roleType = 'superAdmin';
                        break;
                    case 'ADMIN':
                        displayRole = 'Admin';
                        roleType = 'admin';
                        break;
                    case 'READER':
                        displayRole = 'Reader';
                        roleType = 'reader';
                        break;
                    default:
                        displayRole = newRole;
                        roleType = newRole.toLowerCase();
                }

                setAdmins(prev => prev.map(a => 
                    a.id === adminId
                        ? { ...a, role: displayRole, roleType: roleType }
                        : a
                ));
                
                alert(`${adminName}의 권한이 ${displayRole}으로 변경되었습니다.`);
                handleCloseRoleModal();
            }
        } catch (err) {
            console.error('권한 변경 실패:', err);
            alert(err.message || '권한 변경에 실패했습니다.');
        } finally {
            setLoading(false);
        }
    };

    // 관리자 삭제
    const handleDeleteAdmin = async (adminId) => {
        if (!isSuperAdmin()) {
            alert('관리자 삭제는 Super Admin만 가능합니다.');
            return;
        }

        const admin = admins.find(a => a.id === adminId);
        if (!admin) return;

        if (window.confirm(`정말로 ${admin.name} 관리자를 삭제하시겠습니까?`)) {
            try {
                setLoading(true);
                await userAPI.deleteAdmin(adminId);

                setAdmins(prev => prev.filter(a => a.id !== adminId));
                alert(`${admin.name} 관리자가 삭제되었습니다.`);
            } catch (err) {
                console.error('관리자 삭제 실패:', err);
                alert(err.message || '관리자 삭제에 실패했습니다.');
            } finally {
                setLoading(false);
            }
        }
    };

    return (
        <div className={styles.page}>
            {/* 페이지 헤더 */}
            <header className={styles.pageHeader}>
                <h1 className={styles.pageTitle}>계정 관리</h1>
            </header>

            {/* 메인 컨텐츠 */}
            <div className={styles.contentSection}>
                {/* 왼쪽: 내 계정 관리 */}
                <section className={styles.myAccountSection}>
                    <h2 className={styles.sectionTitle}>내 계정 관리</h2>

                    {/* 내 계정 정보 섹션 */}
                    <div className={styles.accountInfoSection}>
                        <h3 className={styles.subSectionTitle}>내 계정 정보</h3>

                        {myAccount ? (
                            <div className={styles.profileInfo}>
                                <div className={styles.infoGroup}>
                                    <span className={styles.infoLabel}>아이디</span>
                                    {isEditing ? (
                                        <input
                                            type="email"
                                            className={styles.editInput}
                                            value={editForm.email}
                                            onChange={(e) => handleEditFormChange('email', e.target.value)}
                                        />
                                    ) : (
                                        <div className={styles.infoValue}>{myAccount.username}</div>
                                    )}
                                </div>

                                <div className={styles.infoGroup}>
                                    <span className={styles.infoLabel}>이름</span>
                                    {isEditing ? (
                                        <input
                                            type="text"
                                            className={styles.editInput}
                                            value={editForm.name}
                                            onChange={(e) => handleEditFormChange('name', e.target.value)}
                                        />
                                    ) : (
                                        <div className={styles.infoValue}>{myAccount.name}</div>
                                    )}
                                </div>

                                <div className={styles.infoGroup}>
                                    <span className={styles.infoLabel}>연락처</span>
                                    {isEditing ? (
                                        <input
                                            type="tel"
                                            className={styles.editInput}
                                            value={editForm.phone}
                                            onChange={(e) => handleEditFormChange('phone', e.target.value)}
                                        />
                                    ) : (
                                        <div className={styles.infoValue}>{myAccount.phone}</div>
                                    )}
                                </div>

                                <div className={styles.infoGroup}>
                                    <span className={styles.infoLabel}>권한</span>
                                    <div className={styles.infoValue}>{myAccount.role}</div>
                                </div>
                            </div>
                        ) : (
                            <div className={styles.loadingState}>
                                계정 정보를 불러오는 중...
                            </div>
                        )}

                        {/* 정보 수정 버튼 */}
                        <div className={styles.editButtonGroup}>
                            {isEditing ? (
                                <>
                                    <button
                                        className={styles.saveButton}
                                        onClick={handleSaveEdit}
                                        disabled={loading}
                                    >
                                        {loading ? '저장 중...' : '저장'}
                                    </button>
                                    <button
                                        className={styles.cancelButton}
                                        onClick={handleCancelEdit}
                                        disabled={loading}
                                    >
                                        취소
                                    </button>
                                </>
                            ) : (
                                <button
                                    className={styles.editButton}
                                    onClick={handleEditProfile}
                                >
                                    <span className={styles.icon}>👤</span>
                                    정보 수정
                                </button>
                            )}
                        </div>
                    </div>

                    {/* 비밀번호 변경 섹션 */}
                    <div className={styles.passwordSection}>
                        <h3 className={styles.subSectionTitle}>비밀번호 변경</h3>

                        <div className={styles.passwordForm}>
                            <div className={styles.infoGroup}>
                                <span className={styles.infoLabel}>현재 비밀번호</span>
                                <input
                                    type="password"
                                    className={`${styles.passwordInput} ${passwordValidation.currentPassword.isValid === false ? styles.error : passwordValidation.currentPassword.isValid === true ? styles.success : ''}`}
                                    value={passwordForm.currentPassword}
                                    onChange={(e) => handlePasswordInputChange('currentPassword', e.target.value)}
                                    placeholder="현재 비밀번호를 입력하세요"
                                />
                                {passwordValidation.currentPassword.message && (
                                    <span className={passwordValidation.currentPassword.isValid ? styles.successMessage : styles.errorMessage}>
                                        {passwordValidation.currentPassword.message}
                                    </span>
                                )}
                            </div>

                            <div className={styles.infoGroup}>
                                <span className={styles.infoLabel}>새 비밀번호</span>
                                <input
                                    type="password"
                                    className={`${styles.passwordInput} ${passwordValidation.newPassword.isValid === false ? styles.error : passwordValidation.newPassword.isValid === true ? styles.success : ''}`}
                                    value={passwordForm.newPassword}
                                    onChange={(e) => handlePasswordInputChange('newPassword', e.target.value)}
                                    placeholder="새 비밀번호를 입력하세요 (8-16자, 영문+숫자+특수문자)"
                                />
                                {passwordValidation.newPassword.message && (
                                    <span className={passwordValidation.newPassword.isValid ? styles.successMessage : styles.errorMessage}>
                                        {passwordValidation.newPassword.message}
                                    </span>
                                )}
                            </div>

                            <div className={styles.infoGroup}>
                                <span className={styles.infoLabel}>비밀번호 확인</span>
                                <input
                                    type="password"
                                    className={`${styles.passwordInput} ${passwordValidation.confirmPassword.isValid === false ? styles.error : passwordValidation.confirmPassword.isValid === true ? styles.success : ''}`}
                                    value={passwordForm.confirmPassword}
                                    onChange={(e) => handlePasswordInputChange('confirmPassword', e.target.value)}
                                    placeholder="새 비밀번호를 다시 입력하세요"
                                />
                                {passwordValidation.confirmPassword.message && (
                                    <span className={passwordValidation.confirmPassword.isValid ? styles.successMessage : styles.errorMessage}>
                                        {passwordValidation.confirmPassword.message}
                                    </span>
                                )}
                            </div>
                        </div>

                        {/* 비밀번호 변경 버튼 */}
                        <button
                            className={styles.passwordChangeButton}
                            onClick={handleChangePassword}
                            disabled={loading}
                        >
                            <span className={styles.icon}>🔒</span>
                            {loading ? '변경 중...' : '비밀번호 변경'}
                        </button>
                    </div>
                </section>

                {/* 오른쪽: 관리자 계정 관리 */}
                <section className={styles.adminManagementSection}>
                    <div className={styles.adminHeader}>
                        <div>
                            <h2 className={styles.sectionTitle}>관리자 계정 관리</h2>
                            <p className={styles.adminCount}>등록된 관리자: {admins.length}명</p>
                        </div>
                    </div>

                    {/* 관리자 테이블 */}
                    {loading && (
                        <div className={styles.loadingState}>
                            관리자 목록을 불러오는 중...
                        </div>
                    )}

                    {error && (
                        <div className={styles.errorState}>
                            {error}
                            <button onClick={fetchAdminData}>
                                다시 시도
                            </button>
                        </div>
                    )}

                    {!loading && !error && (
                        <>
                            {admins.length > 0 ? (
                                <table className={styles.adminTable}>
                                    <thead>
                                    <tr>
                                        <th>이름</th>
                                        <th>이메일</th>
                                        <th>전화번호</th>
                                        <th>권한</th>
                                        <th>작업</th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    {admins.map((admin) => (
                                        <tr key={admin.id}>
                                            <td data-label="이름">{admin.name}</td>
                                            <td data-label="이메일">{admin.email}</td>
                                            <td data-label="연락처">{admin.phone}</td>
                                            <td data-label="권한">
                                                    <span className={`${styles.roleBadge} ${styles[admin.roleType]}`}>
                                                        {admin.role}
                                                    </span>
                                            </td>
                                            <td data-label="작업">
                                                <div className={styles.actionButtons}>
                                                    {isSuperAdmin() ? (
                                                        <>
                                                            <button
                                                                className={`${styles.actionButton} ${styles.editActionButton}`}
                                                                onClick={() => handleEditAdmin(admin.id)}
                                                                disabled={loading}
                                                            >
                                                                권한변경
                                                            </button>
                                                            <button
                                                                className={`${styles.actionButton} ${styles.deleteActionButton}`}
                                                                onClick={() => handleDeleteAdmin(admin.id)}
                                                                disabled={loading}
                                                            >
                                                                삭제
                                                            </button>
                                                        </>
                                                    ) : (
                                                        <>
                                                            <button
                                                                className={`${styles.actionButton} ${styles.editActionButton}`}
                                                                disabled={true}
                                                                style={{ opacity: 0.4, cursor: 'not-allowed' }}
                                                            >
                                                                권한변경
                                                            </button>
                                                            <button
                                                                className={`${styles.actionButton} ${styles.deleteActionButton}`}
                                                                disabled={true}
                                                                style={{ opacity: 0.4, cursor: 'not-allowed' }}
                                                            >
                                                                삭제
                                                            </button>
                                                        </>
                                                    )}
                                                </div>
                                            </td>
                                        </tr>
                                    ))}
                                    </tbody>
                                </table>
                            ) : (
                                <div className={styles.emptyState}>
                                    등록된 관리자가 없습니다.
                                </div>
                            )}
                        </>
                    )}
                </section>
            </div>

            {/* 권한 변경 모달 */}
            {roleChangeModal.isOpen && (
                <div className={styles.modalOverlay}>
                    <div className={styles.modalContent}>
                        <h3 className={styles.modalTitle}>관리자 권한 변경</h3>
                        
                        <div className={styles.modalBody}>
                            <p className={styles.modalText}>
                                <strong>{roleChangeModal.adminName}</strong>의 권한을 변경합니다.
                            </p>
                            
                            <div className={styles.roleSelectGroup}>
                                <label className={styles.roleSelectLabel}>현재 권한:</label>
                                <div className={styles.currentRoleDisplay}>
                                    <span className={`${styles.roleBadge} ${styles[getRoleClass(roleChangeModal.currentRole)]}`}>
                                        {roleChangeModal.currentRole === 'SUPER_ADMIN' ? 'Super Admin' : 
                                         roleChangeModal.currentRole === 'ADMIN' ? 'Admin' : 'Reader'}
                                    </span>
                                </div>
                            </div>
                            
                            <div className={styles.roleSelectGroup}>
                                <label className={styles.roleSelectLabel}>새로운 권한:</label>
                                <select 
                                    className={styles.roleSelect}
                                    value={roleChangeModal.newRole}
                                    onChange={(e) => setRoleChangeModal(prev => ({...prev, newRole: e.target.value}))}
                                >
                                    <option value="SUPER_ADMIN">Super Admin</option>
                                    <option value="ADMIN">Admin</option>
                                    <option value="READER">Reader</option>
                                </select>
                            </div>
                        </div>
                        
                        <div className={styles.modalActions}>
                            <button 
                                className={styles.modalCancelButton}
                                onClick={handleCloseRoleModal}
                                disabled={loading}
                            >
                                취소
                            </button>
                            <button 
                                className={styles.modalConfirmButton}
                                onClick={handleConfirmRoleChange}
                                disabled={loading || roleChangeModal.newRole === roleChangeModal.currentRole}
                            >
                                {loading ? '변경 중...' : '변경하기'}
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default SettingsPage;