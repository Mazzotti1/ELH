package com.elh.commons.events;

import lombok.*;
import lombok.experimental.SuperBuilder;

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
