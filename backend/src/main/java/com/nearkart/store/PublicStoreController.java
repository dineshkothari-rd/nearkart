package com.nearkart.store;

import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nearkart.common.ApiResponse;
import com.nearkart.store.StoreModels.StoreView;

@RestController
@RequestMapping("/api/v1/stores")
class PublicStoreController {

	private final StoreService stores;
	private final StoreAnalyticsService analytics;

	PublicStoreController(StoreService stores, StoreAnalyticsService analytics) {
		this.stores = stores;
		this.analytics = analytics;
	}

	@GetMapping("/{id}")
	ApiResponse<StoreView> get(@PathVariable UUID id) {
		StoreView store = stores.publicStore(id);
		analytics.recordView(id);
		return ApiResponse.success(store);
	}

	@PostMapping("/{id}/directions")
	ApiResponse<Void> directions(@PathVariable UUID id) {
		stores.publicStore(id);
		analytics.recordDirections(id);
		return ApiResponse.success(null);
	}
}
