package com.nearkart.store;

import static org.springframework.http.HttpStatus.NOT_FOUND;

import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.UUID;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nearkart.common.ApiException;

@Service
public class StoreAnalyticsService {
	private final JdbcClient jdbc;
	private final StoreRepository stores;
	private final Clock clock;

	StoreAnalyticsService(JdbcClient jdbc, StoreRepository stores, Clock clock) {
		this.jdbc = jdbc; this.stores = stores; this.clock = clock;
	}

	@Transactional
	public void recordSearchImpressions(Collection<UUID> storeIds) {
		storeIds.stream().distinct().forEach(id -> record(id, "SEARCH_IMPRESSION"));
	}

	@Transactional
	void recordView(UUID storeId) { record(storeId, "STORE_VIEW"); }

	@Transactional
	void recordDirections(UUID storeId) { record(storeId, "DIRECTIONS_CLICK"); }

	@Transactional(readOnly = true)
	AnalyticsView analytics(UUID ownerId, UUID storeId) {
		stores.findByIdAndOwnerId(storeId, ownerId)
			.orElseThrow(() -> new ApiException(NOT_FOUND, "Store not found"));
		Instant since = clock.instant().minus(30, ChronoUnit.DAYS);
		return jdbc.sql("""
			SELECT count(sp.id) products,
			  count(sp.id) FILTER (WHERE i.availability = 'AVAILABLE') available,
			  count(sp.id) FILTER (WHERE i.availability = 'LOW_STOCK') low_stock,
			  count(sp.id) FILTER (WHERE i.availability = 'OUT_OF_STOCK') out_of_stock,
			  (SELECT count(*) FROM store_events e WHERE e.store_id = :storeId AND e.event_type = 'SEARCH_IMPRESSION' AND e.created_at >= :since) searches,
			  (SELECT count(*) FROM store_events e WHERE e.store_id = :storeId AND e.event_type = 'STORE_VIEW' AND e.created_at >= :since) store_views,
			  (SELECT count(*) FROM store_events e WHERE e.store_id = :storeId AND e.event_type = 'DIRECTIONS_CLICK' AND e.created_at >= :since) directions_clicks
			FROM store_products sp LEFT JOIN inventory i ON i.store_product_id = sp.id
			WHERE sp.store_id = :storeId AND sp.status = 'ACTIVE'
			""").param("storeId", storeId).param("since", Timestamp.from(since))
			.query((rs, row) -> new AnalyticsView(rs.getLong("products"), rs.getLong("available"),
				rs.getLong("low_stock"), rs.getLong("out_of_stock"), rs.getLong("searches"),
				rs.getLong("store_views"), rs.getLong("directions_clicks"), since)).single();
	}

	private void record(UUID storeId, String type) {
		jdbc.sql("""
			INSERT INTO store_events (id, store_id, event_type, created_at)
			SELECT :id, id, :type, :createdAt FROM stores WHERE id = :storeId AND status = 'APPROVED'
			""").param("id", UUID.randomUUID()).param("storeId", storeId)
			.param("type", type).param("createdAt", Timestamp.from(clock.instant())).update();
	}

	public record AnalyticsView(long products, long available, long lowStock, long outOfStock,
		long searches, long storeViews, long directionsClicks, Instant since) {}
}
