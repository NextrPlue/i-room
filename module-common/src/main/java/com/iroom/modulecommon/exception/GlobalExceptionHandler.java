package com.iroom.modulecommon.exception;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
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

	@ExceptionHandler(HttpMediaTypeNotSupportedException.class)
	public ResponseEntity<ApiResponse<ErrorDetail>> handleHttpMediaTypeNotSupportedException(
		HttpMediaTypeNotSupportedException e) {
		log.error("HttpMediaTypeNotSupportedException occurred: {}", e.getMessage());

		ErrorDetail errorDetail = new ErrorDetail(ErrorCode.GLOBAL_UNSUPPORTED_MEDIA_TYPE.getCode());

		ApiResponse<ErrorDetail> response = ApiResponse.error(ErrorCode.GLOBAL_UNSUPPORTED_MEDIA_TYPE.getMessage(),
			errorDetail);

		return ResponseEntity.status(ErrorCode.GLOBAL_UNSUPPORTED_MEDIA_TYPE.getStatus()).body(response);
	}

	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<ApiResponse<ErrorDetail>> handleHttpRequestMethodNotSupportedException(
		HttpRequestMethodNotSupportedException e) {
		log.error("HttpRequestMethodNotSupportedException occurred: {}", e.getMessage());

		ErrorDetail errorDetail = new ErrorDetail(ErrorCode.GLOBAL_METHOD_NOT_ALLOWED.getCode());

		ApiResponse<ErrorDetail> response = ApiResponse.error(ErrorCode.GLOBAL_METHOD_NOT_ALLOWED.getMessage(),
			errorDetail);

		return ResponseEntity.status(ErrorCode.GLOBAL_METHOD_NOT_ALLOWED.getStatus()).body(response);
	}

	@ExceptionHandler(MissingServletRequestParameterException.class)
	public ResponseEntity<ApiResponse<ErrorDetail>> handleMissingServletRequestParameterException(
		MissingServletRequestParameterException e) {
		log.error("MissingServletRequestParameterException occurred: {}", e.getMessage());

		String message = String.format("필수 파라미터 '%s'가 누락되었습니다.", e.getParameterName());
		ErrorDetail errorDetail = new ErrorDetail(ErrorCode.GLOBAL_MISSING_PARAMETER.getCode());

		ApiResponse<ErrorDetail> response = ApiResponse.error(message, errorDetail);

		return ResponseEntity.status(ErrorCode.GLOBAL_MISSING_PARAMETER.getStatus()).body(response);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<ErrorDetail>> handleException(Exception e) {
		log.error("Unexpected exception occurred: {}", e.getMessage(), e);

		ErrorDetail errorDetail = new ErrorDetail(ErrorCode.GLOBAL_INTERNAL_SERVER_ERROR.getCode());

		ApiResponse<ErrorDetail> response = ApiResponse.error(ErrorCode.GLOBAL_INTERNAL_SERVER_ERROR.getMessage(),
			errorDetail);

		return ResponseEntity.status(ErrorCode.GLOBAL_INTERNAL_SERVER_ERROR.getStatus()).body(response);
	}
}
