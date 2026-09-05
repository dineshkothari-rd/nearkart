package com.nearkart.customer;

import java.util.List;
import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nearkart.common.ApiResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1")
class CustomerController {
	private final CustomerService customers;
	CustomerController(CustomerService customers) { this.customers = customers; }

	@GetMapping("/products/{id}")
	ApiResponse<CustomerModels.ProductDetail> product(@PathVariable UUID id) {
		return ApiResponse.success(customers.product(id));
	}

	@GetMapping("/stores/{id}/products")
	ApiResponse<List<CustomerModels.StoreProductView>> storeProducts(@PathVariable UUID id) {
		return ApiResponse.success(customers.storeProducts(id));
	}

	@GetMapping("/stores/{id}/reviews")
	ApiResponse<List<CustomerModels.ReviewView>> reviews(@PathVariable UUID id) {
		return ApiResponse.success(customers.publicReviews(id));
	}

	@GetMapping("/me/favorites")
	@PreAuthorize("hasRole('CUSTOMER')")
	ApiResponse<List<CustomerModels.FavoriteView>> favorites(@AuthenticationPrincipal Jwt jwt) {
		return ApiResponse.success(customers.favorites(userId(jwt)));
	}

	@PostMapping("/favorites")
	@PreAuthorize("hasRole('CUSTOMER')")
	ApiResponse<CustomerModels.FavoriteView> favorite(@AuthenticationPrincipal Jwt jwt,
			@Valid @RequestBody CustomerModels.FavoriteRequest request) {
		return ApiResponse.success(customers.favorite(userId(jwt), request));
	}

	@DeleteMapping("/favorites/{id}")
	@PreAuthorize("hasRole('CUSTOMER')")
	ApiResponse<Void> deleteFavorite(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
		customers.deleteFavorite(userId(jwt), id); return ApiResponse.success(null);
	}

	@GetMapping("/me/search-history")
	@PreAuthorize("hasRole('CUSTOMER')")
	ApiResponse<List<CustomerModels.SearchHistoryView>> history(@AuthenticationPrincipal Jwt jwt) {
		return ApiResponse.success(customers.history(userId(jwt)));
	}

	@DeleteMapping("/me/search-history")
	@PreAuthorize("hasRole('CUSTOMER')")
	ApiResponse<Void> clearHistory(@AuthenticationPrincipal Jwt jwt) {
		customers.clearHistory(userId(jwt)); return ApiResponse.success(null);
	}

	@PostMapping("/reviews")
	@PreAuthorize("hasRole('CUSTOMER')")
	ApiResponse<CustomerModels.ReviewView> review(@AuthenticationPrincipal Jwt jwt,
			@Valid @RequestBody CustomerModels.ReviewRequest request) {
		return ApiResponse.success(customers.review(userId(jwt), request));
	}

	@PostMapping("/reports")
	@PreAuthorize("hasRole('CUSTOMER')")
	ApiResponse<CustomerModels.ReportView> report(@AuthenticationPrincipal Jwt jwt,
			@Valid @RequestBody CustomerModels.ReportRequest request) {
		return ApiResponse.success(customers.report(userId(jwt), request));
	}

	private static UUID userId(Jwt jwt) { return UUID.fromString(jwt.getSubject()); }
}
