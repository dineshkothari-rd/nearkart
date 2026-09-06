package com.nearkart.admin;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nearkart.common.ApiException;
import com.nearkart.common.PageView;
import com.nearkart.user.UserStatus;

@Service
class AdminService {
	private final JdbcClient jdbc;
	private final Clock clock;

	AdminService(JdbcClient jdbc, Clock clock) { this.jdbc = jdbc; this.clock = clock; }

	DashboardView dashboard() {
		return jdbc.sql("""
			SELECT (SELECT count(*) FROM users) users,
			  (SELECT count(*) FROM stores) stores,
			  (SELECT count(*) FROM stores WHERE status = 'PENDING') pending_stores,
			  (SELECT count(*) FROM products) products,
			  (SELECT count(*) FROM reviews WHERE status = 'PENDING') pending_reviews,
			  (SELECT count(*) FROM reports WHERE status = 'OPEN') open_reports,
			  (SELECT count(*) FROM search_history WHERE created_at >= now() - interval '30 days') searches
			""").query((rs, row) -> new DashboardView(rs.getLong("users"), rs.getLong("stores"),
			rs.getLong("pending_stores"), rs.getLong("products"), rs.getLong("pending_reviews"),
			rs.getLong("open_reports"), rs.getLong("searches"))).single();
	}

	PageView<UserView> users(int page, int size) {
		List<UserView> content = jdbc.sql("""
			SELECT u.id, u.email, u.display_name, u.status,
			  string_agg(r.name, ', ' ORDER BY r.name) roles, u.created_at
			FROM users u JOIN user_roles ur ON ur.user_id = u.id JOIN roles r ON r.id = ur.role_id
			GROUP BY u.id ORDER BY u.created_at DESC LIMIT :size OFFSET :offset
			""").param("size", size).param("offset", (long) page * size).query((rs, row) -> new UserView(
			rs.getObject("id", UUID.class), rs.getString("email"), rs.getString("display_name"),
			rs.getString("status"), rs.getString("roles"), rs.getTimestamp("created_at").toInstant())).list();
		return page(content, page, size, count("users"));
	}

	PageView<ReviewView> reviews(String status, int page, int size) {
		status = status == null ? "" : status;
		List<ReviewView> content = jdbc.sql("""
			SELECT r.id, r.store_id, s.name store_name, u.display_name, r.rating, r.text, r.status, r.created_at
			FROM reviews r JOIN users u ON u.id = r.user_id JOIN stores s ON s.id = r.store_id
			WHERE (:status = '' OR r.status = :status) ORDER BY r.created_at DESC LIMIT :size OFFSET :offset
			""").param("status", status).param("size", size).param("offset", (long) page * size)
			.query((rs, row) -> new ReviewView(rs.getObject("id", UUID.class),
				rs.getObject("store_id", UUID.class), rs.getString("store_name"), rs.getString("display_name"),
				rs.getInt("rating"), rs.getString("text"), rs.getString("status"),
				rs.getTimestamp("created_at").toInstant())).list();
		return page(content, page, size, countFiltered("reviews", status));
	}

	PageView<ReportView> reports(String status, int page, int size) {
		status = status == null ? "" : status;
		List<ReportView> content = jdbc.sql("""
			SELECT r.id, r.store_id, s.name store_name, u.display_name, r.type, r.details, r.status, r.created_at
			FROM reports r JOIN users u ON u.id = r.user_id JOIN stores s ON s.id = r.store_id
			WHERE (:status = '' OR r.status = :status) ORDER BY r.created_at DESC LIMIT :size OFFSET :offset
			""").param("status", status).param("size", size).param("offset", (long) page * size)
			.query((rs, row) -> new ReportView(rs.getObject("id", UUID.class),
				rs.getObject("store_id", UUID.class), rs.getString("store_name"), rs.getString("display_name"),
				rs.getString("type"), rs.getString("details"), rs.getString("status"),
				rs.getTimestamp("created_at").toInstant())).list();
		return page(content, page, size, countFiltered("reports", status));
	}

	PageView<AuditView> audits(int page, int size) {
		List<AuditView> content = jdbc.sql("""
			SELECT a.id, u.email actor, a.action, a.target_type, a.target_id, a.metadata::text, a.created_at
			FROM audit_logs a LEFT JOIN users u ON u.id = a.actor_id
			ORDER BY a.created_at DESC LIMIT :size OFFSET :offset
			""").param("size", size).param("offset", (long) page * size).query((rs, row) -> new AuditView(
			rs.getObject("id", UUID.class), rs.getString("actor"), rs.getString("action"),
			rs.getString("target_type"), rs.getObject("target_id", UUID.class), rs.getString("metadata"),
			rs.getTimestamp("created_at").toInstant())).list();
		return page(content, page, size, count("audit_logs"));
	}

	List<CategoryView> categories() {
		return jdbc.sql("SELECT id, name, slug FROM categories ORDER BY name")
			.query((rs, row) -> new CategoryView(rs.getObject("id", UUID.class), rs.getString("name"), rs.getString("slug"))).list();
	}

