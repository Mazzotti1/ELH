package com.elh.commons.events;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.UUID;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "eventType")
@JsonSubTypes({
    @JsonSubTypes.Type(value = MessageReceivedEvent.class,  name = "MESSAGE_RECEIVED"),
    @JsonSubTypes.Type(value = MediaDetectedEvent.class,    name = "MEDIA_DETECTED"),
    @JsonSubTypes.Type(value = MediaSavedEvent.class,       name = "MEDIA_SAVED"),
    @JsonSubTypes.Type(value = CommandReceivedEvent.class,  name = "COMMAND_RECEIVED"),
    @JsonSubTypes.Type(value = PollCreatedEvent.class,      name = "POLL_CREATED"),
    @JsonSubTypes.Type(value = PollClosedEvent.class,       name = "POLL_CLOSED"),
    @JsonSubTypes.Type(value = ChatRequestedEvent.class,    name = "CHAT_REQUESTED"),
    @JsonSubTypes.Type(value = ChatRespondedEvent.class,    name = "CHAT_RESPONDED"),
    @JsonSubTypes.Type(value = ReactionAddedEvent.class,   name = "REACTION_ADDED"),
})
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseEvent {

    private String eventId = UUID.randomUUID().toString();
    private Instant occurredAt = Instant.now();
    private String guildId;
    public abstract String getEventType();
}
