package com.elh.chat.service.action;

import com.elh.commons.config.KafkaTopics;
import com.elh.commons.events.BaseEvent;
import com.elh.commons.events.CommandReceivedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Slf4j
@Component
@RequiredArgsConstructor
public class ServerActionExecutor {

    private final KafkaTemplate<String, BaseEvent> kafkaTemplate;

    public void execute(BotAction action, String guildId, String channelId, String authorId, String authorName) {
        CommandReceivedEvent event = CommandReceivedEvent.builder()
                .guildId(guildId)
                .command(action.command())
                .channelId(channelId)
                .authorId(authorId)
                .authorName(authorName)
                .options(action.options() != null ? action.options() : Collections.emptyMap())
                .build();

        kafkaTemplate.send(KafkaTopics.DISCORD_COMMANDS, guildId, event);
        log.info("Comando /{} disparado via chat por {} no guild {}", action.command(), authorName, guildId);
    }
}
