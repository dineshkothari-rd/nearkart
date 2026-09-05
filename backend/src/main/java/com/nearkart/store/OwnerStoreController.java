package com.nearkart.store;

import java.net.URI;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nearkart.common.ApiResponse;
import com.nearkart.common.PageView;
import com.nearkart.store.StoreModels.CreateStoreRequest;
import com.nearkart.store.StoreModels.HoursRequest;
import com.nearkart.store.StoreModels.StoreView;
import com.nearkart.store.StoreModels.UpdateStoreRequest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@RestController
@RequestMapping("/api/v1/owner/stores")
@PreAuthorize("hasRole('STORE_OWNER')")
class OwnerStoreController {

	private final StoreService stores;

	OwnerStoreController(StoreService stores) {
		this.stores = stores;
	}

	@PostMapping
	ResponseEntity<ApiResponse<StoreView>> create(@AuthenticationPrincipal Jwt jwt,
			@Valid @RequestBody CreateStoreRequest request) {
		StoreView store = stores.create(subject(jwt), request);
		return ResponseEntity.created(URI.create("/api/v1/owner/stores/" + store.id()))
			.body(ApiResponse.success(store));
	}

	@GetMapping
	ApiResponse<PageView<StoreView>> list(@AuthenticationPrincipal Jwt jwt,
			@RequestParam(defaultValue = "0") @Min(0) int page,
			@RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
		return ApiResponse.success(PageView.from(stores.owned(subject(jwt), PageRequest.of(page, size))));
	}

	@GetMapping("/{id}")
	ApiResponse<StoreView> get(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
		return ApiResponse.success(stores.owned(subject(jwt), id));
	}

	@PatchMapping("/{id}")
	ApiResponse<StoreView> update(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id,
			@Valid @RequestBody UpdateStoreRequest request) {
		return ApiResponse.success(stores.update(subject(jwt), id, request));
	}

	@PutMapping("/{id}/hours")
	ApiResponse<StoreView> hours(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id,
			@Valid @RequestBody HoursRequest request) {
		return ApiResponse.success(stores.replaceHours(subject(jwt), id, request));
	}

	private static UUID subject(Jwt jwt) {
		return UUID.fromString(jwt.getSubject());
	}
}
