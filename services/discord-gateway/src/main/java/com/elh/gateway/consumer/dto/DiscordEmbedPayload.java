package com.elh.gateway.consumer.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class DiscordEmbedPayload {
    private String channelId;
    private String interactionId;
    private String interactionToken;
    private String title;
    private String description;
    private String color;
    private String thumbnailUrl;
    private List<EmbedField> fields;
    private List<String> imageUrls;
    private boolean followUp;

    @Data
    @NoArgsConstructor
    public static class EmbedField {
        private String name;
        private String value;
        private boolean inline;
    }
}
