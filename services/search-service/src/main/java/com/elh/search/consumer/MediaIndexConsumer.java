package com.elh.search.consumer;

import com.elh.commons.events.MediaSavedEvent;
import com.elh.search.document.MediaDocument;
import com.elh.search.repository.MediaSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Collections;

import static com.elh.commons.config.KafkaTopics.MEDIA_SAVED;

@Slf4j
@Component
@RequiredArgsConstructor
public class MediaIndexConsumer {

    private final MediaSearchRepository searchRepository;

    @KafkaListener(topics = MEDIA_SAVED, groupId = "search-service")
    public void onMediaSaved(MediaSavedEvent event) {
        MDC.put("correlationId", event.getCorrelationId() != null ? event.getCorrelationId() : event.getEventId());
        try {
        log.info("Indexando media id={} de {} no Elasticsearch", event.getMediaId(), event.getAuthorName());

        MediaDocument doc = MediaDocument.builder()
                .id(String.valueOf(event.getMediaId()))
                .guildId(event.getGuildId())
                .channelId(event.getChannelId())
                .authorId(event.getAuthorId())
                .authorName(event.getAuthorName())
                .mediaType(event.getMediaType().name())
                .mimeType(event.getMimeType())
                .permanentUrl(event.getPermanentUrl())
                .thumbnailUrl(event.getS3ThumbnailKey())
                .tags(Collections.emptyList())
                .sizeBytes(event.getSizeBytes())
                .createdAt(Instant.now())
                .build();

        searchRepository.save(doc);
        log.info("Media id={} indexada com sucesso", event.getMediaId());
        } finally {
            MDC.remove("correlationId");
        }
    }
}
