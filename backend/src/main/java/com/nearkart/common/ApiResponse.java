package com.nearkart.common;

import java.util.List;

public record ApiResponse<T>(boolean success, T data, String message, List<FieldError> errors) {

	public static <T> ApiResponse<T> success(T data) {
		return new ApiResponse<>(true, data, null, List.of());
	}

	public static ApiResponse<Void> error(String message, List<FieldError> errors) {
		return new ApiResponse<>(false, null, message, errors);
	}

	public record FieldError(String field, String message) {
	}
}
