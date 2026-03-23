package com.elh.commons.events;

import lombok.*;
import lombok.experimental.SuperBuilder;

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
