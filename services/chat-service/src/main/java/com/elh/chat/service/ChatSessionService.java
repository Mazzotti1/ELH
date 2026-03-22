package com.elh.chat.service;

import com.elh.chat.entity.ChatMessage;
import com.elh.chat.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatSessionService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ChatMessageRepository chatMessageRepository;

    @Value("${chat.session.ttl-minutes:30}")
    private int ttlMinutes;

    @Value("${chat.session.max-messages:20}")
    private int maxMessages;

    private static final String KEY_PREFIX = "chat:session:";

    public List<Map<String, String>> getConversationHistory(String sessionKey) {
        String redisKey = KEY_PREFIX + sessionKey;
        List<Object> raw = redisTemplate.opsForList().range(redisKey, 0, -1);

        if (raw != null && !raw.isEmpty()) {
            log.debug("Sessao Redis encontrada para {}: {} mensagens", sessionKey, raw.size());
            return raw.stream()
                    .map(obj -> {
                        @SuppressWarnings("unchecked")
                        Map<String, String> map = (Map<String, String>) obj;
                        return map;
                    })
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

                history.forEach(msg -> redisTemplate.opsForList().rightPush(redisKey, msg));
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

        redisTemplate.opsForList().rightPush(redisKey, message);
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
}
