package com.nearkart.search;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ProductDiscoveryServiceTests {
	@Test
	void alwaysReturnsFreeProductIdeas() {
		var result = new ProductDiscoveryService().suggestions("apple");

		assertThat(result).hasSize(5);
		assertThat(result.get(0).productName()).isEqualTo("Apple");
	}
}
