package com.elh.stats.service;

import com.elh.stats.repository.MediaStatsRepository;
import com.elh.stats.repository.ReactionStatsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatsService {

    private final MediaStatsRepository mediaRepo;
    private final ReactionStatsRepository reactionRepo;

    public String buildGuildStats(String guildId) {
        long total = mediaRepo.countByGuildId(guildId);
        long images = mediaRepo.countByGuildIdAndMediaType(guildId, "IMAGE");
        long videos = mediaRepo.countByGuildIdAndMediaType(guildId, "VIDEO");
        long links = mediaRepo.countByGuildIdAndMediaType(guildId, "LINK");

        List<Object[]> topAuthors = mediaRepo.countByGuildIdGroupByAuthor(guildId);
        List<Object[]> typeBreakdown = mediaRepo.countByGuildIdGroupByType(guildId);

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("**Total de midias:** %d\n", total));
        sb.append(String.format("🖼️ Imagens: %d | 🎬 Videos: %d | 🔗 Links: %d\n\n", images, videos, links));

        sb.append("**Top contribuidores (all-time):**\n");
        int rank = 1;
        for (Object[] row : topAuthors.stream().limit(10).toList()) {
            sb.append(String.format("%d. **%s** — %d midias\n", rank++, row[0], ((Number) row[1]).longValue()));
        }

        return sb.toString();
    }

    public String buildMemberStats(String guildId, String authorId) {
        long total = mediaRepo.countByGuildIdAndAuthorId(guildId, authorId);
        List<Object[]> byType = mediaRepo.countByGuildIdAndAuthorIdGroupByType(guildId, authorId);

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("**Total de midias:** %d\n", total));

        for (Object[] row : byType) {
            String emoji = switch ((String) row[0]) {
                case "IMAGE" -> "🖼️";
                case "VIDEO" -> "🎬";
                case "LINK" -> "🔗";
                default -> "📎";
            };
            sb.append(String.format("%s %s: %d\n", emoji, row[0], ((Number) row[1]).longValue()));
        }

        return sb.toString();
    }

    public String buildTopReacted(String guildId, String periodo) {
        List<Object[]> top;
        String titulo;

        switch (periodo != null ? periodo.toLowerCase() : "semana") {
            case "mes" -> {
                top = reactionRepo.findTopReactedMediaSince(guildId, "30 days", 10);
                titulo = "Top midias do mes";
            }
            case "all-time", "all" -> {
                top = reactionRepo.findTopReactedMedia(guildId, 10);
                titulo = "Top midias all-time";
            }
            default -> {
                top = reactionRepo.findTopReactedMediaSince(guildId, "7 days", 10);
                titulo = "Top midias da semana";
            }
        }

        if (top.isEmpty()) {
            return "Nenhuma midia com reacoes encontrada.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("**").append(titulo).append("**\n\n");

        int rank = 1;
        for (Object[] row : top) {
            long reactions = ((Number) row[4]).longValue();
            if (reactions == 0) continue;
            sb.append(String.format("%d. **%s** (%s) — %d reacoes [ver](%s)\n",
                    rank++, row[1], row[2], reactions, row[3]));
        }

        return sb.length() > 0 ? sb.toString() : "Nenhuma midia com reacoes encontrada.";
    }

    public String buildWeeklyActivity(String guildId) {
        Instant weekAgo = Instant.now().minus(7, ChronoUnit.DAYS);
        List<Object[]> weeklyAuthors = mediaRepo.countByGuildIdAndCreatedAtAfterGroupByAuthor(guildId, weekAgo);

        if (weeklyAuthors.isEmpty()) {
            return "Nenhuma atividade na ultima semana.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("**Atividade dos ultimos 7 dias:**\n\n");

        int rank = 1;
        for (Object[] row : weeklyAuthors.stream().limit(10).toList()) {
            sb.append(String.format("%d. **%s** — %d midias\n", rank++, row[0], ((Number) row[1]).longValue()));
        }

        return sb.toString();
    }
}
