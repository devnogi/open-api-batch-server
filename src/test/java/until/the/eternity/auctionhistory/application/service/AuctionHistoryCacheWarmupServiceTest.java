package until.the.eternity.auctionhistory.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.CacheManager;
import until.the.eternity.ranking.application.service.AllTimeRankingService;

@ExtendWith(MockitoExtension.class)
class AuctionHistoryCacheWarmupServiceTest {

    @Mock private AuctionHistoryService auctionHistoryService;
    @Mock private AllTimeRankingService allTimeRankingService;
    @Mock private CacheManager cacheManager;

    @InjectMocks private AuctionHistoryCacheWarmupService service;

    @Test
    @DisplayName("배치 후 역대 랭킹 캐시는 홈 7건과 목록 20건을 워밍한다")
    void evictAndWarm_warmsHomeAndListRankingLimits() {
        service.evictAndWarm();

        ArgumentCaptor<Integer> highestPriceLimits = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Integer> monthlyVolumeLimits = ArgumentCaptor.forClass(Integer.class);
        verify(allTimeRankingService, times(2))
                .getAllTimeHighestPrice(highestPriceLimits.capture());
        verify(allTimeRankingService, times(2))
                .getMonthLargestVolume(monthlyVolumeLimits.capture());

        assertThat(highestPriceLimits.getAllValues()).isEqualTo(List.of(7, 20));
        assertThat(monthlyVolumeLimits.getAllValues()).isEqualTo(List.of(7, 20));
    }
}
