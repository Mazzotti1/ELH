package com.elh.chat.service.ai;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class OpenAiProvider implements AiProvider {

    private final RestClient restClient;
    private final String model;
    private final int maxTokens;
    private final String systemPrompt;

    public OpenAiProvider(RestClient.Builder restClientBuilder,
                          String apiKey,
                          String model,
                          int maxTokens,
                          String systemPrompt) {
        this.model = model;
        this.maxTokens = maxTokens;
        this.systemPrompt = systemPrompt;
        this.restClient = restClientBuilder
                .baseUrl("https://api.openai.com")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Override
    @CircuitBreaker(name = "ai-provider", fallbackMethod = "fallbackChat")
    @RateLimiter(name = "ai-provider")
    @Retry(name = "ai-provider")
    public ChatResponse chat(List<Map<String, String>> conversationHistory) {
        log.info("Chamando OpenAI API (model={}, mensagens={})", model, conversationHistory.size());

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemPrompt));
        messages.addAll(conversationHistory);

        Map<String, Object> requestBody = Map.of(
                "model", model,
                "max_tokens", maxTokens,
                "messages", messages
        );

        @SuppressWarnings("unchecked")
        Map<String, Object> response = restClient.post()
                .uri("/v1/chat/completions")
                .body(requestBody)
                .retrieve()
                .body(Map.class);

        if (response == null) {
            throw new RuntimeException("Resposta nula da OpenAI API");
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
        @SuppressWarnings("unchecked")
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        String text = (String) message.get("content");

        @SuppressWarnings("unchecked")
        Map<String, Object> usage = (Map<String, Object>) response.get("usage");
        int inputTokens = usage != null ? ((Number) usage.get("prompt_tokens")).intValue() : 0;
        int outputTokens = usage != null ? ((Number) usage.get("completion_tokens")).intValue() : 0;

        log.info("OpenAI respondeu: {} input tokens, {} output tokens", inputTokens, outputTokens);
        return new ChatResponse(text, inputTokens, outputTokens);
    }

    @SuppressWarnings("unused")
    private ChatResponse fallbackChat(List<Map<String, String>> conversationHistory, Throwable t) {
        log.warn("OpenAI API indisponivel ({}), enviando fallback", t.getMessage());
        return new ChatResponse(
                "Desculpa, estou com dificuldades tecnicas no momento. Tente novamente em alguns segundos!",
                0, 0
        );
    }
}
