package com.elh.chat.config;

import com.elh.chat.service.ai.AiProvider;
import com.elh.chat.service.ai.AnthropicProvider;
import com.elh.chat.service.ai.OpenAiProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Slf4j
@Configuration
public class AiProviderConfig {

    @Value("${ai.provider:anthropic}")
    private String provider;

    @Value("${ai.system-prompt:Voce eh o ELH (Every Losted History), um bot assistente de um servidor Discord. Voce eh amigavel, direto e responde em portugues brasileiro. Quando nao souber algo, diga que nao sabe. Mantenha respostas concisas.}")
    private String systemPrompt;

    @Value("${ai.max-tokens:1024}")
    private int maxTokens;

    @Value("${anthropic.api-key:}")
    private String anthropicApiKey;

    @Value("${anthropic.model:claude-haiku-4-5-20251001}")
    private String anthropicModel;

    @Value("${openai.api-key:}")
    private String openaiApiKey;

    @Value("${openai.model:gpt-4o-mini}")
    private String openaiModel;

    @Bean
    public AiProvider aiProvider(RestClient.Builder restClientBuilder) {
        return switch (provider.toLowerCase()) {
            case "openai" -> {
                log.info("AI Provider: OpenAI (model={})", openaiModel);
                yield new OpenAiProvider(restClientBuilder, openaiApiKey, openaiModel, maxTokens, systemPrompt);
            }
            default -> {
                log.info("AI Provider: Anthropic (model={})", anthropicModel);
                yield new AnthropicProvider(restClientBuilder, anthropicApiKey, anthropicModel, maxTokens, systemPrompt);
            }
        };
    }
}
