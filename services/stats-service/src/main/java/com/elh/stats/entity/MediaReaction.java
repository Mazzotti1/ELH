package com.elh.stats.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "media_reactions")
@Getter
@NoArgsConstructor
public class MediaReaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "media_id")
    private Long mediaId;

    @Column
    private String emoji;

    @Column
    private Integer count;
}
