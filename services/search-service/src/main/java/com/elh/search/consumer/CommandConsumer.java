package com.elh.search.consumer;

import com.elh.commons.events.CommandReceivedEvent;
import com.elh.search.handler.SearchCommandHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Set;

import static com.elh.commons.config.KafkaTopics.DISCORD_COMMANDS;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommandConsumer {

    private static final Set<String> HANDLED_COMMANDS = Set.of("img", "buscar", "historico", "midia", "tag");

    private final SearchCommandHandler commandHandler;

    @KafkaListener(topics = DISCORD_COMMANDS, groupId = "search-service")
    public void onCommandReceived(CommandReceivedEvent event) {
        if (!HANDLED_COMMANDS.contains(event.getCommand())) return;

        MDC.put("correlationId", event.getCorrelationId() != null ? event.getCorrelationId() : event.getEventId());
        try {
            log.info("Comando /{} recebido de {} (guild={})", event.getCommand(), event.getAuthorName(), event.getGuildId());
            commandHandler.handle(event);
        } finally {
            MDC.remove("correlationId");
        }
    }
}
