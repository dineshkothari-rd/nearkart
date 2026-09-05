package com.nearkart.store;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.nearkart.audit.AuditLogRepository;
import com.nearkart.common.ApiException;
import com.nearkart.user.UserRepository;

class StoreServiceTests {

	@Test
	void ownerLookupAlwaysIncludesAuthenticatedOwner() {
		var stores = mock(StoreRepository.class);
		var ownerId = UUID.randomUUID();
		var storeId = UUID.randomUUID();
		when(stores.findByIdAndOwnerId(storeId, ownerId)).thenReturn(Optional.empty());
		var service = new StoreService(stores, mock(UserRepository.class), mock(AuditLogRepository.class), Clock.systemUTC());

		assertThatThrownBy(() -> service.owned(ownerId, storeId)).isInstanceOf(ApiException.class);
		verify(stores).findByIdAndOwnerId(storeId, ownerId);
	}
}
