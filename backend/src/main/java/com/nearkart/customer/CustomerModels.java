package com.nearkart.customer;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

final class CustomerModels {
	private CustomerModels() {}

	enum FavoriteType { PRODUCT, STORE }
	enum ReviewStatus { PENDING, APPROVED, REJECTED }
	enum ReportType { PRODUCT_UNAVAILABLE, WRONG_PRICE, STORE_CLOSED, WRONG_ADDRESS, INCORRECT_STORE_INFORMATION }
	enum ReportStatus { OPEN, RESOLVED, DISMISSED }

	record FavoriteRequest(@NotNull FavoriteType type, @NotNull UUID targetId) {}
	record FavoriteView(UUID id, FavoriteType type, UUID targetId, String name, String subtitle, Instant createdAt) {}
	record SearchHistoryView(UUID id, String query, Instant createdAt) {}
	record ReviewRequest(@NotNull UUID storeId, @Min(1) @Max(5) int rating,
		@NotBlank @Size(min = 3, max = 1000) String text) {}
	record ReviewView(UUID id, UUID storeId, String displayName, int rating, String text,
		ReviewStatus status, Instant createdAt) {}
	record ReportRequest(@NotNull UUID storeId, UUID storeProductId, @NotNull ReportType type,
		@Size(max = 1000) String details) {}
	record ReportView(UUID id, UUID storeId, UUID storeProductId, ReportType type, String details,
		ReportStatus status, Instant createdAt) {}
	record VariantView(UUID id, String label, String barcode) {}
	record ProductDetail(UUID id, String name, String brand, String category, List<VariantView> variants) {}
	record StoreProductView(UUID listingId, UUID productId, String productName, String brand,
		UUID variantId, String variant, BigDecimal amount, String currency, String availability,
		Instant observedAt) {}
}
