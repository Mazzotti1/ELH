package com.elh.commons.events;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

//  TÓPICO: discord.messages
//  Producer: discord-gateway
//  Consumers: media-ingestor, stats-service
/**
 * Publicado quando qualquer mensagem é recebida no Discord.
 * O media-ingestor verifica se tem attachments. O stats-service contabiliza.
 */
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class MessageReceivedEvent extends BaseEvent {

    private String messageId;
    private String channelId;
    private String channelName;
    private String authorId;
    private String authorName;
    private String content;

    private boolean hasAttachments;

    private boolean hasLinks;

    @Override
    public String getEventType() { return "MESSAGE_RECEIVED"; }
}

//  TÓPICO: media.detected
//  Producer: discord-gateway
//  Consumers: media-ingestor
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

//  TÓPICO: media.saved
//  Producer: media-ingestor
//  Consumers: search-service (indexar), stats-service, notification-service
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

//  TÓPICO: discord.commands
//  Producer: discord-gateway
//  Consumers: search-service (/img, /buscar), chat-service (/elh),
//             poll-scheduler (/poll), stats-service (/stats)
/**
 * Publicado quando um slash command é recebido.
 * Cada consumer filtra pelo campo `command`.
 */
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CommandReceivedEvent extends BaseEvent {

    private String command;

    private String channelId;
    private String authorId;
    private String authorName;

    private String interactionId;
    private String interactionToken;

    private java.util.Map<String, String> options;

    @Override
    public String getEventType() { return "COMMAND_RECEIVED"; }
}

//  TÓPICO: poll.created / poll.closed
//  Producer: poll-scheduler
//  Consumers: notification-service, stats-service
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PollCreatedEvent extends BaseEvent {

    private Long pollId;
    private String channelId;
    private String title;

    private List<Long> mediaIds;

    private java.time.Instant closesAt;

    @Override
    public String getEventType() { return "POLL_CREATED"; }
}

@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PollClosedEvent extends BaseEvent {

    private Long pollId;
    private String channelId;
    private Long winnerMediaId;
    private String winnerMediaUrl;
    private String winnerAuthorName;
    private int totalVotes;

    @Override
    public String getEventType() { return "POLL_CLOSED"; }
}


//  TÓPICO: chat.requested / chat.responded
//  Producer: discord-gateway (requested), chat-service (responded)
//  Consumers: chat-service (requested), notification-service (responded)
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequestedEvent extends BaseEvent {

    private String channelId;
    private String authorId;
    private String authorName;
    private String interactionId;
    private String interactionToken;

    private String userMessage;

    private String sessionKey;

    @Override
    public String getEventType() { return "CHAT_REQUESTED"; }
}

@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRespondedEvent extends BaseEvent {

    private String channelId;
    private String interactionId;
    private String interactionToken;

    private String botResponse;

    private int inputTokens;
    private int outputTokens;

    @Override
    public String getEventType() { return "CHAT_RESPONDED"; }
}

//  TÓPICO: discord.reactions
//  Producer: discord-gateway
//  Consumers: stats-service, media-ingestor (contagem de reações)
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ReactionAddedEvent extends BaseEvent {

    private String channelId;
    private String messageId;
    private String userId;
    private String emoji;

    @Override
    public String getEventType() { return "REACTION_ADDED"; }
}
