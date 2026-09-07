package com.nearkart.search;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import tools.jackson.databind.ObjectMapper;

class AiDiscoveryServiceTests {
	@Test
	void parsesStructuredResponse() throws Exception {
		String response = """
			{"output":[{"content":[{"type":"output_text","text":"{\\"suggestions\\":[{\\"productName\\":\\"Fresh Apple\\",\\"brand\\":null,\\"variant\\":\\"1 kg\\",\\"reason\\":\\"Matches the requested item\\"}]}"}]}]}
			""";

		var result = AiDiscoveryService.parse(new ObjectMapper(), response);

		assertThat(result).containsExactly(new SearchModels.DiscoverySuggestion(
			"Fresh Apple", null, "1 kg", "Matches the requested item"));
	}
}
