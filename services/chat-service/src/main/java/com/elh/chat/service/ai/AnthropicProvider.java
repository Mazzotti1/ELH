package com.elh.chat.service.ai;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Slf4j
public class AnthropicProvider implements AiProvider {

    private final RestClient restClient;
    private final String model;
    private final int maxTokens;
    private final String systemPrompt;

    public AnthropicProvider(RestClient.Builder restClientBuilder,
                             String apiKey,
                             String model,
                             int maxTokens,
                             String systemPrompt) {
        this.model = model;
        this.maxTokens = maxTokens;
        this.systemPrompt = systemPrompt;
        this.restClient = restClientBuilder
                .baseUrl("https://api.anthropic.com")
                .defaultHeader("x-api-key", apiKey)
                .defaultHeader("anthropic-version", "2023-06-01")
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Override
    @CircuitBreaker(name = "ai-provider", fallbackMethod = "fallbackChat")
    @RateLimiter(name = "ai-provider")
    @Retry(name = "ai-provider")
    public ChatResponse chat(List<Map<String, String>> conversationHistory) {
        log.info("Chamando Anthropic API (model={}, mensagens={})", model, conversationHistory.size());

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
            throw new RuntimeException("Resposta nula da Anthropic API");
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

        log.info("Anthropic respondeu: {} input tokens, {} output tokens", inputTokens, outputTokens);
        return new ChatResponse(text, inputTokens, outputTokens);
    }

    @SuppressWarnings("unused")
    private ChatResponse fallbackChat(List<Map<String, String>> conversationHistory, Throwable t) {
        log.warn("Anthropic API indisponivel ({}), enviando fallback", t.getMessage());
        return new ChatResponse(
                "Desculpa, estou com dificuldades tecnicas no momento. Tente novamente em alguns segundos!",
                0, 0
        );
    }
}
