package com.nearkart.auth;

import java.time.Duration;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nearkart.auth.AuthModels.AuthResponse;
import com.nearkart.auth.AuthModels.LoginRequest;
import com.nearkart.auth.AuthModels.RegisterRequest;
import com.nearkart.auth.AuthModels.Session;
import com.nearkart.common.ApiResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
class AuthController {

	private static final String REFRESH_COOKIE = "nearkart_refresh";

	private final AuthService auth;
	private final AuthProperties properties;

	AuthController(AuthService auth, AuthProperties properties) {
		this.auth = auth;
		this.properties = properties;
	}

	@PostMapping("/register")
	ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
		return session(auth.register(request));
	}

	@PostMapping("/login")
	ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
		return session(auth.login(request));
	}

	@PostMapping("/refresh")
	ResponseEntity<ApiResponse<AuthResponse>> refresh(
			@CookieValue(name = REFRESH_COOKIE, required = false) String refreshToken) {
		return session(auth.refresh(refreshToken));
	}

	@PostMapping("/logout")
	ResponseEntity<ApiResponse<Void>> logout(
			@CookieValue(name = REFRESH_COOKIE, required = false) String refreshToken) {
		auth.logout(refreshToken);
		return ResponseEntity.ok()
			.header(HttpHeaders.SET_COOKIE, cookie("", Duration.ZERO).toString())
			.body(ApiResponse.success(null));
	}

	private ResponseEntity<ApiResponse<AuthResponse>> session(Session session) {
		return ResponseEntity.ok()
			.header(HttpHeaders.SET_COOKIE, cookie(session.refreshToken(), properties.refreshTtl()).toString())
			.body(ApiResponse.success(session.response()));
	}

	private ResponseCookie cookie(String value, Duration maxAge) {
		return ResponseCookie.from(REFRESH_COOKIE, value)
			.httpOnly(true)
			.secure(properties.secureCookie())
			.sameSite("Strict")
			.path("/api/v1/auth")
			.maxAge(maxAge)
			.build();
	}
}
