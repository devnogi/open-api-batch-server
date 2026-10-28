package until.the.eternity.hornBugle.application.runner;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import until.the.eternity.hornBugle.domain.entity.HornBugleWorldHistory;
import until.the.eternity.hornBugle.domain.repository.HornBugleRepositoryPort;
import until.the.eternity.hornBugle.infrastructure.elasticsearch.HornBugleIndexService;

/**
 * 서버 재기동 시 DB 데이터를 Elasticsearch에 일괄 색인하는 Runner. application.yml에서
 * elasticsearch.index.enabled=true로 설정 시 활성화됩니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = {"elasticsearch.enabled", "elasticsearch.index.enabled"},
        havingValue = "true",
        matchIfMissing = false)
public class HornBugleIndexRunner implements ApplicationRunner {

    private static final int BATCH_SIZE = 500;

    private final HornBugleRepositoryPort repository;
    private final HornBugleIndexService indexService;

    @Override
    public void run(ApplicationArguments args) {
        log.info("[ES] Starting batch indexing on application startup...");

        try {
            // 인덱스 삭제 후 재생성
            indexService.recreateIndex();

            // DB 전체 데이터를 배치로 조회하여 색인
            long totalIndexed = indexAllFromDatabase();

            log.info("[ES] Batch indexing completed. Total indexed: {} documents", totalIndexed);
        } catch (Exception e) {
            log.error("[ES] Batch indexing failed: {}", e.getMessage(), e);
        }
    }

    /**
     * DB의 모든 데이터를 배치로 조회하여 Elasticsearch에 색인한다.
     *
     * @return 색인된 총 문서 수
     */
    private long indexAllFromDatabase() {
        long totalIndexed = 0;
        int pageNumber = 0;

        while (true) {
            Pageable pageable = PageRequest.of(pageNumber, BATCH_SIZE);
            Page<HornBugleWorldHistory> page = repository.findAll(pageable);

            if (!page.hasContent()) {
                break;
            }

            List<HornBugleWorldHistory> entities = page.getContent();
            indexService.indexAll(entities);

            totalIndexed += entities.size();
            log.debug(
                    "[ES] Indexed page {}: {} documents (total: {})",
                    pageNumber,
                    entities.size(),
                    totalIndexed);

            if (!page.hasNext()) {
                break;
            }

            pageNumber++;
        }

        return totalIndexed;
    }
}
