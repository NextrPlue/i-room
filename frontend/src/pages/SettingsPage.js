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

    // 관리자 목록
    const [admins, setAdmins] = useState([]);

    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    const isSuperAdmin = () => {
        return myAccount && (myAccount.role === 'SUPER_ADMIN' || myAccount.role === 'Super Admin');
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

    const handlePasswordInputChange = (field, value) => {
        setPasswordForm(prev => ({
            ...prev,
            [field]: value
        }));
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

            // 폼 초기화
            setPasswordForm({
                currentPassword: '',
                newPassword: '',
                confirmPassword: ''
            });
        } catch (err) {
            console.error('비밀번호 변경 실패:', err);
            alert(err.message || '비밀번호 변경에 실패했습니다.');
        } finally {
            setLoading(false);
        }
    };

    // 관리자 수정
    const handleEditAdmin = async (adminId) => {
        if (!isSuperAdmin()) {
            alert('관리자 권한 변경은 Super Admin만 가능합니다.');
            return;
        }

        const admin = admins.find(a => a.id === adminId);
        if (!admin) return;

        const currentRole = admin.roleType === 'superAdmin' ? 'SUPER_ADMIN' : 'READER';

        const newRole = prompt(
            `${admin.name}의 현재 권한: ${admin.role}\n\n새로운 권한을 입력하세요:\n- SUPER_ADMIN\n- READER`,
            currentRole
        );

        if (!newRole || newRole === currentRole) return;

        if (!['SUPER_ADMIN', 'READER'].includes(newRole)) {
            alert('올바른 권한을 입력해주세요 (SUPER_ADMIN, READER)');
            return;
        }

        try {
            setLoading(true);
            const response = await userAPI.changeAdminRole(adminId, newRole);
            
            if (response.data) {
                const displayRole = newRole === 'SUPER_ADMIN' ? 'Super Admin' : 'Reader';
                const roleType = newRole === 'SUPER_ADMIN' ? 'superAdmin' : 'reader';

                setAdmins(prev => prev.map(a => 
                    a.id === adminId
                        ? { ...a, role: displayRole, roleType: roleType }
                        : a
                ));
                
                alert(`${admin.name}의 권한이 ${displayRole}으로 변경되었습니다.`);
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
                                    className={styles.passwordInput}
                                    value={passwordForm.currentPassword}
                                    onChange={(e) => handlePasswordInputChange('currentPassword', e.target.value)}
                                    placeholder="현재 비밀번호를 입력하세요"
                                />
                            </div>

                            <div className={styles.infoGroup}>
                                <span className={styles.infoLabel}>새 비밀번호</span>
                                <input
                                    type="password"
                                    className={styles.passwordInput}
                                    value={passwordForm.newPassword}
                                    onChange={(e) => handlePasswordInputChange('newPassword', e.target.value)}
                                    placeholder="새 비밀번호를 입력하세요"
                                />
                            </div>

                            <div className={styles.infoGroup}>
                                <span className={styles.infoLabel}>비밀번호 확인</span>
                                <input
                                    type="password"
                                    className={styles.passwordInput}
                                    value={passwordForm.confirmPassword}
                                    onChange={(e) => handlePasswordInputChange('confirmPassword', e.target.value)}
                                    placeholder="새 비밀번호를 다시 입력하세요"
                                />
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
        </div>
    );
};

export default SettingsPage;