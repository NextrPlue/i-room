package com.iroom.modulecommon.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
	// Global
	GLOBAL_VALIDATION_FAILED("GLOBAL_0001", "입력 값 검증에 실패했습니다.", HttpStatus.BAD_REQUEST),
	GLOBAL_ACCESS_DENIED("GLOBAL_0002", "해당 리소스에 대한 접근 권한이 없습니다.", HttpStatus.FORBIDDEN),
	GLOBAL_INVALID_REQUEST_FORMAT("GLOBAL_0003", "요청 형식이 올바르지 않습니다.", HttpStatus.BAD_REQUEST),
	GLOBAL_UNSUPPORTED_MEDIA_TYPE("GLOBAL_0004", "지원되지 않는 미디어 타입입니다.", HttpStatus.UNSUPPORTED_MEDIA_TYPE),
	GLOBAL_METHOD_NOT_ALLOWED("GLOBAL_0005", "지원되지 않는 HTTP 메서드입니다.", HttpStatus.METHOD_NOT_ALLOWED),
	GLOBAL_MISSING_PARAMETER("GLOBAL_0006", "필수 요청 파라미터가 누락되었습니다.", HttpStatus.BAD_REQUEST),
	GLOBAL_INTERNAL_SERVER_ERROR("GLOBAL_0007", "서버 내부 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),

	// User Service - Authentication & Authorization (401)
	USER_UNREGISTERED_EMAIL("USER_1001", "가입되지 않은 이메일입니다.", HttpStatus.UNAUTHORIZED),
	USER_INVALID_PASSWORD("USER_1002", "잘못된 비밀번호입니다.", HttpStatus.UNAUTHORIZED),
	USER_INVALID_API_KEY("USER_1003", "유효하지 않은 API Key입니다.", HttpStatus.UNAUTHORIZED),

	// User Service - Bad Request (400)
	USER_EMAIL_ALREADY_EXISTS("USER_1004", "이미 사용 중인 이메일입니다.", HttpStatus.BAD_REQUEST),
	USER_CURRENT_PASSWORD_MISMATCH("USER_1005", "현재 비밀번호가 일치하지 않습니다.", HttpStatus.BAD_REQUEST),

	// User Service - Not Found (404)
	USER_WORKER_NOT_FOUND("USER_1006", "해당하는 근로자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
	USER_ADMIN_NOT_FOUND("USER_1007", "해당하는 관리자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

	// Management Service - Bad Request (400)
	MANAGEMENT_INVALID_WORKER_ID("MANAGEMENT_2001", "workerId는 null일 수 없습니다.", HttpStatus.BAD_REQUEST),
	MANAGEMENT_WORKER_ALREADY_ENTERED("MANAGEMENT_2002", "이미 입장한 근로자입니다.", HttpStatus.BAD_REQUEST),
	MANAGEMENT_WORKER_NOT_ENTERED("MANAGEMENT_2003", "입장하지 않은 근로자는 퇴장할 수 없습니다.",
		HttpStatus.BAD_REQUEST),

	// Management Service - Not Found (404)
	MANAGEMENT_WORKER_NOT_FOUND("MANAGEMENT_2004", "유효하지 않은 근로자입니다.", HttpStatus.NOT_FOUND),

	// Sensor Service - Bad Request (400)
	SENSOR_INVALID_BINARY_DATA("SENSOR_3001", "센서 바이너리 데이터 형식이 올바르지 않습니다.",
		HttpStatus.BAD_REQUEST),
	SENSOR_INVALID_COORDINATES("SENSOR_3002", "유효하지 않은 위치 좌표입니다.", HttpStatus.BAD_REQUEST),
	SENSOR_INVALID_BIOMETRIC_DATA("SENSOR_3003", "유효하지 않은 생체 데이터입니다.", HttpStatus.BAD_REQUEST),
	SENSOR_INVALID_RADIUS("SENSOR_3004", "유효하지 않은 반경 값입니다.", HttpStatus.BAD_REQUEST),

	// Sensor Service - Not Found (404)
	SENSOR_WORKER_NOT_FOUND("SENSOR_3005", "유효하지 않은 근로자입니다.", HttpStatus.NOT_FOUND),
	SENSOR_EQUIPMENT_NOT_FOUND("SENSOR_3006", "해당 중장비를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);

	private final String code;
	private final String message;
	private final HttpStatus status;
}