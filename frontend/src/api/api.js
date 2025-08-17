import {authUtils} from '../utils/auth';

// API 기본 설정
const API_CONFIG = {
    gateway: "http://135.149.162.178"
};

/**
 * FormData fetch 응답의 에러를 처리하는 공통 함수
 * @param {Response} response - fetch 응답 객체
 * @returns {Promise<string>} 에러 메시지
 */
const handleFetchError = async (response) => {
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
        }
    }

    return errorMessage;
};

/**
 * 도면 데이터로 FormData를 생성하는 함수
 * @param {object} blueprintData - 도면 데이터
 * @param {File} [blueprintData.file] - 이미지 파일 (선택사항)
 * @param {string} blueprintData.name - 도면 이름
 * @param {number} blueprintData.floor - 층수
 * @param {number} blueprintData.width - 가로 크기
 * @param {number} blueprintData.height - 세로 크기
 * @param {object} blueprintData.topLeft - 좌상단 GPS 좌표
 * @param {object} blueprintData.topRight - 우상단 GPS 좌표
 * @param {object} blueprintData.bottomRight - 우하단 GPS 좌표
 * @param {object} blueprintData.bottomLeft - 좌하단 GPS 좌표
 * @returns {FormData} 생성된 FormData
 */
const createBlueprintFormData = (blueprintData) => {
    const formData = new FormData();
    const dataBlob = new Blob(
        [JSON.stringify({
            name: blueprintData.name,
            blueprintUrl: "",
            floor: blueprintData.floor,
            width: blueprintData.width,
            height: blueprintData.height,
            topLeft: blueprintData.topLeft,
            topRight: blueprintData.topRight,
            bottomRight: blueprintData.bottomRight,
            bottomLeft: blueprintData.bottomLeft
        })],
        {type: "application/json"}
    );

    formData.append("data", dataBlob);

    if (blueprintData.file) {
        formData.append("file", blueprintData.file);
    }

    return formData;
};

/**
 * FormData를 사용한 multipart/form-data 요청을 처리하는 함수
 * @param {string} url - 요청 URL
 * @param {string} method - HTTP 메소드
 * @param {FormData} formData - 전송할 FormData
 * @returns {Promise} 응답 데이터
 */
const fetchWithFormData = async (url, method, formData) => {
    const response = await fetch(url, {
        method,
        headers: {
            Authorization: authUtils.getAuthHeader()
        },
        body: formData
    });

    if (!response.ok) {
        const errorMessage = await handleFetchError(response);
        console.error(`${method} 요청 실패:`, errorMessage);
        throw new Error(errorMessage);
    }

    return await response.json();
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
     * 근로자 삭제
     * @param {string} workerId - 삭제할 근로자 ID
     * @returns {Promise} 삭제 응답
     */
    deleteWorker: async (workerId) => {
        const url = `${API_CONFIG.gateway}/api/user/workers/${workerId}`;
        return await apiRequest(url, {
            method: 'DELETE'
        });
    },

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

        if (response.data && response.data.token) {
            authUtils.setToken(response.data.token);
        } else if (response.token) {
            authUtils.setToken(response.token);
        } else {
            console.warn('[경고] 응답에서 토큰을 찾을 수 없습니다.');
        }
        return response;
    },

    /**
     * 관리자 회원가입
     * @param {object} signUpData - 회원가입 데이터
     * @param {string} signUpData.name - 이름
     * @param {string} signUpData.email - 이메일
     * @param {string} signUpData.password - 비밀번호
     * @param {string} signUpData.phone - 전화번호
     * @returns {Promise} 회원가입 응답
     */
    signUp: async (signUpData) => {
        const url = `${API_CONFIG.gateway}/api/user/admins/signup`;
        return await apiRequest(url, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(signUpData)
        });
    },

    /**
     * 현재 로그인한 관리자 정보 조회
     * @returns {Promise} 내 계정 정보
     */
    getMyInfo: async () => {
        const url = `${API_CONFIG.gateway}/api/user/admins/me`;
        return await apiRequest(url);
    },

    /**
     * 비밀번호 변경
     * @param {object} passwordData - 비밀번호 변경 데이터
     * @param {string} passwordData.password - 현재 비밀번호
     * @param {string} passwordData.newPassword - 새 비밀번호
     * @returns {Promise} 변경 응답
     */
    changePassword: async (passwordData) => {
        const url = `${API_CONFIG.gateway}/api/user/admins/password`;
        return await apiRequest(url, {
            method: 'PUT',
            body: JSON.stringify(passwordData)
        });
    },

    /**
     * 관리자 정보 수정
     * @param {object} updateData - 수정할 정보
     * @param {string} updateData.name - 이름
     * @param {string} updateData.email - 이메일
     * @param {string} updateData.phone - 전화번호
     * @returns {Promise} 수정된 관리자 정보
     */
    updateMyInfo: async (updateData) => {
        const url = `${API_CONFIG.gateway}/api/user/admins/me`;
        return await apiRequest(url, {
            method: 'PUT',
            body: JSON.stringify(updateData)
        });
    },

    /**
     * 관리자 목록 조회
     * @param {object} options
     * @param {number} options.page - 페이지 번호 (기본값: 0)
     * @param {number} options.size - 페이지당 개수 (기본값: 10)
     * @param {string} [options.target] - 검색 대상 (name, email)
     * @param {string} [options.keyword] - 검색어
     * @returns {Promise} 관리자 목록 데이터
     */
    getAdmins: async ({page = 0, size = 10, target = '', keyword = ''} = {}) => {
        const queryParams = new URLSearchParams({
            page: page.toString(),
            size: size.toString(),
        });

        if (target && keyword && keyword.trim()) {
            queryParams.append('target', target);
            queryParams.append('keyword', keyword.trim());
        }

        const url = `${API_CONFIG.gateway}/api/user/admins?${queryParams.toString()}`;
        return await apiRequest(url);
    },

    /**
     * 관리자 권한 변경
     * @param {string} adminId - 관리자 ID
     * @param {string} role - 새로운 권한 (SUPER_ADMIN, ADMIN, READER)
     * @returns {Promise} 변경된 관리자 정보
     */
    changeAdminRole: async (adminId, role) => {
        const url = `${API_CONFIG.gateway}/api/user/admins/${adminId}/role`;
        return await apiRequest(url, {
            method: 'PUT',
            body: JSON.stringify({ role })
        });
    },

    /**
     * 관리자 삭제
     * @param {string} adminId - 삭제할 관리자 ID
     * @returns {Promise} 삭제 응답
     */
    deleteAdmin: async (adminId) => {
        const url = `${API_CONFIG.gateway}/api/user/admins/${adminId}`;
        return await apiRequest(url, {
            method: 'DELETE'
        });
    },
};

