package until.the.eternity.totalsearch.application;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import until.the.eternity.totalsearch.infrastructure.TotalSearchKeywordRepository;

@ExtendWith(MockitoExtension.class)
class TotalSearchKeywordSyncServiceTest {

    @Mock private TotalSearchKeywordRepository repository;

    @Test
    void syncUpsertsDistinctItemNamesBeforeRemovingMissingItems() {
        when(repository.upsertItemKeywords()).thenReturn(3);
        when(repository.deleteMissingItemKeywords()).thenReturn(1);

        new TotalSearchKeywordSyncService(repository).synchronizeItems();

        InOrder order = inOrder(repository);
        order.verify(repository).upsertItemKeywords();
        order.verify(repository).deleteMissingItemKeywords();
    }
}
