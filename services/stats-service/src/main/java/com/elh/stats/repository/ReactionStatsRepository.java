package com.elh.stats.repository;

import com.elh.stats.entity.MediaReaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ReactionStatsRepository extends JpaRepository<MediaReaction, Long> {

    @Query(value = """
            SELECT m.id, m.author_name, m.media_type, m.permanent_url, COALESCE(SUM(mr.count), 0) as total_reactions
            FROM medias m
            LEFT JOIN media_reactions mr ON m.id = mr.media_id
            WHERE m.guild_id = :guildId
            GROUP BY m.id, m.author_name, m.media_type, m.permanent_url
            ORDER BY total_reactions DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<Object[]> findTopReactedMedia(String guildId, int limit);

    @Query(value = """
            SELECT m.id, m.author_name, m.media_type, m.permanent_url, COALESCE(SUM(mr.count), 0) as total_reactions
            FROM medias m
            LEFT JOIN media_reactions mr ON m.id = mr.media_id
            WHERE m.guild_id = :guildId AND m.created_at >= NOW() - CAST(:interval AS INTERVAL)
            GROUP BY m.id, m.author_name, m.media_type, m.permanent_url
            ORDER BY total_reactions DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<Object[]> findTopReactedMediaSince(String guildId, String interval, int limit);
}
