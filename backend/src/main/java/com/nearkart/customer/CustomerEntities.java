package com.nearkart.customer;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity @Table(name = "favorites")
class Favorite {
	@Id UUID id;
	UUID userId;
	@Enumerated(EnumType.STRING) CustomerModels.FavoriteType targetType;
	UUID productId;
	UUID storeId;
	Instant createdAt;
	protected Favorite() {}
	Favorite(UUID userId, CustomerModels.FavoriteType type, UUID targetId, Instant now) {
		id = UUID.randomUUID(); this.userId = userId; targetType = type; createdAt = now;
		if (type == CustomerModels.FavoriteType.PRODUCT) productId = targetId; else storeId = targetId;
	}
}

@Entity @Table(name = "search_history")
class SearchHistory {
	@Id UUID id;
	UUID userId;
	String query;
	Instant createdAt;
	protected SearchHistory() {}
	SearchHistory(UUID userId, String query, Instant now) {
		id = UUID.randomUUID(); this.userId = userId; this.query = query; createdAt = now;
	}
}

@Entity @Table(name = "reviews")
class Review {
	@Id UUID id;
	UUID userId;
	UUID storeId;
	short rating;
	String text;
	@Enumerated(EnumType.STRING) CustomerModels.ReviewStatus status;
	Instant createdAt;
	Instant updatedAt;
	protected Review() {}
	Review(UUID userId, CustomerModels.ReviewRequest request, Instant now) {
		id = UUID.randomUUID(); this.userId = userId; storeId = request.storeId(); rating = (short) request.rating();
		text = request.text().trim(); status = CustomerModels.ReviewStatus.PENDING; createdAt = updatedAt = now;
	}
}

@Entity @Table(name = "reports")
class Report {
	@Id UUID id;
	UUID userId;
	UUID storeId;
	UUID storeProductId;
	@Enumerated(EnumType.STRING) CustomerModels.ReportType type;
	String details;
	@Enumerated(EnumType.STRING) CustomerModels.ReportStatus status;
	Instant createdAt;
	Instant updatedAt;
	protected Report() {}
	Report(UUID userId, CustomerModels.ReportRequest request, Instant now) {
		id = UUID.randomUUID(); this.userId = userId; storeId = request.storeId();
		storeProductId = request.storeProductId(); type = request.type();
		details = request.details() == null || request.details().isBlank() ? null : request.details().trim();
		status = CustomerModels.ReportStatus.OPEN; createdAt = updatedAt = now;
	}
}
