package com.nearkart.catalog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.Test;

class CatalogServiceTests {
	@Test
	void classifiesInventoryFreshnessAtConfiguredThresholds() {
		Instant now = Instant.parse("2026-09-05T12:00:00Z");
		Duration fresh = Duration.ofMinutes(30), recent = Duration.ofHours(2), stale = Duration.ofDays(1);

		assertThat(CatalogService.freshness(now.minusSeconds(60), now, fresh, recent, stale)).isEqualTo("FRESH");
		assertThat(CatalogService.freshness(now.minus(Duration.ofHours(1)), now, fresh, recent, stale)).isEqualTo("RECENT");
		assertThat(CatalogService.freshness(now.minus(Duration.ofHours(12)), now, fresh, recent, stale)).isEqualTo("POSSIBLY_STALE");
		assertThat(CatalogService.freshness(now.minus(Duration.ofDays(2)), now, fresh, recent, stale)).isEqualTo("STALE");
	}

	@Test
	void rejectsContradictoryStockValues() {
		assertThatThrownBy(() -> CatalogService.validateStock(2, Availability.OUT_OF_STOCK))
			.hasMessage("Quantity and availability do not agree");
		assertThatThrownBy(() -> CatalogService.validateStock(0, Availability.AVAILABLE))
			.hasMessage("Quantity and availability do not agree");
	}
}
