package com.nearkart.auth;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.boot.ApplicationArguments;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.nearkart.audit.AuditLog;
import com.nearkart.audit.AuditLogRepository;
import com.nearkart.user.Role;
import com.nearkart.user.RoleName;
import com.nearkart.user.RoleRepository;
import com.nearkart.user.User;
import com.nearkart.user.UserRepository;

class AdminBootstrapTests {
	@Test
	void createsTheConfiguredAdminOnlyOnce() {
		var users = mock(UserRepository.class);
		var roles = mock(RoleRepository.class);
		var audits = mock(AuditLogRepository.class);
		var encoder = mock(PasswordEncoder.class);
		var role = mock(Role.class);
		when(role.getName()).thenReturn(RoleName.ADMIN);
		when(roles.findByName(RoleName.ADMIN)).thenReturn(Optional.of(role));
		when(encoder.encode("production-password")).thenReturn("hash");
		when(users.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(users.findByEmail("admin@nearkart.test")).thenReturn(Optional.empty())
			.thenAnswer(invocation -> Optional.of(new User("admin@nearkart.test", "hash", "NearKart Admin", role,
				Instant.parse("2026-09-05T12:00:00Z"))));
		var bootstrap = new AdminBootstrap(users, roles, audits, encoder,
			Clock.fixed(Instant.parse("2026-09-05T12:00:00Z"), ZoneOffset.UTC),
			"admin@nearkart.test", "production-password");

		bootstrap.run(mock(ApplicationArguments.class));
		bootstrap.run(mock(ApplicationArguments.class));

		verify(users, times(1)).save(any(User.class));
		verify(audits, times(1)).save(any(AuditLog.class));
	}
}
