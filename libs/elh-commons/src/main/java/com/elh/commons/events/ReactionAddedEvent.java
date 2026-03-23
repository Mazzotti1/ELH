package com.elh.commons.events;

import lombok.*;
import lombok.experimental.SuperBuilder;

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
