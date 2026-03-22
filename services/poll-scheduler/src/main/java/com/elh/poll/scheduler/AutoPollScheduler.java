package com.elh.poll.scheduler;

import com.elh.poll.entity.Poll;
import com.elh.poll.repository.PollRepository;
import com.elh.poll.service.PollService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "poll.auto.enabled", havingValue = "true")
public class AutoPollScheduler {

    private final PollService pollService;
    private final PollRepository pollRepository;

    @Value("${poll.auto.candidates-count:5}")
    private int candidatesCount;

    @Value("${poll.auto.duration-hours:48}")
    private long durationHours;

    @Value("${poll.auto.default-title:Midia da Semana}")
    private String defaultTitle;

    @Scheduled(cron = "${poll.auto.cron}")
    public void createWeeklyPoll() {
        log.info("Iniciando criacao de enquetes automaticas semanais...");

        List<String> guildIds = pollRepository.findAll().stream()
                .map(Poll::getGuildId)
                .distinct()
                .toList();

        if (guildIds.isEmpty()) {
            log.info("Nenhum guild registrado para enquete automatica");
            return;
        }

        for (String guildId : guildIds) {
            List<Poll> recentPolls = pollRepository.findByGuildIdAndStatusOrderByCreatedAtDesc(guildId, "CLOSED");
            String channelId = recentPolls.isEmpty() ? null : recentPolls.get(0).getChannelId();

            if (channelId == null) {
                List<Poll> openPolls = pollRepository.findByGuildIdAndStatusOrderByCreatedAtDesc(guildId, "OPEN");
                channelId = openPolls.isEmpty() ? null : openPolls.get(0).getChannelId();
            }

            if (channelId == null) {
                log.warn("Nenhum canal encontrado para guild {}, pulando enquete automatica", guildId);
                continue;
            }

            pollService.createPoll(guildId, channelId, defaultTitle, candidatesCount, durationHours);
        }
    }

    @Scheduled(fixedRate = 60_000)
    public void checkExpiredPolls() {
        pollService.closeExpiredPolls();
    }
}
