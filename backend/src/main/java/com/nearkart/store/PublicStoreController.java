package com.nearkart.store;

import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nearkart.common.ApiResponse;
import com.nearkart.store.StoreModels.StoreView;

@RestController
@RequestMapping("/api/v1/stores")
class PublicStoreController {

	private final StoreService stores;

	PublicStoreController(StoreService stores) {
		this.stores = stores;
	}

	@GetMapping("/{id}")
	ApiResponse<StoreView> get(@PathVariable UUID id) {
		return ApiResponse.success(stores.publicStore(id));
	}
}
