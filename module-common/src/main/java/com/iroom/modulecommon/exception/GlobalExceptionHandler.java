package com.iroom.modulecommon.exception;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
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

		ErrorDetail errorDetail = new ErrorDetail(ErrorCode.GLOBAL_ACCESS_DENIED.getCode());

		ApiResponse<ErrorDetail> response = ApiResponse.error(ErrorCode.GLOBAL_ACCESS_DENIED.getMessage(), errorDetail);

		return ResponseEntity.status(ErrorCode.GLOBAL_ACCESS_DENIED.getStatus()).body(response);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiResponse<ErrorDetail>> handleHttpMessageNotReadableException(
		HttpMessageNotReadableException e) {
		log.error("HttpMessageNotReadableException occurred: {}", e.getMessage());

		ErrorDetail errorDetail = new ErrorDetail(ErrorCode.GLOBAL_INVALID_REQUEST_FORMAT.getCode());

		ApiResponse<ErrorDetail> response = ApiResponse.error(ErrorCode.GLOBAL_INVALID_REQUEST_FORMAT.getMessage(),
			errorDetail);

		return ResponseEntity.status(ErrorCode.GLOBAL_INVALID_REQUEST_FORMAT.getStatus()).body(response);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse<ErrorDetail>> handleMethodArgumentNotValidException(
		MethodArgumentNotValidException e) {
		log.error("MethodArgumentNotValidException occurred: {}", e.getMessage());

		String errorMessage = e.getBindingResult().getFieldErrors().stream()
			.findFirst()
			.map(DefaultMessageSourceResolvable::getDefaultMessage)
			.orElse(ErrorCode.GLOBAL_VALIDATION_FAILED.getMessage());

		ErrorDetail errorDetail = new ErrorDetail(ErrorCode.GLOBAL_VALIDATION_FAILED.getCode());

		ApiResponse<ErrorDetail> response = ApiResponse.error(errorMessage, errorDetail);

		return ResponseEntity.status(ErrorCode.GLOBAL_VALIDATION_FAILED.getStatus()).body(response);
	}
}
