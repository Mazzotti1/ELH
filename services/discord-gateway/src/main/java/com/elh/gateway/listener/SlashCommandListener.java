package com.elh.gateway.listener;

import com.elh.commons.events.ChatRequestedEvent;
import com.elh.commons.events.CommandReceivedEvent;
import com.elh.gateway.producer.EventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class SlashCommandListener extends ListenerAdapter {

    private final EventPublisher publisher;

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.isFromGuild()) return;

        String command = event.getName();
        String guildId = event.getGuild().getId();
        String channelId = event.getChannel().getId();
        String authorId = event.getUser().getId();
        String authorName = event.getUser().getName();
        String interactionId = event.getInteraction().getId();
        String interactionToken = event.getInteraction().getToken();

        event.deferReply().queue();

        log.info("Slash command recebido: /{} de {} no guild {}", command, authorName, guildId);

        String correlationId = UUID.randomUUID().toString().substring(0, 8);
        Map<String, String> options = new HashMap<>();
        event.getOptions().forEach(opt ->
                options.put(opt.getName(), opt.getAsString())
        );

        if ("elh".equals(command)) {
            String userMessage = options.getOrDefault("mensagem", "");
            ChatRequestedEvent chatEvent = ChatRequestedEvent.builder()
                    .guildId(guildId)
                    .channelId(channelId)
                    .authorId(authorId)
                    .authorName(authorName)
                    .interactionId(interactionId)
                    .interactionToken(interactionToken)
                    .userMessage(userMessage)
                    .sessionKey(guildId + ":" + channelId + ":" + authorId)
                    .correlationId(correlationId)
                    .build();
            publisher.publishChatRequested(chatEvent);
            return;
        }

        CommandReceivedEvent cmdEvent = CommandReceivedEvent.builder()
                .guildId(guildId)
                .command(command)
                .channelId(channelId)
                .authorId(authorId)
                .authorName(authorName)
                .interactionId(interactionId)
                .interactionToken(interactionToken)
                .options(options)
                .correlationId(correlationId)
                .build();

        publisher.publishCommandReceived(cmdEvent);
    }
}
