package com.nearkart.search;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

final class SearchModels {
	private SearchModels() {}
	record Suggestion(UUID productId, String productName, String brand, UUID variantId, String variant) {}
	record Candidate(UUID productId, String productName, String brand, UUID variantId, String variant,
		UUID storeId, String storeName, BigDecimal amount, String currency, String availability,
		Instant observedAt, double distanceMeters, double storeLatitude, double storeLongitude) {}
	record Recommendation(double score, String reason) {}
	record NearbyOffer(UUID productId, String productName, String brand, String variant,
		UUID storeId, String storeName, BigDecimal amount, String currency, String availability,
		String freshness, Instant observedAt, long distanceMeters, double storeLatitude, double storeLongitude,
		Recommendation recommendation) {}
}
