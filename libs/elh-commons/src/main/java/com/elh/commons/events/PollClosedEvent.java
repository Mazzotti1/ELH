package com.elh.commons.events;

import lombok.*;
import lombok.experimental.SuperBuilder;

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
