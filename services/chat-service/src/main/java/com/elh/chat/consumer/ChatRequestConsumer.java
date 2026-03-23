package com.elh.chat.consumer;

import com.elh.chat.service.ChatSessionService;
import com.elh.chat.service.DiscordResponseSender;
import com.elh.chat.service.action.ActionParser;
import com.elh.chat.service.action.ParsedResponse;
import com.elh.chat.service.action.ServerActionExecutor;
import com.elh.chat.service.ai.AiProvider;
import com.elh.chat.service.ai.ChatResponse;
import com.elh.commons.config.KafkaTopics;
import com.elh.commons.events.ChatRequestedEvent;
import com.elh.commons.events.ChatRespondedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import com.elh.commons.events.BaseEvent;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatRequestConsumer {

    private final ChatSessionService sessionService;
    private final AiProvider aiProvider;
    private final DiscordResponseSender sender;
    private final KafkaTemplate<String, BaseEvent> kafkaTemplate;
    private final ActionParser actionParser;
    private final ServerActionExecutor actionExecutor;

    @KafkaListener(topics = KafkaTopics.CHAT_REQUESTED)
    public void handle(ChatRequestedEvent event) {
        log.info("Chat request de {} no guild {}: '{}'",
                event.getAuthorName(), event.getGuildId(),
                truncateLog(event.getUserMessage()));

        String sessionKey = event.getSessionKey();

        List<Map<String, String>> history = new ArrayList<>(
                sessionService.getConversationHistory(sessionKey));

        history.add(Map.of("role", "user", "content", event.getUserMessage()));
        sessionService.addMessage(sessionKey, "user", event.getUserMessage());

        ChatResponse response = aiProvider.chat(history);

        ParsedResponse parsed = actionParser.parse(response.text());

        sessionService.addMessage(sessionKey, "assistant", parsed.text());

        sessionService.persistMessage(
                event.getGuildId(), event.getChannelId(), event.getAuthorId(),
                "user", event.getUserMessage(), response.inputTokens());
        sessionService.persistMessage(
                event.getGuildId(), event.getChannelId(), event.getAuthorId(),
                "assistant", parsed.text(), response.outputTokens());

        String replyText = parsed.text().isEmpty()
                ? "Pronto, acao executada!"
                : parsed.text();
        sender.sendMessage(event.getChannelId(), event.getInteractionToken(), replyText);

        if (parsed.hasActions()) {
            log.info("Executando {} comando(s) via chat no canal {}", parsed.actions().size(), event.getChannelId());
            for (var action : parsed.actions()) {
                try {
                    actionExecutor.execute(action,
                            event.getGuildId(), event.getChannelId(),
                            event.getAuthorId(), event.getAuthorName());
                } catch (Exception e) {
                    log.error("Erro ao executar comando /{}: {}", action.command(), e.getMessage(), e);
                }
            }
        }

        ChatRespondedEvent respondedEvent = ChatRespondedEvent.builder()
                .guildId(event.getGuildId())
                .channelId(event.getChannelId())
                .interactionId(event.getInteractionId())
                .interactionToken(event.getInteractionToken())
                .botResponse(parsed.text())
                .inputTokens(response.inputTokens())
                .outputTokens(response.outputTokens())
                .build();

        kafkaTemplate.send(KafkaTopics.CHAT_RESPONDED, event.getGuildId(), respondedEvent);
        log.info("Chat respondido para {} ({} in / {} out tokens)",
                event.getAuthorName(), response.inputTokens(), response.outputTokens());
    }

    private String truncateLog(String s) {
        return s != null && s.length() > 80 ? s.substring(0, 80) + "..." : s;
    }
}
