package until.the.eternity.totalsearch.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import until.the.eternity.totalsearch.infrastructure.TotalSearchKeywordRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class TotalSearchKeywordSyncService {

    private final TotalSearchKeywordRepository repository;

    @Transactional
    public void synchronizeItems() {
        int upserted = repository.upsertItemKeywords();
        int deleted = repository.deleteMissingItemKeywords();
        log.info(
                "[Total Search] item keywords synchronized: upserted={}, deleted={}",
                upserted,
                deleted);
    }
}