	PageView<ProductView> products(int page, int size) {
		List<ProductView> content = jdbc.sql("""
			SELECT p.id, p.category_id, c.name category, p.name, p.brand, p.status, count(v.id) variants
			FROM products p JOIN categories c ON c.id = p.category_id
			LEFT JOIN product_variants v ON v.product_id = p.id
			GROUP BY p.id, c.name ORDER BY p.name LIMIT :size OFFSET :offset
			""").param("size", size).param("offset", (long) page * size).query((rs, row) -> new ProductView(
			rs.getObject("id", UUID.class), rs.getObject("category_id", UUID.class), rs.getString("category"),
			rs.getString("name"), rs.getString("brand"), rs.getString("status"), rs.getLong("variants"))).list();
		return page(content, page, size, count("products"));
	}

	@Transactional
	void updateUser(UUID actorId, UUID userId, UserStatus status) {
		if (actorId.equals(userId) && status == UserStatus.SUSPENDED)
			throw new ApiException(BAD_REQUEST, "You cannot suspend your own account");
		Instant now = clock.instant();
		updated(jdbc.sql("UPDATE users SET status = :status, updated_at = :now WHERE id = :id")
			.param("status", status.name()).param("now", Timestamp.from(now)).param("id", userId).update(), "User");
		if (status == UserStatus.SUSPENDED)
			jdbc.sql("UPDATE refresh_tokens SET revoked_at = :now, updated_at = :now WHERE user_id = :id AND revoked_at IS NULL")
				.param("now", Timestamp.from(now)).param("id", userId).update();
		audit(actorId, "USER_" + status.name(), "USER", userId, now);
	}

	@Transactional
	void moderateReview(UUID actorId, UUID id, ReviewStatus status) {
		Instant now = clock.instant();
		updated(jdbc.sql("UPDATE reviews SET status = :status, updated_at = :now WHERE id = :id")
			.param("status", status.name()).param("now", Timestamp.from(now)).param("id", id).update(), "Review");
		audit(actorId, "REVIEW_" + status.name(), "REVIEW", id, now);
	}

	@Transactional
	void resolveReport(UUID actorId, UUID id, ReportStatus status) {
		Instant now = clock.instant();
		updated(jdbc.sql("UPDATE reports SET status = :status, updated_at = :now WHERE id = :id")
			.param("status", status.name()).param("now", Timestamp.from(now)).param("id", id).update(), "Report");
		audit(actorId, "REPORT_" + status.name(), "REPORT", id, now);
	}

	@Transactional
	void updateProduct(UUID actorId, UUID id, ProductStatus status) {
		Instant now = clock.instant();
		updated(jdbc.sql("UPDATE products SET status = :status, updated_at = :now WHERE id = :id")
			.param("status", status.name()).param("now", Timestamp.from(now)).param("id", id).update(), "Product");
		audit(actorId, "PRODUCT_" + status.name(), "PRODUCT", id, now);
	}

	private long count(String table) { return jdbc.sql("SELECT count(*) FROM " + table).query(Long.class).single(); }
	private long countFiltered(String table, String status) {
		return jdbc.sql("SELECT count(*) FROM " + table + " WHERE (:status = '' OR status = :status)")
			.param("status", status).query(Long.class).single();
	}
	private void audit(UUID actorId, String action, String targetType, UUID targetId, Instant now) {
		jdbc.sql("""
			INSERT INTO audit_logs (id, actor_id, action, target_type, target_id, metadata, created_at, updated_at)
			VALUES (:id, :actorId, :action, :targetType, :targetId, '{}'::jsonb, :now, :now)
			""").param("id", UUID.randomUUID()).param("actorId", actorId).param("action", action)
			.param("targetType", targetType).param("targetId", targetId).param("now", Timestamp.from(now)).update();
	}
	private static void updated(int rows, String resource) {
		if (rows == 0) throw new ApiException(NOT_FOUND, resource + " not found");
	}
	private static <T> PageView<T> page(List<T> content, int page, int size, long total) {
		return new PageView<>(content, page, size, total, (int) Math.ceil((double) total / size));
	}

	enum ReviewStatus { APPROVED, REJECTED }
	enum ReportStatus { RESOLVED, DISMISSED }
	enum ProductStatus { ACTIVE, INACTIVE }
	record DashboardView(long users, long stores, long pendingStores, long products,
		long pendingReviews, long openReports, long searches) {}
	record UserView(UUID id, String email, String displayName, String status, String roles, Instant createdAt) {}
	record ReviewView(UUID id, UUID storeId, String storeName, String displayName, int rating,
		String text, String status, Instant createdAt) {}
	record ReportView(UUID id, UUID storeId, String storeName, String displayName, String type,
		String details, String status, Instant createdAt) {}
	record AuditView(UUID id, String actor, String action, String targetType, UUID targetId,
		String metadata, Instant createdAt) {}
	record CategoryView(UUID id, String name, String slug) {}
	record ProductView(UUID id, UUID categoryId, String category, String name, String brand,
		String status, long variants) {}
}
