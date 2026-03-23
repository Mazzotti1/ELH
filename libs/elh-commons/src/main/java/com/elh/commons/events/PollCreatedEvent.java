package com.elh.commons.events;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

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
