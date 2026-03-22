package com.elh.search.service;

import com.elh.search.document.MediaDocument;
import com.elh.search.repository.MediaSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class InternalSearchService {

    private final MediaSearchRepository repository;
    private final ElasticsearchOperations esOps;

    public List<MediaDocument> search(String guildId, String term, String author, String type, int limit) {
        Criteria criteria = new Criteria("guildId").is(guildId);

        if (term != null && !term.isBlank()) {
            criteria = criteria.and(
                    new Criteria("tags").contains(term)
                            .or("authorName").contains(term)
            );
        }
        if (author != null && !author.isBlank()) {
            criteria = criteria.and("authorName").contains(author);
        }
        if (type != null && !type.isBlank()) {
            criteria = criteria.and("mediaType").is(type.toUpperCase());
        }

        CriteriaQuery query = new CriteriaQuery(criteria)
                .setPageable(PageRequest.of(0, Math.min(limit, 25), Sort.by(Sort.Direction.DESC, "createdAt")));

        SearchHits<MediaDocument> hits = esOps.search(query, MediaDocument.class);
        log.debug("Busca interna '{}': {} resultados", term, hits.getTotalHits());

        return hits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .toList();
    }

    public List<MediaDocument> getHistory(String guildId, String channelId, String type, int limit) {
        Criteria criteria = new Criteria("guildId").is(guildId);

        if (channelId != null) {
            criteria = criteria.and("channelId").is(channelId);
        }
        if (type != null && !type.isBlank()) {
            criteria = criteria.and("mediaType").is(type.toUpperCase());
        }

        CriteriaQuery query = new CriteriaQuery(criteria)
                .setPageable(PageRequest.of(0, Math.min(limit, 25), Sort.by(Sort.Direction.DESC, "createdAt")));

        SearchHits<MediaDocument> hits = esOps.search(query, MediaDocument.class);
        return hits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .toList();
    }

    public MediaDocument findById(String id) {
        return repository.findById(id).orElse(null);
    }
}
