package com.nearkart.customer;

import static org.springframework.http.HttpStatus.NOT_FOUND;

import java.util.List;
import java.util.UUID;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import com.nearkart.common.ApiException;

@Repository
class CustomerReadRepository {
	private final JdbcClient jdbc;
	CustomerReadRepository(JdbcClient jdbc) { this.jdbc = jdbc; }

	boolean activeProductExists(UUID id) {
		return jdbc.sql("SELECT EXISTS (SELECT 1 FROM products WHERE id = :id AND status = 'ACTIVE')")
			.param("id", id).query(Boolean.class).single();
	}

	boolean approvedStoreExists(UUID id) {
		return jdbc.sql("SELECT EXISTS (SELECT 1 FROM stores WHERE id = :id AND status = 'APPROVED')")
			.param("id", id).query(Boolean.class).single();
	}

	boolean listingBelongsToStore(UUID listingId, UUID storeId) {
		return jdbc.sql("SELECT EXISTS (SELECT 1 FROM store_products WHERE id = :listingId AND store_id = :storeId)")
			.param("listingId", listingId).param("storeId", storeId).query(Boolean.class).single();
	}

	List<CustomerModels.FavoriteView> favorites(UUID userId) {
		return jdbc.sql("""
			SELECT f.id, f.target_type, COALESCE(f.product_id, f.store_id) target_id,
			  COALESCE(p.name, s.name) name,
			  CASE WHEN p.id IS NOT NULL THEN p.brand ELSE concat_ws(', ', l.locality, l.city) END subtitle,
			  f.created_at
			FROM favorites f
			LEFT JOIN products p ON p.id = f.product_id
			LEFT JOIN stores s ON s.id = f.store_id
			LEFT JOIN store_locations l ON l.store_id = s.id
			WHERE f.user_id = :userId
			ORDER BY f.created_at DESC
			""").param("userId", userId).query((rs, row) -> new CustomerModels.FavoriteView(
			rs.getObject("id", UUID.class), CustomerModels.FavoriteType.valueOf(rs.getString("target_type")),
			rs.getObject("target_id", UUID.class), rs.getString("name"), rs.getString("subtitle"),
			rs.getTimestamp("created_at").toInstant())).list();
	}

	CustomerModels.ProductDetail product(UUID id) {
		CustomerModels.ProductDetail product = jdbc.sql("""
			SELECT p.id, p.name, p.brand, c.name category
			FROM products p JOIN categories c ON c.id = p.category_id
			WHERE p.id = :id AND p.status = 'ACTIVE'
			""").param("id", id).query((rs, row) -> new CustomerModels.ProductDetail(
			rs.getObject("id", UUID.class), rs.getString("name"), rs.getString("brand"),
			rs.getString("category"), List.of())).optional()
			.orElseThrow(() -> new ApiException(NOT_FOUND, "Product not found"));
		List<CustomerModels.VariantView> variants = jdbc.sql("""
			SELECT id, label, barcode FROM product_variants WHERE product_id = :id ORDER BY label
			""").param("id", id).query((rs, row) -> new CustomerModels.VariantView(
			rs.getObject("id", UUID.class), rs.getString("label"), rs.getString("barcode"))).list();
		return new CustomerModels.ProductDetail(product.id(), product.name(), product.brand(), product.category(), variants);
	}

	List<CustomerModels.StoreProductView> storeProducts(UUID storeId) {
		if (!approvedStoreExists(storeId)) throw new ApiException(NOT_FOUND, "Store not found");
		return jdbc.sql("""
			SELECT sp.id listing_id, p.id product_id, p.name product_name, p.brand,
			  v.id variant_id, v.label variant, pr.amount, pr.currency, i.availability, i.observed_at
			FROM store_products sp
			JOIN product_variants v ON v.id = sp.product_variant_id
			JOIN products p ON p.id = v.product_id AND p.status = 'ACTIVE'
			JOIN prices pr ON pr.store_product_id = sp.id
			JOIN inventory i ON i.store_product_id = sp.id
			WHERE sp.store_id = :storeId AND sp.status = 'ACTIVE'
			ORDER BY p.name, v.label
			""").param("storeId", storeId).query((rs, row) -> new CustomerModels.StoreProductView(
			rs.getObject("listing_id", UUID.class), rs.getObject("product_id", UUID.class),
			rs.getString("product_name"), rs.getString("brand"), rs.getObject("variant_id", UUID.class),
			rs.getString("variant"), rs.getBigDecimal("amount"), rs.getString("currency"),
			rs.getString("availability"), rs.getTimestamp("observed_at").toInstant())).list();
	}

	List<CustomerModels.ReviewView> reviews(UUID storeId) {
		if (!approvedStoreExists(storeId)) throw new ApiException(NOT_FOUND, "Store not found");
		return jdbc.sql("""
			SELECT r.id, r.store_id, u.display_name, r.rating, r.text, r.status, r.created_at
			FROM reviews r JOIN users u ON u.id = r.user_id
			WHERE r.store_id = :storeId AND r.status = 'APPROVED'
			ORDER BY r.created_at DESC LIMIT 100
			""").param("storeId", storeId).query((rs, row) -> new CustomerModels.ReviewView(
			rs.getObject("id", UUID.class), rs.getObject("store_id", UUID.class), rs.getString("display_name"),
			rs.getInt("rating"), rs.getString("text"), CustomerModels.ReviewStatus.valueOf(rs.getString("status")),
			rs.getTimestamp("created_at").toInstant())).list();
	}
}
