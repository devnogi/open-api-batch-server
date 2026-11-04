package until.the.eternity.totalsearch.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Order(30)
@Component
@RequiredArgsConstructor
public class TotalSearchRefreshScheduler implements ApplicationRunner {

    private final TotalSearchKeywordSyncService syncService;
    private final TotalSearchService searchService;

    @Override
    public void run(ApplicationArguments args) {
        try {
            refresh();
        } catch (RuntimeException e) {
            log.warn("[Total Search] startup refresh failed; application will continue", e);
        }
    }

    @Scheduled(cron = "${total-search.refresh-cron:0 0 * * * *}", zone = "Asia/Seoul")
    public void refresh() {
        syncService.synchronizeItems();
        searchService.refreshCache();
    }
}
