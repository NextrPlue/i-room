import React, { useState, useEffect } from 'react';
import styles from '../styles/Settings.module.css';
// import { adminAPI } from '../api/api'; // API 연동 시 사용

const SettingsPage = () => {
    // 내 계정 정보
    const [myAccount] = useState({
        id: 1,
        username: 'admin@company.com',
        name: '김관리자',
        phone: '010-1234-5678',
        role: 'Super Admin'
    });

    // 비밀번호 변경 폼 데이터
    const [passwordForm, setPasswordForm] = useState({
        currentPassword: '',
        newPassword: '',
        confirmPassword: ''
    });

    // 관리자 목록
    const [admins, setAdmins] = useState([
        {
            id: 1,
            name: '관리자1',
            email: 'admin@company.com',
            phone: '010-1234-5678',
            role: 'Super Admin',
            roleType: 'superAdmin'
        },
        {
            id: 2,
            name: '관리자2',
            email: 'admin2@company.com',
            phone: '010-8765-4321',
            role: 'Admin',
            roleType: 'admin'
        },
        {
            id: 3,
            name: '관리자3',
            email: 'admin3@company.com',
            phone: '010-5678-1234',
            role: 'Manager',
            roleType: 'manager'
        }
    ]);

    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    // 컴포넌트 마운트 시 데이터 로드
    useEffect(() => {
        fetchAdminData();
    }, []);

    // 관리자 데이터 조회
    const fetchAdminData = async () => {
        try {
            setLoading(true);
            setError(null);

            // API 호출 예시
            // const response = await adminAPI.getAdmins();
            // setAdmins(response.data || []);

        } catch (err) {
            console.error('관리자 데이터 조회 실패:', err);
            setError(err.message || '데이터를 불러오는데 실패했습니다.');
        } finally {
            setLoading(false);
        }
    };

    // 비밀번호 폼 입력 처리
    const handlePasswordInputChange = (field, value) => {
        setPasswordForm(prev => ({
            ...prev,
            [field]: value
        }));
    };

    // 정보 수정 버튼 클릭
    const handleEditProfile = () => {
        // 정보 수정 모달 또는 페이지로 이동
        console.log('정보 수정');
    };

    // 비밀번호 변경 버튼 클릭
    const handleChangePassword = () => {
        if (!passwordForm.currentPassword || !passwordForm.newPassword || !passwordForm.confirmPassword) {
            alert('모든 필드를 입력해주세요.');
            return;
        }

        if (passwordForm.newPassword !== passwordForm.confirmPassword) {
            alert('새 비밀번호가 일치하지 않습니다.');
            return;
        }

        // API 호출 로직
        console.log('비밀번호 변경:', passwordForm);
        alert('비밀번호가 변경되었습니다.');

        // 폼 초기화
        setPasswordForm({
            currentPassword: '',
            newPassword: '',
            confirmPassword: ''
        });
    };

    // 관리자 추가
    const handleAddAdmin = () => {
        // 관리자 추가 모달 또는 페이지로 이동
        console.log('관리자 추가');
    };

    // 관리자 수정
    const handleEditAdmin = (adminId) => {
        console.log('관리자 수정:', adminId);
    };

    // 관리자 삭제
    const handleDeleteAdmin = (adminId) => {
        if (window.confirm('정말로 이 관리자를 삭제하시겠습니까?')) {
            setAdmins(prev => prev.filter(admin => admin.id !== adminId));
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

                        <div className={styles.profileInfo}>
                            <div className={styles.infoGroup}>
                                <span className={styles.infoLabel}>아이디</span>
                                <div className={styles.infoValue}>{myAccount.username}</div>
                            </div>

                            <div className={styles.infoGroup}>
                                <span className={styles.infoLabel}>이름</span>
                                <div className={styles.infoValue}>{myAccount.name}</div>
                            </div>

                            <div className={styles.infoGroup}>
                                <span className={styles.infoLabel}>연락처</span>
                                <div className={styles.infoValue}>{myAccount.phone}</div>
                            </div>
                        </div>

                        {/* 정보 수정 버튼 */}
                        <button
                            className={styles.editButton}
                            onClick={handleEditProfile}
                        >
                            <span className={styles.icon}>👤</span>
                            정보 수정
                        </button>
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
                        >
                            <span className={styles.icon}>🔒</span>
                            비밀번호 변경
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
                        <button
                            className={styles.addAdminButton}
                            onClick={handleAddAdmin}
                        >
                            + 관리자 추가
                        </button>
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
                                                    <button
                                                        className={`${styles.actionButton} ${styles.editActionButton}`}
                                                        onClick={() => handleEditAdmin(admin.id)}
                                                    >
                                                        수정
                                                    </button>
                                                    <button
                                                        className={`${styles.actionButton} ${styles.deleteActionButton}`}
                                                        onClick={() => handleDeleteAdmin(admin.id)}
                                                    >
                                                        삭제
                                                    </button>
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