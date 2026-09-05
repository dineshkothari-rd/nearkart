package com.nearkart.common;

import java.util.List;

import org.springframework.data.domain.Page;

public record PageView<T>(List<T> content, int page, int size, long totalElements, int totalPages) {

	public static <T> PageView<T> from(Page<T> page) {
		return new PageView<>(page.getContent(), page.getNumber(), page.getSize(),
			page.getTotalElements(), page.getTotalPages());
	}
}
