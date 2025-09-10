package until.the.eternity.auctionhistory.application.service.fetcher;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import until.the.eternity.auctionhistory.domain.service.AuctionHistoryDuplicateChecker;
import until.the.eternity.auctionhistory.infrastructure.client.AuctionHistoryClient;
import until.the.eternity.auctionhistory.interfaces.external.dto.OpenApiAuctionHistoryListResponse;
import until.the.eternity.auctionhistory.interfaces.external.dto.OpenApiAuctionHistoryResponse;
import until.the.eternity.common.enums.ItemCategory;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
    @DisplayName("OPEN API 끝까지 호출 시나리오")
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

            when(client.fetchAuctionHistory(ItemCategory.SWORD, "")).thenReturn(Mono.just(page1));
            when(client.fetchAuctionHistory(ItemCategory.SWORD, "cursor-1"))
                    .thenReturn(Mono.just(page2));
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
    @DisplayName("OPEN API 호출 중단 시나리오")
    class EarlyBreakFlow {

        @Test
        @DisplayName("duplicateChecker가 true를 반환하면 수집을 중단한다")
        void stopOnDuplicate() {
            // given - API 호출을 1번만 하고 중복으로 인해 중단
            var page1 =
                    new OpenApiAuctionHistoryListResponse(
                            List.of(dummy("1"), dummy("2")), "cursor-1");

            when(client.fetchAuctionHistory(ItemCategory.SWORD, "")).thenReturn(Mono.just(page1));
            // when - 기존 데이터와 첫 페이지 데이터의 중복이 있다고 가정
            when(duplicateChecker.hasDuplicate(page1.auctionHistory().getLast())).thenReturn(true);

            var result = fetcher.fetch(ItemCategory.SWORD);

            // then - 첫 페이지 데이터만 수집하고 종료
            assertThat(result).hasSize(2);
            assertThat(result.getFirst().auctionBuyId()).isEqualTo("1");

            verify(client, times(1)).fetchAuctionHistory(ItemCategory.SWORD, "");
            verifyNoMoreInteractions(client);
        }

        @Test
        @DisplayName("첫 응답이 null(Mono.empty)이면 빈 리스트를 반환한다")
        void responseNull() {
            when(client.fetchAuctionHistory(ItemCategory.SWORD, "")).thenReturn(Mono.empty());

            var result = fetcher.fetch(ItemCategory.SWORD);

            assertThat(result).isEmpty();
            verify(duplicateChecker, never()).hasDuplicate(any());
        }

        @Test
        @DisplayName("auctionHistory()가 비어있으면 빈 리스트를 반환한다")
        void auctionHistoryEmpty() {
            var page = new OpenApiAuctionHistoryListResponse(List.of(), "ignored");

            when(client.fetchAuctionHistory(ItemCategory.SWORD, "")).thenReturn(Mono.just(page));

            var result = fetcher.fetch(ItemCategory.SWORD);

            assertThat(result).isEmpty();
            verify(duplicateChecker, never()).hasDuplicate(any());
        }

        @Test
        @DisplayName("nextCursor가 빈 문자열이면 수집을 중단한다")
        void stopWhenNextCursorIsEmptyString() {
            // given
            var page1 = new OpenApiAuctionHistoryListResponse(List.of(dummy("1")), ""); // 커서가 비어있음
            when(client.fetchAuctionHistory(ItemCategory.SWORD, "")).thenReturn(Mono.just(page1));
            when(duplicateChecker.hasDuplicate(any())).thenReturn(false);

            // when
            var result = fetcher.fetch(ItemCategory.SWORD);

            // then
            assertThat(result).hasSize(1);
            verify(client, times(1)).fetchAuctionHistory(eq(ItemCategory.SWORD), any());
            verifyNoMoreInteractions(client);
        }

        @Test
        @DisplayName("중간 페이지의 auctionHistory가 비어있으면 수집을 중단한다")
        void stopWhenMiddlePageIsEmpty() {
            // given
            var page1 = new OpenApiAuctionHistoryListResponse(List.of(dummy("1")), "cursor-1");
            var emptyPage =
                    new OpenApiAuctionHistoryListResponse(List.of(), "cursor-2"); // 비어있는 페이지

            when(client.fetchAuctionHistory(ItemCategory.SWORD, "")).thenReturn(Mono.just(page1));
            when(client.fetchAuctionHistory(ItemCategory.SWORD, "cursor-1"))
                    .thenReturn(Mono.just(emptyPage));
            when(duplicateChecker.hasDuplicate(any())).thenReturn(false);

            // when
            var result = fetcher.fetch(ItemCategory.SWORD);

            // then
            assertThat(result).hasSize(1);
            verify(client, times(2)).fetchAuctionHistory(eq(ItemCategory.SWORD), any());
            verifyNoMoreInteractions(client);
        }

        @Test
        @DisplayName("응답 내 auctionHistory 리스트가 null이면 수집을 중단한다")
        void stopWhenAuctionHistoryListIsNull() {
            // given
            var pageWithNullList = new OpenApiAuctionHistoryListResponse(null, "cursor-1");
            when(client.fetchAuctionHistory(ItemCategory.SWORD, ""))
                    .thenReturn(Mono.just(pageWithNullList));

            // when
            var result = fetcher.fetch(ItemCategory.SWORD);

            // then
            assertThat(result).isEmpty();
            verify(client, times(1)).fetchAuctionHistory(eq(ItemCategory.SWORD), any());
            verifyNoMoreInteractions(client);
            verify(duplicateChecker, never()).hasDuplicate(any());
        }
    }
}
