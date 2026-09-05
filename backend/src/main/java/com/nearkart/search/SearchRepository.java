package com.nearkart.search;

import java.util.List;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class SearchRepository {
	private final JdbcClient jdbc;
	SearchRepository(JdbcClient jdbc) { this.jdbc = jdbc; }

	List<SearchModels.Suggestion> suggestions(String query, int limit, int offset) {
		return jdbc.sql("""
			SELECT p.id, p.name, p.brand, v.id, v.label
			FROM products p JOIN product_variants v ON v.product_id = p.id
			WHERE p.status = 'ACTIVE' AND (p.normalized_name % :query OR p.normalized_name LIKE '%' || :query || '%'
			  OR EXISTS (SELECT 1 FROM product_aliases a WHERE a.product_id = p.id
			    AND (a.normalized_alias % :query OR a.normalized_alias LIKE '%' || :query || '%')))
			ORDER BY similarity(p.normalized_name, :query) DESC, p.name, v.label
			LIMIT :limit OFFSET :offset
			""").param("query", query).param("limit", limit).param("offset", offset)
			.query((rs, row) -> new SearchModels.Suggestion(rs.getObject(1, java.util.UUID.class), rs.getString(2),
				rs.getString(3), rs.getObject(4, java.util.UUID.class), rs.getString(5))).list();
	}

	List<SearchModels.Candidate> nearby(String query, double latitude, double longitude,
			double radiusMeters, double latitudeDelta, double longitudeDelta) {
		return jdbc.sql("""
			WITH offers AS (
			  SELECT p.id product_id, p.name product_name, p.brand, v.id variant_id, v.label variant,
			    s.id store_id, s.name store_name, pr.amount, pr.currency, i.availability, i.observed_at,
			    l.latitude store_latitude, l.longitude store_longitude,
			    6371000 * 2 * asin(sqrt(power(sin(radians(l.latitude - :latitude) / 2), 2)
			      + cos(radians(:latitude)) * cos(radians(l.latitude))
			      * power(sin(radians(l.longitude - :longitude) / 2), 2))) distance_meters
			  FROM products p JOIN product_variants v ON v.product_id = p.id
			  JOIN store_products sp ON sp.product_variant_id = v.id AND sp.status = 'ACTIVE'
			  JOIN stores s ON s.id = sp.store_id AND s.status = 'APPROVED'
			  JOIN store_locations l ON l.store_id = s.id
			  JOIN inventory i ON i.store_product_id = sp.id
			  JOIN prices pr ON pr.store_product_id = sp.id
			  WHERE p.status = 'ACTIVE'
			    AND l.latitude BETWEEN :latitude - :latitudeDelta AND :latitude + :latitudeDelta
			    AND l.longitude BETWEEN :longitude - :longitudeDelta AND :longitude + :longitudeDelta
			    AND (p.normalized_name % :query OR p.normalized_name LIKE '%' || :query || '%'
			      OR EXISTS (SELECT 1 FROM product_aliases a WHERE a.product_id = p.id
			        AND (a.normalized_alias % :query OR a.normalized_alias LIKE '%' || :query || '%')))
			)
			SELECT * FROM offers WHERE distance_meters <= :radius ORDER BY distance_meters LIMIT 500
			""").param("query", query).param("latitude", latitude).param("longitude", longitude)
			.param("latitudeDelta", latitudeDelta).param("longitudeDelta", longitudeDelta).param("radius", radiusMeters)
			.query((rs, row) -> new SearchModels.Candidate(rs.getObject("product_id", java.util.UUID.class),
				rs.getString("product_name"), rs.getString("brand"), rs.getObject("variant_id", java.util.UUID.class),
				rs.getString("variant"), rs.getObject("store_id", java.util.UUID.class), rs.getString("store_name"),
				rs.getBigDecimal("amount"), rs.getString("currency"), rs.getString("availability"),
				rs.getTimestamp("observed_at").toInstant(), rs.getDouble("distance_meters"),
				rs.getDouble("store_latitude"), rs.getDouble("store_longitude"))).list();
	}
}
