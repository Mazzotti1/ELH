package com.elh.commons.events;

import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Publicado quando o discord-gateway identifica uma mídia em uma mensagem.
 * Contém a URL temporária do Discord (expira em 24h!) e metadados básicos.
 * O media-ingestor deve fazer o download IMEDIATAMENTE.
 */
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class MediaDetectedEvent extends BaseEvent {

    private String messageId;
    private String channelId;
    private String authorId;
    private String authorName;

    private String discordUrl;

    private String fileName;

    private String mimeType;

    private Long sizeBytes;

    @Override
    public String getEventType() { return "MEDIA_DETECTED"; }
}
