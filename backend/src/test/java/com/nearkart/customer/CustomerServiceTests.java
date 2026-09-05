package com.nearkart.customer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.nearkart.user.User;
import com.nearkart.user.UserRepository;
import com.nearkart.user.UserStatus;

class CustomerServiceTests {
	@Test
	void recordsOnlyTheNormalizedQueryWithoutLocation() {
		var favorites = mock(FavoriteRepository.class);
		var history = mock(SearchHistoryRepository.class);
		var users = mock(UserRepository.class);
		var user = mock(User.class);
		UUID userId = UUID.randomUUID();
		when(users.findById(userId)).thenReturn(Optional.of(user));
		when(user.getStatus()).thenReturn(UserStatus.ACTIVE);
		var service = new CustomerService(favorites, history, mock(ReviewRepository.class),
			mock(ReportRepository.class), mock(CustomerReadRepository.class), users,
			Clock.fixed(Instant.parse("2026-09-06T00:00:00Z"), ZoneOffset.UTC));

		service.recordSearch(userId, "  Amul   Butter  ");

		verify(history).deleteAllByUserIdAndQueryIgnoreCase(userId, "Amul Butter");
		var saved = ArgumentCaptor.forClass(SearchHistory.class);
		verify(history).save(saved.capture());
		assertThat(saved.getValue().query).isEqualTo("Amul Butter");
	}
}
