package com.elh.poll.consumer;

import com.elh.commons.config.KafkaTopics;
import com.elh.commons.events.CommandReceivedEvent;
import com.elh.poll.entity.Poll;
import com.elh.poll.service.DiscordPollSender;
import com.elh.poll.service.PollService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class PollCommandConsumer {

    private final PollService pollService;
    private final DiscordPollSender sender;

    @Value("${poll.auto.candidates-count:5}")
    private int candidatesCount;

    @Value("${poll.auto.duration-hours:48}")
    private long durationHours;

    @KafkaListener(topics = KafkaTopics.DISCORD_COMMANDS)
    public void handle(CommandReceivedEvent event) {
        if (!"poll".equals(event.getCommand())) return;

        MDC.put("correlationId", event.getCorrelationId() != null ? event.getCorrelationId() : event.getEventId());
        try {
        log.info("Comando /poll recebido de {} no guild {}", event.getAuthorName(), event.getGuildId());

        Map<String, String> opts = event.getOptions();
        String titulo = opts.getOrDefault("titulo", "Enquete Manual");

        Poll poll = pollService.createPoll(
                event.getGuildId(),
                event.getChannelId(),
                titulo,
                candidatesCount,
                durationHours
        );

        if (poll == null) {
            sender.sendMessage(
                    event.getChannelId(),
                    event.getInteractionToken(),
                    "Nao ha midias suficientes neste servidor para criar uma enquete (minimo 2)."
            );
            return;
        }

        sender.sendEmbed(
                event.getChannelId(),
                event.getInteractionToken(),
                "Enquete criada!",
                String.format("**%s**\n%d candidatos | Fecha em %dh\nReaja com os numeros para votar!",
                        titulo, poll.getCandidates().size(), durationHours),
                "#f4c430"
        );
        } finally {
            MDC.remove("correlationId");
        }
    }
}
