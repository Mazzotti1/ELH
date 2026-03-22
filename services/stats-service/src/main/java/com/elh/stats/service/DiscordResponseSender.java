package com.elh.stats.service;

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
public class DiscordResponseSender {

    private final RabbitTemplate rabbitTemplate;

    public void sendEmbed(String channelId, String interactionToken, String title,
                          String description, String color) {
        Map<String, Object> payload = Map.of(
                "channelId", channelId,
                "interactionToken", interactionToken,
                "title", title,
                "description", description != null ? description : "",
                "color", color != null ? color : "#5865f2",
                "imageUrls", List.of()
        );

        rabbitTemplate.convertAndSend(DISCORD_EXCHANGE, RK_SEND_EMBED, payload);
        log.debug("Stats embed enviado: '{}' para canal {}", title, channelId);
    }

    public void sendMessage(String channelId, String interactionToken, String content) {
        Map<String, Object> payload = Map.of(
                "channelId", channelId,
                "interactionToken", interactionToken,
                "content", content
        );

        rabbitTemplate.convertAndSend(DISCORD_EXCHANGE, RK_SEND_MESSAGE, payload);
    }
}
