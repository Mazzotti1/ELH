package com.elh.gateway.consumer.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DiscordMessagePayload {
    private String channelId;
    private String content;
    private String interactionId;
    private String interactionToken;
}
