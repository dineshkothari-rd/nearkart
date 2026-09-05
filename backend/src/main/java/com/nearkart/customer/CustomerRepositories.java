package com.nearkart.customer;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

interface FavoriteRepository extends JpaRepository<Favorite, UUID> {
	List<Favorite> findAllByUserIdOrderByCreatedAtDesc(UUID userId);
	void deleteByIdAndUserId(UUID id, UUID userId);
}

interface SearchHistoryRepository extends JpaRepository<SearchHistory, UUID> {
	List<SearchHistory> findTop20ByUserIdOrderByCreatedAtDesc(UUID userId);
	void deleteAllByUserId(UUID userId);
	void deleteAllByUserIdAndQueryIgnoreCase(UUID userId, String query);
}

interface ReviewRepository extends JpaRepository<Review, UUID> {}
interface ReportRepository extends JpaRepository<Report, UUID> {}
