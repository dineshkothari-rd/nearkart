package com.nearkart.search;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
class ProductDiscoveryService {
	List<SearchModels.DiscoverySuggestion> suggestions(String query) {
		String product = Character.toUpperCase(query.charAt(0)) + query.substring(1);
		// ponytail: generic free variants; replace with verified catalog data when stores supply it.
		return List.of(
			new SearchModels.DiscoverySuggestion(product, null, "Standard", "Exact product search"),
			new SearchModels.DiscoverySuggestion(product, null, "Budget option", "Compare lower-priced variants"),
			new SearchModels.DiscoverySuggestion(product, null, "Premium option", "Compare higher-quality variants"),
			new SearchModels.DiscoverySuggestion(product, null, "Value pack", "Compare larger or multipack sizes"),
			new SearchModels.DiscoverySuggestion(product, null, "Local alternative", "Ask nearby stores for a local equivalent"));
	}
}
