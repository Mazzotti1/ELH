package com.elh.ingestor.repository;

import com.elh.ingestor.entity.Media;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MediaRepository extends JpaRepository<Media, Long> {

    Optional<Media> findByGuildIdAndMessageId(String guildId, String messageId);

    boolean existsByGuildIdAndOriginalUrl(String guildId, String originalUrl);
}
