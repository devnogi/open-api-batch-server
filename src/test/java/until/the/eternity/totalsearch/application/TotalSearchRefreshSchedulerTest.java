package until.the.eternity.totalsearch.application;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TotalSearchRefreshSchedulerTest {

    @Mock private TotalSearchKeywordSyncService syncService;
    @Mock private TotalSearchService searchService;

    @Test
    void refreshSynchronizesDatabaseBeforeReplacingCache() {
        TotalSearchRefreshScheduler scheduler =
                new TotalSearchRefreshScheduler(syncService, searchService);

        scheduler.refresh();

        InOrder order = inOrder(syncService, searchService);
        order.verify(syncService).synchronizeItems();
        order.verify(searchService).refreshCache();
    }

    @Test
    void startupFailureDoesNotStopApplication() {
        TotalSearchRefreshScheduler scheduler =
                new TotalSearchRefreshScheduler(syncService, searchService);
        doThrow(new IllegalStateException("database unavailable"))
                .when(syncService)
                .synchronizeItems();

        assertThatCode(() -> scheduler.run(null)).doesNotThrowAnyException();
    }
}
