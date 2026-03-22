package com.elh.stats.consumer;

import com.elh.commons.config.KafkaTopics;
import com.elh.commons.events.CommandReceivedEvent;
import com.elh.stats.service.DiscordResponseSender;
import com.elh.stats.service.StatsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class StatsCommandConsumer {

    private final StatsService statsService;
    private final DiscordResponseSender sender;

    @KafkaListener(topics = KafkaTopics.DISCORD_COMMANDS)
    public void handle(CommandReceivedEvent event) {
        switch (event.getCommand()) {
            case "stats" -> handleStats(event);
            case "top" -> handleTop(event);
            default -> { /*ignorar*/ }
        }
    }

    private void handleStats(CommandReceivedEvent event) {
        log.info("Comando /stats de {} no guild {}", event.getAuthorName(), event.getGuildId());

        Map<String, String> opts = event.getOptions();
        String memberId = opts.get("member");

        String description;
        String title;

        if (memberId != null && !memberId.isBlank()) {
            description = statsService.buildMemberStats(event.getGuildId(), memberId);
            title = "📊 Stats de " + memberId;
        } else {
            description = statsService.buildGuildStats(event.getGuildId());
            title = "📊 Estatisticas do Servidor";
        }

        sender.sendEmbed(
                event.getChannelId(),
                event.getInteractionToken(),
                title,
                description,
                "#5865f2"
        );
    }

    private void handleTop(CommandReceivedEvent event) {
        log.info("Comando /top de {} no guild {}", event.getAuthorName(), event.getGuildId());

        String periodo = event.getOptions().get("periodo");
        String description = statsService.buildTopReacted(event.getGuildId(), periodo);

        sender.sendEmbed(
                event.getChannelId(),
                event.getInteractionToken(),
                "🏆 Ranking de Midias",
                description,
                "#f4c430"
        );
    }
}
