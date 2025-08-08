import {authUtils} from '../utils/auth';

// API 기본 설정
const API_CONFIG = {
    gateway: "http://localhost:8080"
};

/**
 * HTTP 요청을 처리하는 기본 fetch 래퍼
 * @param {string} url - 요청 URL
 * @param {object} options - fetch 옵션
 * @returns {Promise} 응답 데이터
 */
const apiRequest = async (url, options = {}) => {
    // 기본 헤더 설정
    const baseHeaders = {
        'Content-Type': 'application/json',
        ...options.headers,
    };

    // 토큰이 있으면 Authorization 헤더 추가
    const authHeader = authUtils.getAuthHeader();
    if (authHeader) {
        baseHeaders['Authorization'] = authHeader;
    }

    const defaultOptions = {
        headers: baseHeaders,
        ...options,
    };

    try {
        const response = await fetch(url, defaultOptions);
        // 응답 처리
        if (!response.ok) {
            let errorData;
            const contentType = response.headers.get('content-type');

            // JSON 응답인지 확인
            if (contentType && contentType.includes('application/json')) {
                try {
                    errorData = await response.json();
                    console.error('API JSON 오류 응답:', errorData);
                } catch (e) {
                    const errorText = await response.text();
                    console.error('API 텍스트 오류 응답:', errorText);
                    errorData = {message: errorText};
                }
            } else {
                const errorText = await response.text();
                console.error('API 텍스트 오류 응답:', errorText);
                errorData = {message: errorText};
            }

            let errorMessage = errorData?.message || `HTTP ${response.status}: ${response.statusText}`;
            if (response.status === 400 && errorData?.errors) {
                errorMessage = errorData.errors.map(err => err.message).join(', ');
            } else if (response.status === 403 && errorData?.message) {
                errorMessage = errorData.message;
            }
            throw new Error(errorMessage);
        }
        return await response.json();
    } catch (error) {
        console.error('API 요청 실패:', error);
        if (error.name === 'TypeError' && error.message.includes('fetch')) {
            throw new Error('서버에 연결할 수 없습니다. 네트워크 연결을 확인해주세요.');
        }
        throw error;
    }
};

/**
 * User API 서비스
 */
export const userAPI = {

    /**
     * 근로자 목록 조회
     * @param {object} options
     * @param {number} options.page - 페이지 번호 (기본값: 0)
     * @param {number} options.size - 페이지당 개수 (기본값: 10)
     * @param {string} [options.target] - 검색 대상 (name, email)
     * @param {string} [options.keyword] - 검색어
     */
    getWorkers: async ({page = 0, size = 10, target = '', keyword = ''} = {}) => {
        const queryParams = new URLSearchParams({
            page: page.toString(),
            size: size.toString(),
        });

        // target과 keyword가 모두 있을 때만 검색 파라미터 추가
        if (target && keyword && keyword.trim()) {
            queryParams.append('target', target);
            queryParams.append('keyword', keyword.trim());
        }

        const url = `${API_CONFIG.gateway}/api/user/workers?${queryParams.toString()}`;
        return await apiRequest(url);
    },

    /**
     * 근로자 등록
     * @param {object} workerData - 등록할 근로자 데이터
     * @param {string} workerData.name - 이름
     * @param {string} workerData.email - 이메일
     * @param {string} workerData.password - 비밀번호
     * @param {string} workerData.phone - 연락처
     * @param {string} workerData.bloodType - 혈액형
     * @param {string} workerData.gender - 성별
     * @param {number} workerData.age - 나이
     * @param {number} workerData.weight - 몸무게
     * @param {number} workerData.height - 키
     * @param {string} workerData.jobTitle - 직책
     * @param {string} workerData.occupation - 직종
     * @param {string} workerData.department - 부서
     * @param {string} workerData.faceImageUrl - 얼굴 이미지 URL  # 얜 나중에 없애야함
     * @returns {Promise} 등록된 근로자 정보
     */
    createWorker: async (workerData) => {
        const url = `${API_CONFIG.gateway}/api/user/workers/register`;
        return await apiRequest(url, {
            method: 'POST',
            body: JSON.stringify(workerData)
        });
    },


    /**
     * 근로자 상세 정보 조회
     * @param {string} workerId - 근로자 ID
     * @returns {Promise} 근로자 상세 정보
     */
    getWorkerDetail: async (workerId) => {
        const url = `${API_CONFIG.gateway}/api/user/workers/${workerId}`;
        return await apiRequest(url);
    },

    /**
     * 근로자 안전교육 이력 조회
     * @param {string} workerId - 근로자 ID
     * @param {number} page - 페이지 번호 (기본값: 0)
     * @param {number} size - 페이지당 개수 (기본값: 10)
     * @returns {Promise} 안전교육 이력 데이터
     */
    getWorkerEducation: async (workerId, page = 0, size = 10) => {
        const queryParams = new URLSearchParams({
            page: page.toString(),
            size: size.toString()
        });

        const url = `${API_CONFIG.gateway}/api/management/worker-education/workers/${workerId}?${queryParams.toString()}`;
        return await apiRequest(url);
    },

    /**
     * 안전교육 등록
     * @param {object} educationData - 등록할 교육 데이터
     * @param {string} educationData.workerId - 근로자 ID
     * @param {string} educationData.name - 교육명
     * @param {string} educationData.eduDate - 교육 일시 (YYYY-MM-DD)
     * @param {string} educationData.certUrl - 수료증 URL
     * @returns {Promise} 등록된 교육 정보
     */
    createWorkerEducation: async (educationData) => {
        const url = `${API_CONFIG.gateway}/api/management/worker-education`;
        return await apiRequest(url, {
            method: 'POST',
            body: JSON.stringify(educationData)
        });
    },

    /**
     * 근로자 정보 수정
     * @param {string} workerId - 근로자 ID
     * @param {object} workerData - 수정할 근로자 데이터
     * @returns {Promise} 수정된 근로자 정보
     */
    updateWorker: async (workerId, workerData) => {
        const url = `${API_CONFIG.gateway}/api/user/workers/${workerId}`;
        return await apiRequest(url, {
            method: 'PUT',
            body: JSON.stringify(workerData)
        });
    },

    /**
     * 근로자 출입현황 조회
     * @param {string} workerId - 근로자 ID
     * @returns {Promise} 출입현황 데이터 { id, workerId, enterDate, outDate }
     */
    getWorkerAttendance: async (workerId) => {
        const url = `${API_CONFIG.gateway}/api/management/entries/${workerId}`;
        return await apiRequest(url);
    },

};

