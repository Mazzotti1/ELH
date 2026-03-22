package com.elh.stats.repository;

import com.elh.stats.entity.Media;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;

public interface MediaStatsRepository extends JpaRepository<Media, Long> {

    long countByGuildId(String guildId);

    long countByGuildIdAndMediaType(String guildId, String mediaType);

    @Query("SELECT m.authorName, COUNT(m) FROM Media m WHERE m.guildId = :guildId " +
            "GROUP BY m.authorName ORDER BY COUNT(m) DESC")
    List<Object[]> countByGuildIdGroupByAuthor(String guildId);

    @Query("SELECT m.authorName, COUNT(m) FROM Media m WHERE m.guildId = :guildId " +
            "AND m.createdAt >= :since GROUP BY m.authorName ORDER BY COUNT(m) DESC")
    List<Object[]> countByGuildIdAndCreatedAtAfterGroupByAuthor(String guildId, Instant since);

    @Query("SELECT m.mediaType, COUNT(m) FROM Media m WHERE m.guildId = :guildId " +
            "GROUP BY m.mediaType ORDER BY COUNT(m) DESC")
    List<Object[]> countByGuildIdGroupByType(String guildId);

    @Query("SELECT m.authorName, SUM(m.sizeBytes) FROM Media m WHERE m.guildId = :guildId " +
            "GROUP BY m.authorName ORDER BY SUM(m.sizeBytes) DESC")
    List<Object[]> sumSizeByGuildIdGroupByAuthor(String guildId);

    long countByGuildIdAndAuthorId(String guildId, String authorId);

    @Query("SELECT m.mediaType, COUNT(m) FROM Media m WHERE m.guildId = :guildId " +
            "AND m.authorId = :authorId GROUP BY m.mediaType")
    List<Object[]> countByGuildIdAndAuthorIdGroupByType(String guildId, String authorId);
}
