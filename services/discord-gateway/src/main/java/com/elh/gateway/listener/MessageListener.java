package com.elh.gateway.listener;

import com.elh.commons.events.MediaDetectedEvent;
import com.elh.commons.events.MessageReceivedEvent;
import com.elh.gateway.producer.EventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageListener extends ListenerAdapter {

    private final EventPublisher publisher;

    @Override
    public void onMessageReceived(net.dv8tion.jda.api.events.message.MessageReceivedEvent jdaEvent) {
        if (jdaEvent.getAuthor().isBot()) return;
        if (!jdaEvent.isFromGuild()) return;

        Message msg = jdaEvent.getMessage();

        MessageReceivedEvent event = MessageReceivedEvent.builder()
                .guildId(jdaEvent.getGuild().getId())
                .messageId(msg.getId())
                .channelId(jdaEvent.getChannel().getId())
                .channelName(jdaEvent.getChannel().getName())
                .authorId(jdaEvent.getAuthor().getId())
                .authorName(jdaEvent.getAuthor().getName())
                .content(msg.getContentRaw())
                .hasAttachments(!msg.getAttachments().isEmpty())
                .hasLinks(msg.getContentRaw().contains("http"))
                .build();

        publisher.publishMessageReceived(event);

        msg.getAttachments().forEach(att -> {
            MediaDetectedEvent mediaEvent = MediaDetectedEvent.builder()
                    .guildId(jdaEvent.getGuild().getId())
                    .messageId(msg.getId())
                    .channelId(jdaEvent.getChannel().getId())
                    .authorId(jdaEvent.getAuthor().getId())
                    .authorName(jdaEvent.getAuthor().getName())
                    .discordUrl(att.getUrl())
                    .fileName(att.getFileName())
                    .mimeType(att.getContentType())
                    .sizeBytes((long) att.getSize())
                    .build();

            publisher.publishMediaDetected(mediaEvent);
            log.info("Media detectada: {} ({} bytes) de {}", att.getFileName(), att.getSize(), jdaEvent.getAuthor().getName());
        });
    }
}
