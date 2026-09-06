package com.nearkart.store;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.simple.JdbcClient;

import com.nearkart.common.ApiException;

class StoreAnalyticsServiceTests {

	@Test
	void analyticsAlwaysIncludesAuthenticatedOwner() {
		var stores = mock(StoreRepository.class);
		var ownerId = UUID.randomUUID();
		var storeId = UUID.randomUUID();
		when(stores.findByIdAndOwnerId(storeId, ownerId)).thenReturn(Optional.empty());
		var service = new StoreAnalyticsService(mock(JdbcClient.class), stores, Clock.systemUTC());

		assertThatThrownBy(() -> service.analytics(ownerId, storeId)).isInstanceOf(ApiException.class);
		verify(stores).findByIdAndOwnerId(storeId, ownerId);
	}
}
