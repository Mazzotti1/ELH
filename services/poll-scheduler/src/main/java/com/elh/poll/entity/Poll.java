package com.elh.poll.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "polls")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Poll {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "guild_id", nullable = false, length = 20)
    private String guildId;

    @Column(name = "channel_id", nullable = false, length = 20)
    private String channelId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(name = "discord_message_id", length = 20)
    private String discordMessageId;

    @Column(nullable = false, length = 10)
    @Builder.Default
    private String status = "OPEN";

    @Column(name = "winner_media_id")
    private Long winnerMediaId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "closes_at", nullable = false)
    private Instant closesAt;

    @Column(name = "closed_at")
    private Instant closedAt;

    @OneToMany(mappedBy = "poll", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PollCandidate> candidates = new ArrayList<>();

    public void addCandidate(Long mediaId) {
        PollCandidate candidate = PollCandidate.builder()
                .poll(this)
                .mediaId(mediaId)
                .voteCount(0)
                .build();
        candidates.add(candidate);
    }
}
