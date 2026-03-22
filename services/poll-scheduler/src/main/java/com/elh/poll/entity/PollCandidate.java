package com.elh.poll.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "poll_candidates")
@IdClass(PollCandidateId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PollCandidate {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "poll_id", nullable = false)
    private Poll poll;

    @Id
    @Column(name = "media_id", nullable = false)
    private Long mediaId;

    @Column(name = "vote_count", nullable = false)
    @Builder.Default
    private Integer voteCount = 0;
}
