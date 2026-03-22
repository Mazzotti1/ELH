package com.elh.notification.service;

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
public class DiscordNotificationSender {

    private final RabbitTemplate rabbitTemplate;

    public void sendEmbed(String channelId, String title, String description,
                          String color, List<String> imageUrls) {
        Map<String, Object> payload = Map.of(
                "channelId", channelId,
                "title", title,
                "description", description != null ? description : "",
                "color", color != null ? color : "#5865f2",
                "imageUrls", imageUrls != null ? imageUrls : List.of()
        );

        rabbitTemplate.convertAndSend(DISCORD_EXCHANGE, RK_SEND_EMBED, payload);
        log.debug("Notificacao embed enviada: '{}' para canal {}", title, channelId);
    }

    public void sendMessage(String channelId, String content) {
        Map<String, Object> payload = Map.of(
                "channelId", channelId,
                "content", content
        );

        rabbitTemplate.convertAndSend(DISCORD_EXCHANGE, RK_SEND_MESSAGE, payload);
        log.debug("Notificacao mensagem enviada para canal {}", channelId);
    }
}
