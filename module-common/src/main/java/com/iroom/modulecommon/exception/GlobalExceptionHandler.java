package com.iroom.modulecommon.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.iroom.modulecommon.dto.response.ApiResponse;
import com.iroom.modulecommon.dto.response.ErrorDetail;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(CustomException.class)
	public ResponseEntity<ApiResponse<ErrorDetail>> handleCustomException(CustomException e) {
		log.error("CustomException occurred: {} - {}", e.getErrorCode(), e.getMessage());

		ErrorDetail errorDetail = new ErrorDetail(e.getErrorCode().getCode());

		ApiResponse<ErrorDetail> response = ApiResponse.error(e.getMessage(), errorDetail);

		return ResponseEntity.status(e.getErrorCode().getStatus()).body(response);
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ApiResponse<ErrorDetail>> handleAccessDeniedException(AccessDeniedException e) {
		log.error("AccessDeniedException occurred: {}", e.getMessage());

		ErrorDetail errorDetail = new ErrorDetail(ErrorCode.USER_ACCESS_DENIED.getCode());

		ApiResponse<ErrorDetail> response = ApiResponse.error(ErrorCode.USER_ACCESS_DENIED.getMessage(), errorDetail);

		return ResponseEntity.status(ErrorCode.USER_ACCESS_DENIED.getStatus()).body(response);
	}
}
