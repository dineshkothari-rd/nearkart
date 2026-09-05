package com.nearkart.search;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class RankingServiceTests {
	@Test
	void recentAvailabilityCanOutrankAStaleCheaperOffer() {
		Instant now = Instant.parse("2026-09-05T12:00:00Z");
		var staleCheap = candidate("Cheap Store", "270", now.minus(Duration.ofDays(2)), 300);
		var fresh = candidate("Fresh Store", "280", now.minus(Duration.ofMinutes(10)), 450);

		var result = new RankingService().rank(List.of(staleCheap, fresh), 5000, now,
			Duration.ofMinutes(30), Duration.ofHours(2), Duration.ofDays(1), Sort.RECOMMENDED);

		assertThat(result.get(0).storeName()).isEqualTo("Fresh Store");
		assertThat(result.get(0).freshness()).isEqualTo("FRESH");
	}

	private static SearchModels.Candidate candidate(String store, String amount, Instant observedAt, double distance) {
		return new SearchModels.Candidate(UUID.randomUUID(), "Amul Butter", "Amul", UUID.randomUUID(), "500 g",
			UUID.randomUUID(), store, new BigDecimal(amount), "INR", "AVAILABLE", observedAt, distance, 26.9, 75.8);
	}
}
