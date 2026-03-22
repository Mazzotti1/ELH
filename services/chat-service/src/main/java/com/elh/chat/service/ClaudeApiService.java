package com.elh.chat.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ClaudeApiService {

    private final RestClient restClient;

    @Value("${anthropic.model:claude-haiku-4-5-20251001}")
    private String model;

    @Value("${anthropic.max-tokens:1024}")
    private int maxTokens;

    @Value("${anthropic.system-prompt:Voce eh o ELH, um bot assistente de Discord.}")
    private String systemPrompt;

    public ClaudeApiService(RestClient.Builder restClientBuilder,
                            @Value("${anthropic.api-key:}") String apiKey) {
        this.restClient = restClientBuilder
                .baseUrl("https://api.anthropic.com")
                .defaultHeader("x-api-key", apiKey)
                .defaultHeader("anthropic-version", "2023-06-01")
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @CircuitBreaker(name = "claude-api", fallbackMethod = "fallbackChat")
    @RateLimiter(name = "claude-api")
    @Retry(name = "claude-api")
    public ChatResponse chat(List<Map<String, String>> conversationHistory) {
        log.info("Chamando Claude API (model={}, mensagens={})", model, conversationHistory.size());

        Map<String, Object> requestBody = Map.of(
                "model", model,
                "max_tokens", maxTokens,
                "system", systemPrompt,
                "messages", conversationHistory
        );

        @SuppressWarnings("unchecked")
        Map<String, Object> response = restClient.post()
                .uri("/v1/messages")
                .body(requestBody)
                .retrieve()
                .body(Map.class);

        if (response == null) {
            throw new RuntimeException("Resposta nula da Claude API");
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> content = (List<Map<String, Object>>) response.get("content");
        String text = content.stream()
                .filter(c -> "text".equals(c.get("type")))
                .map(c -> (String) c.get("text"))
                .findFirst()
                .orElse("Sem resposta.");

        @SuppressWarnings("unchecked")
        Map<String, Object> usage = (Map<String, Object>) response.get("usage");
        int inputTokens = usage != null ? ((Number) usage.get("input_tokens")).intValue() : 0;
        int outputTokens = usage != null ? ((Number) usage.get("output_tokens")).intValue() : 0;

        log.info("Claude respondeu: {} input tokens, {} output tokens", inputTokens, outputTokens);

        return new ChatResponse(text, inputTokens, outputTokens);
    }

    @SuppressWarnings("unused")
    private ChatResponse fallbackChat(List<Map<String, String>> conversationHistory, Throwable t) {
        log.warn("Claude API indisponivel ({}), enviando fallback", t.getMessage());
        return new ChatResponse(
                "Desculpa, estou com dificuldades tecnicas no momento. Tente novamente em alguns segundos!",
                0, 0
        );
    }

    public record ChatResponse(String text, int inputTokens, int outputTokens) {}
}
