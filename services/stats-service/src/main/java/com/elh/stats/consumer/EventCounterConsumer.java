package com.elh.stats.consumer;

import com.elh.commons.config.KafkaTopics;
import com.elh.commons.events.MediaSavedEvent;
import com.elh.commons.events.MessageReceivedEvent;
import com.elh.commons.events.ReactionAddedEvent;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EventCounterConsumer {

    private final Counter messagesCounter;
    private final Counter mediaSavedCounter;
    private final Counter reactionsCounter;

    public EventCounterConsumer(MeterRegistry registry) {
        this.messagesCounter = Counter.builder("elh.messages.total")
                .description("Total de mensagens recebidas")
                .register(registry);
        this.mediaSavedCounter = Counter.builder("elh.media.saved.total")
                .description("Total de midias salvas")
                .register(registry);
        this.reactionsCounter = Counter.builder("elh.reactions.total")
                .description("Total de reacoes recebidas")
                .register(registry);
    }

    @KafkaListener(topics = KafkaTopics.DISCORD_MESSAGES)
    public void countMessage(MessageReceivedEvent event) {
        messagesCounter.increment();
    }

    @KafkaListener(topics = KafkaTopics.MEDIA_SAVED)
    public void countMediaSaved(MediaSavedEvent event) {
        mediaSavedCounter.increment();
    }

    @KafkaListener(topics = KafkaTopics.DISCORD_REACTIONS)
    public void countReaction(ReactionAddedEvent event) {
        reactionsCounter.increment();
    }
}