/**
 * Management API 서비스
 */
export const managementAPI = {
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
     * 근로자 출입현황 조회
     * @param {string} workerId - 근로자 ID
     * @returns {Promise} 출입현황 데이터 { id, workerId, enterDate, outDate }
     */
    getWorkerAttendance: async (workerId) => {
        const url = `${API_CONFIG.gateway}/api/management/entries/${workerId}`;
        return await apiRequest(url);
    },

    /**
     * 근로자 출입 통계 조회
     * @returns {Promise} 통계 데이터 (총근무자, 근무중, 퇴근, 미출근)
     */
    getWorkerStats: async () => {
        const url = `${API_CONFIG.gateway}/api/management/entries/statistics`;
        return await apiRequest(url);
    },

    /**
     * 근무중인 근로자 목록 조회
     * @returns {Promise} 근무중인 근로자 목록
     */
    getWorkingWorkers: async () => {
        const url = `${API_CONFIG.gateway}/api/management/entries/working-workers`;
        return await apiRequest(url);
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
     * @param {string} [options.target] - 검색 대상 (예: "floor")
     * @param {string} [options.keyword] - 검색 키워드 (예: "1")
     * @returns {Promise} 도면 목록 데이터
     */
    getBlueprints: async ({page = 0, size = 10, target = null, keyword = null} = {}) => {
        const queryParams = new URLSearchParams({
            page: page.toString(),
            size: size.toString(),
        });

        if (target && keyword) {
            queryParams.append('target', target);
            queryParams.append('keyword', keyword);
        }

        const url = `${API_CONFIG.gateway}/api/dashboard/blueprints?${queryParams.toString()}`;
        return await apiRequest(url);
    },

    /**
     * 단일 도면 조회
     * @param {number} blueprintId - 도면 ID
     * @returns {Promise} 도면 데이터
     */
    getBlueprint: async (blueprintId) => {
        const url = `${API_CONFIG.gateway}/api/dashboard/blueprints/${blueprintId}`;
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

        const formData = createBlueprintFormData(blueprintData);
        return await fetchWithFormData(url, "POST", formData);
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
     * 도면 이미지 Blob 데이터 조회 (인증 헤더 포함)
     * @param {number} blueprintId - 도면 ID
     * @returns {Promise<string>} Blob URL
     */
    getBlueprintImageBlob: async (blueprintId) => {
        const url = `${API_CONFIG.gateway}/api/dashboard/blueprints/${blueprintId}/image`;
        
        const response = await fetch(url, {
            headers: {
                'Authorization': authUtils.getAuthHeader()
            }
        });

        if (!response.ok) {
            throw new Error(`이미지 로드 실패: ${response.status}`);
        }

        const blob = await response.blob();
        return URL.createObjectURL(blob);
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

        // 파일이 있든 없든 동일한 방식으로 FormData 생성 및 전송
        const formData = createBlueprintFormData(blueprintData);
        return await fetchWithFormData(url, "PUT", formData);
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

/**
 * Alarm API 서비스
 */
export const alarmAPI = {
    /**
     * 관리자용 알람 목록 조회
     * @param {object} options
     * @param {number} options.page - 페이지 번호 (기본값: 0)
     * @param {number} options.size - 페이지당 개수 (기본값: 10)
     * @param {number} [options.hours] - 최근 N시간 내 알람 (선택사항)
     * @returns {Promise} 알람 목록 데이터
     */
    getAlarmsForAdmin: async ({page = 0, size = 10, hours} = {}) => {
        const queryParams = new URLSearchParams({
            page: page.toString(),
            size: size.toString(),
        });

        if (hours !== undefined) {
            queryParams.append('hours', hours.toString());
        }

        const url = `${API_CONFIG.gateway}/api/alarm/alarms/admins?${queryParams.toString()}`;
        return await apiRequest(url);
    }
};

/**
 * Sensor API 서비스
 */
export const sensorAPI = {
    /**
     * 다중 근로자 위치 정보 조회
     * @param {Array<number>} workerIds - 근로자 ID 배열
     * @returns {Promise} 근로자 위치 정보 배열
     */
    getWorkersLocation: async (workerIds) => {
        const url = `${API_CONFIG.gateway}/api/sensor/worker-sensor/locations`;
        
        const response = await fetch(url, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${authUtils.getToken()}`
            },
            body: JSON.stringify(workerIds)
        });

        if (!response.ok) {
            const errorMessage = await handleFetchError(response);
            throw new Error(errorMessage);
        }

        return await response.json();
    },
};

/**
 * Risk Zone (Danger Area) API 서비스
 */
export const riskZoneAPI = {
    /**
     * 위험구역 목록 조회
     * @param {object} options
     * @param {number} options.page - 페이지 번호 (기본값: 0)
     * @param {number} options.size - 페이지당 개수 (기본값: 10)
     * @returns {Promise} 위험구역 목록 데이터
     */
    getRiskZones: async ({page = 0, size = 10} = {}) => {
        const queryParams = new URLSearchParams({
            page: page.toString(),
            size: size.toString(),
        });

        const url = `${API_CONFIG.gateway}/api/dashboard/danger-areas?${queryParams.toString()}`;
        return await apiRequest(url);
    },

    /**
     * 위험구역 등록
     * @param {object} dangerAreaData - 등록할 위험구역 데이터
     * @param {number} dangerAreaData.blueprintId - 도면 ID
     * @param {number} dangerAreaData.latitude - 위도
     * @param {number} dangerAreaData.longitude - 경도
     * @param {number} dangerAreaData.width - 너비
     * @param {number} dangerAreaData.height - 높이
     * @param {string} dangerAreaData.name - 위험구역 이름
     * @returns {Promise} 등록된 위험구역 정보
     */
    createRiskZone: async (dangerAreaData) => {
        const url = `${API_CONFIG.gateway}/api/dashboard/danger-areas`;
        return await apiRequest(url, {
            method: 'POST',
            body: JSON.stringify(dangerAreaData)
        });
    },

    /**
     * 위험구역 수정
     * @param {number} dangerAreaId - 수정할 위험구역 ID
     * @param {object} dangerAreaData - 수정할 위험구역 데이터
     * @param {number} dangerAreaData.blueprintId - 도면 ID
     * @param {number} dangerAreaData.latitude - 위도
     * @param {number} dangerAreaData.longitude - 경도
     * @param {number} dangerAreaData.width - 너비
     * @param {number} dangerAreaData.height - 높이
     * @param {string} dangerAreaData.name - 위험구역 이름
     * @returns {Promise} 수정된 위험구역 정보
     */
    updateRiskZone: async (dangerAreaId, dangerAreaData) => {
        const url = `${API_CONFIG.gateway}/api/dashboard/danger-areas/${dangerAreaId}`;
        return await apiRequest(url, {
            method: 'PUT',
            body: JSON.stringify(dangerAreaData)
        });
    },

    /**
     * 위험구역 삭제
     * @param {number} dangerAreaId - 삭제할 위험구역 ID
     * @returns {Promise} 삭제 응답 데이터
     */
    deleteRiskZone: async (dangerAreaId) => {
        const url = `${API_CONFIG.gateway}/api/dashboard/danger-areas/${dangerAreaId}`;
        return await apiRequest(url, {
            method: 'DELETE'
        });
    }
};

/**
 * Dashboard API 서비스
 */
export const dashboardAPI = {
    /**
     * 메트릭 점수 조회 (안전 점수 변동 추이용)
     * @param {string} interval - 조회 간격 (day, week, month)
     * @returns {Promise} 메트릭 데이터 배열
     */
    getMetrics: async (interval) => {
        const url = `${API_CONFIG.gateway}/api/dashboard/dashboards/metrics/${interval}`;
        return await apiRequest(url);
    },

    /**
     * 특정 메트릭 타입 대시보드 조회
     * @param {string} metricType - 메트릭 타입 (PPE_VIOLATION, DANGER_ZONE, HEALTH_RISK)
     * @returns {Promise} 대시보드 데이터
     */
    getDashboard: async (metricType) => {
        const url = `${API_CONFIG.gateway}/api/dashboard/dashboards/${metricType}`;
        return await apiRequest(url);
    }
};

/**
 * Report API 서비스
 */
export const reportAPI = {
    /**
     * 일일 리포트 생성 및 다운로드
     * @param {string} date - 조회할 날짜 (YYYY-MM-DD 형식)
     * @returns {Promise<Blob>} PDF 파일 Blob
     */
    generateDailyReport: async (date) => {
        const url = `${API_CONFIG.gateway}/api/dashboard/dashboards/report/${date}`;
        
        const response = await fetch(url, {
            method: 'POST',
            headers: {
                'Authorization': authUtils.getAuthHeader()
            }
        });

        if (!response.ok) {
            const errorMessage = await handleFetchError(response);
            throw new Error(errorMessage);
        }

        return await response.blob();
    },

    /**
     * 개선안 리포트 생성 및 다운로드
     * @param {string} interval - 조회 간격 (day, week, month)
     * @returns {Promise<Blob>} PDF 파일 Blob
     */
    generateImprovementReport: async (interval) => {
        const url = `${API_CONFIG.gateway}/api/dashboard/dashboards/improvement-report/${interval}`;
        
        const response = await fetch(url, {
            method: 'POST',
            headers: {
                'Authorization': authUtils.getAuthHeader()
            }
        });

        if (!response.ok) {
            const errorMessage = await handleFetchError(response);
            throw new Error(errorMessage);
        }

        return await response.blob();
    },

    /**
     * 저장된 리포트 목록 조회
     * @param {object} options
     * @param {number} options.page - 페이지 번호 (기본값: 0)
     * @param {number} options.size - 페이지당 개수 (기본값: 10)
     * @param {string} [options.reportType] - 리포트 타입 필터 (DAILY_REPORT, IMPROVEMENT_REPORT)
     * @param {string} [options.sortBy] - 정렬 기준 (createdAt, reportName)
     * @param {string} [options.sortDir] - 정렬 방향 (asc, desc)
     * @returns {Promise} 리포트 목록 데이터
     */
    getReports: async ({page = 0, size = 10, reportType = '', sortBy = 'createdAt', sortDir = 'desc'} = {}) => {
        const queryParams = new URLSearchParams({
            page: page.toString(),
            size: size.toString(),
            sortBy,
            sortDir
        });

        if (reportType) {
            queryParams.append('reportType', reportType);
        }

        const url = `${API_CONFIG.gateway}/api/dashboard/reports?${queryParams.toString()}`;
        return await apiRequest(url);
    },

    /**
     * 리포트 상세 정보 조회
     * @param {number} reportId - 리포트 ID
     * @returns {Promise} 리포트 상세 정보
     */
    getReportDetail: async (reportId) => {
        const url = `${API_CONFIG.gateway}/api/dashboard/reports/${reportId}`;
        return await apiRequest(url);
    },

    /**
     * 저장된 리포트 파일 다운로드
     * @param {number} reportId - 리포트 ID
     * @returns {Promise<Blob>} PDF 파일 Blob
     */
    downloadStoredReport: async (reportId) => {
        const url = `${API_CONFIG.gateway}/api/dashboard/reports/${reportId}/download`;
        
        const response = await fetch(url, {
            method: 'GET',
            headers: {
                'Authorization': authUtils.getAuthHeader()
            }
        });

        if (!response.ok) {
            const errorMessage = await handleFetchError(response);
            throw new Error(errorMessage);
        }

        return await response.blob();
    },

    /**
     * 리포트 삭제
     * @param {number} reportId - 삭제할 리포트 ID
     * @returns {Promise} 삭제 응답
     */
    deleteReport: async (reportId) => {
        const url = `${API_CONFIG.gateway}/api/dashboard/reports/${reportId}`;
        return await apiRequest(url, {
            method: 'DELETE'
        });
    },

    /**
     * 파일 다운로드 헬퍼 함수
     * @param {Blob} blob - 다운로드할 파일 Blob
     * @param {string} filename - 저장할 파일명
     */
    downloadFile: (blob, filename) => {
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = filename;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        window.URL.revokeObjectURL(url);
    }
};