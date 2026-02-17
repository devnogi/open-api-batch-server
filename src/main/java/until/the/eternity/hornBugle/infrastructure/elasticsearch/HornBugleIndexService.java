package until.the.eternity.hornBugle.infrastructure.elasticsearch;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.stereotype.Service;
import until.the.eternity.hornBugle.domain.entity.HornBugleWorldHistory;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "elasticsearch.enabled", havingValue = "true", matchIfMissing = false)
public class HornBugleIndexService {

    private static final String INDEX_NAME = "horn_bugle_world_history";
    private static final int BATCH_SIZE = 500;

    private final ElasticsearchOperations elasticsearchOperations;

    /**
     * 단일 엔티티를 Elasticsearch에 색인한다.
     *
     * @param entity 색인할 엔티티
     */
    public void index(HornBugleWorldHistory entity) {
        try {
            HornBugleDocument document = HornBugleDocument.from(entity);
            elasticsearchOperations.save(document, IndexCoordinates.of(INDEX_NAME));
            log.debug("[ES] Indexed document: id={}", entity.getId());
        } catch (Exception e) {
            log.error(
                    "[ES] Failed to index document: id={}, error={}",
                    entity.getId(),
                    e.getMessage(),
                    e);
        }
    }

    /**
     * 여러 엔티티를 Elasticsearch에 일괄 색인한다.
     *
     * @param entities 색인할 엔티티 목록
     */
    public void indexAll(List<HornBugleWorldHistory> entities) {
        if (entities == null || entities.isEmpty()) {
            return;
        }

        try {
            List<HornBugleDocument> documents =
                    entities.stream().map(HornBugleDocument::from).toList();

            for (int i = 0; i < documents.size(); i += BATCH_SIZE) {
                int toIndex = Math.min(i + BATCH_SIZE, documents.size());
                List<HornBugleDocument> batch = documents.subList(i, toIndex);
                for (HornBugleDocument document : batch) {
                    elasticsearchOperations.save(document, IndexCoordinates.of(INDEX_NAME));
                }
                log.debug("[ES] Indexed batch: {} documents", batch.size());
            }

            log.info("[ES] Successfully indexed {} documents", entities.size());
        } catch (Exception e) {
            log.error("[ES] Failed to index {} documents: {}", entities.size(), e.getMessage(), e);
        }
    }

    /** 인덱스를 삭제하고 재생성한다. */
    public void recreateIndex() {
        try {
            IndexOperations indexOps = elasticsearchOperations.indexOps(HornBugleDocument.class);

            if (indexOps.exists()) {
                indexOps.delete();
                log.info("[ES] Deleted existing index: {}", INDEX_NAME);
            }

            indexOps.createWithMapping();
            log.info("[ES] Created index with mapping: {}", INDEX_NAME);
        } catch (Exception e) {
            log.error("[ES] Failed to recreate index: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to recreate Elasticsearch index", e);
        }
    }

    /**
     * keyword로 검색한다. serverName이 있으면 필터로 추가한다.
     *
     * @param keyword 검색 키워드
     * @param serverName 서버명 필터 (nullable)
     * @param pageable 페이징 정보
     * @return 검색 결과
     */
    public Page<HornBugleDocument> search(String keyword, String serverName, Pageable pageable) {
        try {
            // Multi-match query for keyword search
            // Note: date_send is Date type, excluded from text search
            // server_name is Keyword type, included for exact match
            Query multiMatchQuery =
                    Query.of(
                            q ->
                                    q.multiMatch(
                                            mm ->
                                                    mm.query(keyword)
                                                            .fields("character_name", "message")));

            // Build bool query
            BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder().must(multiMatchQuery);

            // Add serverName filter if present
            if (serverName != null && !serverName.isBlank()) {
                Query serverFilter =
                        Query.of(q -> q.term(t -> t.field("server_name").value(serverName)));
                boolQueryBuilder.filter(serverFilter);
            }

            Query finalQuery = Query.of(q -> q.bool(boolQueryBuilder.build()));

            NativeQuery searchQuery =
                    NativeQuery.builder().withQuery(finalQuery).withPageable(pageable).build();

            SearchHits<HornBugleDocument> searchHits =
                    elasticsearchOperations.search(
                            searchQuery, HornBugleDocument.class, IndexCoordinates.of(INDEX_NAME));

            List<HornBugleDocument> content =
                    searchHits.getSearchHits().stream().map(SearchHit::getContent).toList();

            return new PageImpl<>(content, pageable, searchHits.getTotalHits());
        } catch (Exception e) {
            log.error(
                    "[ES] Search failed: keyword={}, serverName={}, error={}",
                    keyword,
                    serverName,
                    e.getMessage(),
                    e);
            throw new RuntimeException("Elasticsearch search failed", e);
        }
    }

    /**
     * 인덱스 존재 여부를 확인한다.
     *
     * @return 인덱스 존재 여부
     */
    public boolean indexExists() {
        try {
            return elasticsearchOperations.indexOps(HornBugleDocument.class).exists();
        } catch (Exception e) {
            log.error("[ES] Failed to check index existence: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Elasticsearch 서버가 사용 가능한 상태인지 확인한다.
     *
     * @return ES 서버 사용 가능 여부
     */
    public boolean isAvailable() {
        try {
            return elasticsearchOperations.indexOps(HornBugleDocument.class).exists()
                    || elasticsearchOperations.indexOps(HornBugleDocument.class).create();
        } catch (Exception e) {
            log.warn("[ES] Elasticsearch is not available: {}", e.getMessage());
            return false;
        }
    }
}
