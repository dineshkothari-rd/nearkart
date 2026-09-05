package com.nearkart.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import com.nearkart.auth.AuthModels.AccountType;
import com.nearkart.auth.AuthModels.RegisterRequest;
import com.nearkart.user.Role;
import com.nearkart.user.RoleName;
import com.nearkart.user.RoleRepository;
import com.nearkart.user.User;
import com.nearkart.user.UserRepository;

class AuthServiceTests {

	@Test
	void registrationHashesPasswordAndIssuesSession() {
		var users = mock(UserRepository.class);
		var roles = mock(RoleRepository.class);
		var refreshTokens = mock(RefreshTokenRepository.class);
		var role = mock(Role.class);
		var passwords = new BCryptPasswordEncoder(4);
		var key = new SecretKeySpec("test-secret-with-at-least-32-bytes".getBytes(), "HmacSHA256");
		var jwt = NimbusJwtEncoder.withSecretKey(key).algorithm(MacAlgorithm.HS256).build();
		var properties = new AuthProperties("unused", Duration.ofMinutes(15), Duration.ofDays(30), false);
		var clock = Clock.fixed(Instant.parse("2026-09-05T12:00:00Z"), ZoneOffset.UTC);
		var service = new AuthService(users, roles, refreshTokens, passwords, jwt, properties, clock);

		when(users.existsByEmail("customer@example.com")).thenReturn(false);
		when(roles.findByName(RoleName.CUSTOMER)).thenReturn(Optional.of(role));
		when(role.getName()).thenReturn(RoleName.CUSTOMER);
		when(users.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

		var session = service.register(new RegisterRequest(
			" Customer@Example.com ", "A-secure-password-123", "Customer", AccountType.CUSTOMER));

		var user = ArgumentCaptor.forClass(User.class);
		verify(users).save(user.capture());
		assertThat(user.getValue().getPasswordHash()).isNotEqualTo("A-secure-password-123");
		assertThat(passwords.matches("A-secure-password-123", user.getValue().getPasswordHash())).isTrue();
		assertThat(session.response().accessToken()).isNotBlank();
		verify(refreshTokens).save(any(RefreshToken.class));
	}
}
