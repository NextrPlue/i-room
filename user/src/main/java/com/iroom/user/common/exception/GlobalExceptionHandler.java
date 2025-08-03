package com.iroom.user.common.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.iroom.modulecommon.dto.response.ApiResponse;
import com.iroom.modulecommon.dto.response.ErrorDetail;
import com.iroom.modulecommon.exception.CustomException;

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
}
