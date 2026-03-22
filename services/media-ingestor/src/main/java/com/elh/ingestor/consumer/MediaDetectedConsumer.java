package com.elh.ingestor.consumer;

import com.elh.commons.events.MediaDetectedEvent;
import com.elh.ingestor.service.MediaProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import static com.elh.commons.config.KafkaTopics.MEDIA_DETECTED;

@Slf4j
@Component
@RequiredArgsConstructor
public class MediaDetectedConsumer {

    private final MediaProcessingService processingService;

    @KafkaListener(topics = MEDIA_DETECTED, groupId = "media-ingestor")
    public void onMediaDetected(MediaDetectedEvent event) {
        log.info("Evento media.detected recebido: {} de {} (guild={})",
                event.getFileName(), event.getAuthorName(), event.getGuildId());

        processingService.processMedia(event);
    }
}
