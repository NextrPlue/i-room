const TOKEN_KEY = 'workerToken';

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
        console.log('근로자 토큰이 저장되었습니다.');
    },

    /**
     * 토큰 가져오기
     * @returns {string|null} 저장된 토큰
     */
    getToken: () => {
        const token = localStorage.getItem(TOKEN_KEY);
        if (!token) {
            console.log('저장된 근로자 토큰이 없습니다.');
            return null;
        }
        return token;
    },

    /**
     * Authorization 헤더 가져오기
     * @returns {string|null} Bearer 토큰
     */
    getAuthHeader: () => {
        const token = authUtils.getToken();
        return token ? `Bearer ${token}` : null;
    },

    /**
     * 토큰 삭제
     */
    removeToken: () => {
        localStorage.removeItem(TOKEN_KEY);
        console.log('근로자 토큰이 제거되었습니다.');
    },

    /**
     * 로그인 여부 확인
     * @returns {boolean} 로그인 상태
     */
    isAuthenticated: () => {
        return !!localStorage.getItem(TOKEN_KEY);
    }
};