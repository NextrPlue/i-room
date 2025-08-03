package com.iroom.modulecommon.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
	// User Service - Authentication & Authorization (401)
	USER_UNREGISTERED_EMAIL("USER_1001", "가입되지 않은 이메일입니다.", HttpStatus.UNAUTHORIZED),
	USER_INVALID_PASSWORD("USER_1002", "잘못된 비밀번호입니다.", HttpStatus.UNAUTHORIZED),
	USER_INVALID_API_KEY("USER_1003", "유효하지 않은 API Key입니다.", HttpStatus.UNAUTHORIZED),

	// User Service - Bad Request (400)
	USER_EMAIL_ALREADY_EXISTS("USER_1004", "이미 사용 중인 이메일입니다.", HttpStatus.BAD_REQUEST),
	USER_CURRENT_PASSWORD_MISMATCH("USER_1005", "현재 비밀번호가 일치하지 않습니다.", HttpStatus.BAD_REQUEST),

	// User Service - Not Found (404)
	USER_WORKER_NOT_FOUND("USER_1006", "해당하는 근로자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
	USER_ADMIN_NOT_FOUND("USER_1007", "해당하는 관리자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);

	private final String code;
	private final String message;
	private final HttpStatus status;
}