package com.nearkart.search;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
class AiDiscoveryService {
	private static final Logger log = LoggerFactory.getLogger(AiDiscoveryService.class);
	private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
	private final ObjectMapper json;
	private final String apiKey;
	private final String model;
	// ponytail: per-instance LRU is enough for one Render instance; use a shared cache when scaling out.
	private final Map<String, List<SearchModels.DiscoverySuggestion>> cache = Collections.synchronizedMap(
		new LinkedHashMap<>(128, .75f, true) {
			@Override protected boolean removeEldestEntry(Map.Entry<String, List<SearchModels.DiscoverySuggestion>> eldest) {
				return size() > 200;
			}
		});

	AiDiscoveryService(ObjectMapper json,
			@Value("${nearkart.openai.api-key:}") String apiKey,
			@Value("${nearkart.openai.model:gpt-4o-mini}") String model) {
		this.json = json; this.apiKey = apiKey; this.model = model;
	}

	List<SearchModels.DiscoverySuggestion> suggestions(String query) {
		if (apiKey.isBlank()) return List.of();
		var cached = cache.get(query);
		if (cached != null) return cached;
		try {
			var request = HttpRequest.newBuilder(URI.create("https://api.openai.com/v1/responses"))
				.timeout(Duration.ofSeconds(20))
				.header("Authorization", "Bearer " + apiKey)
				.header("Content-Type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString(json.writeValueAsString(payload(query))))
				.build();
			var response = http.send(request, HttpResponse.BodyHandlers.ofString());
			if (response.statusCode() / 100 != 2) {
				log.warn("OpenAI discovery request failed with status {}", response.statusCode());
				return List.of();
			}
			var result = parse(json, response.body());
			if (!result.isEmpty()) cache.put(query, result);
			return result;
		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			return List.of();
		} catch (Exception exception) {
			log.warn("OpenAI discovery request failed", exception);
			return List.of();
		}
	}

	private Map<String, Object> payload(String query) {
		var item = Map.of(
			"type", "object",
			"additionalProperties", false,
			"properties", Map.of(
				"productName", Map.of("type", "string", "maxLength", 180),
				"brand", Map.of("type", List.of("string", "null"), "maxLength", 120),
				"variant", Map.of("type", "string", "maxLength", 120),
				"reason", Map.of("type", "string", "maxLength", 180)),
			"required", List.of("productName", "brand", "variant", "reason"));
		var schema = Map.of(
			"type", "object",
			"additionalProperties", false,
			"properties", Map.of("suggestions", Map.of("type", "array", "maxItems", 5, "items", item)),
			"required", List.of("suggestions"));
		return Map.of(
			"model", model,
			"store", false,
			"instructions", "Suggest up to five common retail products matching the query for an Indian shopping catalog. Never claim a store, price, stock, or availability. Keep reasons short.",
			"input", query,
			"text", Map.of("format", Map.of("type", "json_schema", "name", "product_suggestions", "strict", true, "schema", schema)));
	}

	static List<SearchModels.DiscoverySuggestion> parse(ObjectMapper json, String response) throws Exception {
		JsonNode output = json.readTree(response).path("output");
		for (JsonNode item : output) for (JsonNode content : item.path("content")) {
			if ("output_text".equals(content.path("type").asText())) {
				JsonNode suggestions = json.readTree(content.path("text").asText()).path("suggestions");
				return json.readerForListOf(SearchModels.DiscoverySuggestion.class).readValue(suggestions);
			}
		}
		return List.of();
	}
}
