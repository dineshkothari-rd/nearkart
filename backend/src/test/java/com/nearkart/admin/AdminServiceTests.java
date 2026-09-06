package com.nearkart.admin;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import java.time.Clock;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.simple.JdbcClient;

import com.nearkart.common.ApiException;
import com.nearkart.user.UserStatus;

class AdminServiceTests {
	@Test
	void adminCannotSuspendOwnAccount() {
		UUID adminId = UUID.randomUUID();
		var service = new AdminService(mock(JdbcClient.class), Clock.systemUTC());
		assertThatThrownBy(() -> service.updateUser(adminId, adminId, UserStatus.SUSPENDED))
			.isInstanceOf(ApiException.class).hasMessage("You cannot suspend your own account");
	}
}
