package com.elh.poll.repository;

import com.elh.poll.entity.Media;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MediaRepository extends JpaRepository<Media, Long> {

    @Query(value = "SELECT * FROM medias WHERE guild_id = :guildId ORDER BY RANDOM() LIMIT :limit",
            nativeQuery = true)
    List<Media> findRandomByGuildId(String guildId, int limit);
}
