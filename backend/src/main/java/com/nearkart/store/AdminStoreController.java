package com.nearkart.store;

import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nearkart.common.ApiResponse;
import com.nearkart.common.PageView;
import com.nearkart.store.StoreModels.ApprovalRequest;
import com.nearkart.store.StoreModels.StoreView;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@RestController
@RequestMapping("/api/v1/admin/stores")
@PreAuthorize("hasRole('ADMIN')")
class AdminStoreController {

	private final StoreService stores;

	AdminStoreController(StoreService stores) {
		this.stores = stores;
	}

	@GetMapping
	ApiResponse<PageView<StoreView>> pending(
			@RequestParam(defaultValue = "0") @Min(0) int page,
			@RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
		return ApiResponse.success(PageView.from(stores.pending(PageRequest.of(page, size))));
	}

	@PatchMapping("/{id}/approval")
	ApiResponse<StoreView> decide(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id,
			@Valid @RequestBody ApprovalRequest request) {
		return ApiResponse.success(stores.decide(UUID.fromString(jwt.getSubject()), id, request));
	}
}
