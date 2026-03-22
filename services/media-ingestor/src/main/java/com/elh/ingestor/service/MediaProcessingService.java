package com.elh.ingestor.service;

import com.elh.commons.config.KafkaTopics;
import com.elh.commons.events.BaseEvent;
import com.elh.commons.events.MediaDetectedEvent;
import com.elh.commons.events.MediaSavedEvent;
import com.elh.ingestor.entity.Media;
import com.elh.ingestor.repository.MediaRepository;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class MediaProcessingService {

    private final S3StorageService s3Service;
    private final ThumbnailService thumbnailService;
    private final MediaRepository mediaRepository;
    private final KafkaTemplate<String, BaseEvent> kafkaTemplate;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Value("${media.download.timeout-seconds:30}")
    private int downloadTimeout;

    @Value("${media.download.max-size-mb:100}")
    private int maxSizeMb;

    @Transactional
    public void processMedia(MediaDetectedEvent event) {
        String discordUrl = event.getDiscordUrl();
        String guildId = event.getGuildId();

        if (mediaRepository.existsByGuildIdAndOriginalUrl(guildId, discordUrl)) {
            log.debug("Media ja processada: {}", discordUrl);
            return;
        }

        if (event.getSizeBytes() != null && event.getSizeBytes() > maxSizeMb * 1024L * 1024L) {
            log.warn("Media muito grande ({} MB), ignorando: {}", event.getSizeBytes() / (1024 * 1024), discordUrl);
            return;
        }

        Media.MediaType mediaType = resolveMediaType(event.getMimeType());
        String bulkheadName = mediaType == Media.MediaType.VIDEO ? "video-download" : "image-download";

        log.info("Processando media: {} ({}) de {}", event.getFileName(), event.getMimeType(), event.getAuthorName());

        try {
            byte[] fileData = downloadFile(discordUrl);

            String s3Key = s3Service.buildS3Key(guildId, mediaType.name(), event.getFileName());
            s3Service.upload(s3Key, fileData, event.getMimeType());

            String thumbnailKey = null;
            if (thumbnailService.canGenerateThumbnail(event.getMimeType())) {
                byte[] thumbData = thumbnailService.generateThumbnail(fileData, event.getMimeType());
                if (thumbData != null) {
                    thumbnailKey = s3Service.buildThumbnailKey(s3Key);
                    s3Service.upload(thumbnailKey, thumbData, "image/jpeg");
                }
            }

            String permanentUrl = s3Service.generatePermanentUrl(s3Key);

            Media media = Media.builder()
                    .guildId(guildId)
                    .channelId(event.getChannelId())
                    .messageId(event.getMessageId())
                    .authorId(event.getAuthorId())
                    .authorName(event.getAuthorName())
                    .s3Key(s3Key)
                    .s3ThumbnailKey(thumbnailKey)
                    .permanentUrl(permanentUrl)
                    .mediaType(mediaType)
                    .mimeType(event.getMimeType())
                    .sizeBytes(event.getSizeBytes())
                    .originalUrl(discordUrl)
                    .build();

            media = mediaRepository.save(media);
            log.info("Media salva: id={} s3Key={}", media.getId(), s3Key);

            MediaSavedEvent savedEvent = MediaSavedEvent.builder()
                    .guildId(guildId)
                    .mediaId(media.getId())
                    .channelId(event.getChannelId())
                    .authorId(event.getAuthorId())
                    .authorName(event.getAuthorName())
                    .s3Key(s3Key)
                    .s3ThumbnailKey(thumbnailKey)
                    .permanentUrl(permanentUrl)
                    .mimeType(event.getMimeType())
                    .sizeBytes(event.getSizeBytes())
                    .mediaType(MediaSavedEvent.MediaType.valueOf(mediaType.name()))
                    .build();

            kafkaTemplate.send(KafkaTopics.MEDIA_SAVED, guildId, savedEvent);
            log.info("Evento media.saved publicado para media id={}", media.getId());

        } catch (Exception e) {
            log.error("Falha ao processar media {}: {}", event.getFileName(), e.getMessage(), e);
            throw new RuntimeException("Falha no processamento de media", e);
        }
    }

    @Bulkhead(name = "image-download")
    public byte[] downloadFile(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(downloadTimeout))
                .GET()
                .build();

        HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());

        if (response.statusCode() != 200) {
            throw new IOException("Download falhou com status " + response.statusCode() + " para URL: " + url);
        }

        log.debug("Download concluido: {} bytes de {}", response.body().length, url);
        return response.body();
    }

    private Media.MediaType resolveMediaType(String mimeType) {
        if (mimeType == null) return Media.MediaType.LINK;
        if (mimeType.startsWith("image/")) return Media.MediaType.IMAGE;
        if (mimeType.startsWith("video/")) return Media.MediaType.VIDEO;
        return Media.MediaType.LINK;
    }
}
