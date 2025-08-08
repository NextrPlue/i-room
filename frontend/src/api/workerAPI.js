// src/api/workerAPI.js
import { authUtils } from '../utils/auth';

// API 기본 설정
const API_CONFIG = {
    gateway: "http://localhost:8080"
};

/**
 * HTTP 요청을 처리하는 기본 fetch 래퍼
 */
const apiRequest = async (url, options = {}) => {
    const baseHeaders = {
        'Content-Type': 'application/json',
        ...options.headers,
    };

    // 토큰이 있으면 Authorization 헤더 추가
    const authHeader = authUtils.getAuthHeader();
    if (authHeader && !options.skipAuth) {
        baseHeaders['Authorization'] = authHeader;
    }

    const defaultOptions = {
        headers: baseHeaders,
        ...options,
    };

    try {
        console.log(`[Worker API 요청] ${options.method || 'GET'} ${url}`);

        const response = await fetch(url, defaultOptions);

        console.log(`[Worker API 응답] ${response.status} ${response.statusText}`);

        if (!response.ok) {
            let errorData;
            const contentType = response.headers.get('content-type');

            if (contentType && contentType.includes('application/json')) {
                try {
                    errorData = await response.json();
                } catch (e) {
                    errorData = { message: `HTTP ${response.status}: ${response.statusText}` };
                }
            } else {
                errorData = { message: `HTTP ${response.status}: ${response.statusText}` };
            }

            // 401 에러시 토큰 제거 및 로그인 페이지로 이동
            if (response.status === 401) {
                authUtils.removeToken();
                window.location.href = '/login';
            }

            throw new Error(errorData.message || `요청 실패: ${response.status}`);
        }

        const data = await response.json();
        console.log('[Worker API 응답 데이터]:', data);

        return data;
    } catch (error) {
        console.error('[Worker API 요청 실패]:', error);
        throw error;
    }
};

/**
 * Worker API 서비스
 */
export const workerAPI = {
    /**
     * 근로자 로그인
     * @param {object} loginData - 로그인 데이터
     * @param {string} loginData.email - 이메일
     * @param {string} loginData.password - 비밀번호
     * @returns {Promise} 로그인 응답
     */
    login: async (loginData) => {
        const url = `${API_CONFIG.gateway}/api/user/workers/login`;
        console.log('[근로자 로그인 요청]', loginData);

        try {
            const response = await apiRequest(url, {
                method: 'POST',
                body: JSON.stringify(loginData),
                skipAuth: true // 로그인은 토큰 없이 요청
            });

            // 로그인 성공 시 토큰 저장
            if (response && response.data && response.data.token) {
                authUtils.setToken(response.data.token);
                console.log('[로그인 성공] 토큰이 저장되었습니다.');
            } else {
                console.warn('[경고] 응답에서 토큰을 찾을 수 없습니다.');
            }

            return response;
        } catch (error) {
            console.error('[로그인 실패]:', error);
            throw error;
        }
    },

    /**
     * 근로자 본인 정보 조회
     * @returns {Promise} 근로자 정보
     */
    getMyInfo: async () => {
        const url = `${API_CONFIG.gateway}/api/user/workers/me`;
        console.log('[내 정보 조회 요청]');

        try {
            const response = await apiRequest(url, {
                method: 'GET'
            });

            return response;
        } catch (error) {
            console.error('[내 정보 조회 실패]:', error);
            throw error;
        }
    },

    /**
     * 로그아웃
     */
    logout: () => {
        authUtils.removeToken();
        console.log('[로그아웃] 토큰이 제거되었습니다.');
    }
};