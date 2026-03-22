package com.elh.poll.repository;

import com.elh.poll.entity.Poll;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface PollRepository extends JpaRepository<Poll, Long> {

    List<Poll> findByStatusAndClosesAtBefore(String status, Instant now);

    List<Poll> findByGuildIdAndStatusOrderByCreatedAtDesc(String guildId, String status);
}
