package com.elh.search.repository;

import com.elh.search.document.MediaDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface MediaSearchRepository extends ElasticsearchRepository<MediaDocument, String> {

    Page<MediaDocument> findByGuildIdAndTagsContaining(String guildId, String tag, Pageable pageable);

    Page<MediaDocument> findByGuildIdAndAuthorNameContainingIgnoreCase(String guildId, String authorName, Pageable pageable);

    Page<MediaDocument> findByGuildIdAndMediaType(String guildId, String mediaType, Pageable pageable);

    Page<MediaDocument> findByGuildIdAndChannelId(String guildId, String channelId, Pageable pageable);

    List<MediaDocument> findByGuildIdOrderByCreatedAtDesc(String guildId, Pageable pageable);
}
