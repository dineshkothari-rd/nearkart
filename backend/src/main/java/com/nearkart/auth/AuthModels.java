package com.nearkart.auth;

import java.util.Set;
import java.util.UUID;

import com.nearkart.user.RoleName;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class AuthModels {

	private AuthModels() {
	}

	public enum AccountType {
		CUSTOMER,
		STORE_OWNER;

		RoleName role() {
			return RoleName.valueOf(name());
		}
	}

	public record RegisterRequest(
		@NotBlank @Email @Size(max = 320) String email,
		@NotBlank @Size(min = 12, max = 72) String password,
		@NotBlank @Size(max = 100) String displayName,
		@NotNull AccountType accountType) {
	}

	public record LoginRequest(
		@NotBlank @Email @Size(max = 320) String email,
		@NotBlank @Size(max = 72) String password) {
	}

	public record UserView(UUID id, String email, String displayName, Set<RoleName> roles) {
	}

	public record AuthResponse(String accessToken, long expiresInSeconds, UserView user) {
	}

	record Session(AuthResponse response, String refreshToken) {
	}
}
