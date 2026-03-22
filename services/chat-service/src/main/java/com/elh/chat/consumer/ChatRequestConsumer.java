package com.elh.chat.consumer;

import com.elh.chat.service.ChatSessionService;
import com.elh.chat.service.ClaudeApiService;
import com.elh.chat.service.ClaudeApiService.ChatResponse;
import com.elh.chat.service.DiscordResponseSender;
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
    private final ClaudeApiService claudeApi;
    private final DiscordResponseSender sender;
    private final KafkaTemplate<String, BaseEvent> kafkaTemplate;

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

        ChatResponse response = claudeApi.chat(history);

        sessionService.addMessage(sessionKey, "assistant", response.text());

        sessionService.persistMessage(
                event.getGuildId(), event.getChannelId(), event.getAuthorId(),
                "user", event.getUserMessage(), response.inputTokens());
        sessionService.persistMessage(
                event.getGuildId(), event.getChannelId(), event.getAuthorId(),
                "assistant", response.text(), response.outputTokens());

        sender.sendMessage(event.getChannelId(), event.getInteractionToken(), response.text());

        ChatRespondedEvent respondedEvent = ChatRespondedEvent.builder()
                .guildId(event.getGuildId())
                .channelId(event.getChannelId())
                .interactionId(event.getInteractionId())
                .interactionToken(event.getInteractionToken())
                .botResponse(response.text())
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
