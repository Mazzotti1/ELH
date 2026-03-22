package com.elh.notification.consumer;

import com.elh.commons.config.KafkaTopics;
import com.elh.commons.events.MediaSavedEvent;
import com.elh.notification.service.DiscordNotificationSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MediaSavedNotifier {

    private final DiscordNotificationSender sender;

    @KafkaListener(topics = KafkaTopics.MEDIA_SAVED)
    public void handle(MediaSavedEvent event) {
        log.debug("Notificacao media.saved: {} por {} no canal {}",
                event.getMediaType(), event.getAuthorName(), event.getChannelId());

        String emoji = switch (event.getMediaType()) {
            case IMAGE -> "🖼️";
            case VIDEO -> "🎬";
            case LINK -> "🔗";
        };

        String sizeKb = event.getSizeBytes() != null
                ? String.valueOf(event.getSizeBytes() / 1024) + " KB"
                : "?";

        String description = String.format(
                "%s **%s** salva por **%s**\nTipo: `%s` | Tamanho: `%s`",
                emoji, event.getMediaType(), event.getAuthorName(),
                event.getMimeType(), sizeKb
        );

        List<String> images = event.getPermanentUrl() != null
                ? List.of(event.getPermanentUrl())
                : List.of();

        sender.sendEmbed(
                event.getChannelId(),
                "Nova midia arquivada!",
                description,
                "#00e5c0",
                images
        );
    }
}
