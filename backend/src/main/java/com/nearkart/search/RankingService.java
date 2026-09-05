package com.nearkart.search;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
class RankingService {
	List<SearchModels.NearbyOffer> rank(List<SearchModels.Candidate> candidates, double radiusMeters,
			Instant now, Duration freshFor, Duration recentFor, Duration staleAfter, Sort sort) {
		BigDecimal min = candidates.stream().map(SearchModels.Candidate::amount).min(Comparator.naturalOrder()).orElse(BigDecimal.ZERO);
		BigDecimal max = candidates.stream().map(SearchModels.Candidate::amount).max(Comparator.naturalOrder()).orElse(min);
		Comparator<SearchModels.NearbyOffer> comparator = comparator(sort);
		return candidates.stream().map(value -> offer(value, radiusMeters, min, max, now, freshFor, recentFor, staleAfter))
			.sorted(comparator).toList();
	}

	private SearchModels.NearbyOffer offer(SearchModels.Candidate value, double radius, BigDecimal min,
			BigDecimal max, Instant now, Duration freshFor, Duration recentFor, Duration staleAfter) {
		String freshness = freshness(value.observedAt(), now, freshFor, recentFor, staleAfter);
		double distance = 1 - Math.min(value.distanceMeters() / radius, 1);
		double price = max.compareTo(min) == 0 ? 1 : max.subtract(value.amount()).divide(max.subtract(min), 6, RoundingMode.HALF_UP).doubleValue();
		double available = switch (value.availability()) { case "AVAILABLE" -> 1; case "LOW_STOCK" -> .7; case "UNKNOWN" -> .3; default -> 0; };
		double fresh = switch (freshness) { case "FRESH" -> 1; case "RECENT" -> .75; case "POSSIBLY_STALE" -> .35; default -> 0; };
		double score = distance * .30 + price * .20 + fresh * .40 + available * .10;
		String reason = available >= .7 && fresh >= .75 ? "Nearby with recently confirmed availability"
			: price >= .8 ? "One of the lowest nearby prices" : "Matches your nearby search";
		return new SearchModels.NearbyOffer(value.productId(), value.productName(), value.brand(), value.variant(),
			value.storeId(), value.storeName(), value.amount(), value.currency(), value.availability(), freshness,
			value.observedAt(), Math.round(value.distanceMeters()), value.storeLatitude(), value.storeLongitude(),
			new SearchModels.Recommendation(score, reason));
	}

	static String freshness(Instant observedAt, Instant now, Duration freshFor, Duration recentFor, Duration staleAfter) {
		Duration age = Duration.between(observedAt, now);
		if (age.compareTo(freshFor) <= 0) return "FRESH";
		if (age.compareTo(recentFor) <= 0) return "RECENT";
		if (age.compareTo(staleAfter) <= 0) return "POSSIBLY_STALE";
		return "STALE";
	}

	private static Comparator<SearchModels.NearbyOffer> comparator(Sort sort) {
		return switch (sort) {
			case PRICE -> Comparator.comparing(SearchModels.NearbyOffer::amount);
			case DISTANCE -> Comparator.comparingLong(SearchModels.NearbyOffer::distanceMeters);
			case FRESHEST -> Comparator.comparing(SearchModels.NearbyOffer::observedAt).reversed();
			case RECOMMENDED -> Comparator.comparingDouble((SearchModels.NearbyOffer value) -> value.recommendation().score()).reversed();
		};
	}
}

enum Sort { RECOMMENDED, PRICE, DISTANCE, FRESHEST }
