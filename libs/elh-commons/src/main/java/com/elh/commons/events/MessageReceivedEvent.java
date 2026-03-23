package com.elh.commons.events;

import lombok.*;
import lombok.experimental.SuperBuilder;

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
