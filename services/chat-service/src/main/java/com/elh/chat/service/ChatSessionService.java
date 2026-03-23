package com.elh.chat.service;

import com.elh.chat.entity.ChatMessage;
import com.elh.chat.repository.ChatMessageRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatSessionService {

    private final StringRedisTemplate redisTemplate;
    private final ChatMessageRepository chatMessageRepository;
    private final ObjectMapper objectMapper;

    @Value("${chat.session.ttl-minutes:30}")
    private int ttlMinutes;

    @Value("${chat.session.max-messages:20}")
    private int maxMessages;

    private static final String KEY_PREFIX = "chat:session:";

    public List<Map<String, String>> getConversationHistory(String sessionKey) {
        String redisKey = KEY_PREFIX + sessionKey;
        List<String> raw = redisTemplate.opsForList().range(redisKey, 0, -1);

        if (raw != null && !raw.isEmpty()) {
            log.debug("Sessao Redis encontrada para {}: {} mensagens", sessionKey, raw.size());
            return raw.stream()
                    .map(this::deserializeMessage)
                    .filter(Objects::nonNull)
                    .toList();
        }

        String[] parts = sessionKey.split(":");
        if (parts.length == 3) {
            List<ChatMessage> dbMessages = chatMessageRepository
                    .findTop20ByGuildIdAndChannelIdAndAuthorIdOrderByCreatedAtDesc(
                            parts[0], parts[1], parts[2]);

            if (!dbMessages.isEmpty()) {
                List<Map<String, String>> history = dbMessages.reversed().stream()
                        .map(msg -> Map.of("role", msg.getRole(), "content", msg.getContent()))
                        .toList();

                history.forEach(msg -> redisTemplate.opsForList().rightPush(redisKey, serializeMessage(msg)));
                redisTemplate.expire(redisKey, Duration.ofMinutes(ttlMinutes));

                log.debug("Sessao restaurada do Postgres para {}: {} mensagens", sessionKey, history.size());
                return history;
            }
        }

        return Collections.emptyList();
    }

    public void addMessage(String sessionKey, String role, String content) {
        String redisKey = KEY_PREFIX + sessionKey;
        Map<String, String> message = Map.of("role", role, "content", content);

        redisTemplate.opsForList().rightPush(redisKey, serializeMessage(message));
        redisTemplate.expire(redisKey, Duration.ofMinutes(ttlMinutes));

        Long size = redisTemplate.opsForList().size(redisKey);
        if (size != null && size > maxMessages) {
            redisTemplate.opsForList().leftPop(redisKey);
        }
    }

    public void persistMessage(String guildId, String channelId, String authorId,
                                String role, String content, Integer tokensUsed) {
        ChatMessage entity = ChatMessage.builder()
                .guildId(guildId)
                .channelId(channelId)
                .authorId(authorId)
                .role(role)
                .content(content)
                .tokensUsed(tokensUsed)
                .build();
        chatMessageRepository.save(entity);
    }

    private String serializeMessage(Map<String, String> msg) {
        try {
            return objectMapper.writeValueAsString(msg);
        } catch (JsonProcessingException e) {
            log.error("Erro ao serializar mensagem para Redis", e);
            return "{}";
        }
    }

    private Map<String, String> deserializeMessage(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            log.error("Erro ao deserializar mensagem do Redis: {}", json, e);
            return null;
        }
    }
}
