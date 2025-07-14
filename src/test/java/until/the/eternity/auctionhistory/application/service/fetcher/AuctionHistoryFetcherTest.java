package until.the.eternity.auctionhistory.application.service.fetcher;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import until.the.eternity.auctionhistory.domain.service.AuctionHistoryDuplicateChecker;
import until.the.eternity.auctionhistory.infrastructure.client.AuctionHistoryClient;
import until.the.eternity.auctionhistory.interfaces.external.dto.OpenApiAuctionHistoryListResponse;
import until.the.eternity.auctionhistory.interfaces.external.dto.OpenApiAuctionHistoryResponse;
import until.the.eternity.common.enums.ItemCategory;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuctionHistoryFetcherTest {

    @Mock AuctionHistoryClient client;

    @Mock AuctionHistoryDuplicateChecker duplicateChecker;

    @InjectMocks AuctionHistoryFetcher fetcher; // 주입할 대상

    // 더미 데이터 생성 메소드
    private OpenApiAuctionHistoryResponse dummy(String id) {
        return new OpenApiAuctionHistoryResponse(
                "페러시우스 타이탄 블레이드", // itemName
                "신성한 페러시우스 타이탄 블레이드", // itemDisplayName
                ItemCategory.SWORD.getSubCategory(), // itemSubCategory
                1L, // itemCount
                100L, // auctionPricePerUnit
                Instant.now(), // dateAuctionBuy
                id, // auctionBuyId
                null // itemOption은 테스트 결과에 상관이 없으니 null 처리
                );
    }

    @Nested
    @DisplayName("정상 흐름")
    class NormalFlow {

        @Test
        @DisplayName("모든 페이지를 수집하고 cursor가 null이면 종료한다")
        void fetchAllPages() {
            // given ─ 첫 번째·두 번째 페이지
            var page1 =
                    new OpenApiAuctionHistoryListResponse(
                            List.of(dummy("1"), dummy("2")), "cursor-1");
            // 2번째 페이지가 끝이라 Nexon Open API가 null을 반환할 때
            var page2 = new OpenApiAuctionHistoryListResponse(List.of(dummy("3")), null);

            when(client.fetchAuctionHistory(ItemCategory.SWORD, null)).thenReturn(page1);
            when(client.fetchAuctionHistory(ItemCategory.SWORD, "cursor-1")).thenReturn(page2);
            // 기존 데이터와 마지막 페이지 (2페이지) 데이터의 중복이 없다고 가정
            when(duplicateChecker.hasDuplicate(any())).thenReturn(false);

            // when
            var result = fetcher.fetch(ItemCategory.SWORD);

            // then - 모든 데이터를 result에 포함
            assertThat(result)
                    .hasSize(3)
                    .extracting(OpenApiAuctionHistoryResponse::auctionBuyId)
                    .containsExactly("1", "2", "3");

            verify(client, times(2)).fetchAuctionHistory(eq(ItemCategory.SWORD), any());
            verify(duplicateChecker, times(2)).hasDuplicate(any());
        }
    }

    @Nested
    @DisplayName("조기 중단 시나리오")
    class EarlyBreakFlow {

        @Test
        @DisplayName("duplicateChecker가 true를 반환하면 수집을 중단한다")
        void stopOnDuplicate() {
            var page1 =
                    new OpenApiAuctionHistoryListResponse(
                            List.of(dummy("1"), dummy("2")), "cursor-1");

            when(client.fetchAuctionHistory(ItemCategory.SWORD, null)).thenReturn(page1);
            // 기존 데이터와 마지막 페이지 (1페이지) 데이터의 중복이 있다고 가정
            when(duplicateChecker.hasDuplicate(page1.auctionHistory().getLast())).thenReturn(true);

            var result = fetcher.fetch(ItemCategory.SWORD);

            // TODO: 중복되지 않은 데이터는 넣기는 해야된다. duplicateChecker 로직 추가 후 테스트 코드 변경
            assertThat(result).hasSize(0); // addAll 되기 전 중단

            verify(client, times(1)).fetchAuctionHistory(ItemCategory.SWORD, null);
            verifyNoMoreInteractions(client);
        }

        @Test
        @DisplayName("첫 응답이 null이면 빈 리스트를 반환한다")
        void responseNull() {
            when(client.fetchAuctionHistory(ItemCategory.SWORD, null)).thenReturn(null);

            var result = fetcher.fetch(ItemCategory.SWORD);

            assertThat(result).isEmpty();
            verify(duplicateChecker, never()).hasDuplicate(any());
        }

        @Test
        @DisplayName("auctionHistory()가 null이면 빈 리스트를 반환한다")
        void auctionHistoryNull() {
            var page = new OpenApiAuctionHistoryListResponse(null, "ignored");

            when(client.fetchAuctionHistory(ItemCategory.SWORD, null)).thenReturn(page);

            var result = fetcher.fetch(ItemCategory.SWORD);

            assertThat(result).isEmpty();
            verify(duplicateChecker, never()).hasDuplicate(any());
        }
    }
}
