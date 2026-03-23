package com.elh.commons.events;

import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Publicado quando a mídia foi salva com sucesso no S3 e no Postgres.
 * A URL agora é permanente. O search-service indexa no Elasticsearch.
 */
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class MediaSavedEvent extends BaseEvent {

    private Long mediaId;

    private String channelId;
    private String authorId;
    private String authorName;

    private String s3Key;

    private String s3ThumbnailKey;

    private String permanentUrl;

    private String mimeType;
    private Long sizeBytes;

    private MediaType mediaType;

    public enum MediaType { IMAGE, VIDEO, LINK }

    @Override
    public String getEventType() { return "MEDIA_SAVED"; }
}
