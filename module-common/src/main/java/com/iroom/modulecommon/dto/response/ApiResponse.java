package com.iroom.modulecommon.dto.response;

import java.time.LocalDateTime;

public record ApiResponse<T>(
	String status,
	String message,
	T data,
	LocalDateTime timestamp
) {
	public static <T> ApiResponse<T> success(T data) {
		return new ApiResponse<>(
			"success",
			"요청이 성공적으로 처리되었습니다.",
			data,
			LocalDateTime.now()
		);
	}

	public static <T> ApiResponse<T> error(String message, T data) {
		return new ApiResponse<>(
			"error",
			message,
			data,
			LocalDateTime.now()
		);
	}

}