/**
 * Admin API 서비스
 */
export const adminAPI = {
    /**
     * 관리자 로그인
     * @param {object} loginData - 로그인 데이터
     * @param {string} loginData.email - 이메일
     * @param {string} loginData.password - 비밀번호
     * @returns {Promise} 로그인 응답 (토큰 등)
     */
    login: async (loginData) => {
        const url = `${API_CONFIG.gateway}/api/user/admins/login`;
        const response = await apiRequest(url, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(loginData)
        });

        if (response && response.data && response.data.token) {
            authUtils.setToken(response.data.token);
        } else if (response && response.token) {
            authUtils.setToken(response.token);
        } else {
            console.warn('[경고] 응답에서 토큰을 찾을 수 없습니다.');
        }
        return response;
    },
};

/**
 * Blueprint API 서비스
 */
export const blueprintAPI = {
    /**
     * 도면 목록 조회
     * @param {object} options
     * @param {number} options.page - 페이지 번호 (기본값: 0)
     * @param {number} options.size - 페이지당 개수 (기본값: 10)
     * @returns {Promise} 도면 목록 데이터
     */
    getBlueprints: async ({page = 0, size = 10} = {}) => {
        const queryParams = new URLSearchParams({
            page: page.toString(),
            size: size.toString(),
        });

        const url = `${API_CONFIG.gateway}/api/dashboard/blueprints?${queryParams.toString()}`;
        return await apiRequest(url);
    },

    /**
     * 도면 등록
     * @param {object} blueprintData - 등록할 도면 데이터
     * @param {File} blueprintData.file - 업로드할 이미지 파일
     * @param {number} blueprintData.floor - 층수
     * @param {number} blueprintData.width - 가로 크기
     * @param {number} blueprintData.height - 세로 크기
     * @returns {Promise} 등록된 도면 정보
     */
    createBlueprint: async (blueprintData) => {
        const url = `${API_CONFIG.gateway}/api/dashboard/blueprints`;

        if (!blueprintData.file) {
            throw new Error("도면 이미지 파일이 선택되지 않았습니다.");
        }

        const formData = new FormData();
        const dataBlob = new Blob(
            [JSON.stringify({
                blueprintUrl: "",
                floor: blueprintData.floor,
                width: blueprintData.width,
                height: blueprintData.height
            })],
            {type: "application/json"}
        );
        formData.append("data", dataBlob);
        formData.append("file", blueprintData.file);

        const response = await fetch(url, {
            method: "POST",
            headers: {
                Authorization: authUtils.getAuthHeader()
            },
            body: /** @type {BodyInit} */ (formData)
        });

        if (!response.ok) {
            let errorMessage = `HTTP ${response.status}: ${response.statusText}`;
            const contentType = response.headers.get('content-type');
            if (contentType && contentType.includes('application/json')) {
                try {
                    const errorData = await response.json();
                    errorMessage = errorData?.message || errorMessage;
                } catch {
                    // JSON 파싱 실패 시 기본 에러 메시지 사용
                }
            } else {
                try {
                    const errorText = await response.text();
                    if (errorText) {
                        errorMessage = errorText;
                    }
                } catch {
                    // 텍스트 파싱 실패 시 기본 에러 메시지 사용
                }
            }

            console.error('도면 업로드 실패:', errorMessage);
            throw new Error(errorMessage);
        }

        return await response.json();
    },

    /**
     * 도면 이미지 URL 생성
     * @param {number} blueprintId - 도면 ID
     * @returns {string} 이미지 URL
     */
    getBlueprintImage(blueprintId) {
        return `${API_CONFIG.gateway}/api/dashboard/blueprints/${blueprintId}/image`;
    },

    /**
     * 도면 수정
     * @param {number} blueprintId - 수정할 도면 ID
     * @param {object} blueprintData - 수정할 도면 데이터
     * @param {File} [blueprintData.file] - 새 이미지 파일 (선택사항)
     * @param {number} blueprintData.floor - 층수
     * @param {number} blueprintData.width - 가로 크기
     * @param {number} blueprintData.height - 세로 크기
     * @returns {Promise} 수정된 도면 정보
     */
    updateBlueprint: async (blueprintId, blueprintData) => {
        const url = `${API_CONFIG.gateway}/api/dashboard/blueprints/${blueprintId}`;

        if (blueprintData.file && blueprintData.file instanceof File) {
            // 파일이 있는 경우 FormData로 전송
            const formData = new FormData();
            const dataBlob = new Blob(
                [JSON.stringify({
                    blueprintUrl: "",
                    floor: blueprintData.floor,
                    width: blueprintData.width,
                    height: blueprintData.height
                })],
                {type: "application/json"}
            );
            formData.append("data", dataBlob);
            formData.append("file", blueprintData.file);

            const response = await fetch(url, {
                method: "PUT",
                headers: {
                    Authorization: authUtils.getAuthHeader()
                },
                body: formData
            });

            if (!response.ok) {
                let errorMessage = `HTTP ${response.status}: ${response.statusText}`;
                const contentType = response.headers.get('content-type');
                if (contentType && contentType.includes('application/json')) {
                    try {
                        const errorData = await response.json();
                        errorMessage = errorData?.message || errorMessage;
                    } catch {
                        // JSON 파싱 실패 시 기본 에러 메시지 사용
                    }
                } else {
                    try {
                        const errorText = await response.text();
                        if (errorText) {
                            errorMessage = errorText;
                        }
                    } catch {
                        // 텍스트 파싱 실패 시 기본 에러 메시지 사용
                    }
                }

                console.error('도면 수정 실패:', errorMessage);
                throw new Error(errorMessage);
            }

            return await response.json();
        } else {
            // 파일이 없는 경우도 FormData로 전송 (data 필드만)
            const formData = new FormData();
            const dataBlob = new Blob(
                [JSON.stringify({
                    floor: blueprintData.floor,
                    width: blueprintData.width,
                    height: blueprintData.height
                })],
                {type: "application/json"}
            );
            formData.append("data", dataBlob);

            const response = await fetch(url, {
                method: "PUT",
                headers: {
                    Authorization: authUtils.getAuthHeader()
                },
                body: formData
            });

            if (!response.ok) {
                let errorMessage = `HTTP ${response.status}: ${response.statusText}`;
                const contentType = response.headers.get('content-type');
                if (contentType && contentType.includes('application/json')) {
                    try {
                        const errorData = await response.json();
                        errorMessage = errorData?.message || errorMessage;
                    } catch {
                        // JSON 파싱 실패 시 기본 에러 메시지 사용
                    }
                } else {
                    try {
                        const errorText = await response.text();
                        if (errorText) {
                            errorMessage = errorText;
                        }
                    } catch {
                        // 텍스트 파싱 실패 시 기본 에러 메시지 사용
                    }
                }

                console.error('도면 수정 실패:', errorMessage);
                throw new Error(errorMessage);
            }

            return await response.json();
        }
    },

    /**
     * 도면 삭제
     * @param {number} blueprintId - 삭제할 도면 ID
     * @returns {Promise} 삭제 응답 데이터
     */
    deleteBlueprint: async (blueprintId) => {
        const url = `${API_CONFIG.gateway}/api/dashboard/blueprints/${blueprintId}`;
        return await apiRequest(url, {
            method: 'DELETE'
        });
    }
};