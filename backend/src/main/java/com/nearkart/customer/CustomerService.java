package com.nearkart.customer;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nearkart.common.ApiException;
import com.nearkart.user.UserRepository;
import com.nearkart.user.UserStatus;

@Service
public class CustomerService {
	private final FavoriteRepository favorites;
	private final SearchHistoryRepository history;
	private final ReviewRepository reviews;
	private final ReportRepository reports;
	private final CustomerReadRepository reads;
	private final UserRepository users;
	private final Clock clock;

	CustomerService(FavoriteRepository favorites, SearchHistoryRepository history, ReviewRepository reviews,
			ReportRepository reports, CustomerReadRepository reads, UserRepository users, Clock clock) {
		this.favorites = favorites; this.history = history; this.reviews = reviews; this.reports = reports;
		this.reads = reads; this.users = users; this.clock = clock;
	}

	@Transactional
	CustomerModels.FavoriteView favorite(UUID userId, CustomerModels.FavoriteRequest request) {
		activeUser(userId);
		boolean exists = request.type() == CustomerModels.FavoriteType.PRODUCT
			? reads.activeProductExists(request.targetId()) : reads.approvedStoreExists(request.targetId());
		if (!exists) throw new ApiException(NOT_FOUND, request.type() == CustomerModels.FavoriteType.PRODUCT
			? "Product not found" : "Store not found");
		try {
			Favorite saved = favorites.saveAndFlush(new Favorite(userId, request.type(), request.targetId(), clock.instant()));
			return reads.favorites(userId).stream().filter(item -> item.id().equals(saved.id)).findFirst().orElseThrow();
		} catch (DataIntegrityViolationException exception) {
			throw new ApiException(CONFLICT, "Already saved");
		}
	}

	@Transactional(readOnly = true)
	List<CustomerModels.FavoriteView> favorites(UUID userId) {
		activeUser(userId); return reads.favorites(userId);
	}

	@Transactional
	void deleteFavorite(UUID userId, UUID favoriteId) {
		activeUser(userId); favorites.deleteByIdAndUserId(favoriteId, userId);
	}

	@Transactional
	public void recordSearch(UUID userId, String query) {
		activeUser(userId); String cleaned = query.trim().replaceAll("\\s+", " ");
		history.deleteAllByUserIdAndQueryIgnoreCase(userId, cleaned);
		history.save(new SearchHistory(userId, cleaned, clock.instant()));
	}

	@Transactional(readOnly = true)
	List<CustomerModels.SearchHistoryView> history(UUID userId) {
		activeUser(userId);
		return history.findTop20ByUserIdOrderByCreatedAtDesc(userId).stream()
			.map(item -> new CustomerModels.SearchHistoryView(item.id, item.query, item.createdAt)).toList();
	}

	@Transactional
	void clearHistory(UUID userId) { activeUser(userId); history.deleteAllByUserId(userId); }

	@Transactional
	CustomerModels.ReviewView review(UUID userId, CustomerModels.ReviewRequest request) {
		activeUser(userId);
		if (!reads.approvedStoreExists(request.storeId())) throw new ApiException(NOT_FOUND, "Store not found");
		try {
			Review saved = reviews.saveAndFlush(new Review(userId, request, clock.instant()));
			return new CustomerModels.ReviewView(saved.id, saved.storeId, displayName(userId), saved.rating,
				saved.text, saved.status, saved.createdAt);
		} catch (DataIntegrityViolationException exception) {
			throw new ApiException(CONFLICT, "You have already reviewed this store");
		}
	}

	@Transactional
	CustomerModels.ReportView report(UUID userId, CustomerModels.ReportRequest request) {
		activeUser(userId);
		if (!reads.approvedStoreExists(request.storeId())) throw new ApiException(NOT_FOUND, "Store not found");
		if (request.storeProductId() != null && !reads.listingBelongsToStore(request.storeProductId(), request.storeId()))
			throw new ApiException(NOT_FOUND, "Store product not found");
		try {
			Report saved = reports.saveAndFlush(new Report(userId, request, clock.instant()));
			return new CustomerModels.ReportView(saved.id, saved.storeId, saved.storeProductId, saved.type,
				saved.details, saved.status, saved.createdAt);
		} catch (DataIntegrityViolationException exception) {
			throw new ApiException(CONFLICT, "This issue has already been reported");
		}
	}

	CustomerModels.ProductDetail product(UUID id) { return reads.product(id); }
	List<CustomerModels.StoreProductView> storeProducts(UUID id) { return reads.storeProducts(id); }
	List<CustomerModels.ReviewView> publicReviews(UUID id) { return reads.reviews(id); }

	private void activeUser(UUID id) {
		users.findById(id).filter(user -> user.getStatus() == UserStatus.ACTIVE)
			.orElseThrow(() -> new ApiException(NOT_FOUND, "User not found"));
	}

	private String displayName(UUID id) { return users.findById(id).orElseThrow().getDisplayName(); }
}
