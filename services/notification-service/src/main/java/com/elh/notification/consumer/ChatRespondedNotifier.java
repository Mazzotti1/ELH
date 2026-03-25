package com.elh.notification.consumer;

import com.elh.commons.config.KafkaTopics;
import com.elh.commons.events.ChatRespondedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatRespondedNotifier {

    @KafkaListener(topics = KafkaTopics.CHAT_RESPONDED)
    public void handle(ChatRespondedEvent event) {
        MDC.put("correlationId", event.getCorrelationId() != null ? event.getCorrelationId() : event.getEventId());
        try {
            log.info("Chat respondido no canal {}: {} input tokens, {} output tokens (total: {})",
                    event.getChannelId(),
                    event.getInputTokens(),
                    event.getOutputTokens(),
                    event.getInputTokens() + event.getOutputTokens());
        } finally {
            MDC.remove("correlationId");
        }
    }
}
