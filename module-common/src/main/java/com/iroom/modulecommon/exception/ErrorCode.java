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
	USER_INVALID_REQUEST_FORMAT("USER_1006", "요청 형식이 올바르지 않습니다.", HttpStatus.BAD_REQUEST),

	// User Service - Forbidden (403)
	USER_ACCESS_DENIED("USER_1007", "해당 리소스에 대한 접근 권한이 없습니다.", HttpStatus.FORBIDDEN),

	// User Service - Not Found (404)
	USER_WORKER_NOT_FOUND("USER_1008", "해당하는 근로자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
	USER_ADMIN_NOT_FOUND("USER_1009", "해당하는 관리자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

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
	SENSOR_INVALID_COORDINATES("SENSOR_3003", "유효하지 않은 위치 좌표입니다.", HttpStatus.BAD_REQUEST),
	SENSOR_INVALID_BIOMETRIC_DATA("SENSOR_3004", "유효하지 않은 생체 데이터입니다.", HttpStatus.BAD_REQUEST),
	SENSOR_INVALID_RADIUS("SENSOR_3007", "유효하지 않은 반경 값입니다.", HttpStatus.BAD_REQUEST),

	// Sensor Service - Not Found (404)
	SENSOR_WORKER_NOT_FOUND("SENSOR_3010", "유효하지 않은 근로자입니다.", HttpStatus.NOT_FOUND),
	SENSOR_EQUIPMENT_NOT_FOUND("SENSOR_3011", "해당 중장비를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

	// Dashboard Service - Bad Request (400)
	DASHBOARD_INVALID_FILE_TYPE("DASHBOARD_4001", "허용되지 않는 파일 형식입니다.", HttpStatus.BAD_REQUEST),
	DASHBOARD_FILE_TOO_LARGE("DASHBOARD_4002", "파일 크기가 제한을 초과했습니다.", HttpStatus.BAD_REQUEST),
	DASHBOARD_INVALID_FILE_NAME("DASHBOARD_4003", "유효하지 않은 파일명입니다.", HttpStatus.BAD_REQUEST),

	// Dashboard Service - Not Found (404)
	DASHBOARD_BLUEPRINT_NOT_FOUND("DASHBOARD_4004", "해당 도면을 찾을 수 없습니다.", HttpStatus.NOT_FOUND);

	private final String code;
	private final String message;
	private final HttpStatus status;
}