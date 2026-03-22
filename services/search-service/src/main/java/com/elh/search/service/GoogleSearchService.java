package com.elh.search.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class GoogleSearchService {

    private final RestClient restClient;
    private final InternalSearchService internalSearch;

    @Value("${google.cse.api-key:}")
    private String apiKey;

    @Value("${google.cse.cx:}")
    private String cx;

    @Value("${google.cse.max-results:5}")
    private int maxResults;

    public GoogleSearchService(RestClient.Builder restClientBuilder, InternalSearchService internalSearch) {
        this.restClient = restClientBuilder
                .baseUrl("https://www.googleapis.com/customsearch/v1")
                .build();
        this.internalSearch = internalSearch;
    }

    @CircuitBreaker(name = "google-search", fallbackMethod = "fallbackSearch")
    @Retry(name = "google-search")
    public List<GoogleImageResult> searchImages(String query, String guildId, int numResults) {
        int n = Math.min(numResults > 0 ? numResults : maxResults, 10);

        log.info("Google Custom Search: '{}' (n={})", query, n);

        Map response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("key", apiKey)
                        .queryParam("cx", cx)
                        .queryParam("q", query)
                        .queryParam("searchType", "image")
                        .queryParam("num", n)
                        .build())
                .retrieve()
                .body(Map.class);

        if (response == null || !response.containsKey("items")) {
            return Collections.emptyList();
        }

        List<Map<String, Object>> items = (List<Map<String, Object>>) response.get("items");
        return items.stream()
                .map(item -> new GoogleImageResult(
                        (String) item.get("title"),
                        (String) item.get("link"),
                        (String) ((Map<String, Object>) item.get("image")).get("thumbnailLink"),
                        (String) item.get("displayLink")
                ))
                .toList();
    }

    @SuppressWarnings("unused")
    private List<GoogleImageResult> fallbackSearch(String query, String guildId, int numResults, Throwable t) {
        log.warn("Google Search indisponivel ({}), usando fallback interno para '{}'", t.getMessage(), query);

        return internalSearch.search(guildId, query, null, "IMAGE", Math.min(numResults, 5))
                .stream()
                .map(doc -> new GoogleImageResult(
                        doc.getAuthorName() + " - " + doc.getMediaType(),
                        doc.getPermanentUrl(),
                        doc.getThumbnailUrl() != null ? doc.getThumbnailUrl() : doc.getPermanentUrl(),
                        "arquivo interno"
                ))
                .toList();
    }

    public record GoogleImageResult(String title, String imageUrl, String thumbnailUrl, String source) {}
}
