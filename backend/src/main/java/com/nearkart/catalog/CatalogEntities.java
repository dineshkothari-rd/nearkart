package com.nearkart.catalog;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity @Table(name = "categories")
class Category {
	@Id UUID id;
	String name;
	String slug;
	Instant createdAt;
	Instant updatedAt;
	protected Category() {}
	Category(String name, String slug, Instant now) { id = UUID.randomUUID(); this.name = name; this.slug = slug; createdAt = updatedAt = now; }
}

@Entity @Table(name = "products")
class Product {
	@Id UUID id;
	UUID categoryId;
	String name;
	String normalizedName;
	String brand;
	@Enumerated(EnumType.STRING) ProductStatus status;
	Instant createdAt;
	Instant updatedAt;
	protected Product() {}
	Product(UUID categoryId, String name, String normalizedName, String brand, Instant now) { id = UUID.randomUUID(); this.categoryId = categoryId; this.name = name; this.normalizedName = normalizedName; this.brand = brand; status = ProductStatus.ACTIVE; createdAt = updatedAt = now; }
}

enum ProductStatus { ACTIVE, INACTIVE }

@Entity @Table(name = "product_variants")
class ProductVariant {
	@Id UUID id;
	UUID productId;
	String label;
	String barcode;
	Instant createdAt;
	Instant updatedAt;
	protected ProductVariant() {}
	ProductVariant(UUID productId, String label, String barcode, Instant now) { id = UUID.randomUUID(); this.productId = productId; this.label = label; this.barcode = barcode; createdAt = updatedAt = now; }
}

@Entity @Table(name = "store_products")
class StoreProduct {
	@Id UUID id;
	UUID storeId;
	UUID productVariantId;
	@Enumerated(EnumType.STRING) ProductStatus status;
	Instant createdAt;
	Instant updatedAt;
	protected StoreProduct() {}
	StoreProduct(UUID storeId, UUID variantId, Instant now) { id = UUID.randomUUID(); this.storeId = storeId; productVariantId = variantId; status = ProductStatus.ACTIVE; createdAt = updatedAt = now; }
}

@Entity @Table(name = "inventory")
class Inventory {
	@Id UUID id;
	UUID storeProductId;
	Integer quantity;
	@Enumerated(EnumType.STRING) Availability availability;
	String source;
	Instant observedAt;
	UUID updatedBy;
	@Version int version;
	Instant createdAt;
	Instant updatedAt;
	protected Inventory() {}
	Inventory(UUID listingId, Integer quantity, Availability availability, UUID userId, Instant now) { id = UUID.randomUUID(); storeProductId = listingId; this.quantity = quantity; this.availability = availability; source = "OWNER"; observedAt = now; updatedBy = userId; createdAt = updatedAt = now; }
	void update(Integer quantity, Availability availability, UUID userId, Instant now) { this.quantity = quantity; this.availability = availability; observedAt = now; updatedBy = userId; updatedAt = now; }
}

@Entity @Table(name = "prices")
class Price {
	@Id UUID id;
	UUID storeProductId;
	@Column(precision = 12, scale = 2) BigDecimal amount;
	String currency;
	@Version int version;
	Instant createdAt;
	Instant updatedAt;
	protected Price() {}
	Price(UUID listingId, BigDecimal amount, String currency, Instant now) { id = UUID.randomUUID(); storeProductId = listingId; this.amount = amount; this.currency = currency; createdAt = updatedAt = now; }
	void update(BigDecimal amount, String currency, Instant now) { this.amount = amount; this.currency = currency; updatedAt = now; }
}

@Entity @Table(name = "price_history")
class PriceHistory {
	@Id UUID id;
	UUID storeProductId;
	@Column(precision = 12, scale = 2) BigDecimal oldAmount;
	@Column(precision = 12, scale = 2) BigDecimal newAmount;
	String currency;
	UUID changedBy;
	Instant createdAt;
	Instant updatedAt;
	protected PriceHistory() {}
	PriceHistory(UUID listingId, BigDecimal oldAmount, BigDecimal newAmount, String currency, UUID userId, Instant now) { id = UUID.randomUUID(); storeProductId = listingId; this.oldAmount = oldAmount; this.newAmount = newAmount; this.currency = currency; changedBy = userId; createdAt = updatedAt = now; }
}
