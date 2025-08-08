/**
 * 인증 토큰 관리 유틸리티
 * 토큰의 저장, 조회, 삭제를 안전하게 처리
 */

const TOKEN_KEY = 'adminToken';

export const authUtils = {
    /**
     * 토큰 저장
     * @param {string} token - JWT 토큰
     */
    setToken: (token) => {
        if (!token) {
            console.warn('빈 토큰을 저장하려고 시도했습니다.');
            return;
        }
        localStorage.setItem(TOKEN_KEY, token);
    },

    /**
     * 토큰 조회
     * @returns {string|null} 저장된 토큰 또는 null
     */
    getToken: () => {
        const token = localStorage.getItem(TOKEN_KEY);
        if (!token) {
            console.log('저장된 토큰이 없습니다.');
            return null;
        }
        return token;
    },

    /**
     * Authorization 헤더 생성
     * @returns {string|null} Bearer 토큰 헤더 또는 null
     */
    getAuthHeader: () => {
        const token = authUtils.getToken();
        return token ? `Bearer ${token}` : null;
    }
};