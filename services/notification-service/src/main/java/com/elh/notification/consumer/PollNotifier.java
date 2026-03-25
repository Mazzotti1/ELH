package com.elh.notification.consumer;

import com.elh.commons.config.KafkaTopics;
import com.elh.commons.events.PollClosedEvent;
import com.elh.notification.service.DiscordNotificationSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PollNotifier {

    private final DiscordNotificationSender sender;

    @KafkaListener(topics = KafkaTopics.POLL_CLOSED)
    public void handlePollClosed(PollClosedEvent event) {
        MDC.put("correlationId", event.getCorrelationId() != null ? event.getCorrelationId() : event.getEventId());
        try {
        log.info("Notificacao poll.closed: enquete #{} vencedor=media#{}",
                event.getPollId(), event.getWinnerMediaId());

        String description;
        List<String> images;

        if (event.getWinnerMediaId() != null) {
            description = String.format(
                    "**Vencedor:** %s\n**Total de votos:** %d\n\nParabens! 🎉",
                    event.getWinnerAuthorName(), event.getTotalVotes()
            );
            images = event.getWinnerMediaUrl() != null
                    ? List.of(event.getWinnerMediaUrl())
                    : List.of();
        } else {
            description = "Nenhum voto registrado nesta enquete.";
            images = List.of();
        }

        sender.sendEmbed(
                event.getChannelId(),
                "Enquete #" + event.getPollId() + " encerrada!",
                description,
                "#f4c430",
                images
        );
        } finally {
            MDC.remove("correlationId");
        }
    }
}
