package com.nearkart.catalog;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

interface CategoryRepository extends JpaRepository<Category, UUID> {}
interface ProductRepository extends JpaRepository<Product, UUID> {}
interface ProductVariantRepository extends JpaRepository<ProductVariant, UUID> {}
interface StoreProductRepository extends JpaRepository<StoreProduct, UUID> {
	Optional<StoreProduct> findByIdAndStoreId(UUID id, UUID storeId);
	Page<StoreProduct> findAllByStoreId(UUID storeId, Pageable pageable);
}
interface InventoryRepository extends JpaRepository<Inventory, UUID> {
	Optional<Inventory> findByStoreProductId(UUID storeProductId);
}
interface PriceRepository extends JpaRepository<Price, UUID> {
	Optional<Price> findByStoreProductId(UUID storeProductId);
}
interface PriceHistoryRepository extends JpaRepository<PriceHistory, UUID> {}
