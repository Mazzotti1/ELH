package com.elh.chat.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

import static com.elh.commons.config.RabbitQueues.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiscordResponseSender {

    private final RabbitTemplate rabbitTemplate;

    public void sendMessage(String channelId, String interactionToken, String content) {
        String truncated = content.length() > 1900
                ? content.substring(0, 1900) + "\n\n*[resposta truncada]*"
                : content;

        Map<String, Object> payload = Map.of(
                "channelId", channelId,
                "interactionToken", interactionToken,
                "content", truncated
        );

        rabbitTemplate.convertAndSend(DISCORD_EXCHANGE, RK_SEND_MESSAGE, payload);
        log.debug("Resposta chat enviada via RabbitMQ para canal {}", channelId);
    }
}
