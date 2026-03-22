package com.elh.gateway.consumer.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class DiscordPollPayload {
    private String channelId;
    private String title;
    private List<PollOption> options;

    @Data
    @NoArgsConstructor
    public static class PollOption {
        private String label;
        private String imageUrl;
        private String mediaId;
    }
}
