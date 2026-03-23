package com.elh.commons.events;

import lombok.*;
import lombok.experimental.SuperBuilder;

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
