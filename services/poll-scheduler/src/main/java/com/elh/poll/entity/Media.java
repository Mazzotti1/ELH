package com.elh.poll.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "medias")
@Getter
@NoArgsConstructor
public class Media {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "guild_id", nullable = false)
    private String guildId;

    @Column(name = "author_name", nullable = false)
    private String authorName;

    @Column(name = "permanent_url", nullable = false)
    private String permanentUrl;

    @Column(name = "s3_thumbnail_key")
    private String s3ThumbnailKey;

    @Column(name = "media_type", nullable = false)
    private String mediaType;
}
