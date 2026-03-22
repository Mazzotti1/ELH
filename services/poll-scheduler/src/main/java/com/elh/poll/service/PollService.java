package com.elh.poll.service;

import com.elh.commons.config.KafkaTopics;
import com.elh.commons.events.BaseEvent;
import com.elh.commons.events.PollClosedEvent;
import com.elh.commons.events.PollCreatedEvent;
import com.elh.poll.entity.Media;
import com.elh.poll.entity.Poll;
import com.elh.poll.entity.PollCandidate;
import com.elh.poll.repository.MediaRepository;
import com.elh.poll.repository.PollRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PollService {

    private final PollRepository pollRepository;
    private final MediaRepository mediaRepository;
    private final DiscordPollSender pollSender;
    private final KafkaTemplate<String, BaseEvent> kafkaTemplate;

    @Transactional
    public Poll createPoll(String guildId, String channelId, String title, int candidatesCount, long durationHours) {
        List<Media> candidates = mediaRepository.findRandomByGuildId(guildId, candidatesCount);

        if (candidates.size() < 2) {
            log.warn("Nao ha midias suficientes no guild {} para criar enquete (encontradas: {})",
                    guildId, candidates.size());
            return null;
        }

        Poll poll = Poll.builder()
                .guildId(guildId)
                .channelId(channelId)
                .title(title)
                .closesAt(Instant.now().plus(durationHours, ChronoUnit.HOURS))
                .build();

        candidates.forEach(media -> poll.addCandidate(media.getId()));

        Poll saved = pollRepository.save(poll);

        List<Map<String, String>> options = candidates.stream()
                .map(m -> Map.of(
                        "label", m.getAuthorName() + " - " + m.getMediaType(),
                        "imageUrl", m.getPermanentUrl(),
                        "mediaId", String.valueOf(m.getId())
                ))
                .toList();

        pollSender.sendPoll(channelId, title, options);

        List<Long> mediaIds = candidates.stream().map(Media::getId).toList();
        PollCreatedEvent event = PollCreatedEvent.builder()
                .guildId(guildId)
                .pollId(saved.getId())
                .channelId(channelId)
                .title(title)
                .mediaIds(mediaIds)
                .closesAt(saved.getClosesAt())
                .build();

        kafkaTemplate.send(KafkaTopics.POLL_CREATED, guildId, event);
        log.info("Enquete #{} criada no guild {} com {} candidatos", saved.getId(), guildId, candidates.size());

        return saved;
    }

    @Transactional
    public void closeExpiredPolls() {
        List<Poll> expired = pollRepository.findByStatusAndClosesAtBefore("OPEN", Instant.now());

        for (Poll poll : expired) {
            PollCandidate winner = poll.getCandidates().stream()
                    .max(Comparator.comparingInt(PollCandidate::getVoteCount))
                    .orElse(null);

            poll.setStatus("CLOSED");
            poll.setClosedAt(Instant.now());

            if (winner != null) {
                poll.setWinnerMediaId(winner.getMediaId());
            }

            pollRepository.save(poll);

            String winnerUrl = "";
            String winnerAuthor = "";
            int totalVotes = poll.getCandidates().stream()
                    .mapToInt(PollCandidate::getVoteCount)
                    .sum();

            if (winner != null) {
                Media winnerMedia = mediaRepository.findById(winner.getMediaId()).orElse(null);
                if (winnerMedia != null) {
                    winnerUrl = winnerMedia.getPermanentUrl();
                    winnerAuthor = winnerMedia.getAuthorName();
                }
            }

            PollClosedEvent event = PollClosedEvent.builder()
                    .guildId(poll.getGuildId())
                    .pollId(poll.getId())
                    .channelId(poll.getChannelId())
                    .winnerMediaId(winner != null ? winner.getMediaId() : null)
                    .winnerMediaUrl(winnerUrl)
                    .winnerAuthorName(winnerAuthor)
                    .totalVotes(totalVotes)
                    .build();

            kafkaTemplate.send(KafkaTopics.POLL_CLOSED, poll.getGuildId(), event);
            log.info("Enquete #{} fechada. Vencedor: media #{} com {} votos totais",
                    poll.getId(), poll.getWinnerMediaId(), totalVotes);
        }
    }
}
