package com.elh.chat.repository;

import com.elh.chat.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findTop20ByGuildIdAndChannelIdAndAuthorIdOrderByCreatedAtDesc(
            String guildId, String channelId, String authorId);
}
