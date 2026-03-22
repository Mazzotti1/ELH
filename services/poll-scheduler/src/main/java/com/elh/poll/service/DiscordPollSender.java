package com.elh.poll.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static com.elh.commons.config.RabbitQueues.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiscordPollSender {

    private final RabbitTemplate rabbitTemplate;

    public void sendPoll(String channelId, String title, List<Map<String, String>> options) {
        Map<String, Object> payload = Map.of(
                "channelId", channelId,
                "title", title,
                "options", options
        );

        rabbitTemplate.convertAndSend(DISCORD_EXCHANGE, RK_SEND_POLL, payload);
        log.debug("Enquete '{}' enviada via RabbitMQ para canal {}", title, channelId);
    }

    public void sendMessage(String channelId, String interactionToken, String content) {
        Map<String, Object> payload = Map.of(
                "channelId", channelId,
                "interactionToken", interactionToken,
                "content", content
        );

        rabbitTemplate.convertAndSend(DISCORD_EXCHANGE, RK_SEND_MESSAGE, payload);
    }

    public void sendEmbed(String channelId, String interactionToken, String title,
                          String description, String color) {
        Map<String, Object> payload = Map.of(
                "channelId", channelId,
                "interactionToken", interactionToken,
                "title", title,
                "description", description != null ? description : "",
                "color", color != null ? color : "#f4c430",
                "imageUrls", List.of()
        );

        rabbitTemplate.convertAndSend(DISCORD_EXCHANGE, RK_SEND_EMBED, payload);
    }
}
