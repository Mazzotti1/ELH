package com.elh.gateway.producer;

import com.elh.commons.config.KafkaTopics;
import com.elh.commons.events.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventPublisher {

    private final KafkaTemplate<String, BaseEvent> kafkaTemplate;

    public void publishMessageReceived(MessageReceivedEvent event) {
        send(KafkaTopics.DISCORD_MESSAGES, event.getGuildId(), event);
    }

    public void publishMediaDetected(MediaDetectedEvent event) {
        send(KafkaTopics.MEDIA_DETECTED, event.getGuildId(), event);
    }

    public void publishCommandReceived(CommandReceivedEvent event) {
        send(KafkaTopics.DISCORD_COMMANDS, event.getGuildId(), event);
    }

    public void publishChatRequested(ChatRequestedEvent event) {
        send(KafkaTopics.CHAT_REQUESTED, event.getGuildId(), event);
    }

    public void publishReactionAdded(ReactionAddedEvent event) {
        send(KafkaTopics.DISCORD_REACTIONS, event.getGuildId(), event);
    }

    private void send(String topic, String key, BaseEvent event) {
        CompletableFuture<SendResult<String, BaseEvent>> future = kafkaTemplate.send(topic, key, event);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Falha ao publicar {} no topico {}: {}", event.getEventType(), topic, ex.getMessage());
            } else {
                log.debug("Evento {} publicado em {}[partition={}, offset={}]",
                        event.getEventType(), topic,
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });
    }
}
