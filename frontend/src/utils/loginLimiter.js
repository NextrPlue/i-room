const LOGIN_ATTEMPT_KEY = 'loginAttempts';
const LOCKOUT_KEY = 'lockoutUntil';
const MAX_ATTEMPTS = 5;
const LOCKOUT_DURATION = 5 * 60 * 1000; // 5분

export const loginLimiter = {
    // 로그인 실패 횟수 증가
    recordFailedAttempt: (identifier) => {
        const key = `${LOGIN_ATTEMPT_KEY}_${identifier}`;
        const attempts = parseInt(localStorage.getItem(key) || '0');
        const newAttempts = attempts + 1;
        
        localStorage.setItem(key, newAttempts.toString());
        
        if (newAttempts >= MAX_ATTEMPTS) {
            const lockoutUntil = Date.now() + LOCKOUT_DURATION;
            localStorage.setItem(`${LOCKOUT_KEY}_${identifier}`, lockoutUntil.toString());
        }
        
        return newAttempts;
    },
    
    // 로그인 성공 시 시도 횟수 초기화
    clearFailedAttempts: (identifier) => {
        localStorage.removeItem(`${LOGIN_ATTEMPT_KEY}_${identifier}`);
        localStorage.removeItem(`${LOCKOUT_KEY}_${identifier}`);
    },
    
    // 현재 로그인 제한 상태 확인
    isLocked: (identifier) => {
        const lockoutUntil = localStorage.getItem(`${LOCKOUT_KEY}_${identifier}`);
        if (!lockoutUntil) return false;
        
        const now = Date.now();
        const lockoutTime = parseInt(lockoutUntil);
        
        if (now >= lockoutTime) {
            // 제한 시간이 지났으면 데이터 삭제
            loginLimiter.clearFailedAttempts(identifier);
            return false;
        }
        
        return true;
    },
    
    // 남은 제한 시간 반환 (밀리초)
    getRemainingLockoutTime: (identifier) => {
        const lockoutUntil = localStorage.getItem(`${LOCKOUT_KEY}_${identifier}`);
        if (!lockoutUntil) return 0;
        
        const remainingTime = parseInt(lockoutUntil) - Date.now();
        return Math.max(0, remainingTime);
    },
    
    // 현재 실패 횟수 반환
    getFailedAttempts: (identifier) => {
        return parseInt(localStorage.getItem(`${LOGIN_ATTEMPT_KEY}_${identifier}`) || '0');
    },
    
    // 남은 시도 횟수 반환
    getRemainingAttempts: (identifier) => {
        const failedAttempts = loginLimiter.getFailedAttempts(identifier);
        return Math.max(0, MAX_ATTEMPTS - failedAttempts);
    },
    
    // 시간을 "분:초" 형태로 포맷
    formatRemainingTime: (milliseconds) => {
        const totalSeconds = Math.ceil(milliseconds / 1000);
        const minutes = Math.floor(totalSeconds / 60);
        const seconds = totalSeconds % 60;
        return `${minutes}:${seconds.toString().padStart(2, '0')}`;
    },
    
    // 상수 값들 노출
    MAX_ATTEMPTS,
    LOCKOUT_DURATION
};