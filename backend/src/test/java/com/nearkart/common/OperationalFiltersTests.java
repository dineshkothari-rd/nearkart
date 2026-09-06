package com.nearkart.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import jakarta.servlet.FilterChain;

class OperationalFiltersTests {
	@Test
	void requestIdsAreSafeAndRateLimitsRejectExcessAuthAttempts() throws Exception {
		var request = new MockHttpServletRequest("POST", "/api/v1/auth/login");
		request.addHeader("X-Request-ID", "unsafe request id");
		var response = new MockHttpServletResponse();
		new RequestIdFilter().doFilter(request, response, mock(FilterChain.class));
		assertThat(response.getHeader("X-Request-ID")).matches("[a-f0-9-]{36}");

		var limiter = new RateLimitFilter(Clock.fixed(Instant.EPOCH, ZoneOffset.UTC));
		for (int attempt = 0; attempt < 21; attempt++) {
			response = new MockHttpServletResponse();
			limiter.doFilter(request, response, mock(FilterChain.class));
		}
		assertThat(response.getStatus()).isEqualTo(429);
		assertThat(response.getHeader("Retry-After")).isEqualTo("60");
	}
}
