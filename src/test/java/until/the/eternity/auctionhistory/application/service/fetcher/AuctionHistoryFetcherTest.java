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
import java.util.OptionalInt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuctionHistoryFetcherTest {

    @Mock AuctionHistoryClient client;

    @Mock AuctionHistoryDuplicateChecker duplicateChecker;

    @InjectMocks AuctionHistoryFetcher fetcher;

    private OpenApiAuctionHistoryResponse dummy(String id) {
        return new OpenApiAuctionHistoryResponse(
                "페러시우스 타이탄 블레이드",
                "신성한 페러시우스 타이탄 블레이드",
                ItemCategory.SWORD.getSubCategory(),
                1L,
                100L,
                Instant.now(),
                id,
                null);
    }

    @Nested
    @DisplayName("OPEN API 끝까지 호출 시나리오")
    class NormalFlow {

        @Test
        @DisplayName("모든 페이지를 수집하고 cursor가 null이면 종료한다")
        void fetchAllPages() {
            // given
            var page1 =
                    new OpenApiAuctionHistoryListResponse(
                            List.of(dummy("1"), dummy("2")), "cursor-1");
            var page2 = new OpenApiAuctionHistoryListResponse(List.of(dummy("3")), null);

            when(client.fetchAuctionHistory(ItemCategory.SWORD, "")).thenReturn(Mono.just(page1));
            when(client.fetchAuctionHistory(ItemCategory.SWORD, "cursor-1"))
                    .thenReturn(Mono.just(page2));
            when(duplicateChecker.checkDuplicateInBatch(any(), eq(ItemCategory.SWORD)))
                    .thenReturn(OptionalInt.empty());

            // when
            var result = fetcher.fetch(ItemCategory.SWORD);

            // then
            assertThat(result)
                    .hasSize(3)
                    .extracting(OpenApiAuctionHistoryResponse::auctionBuyId)
                    .containsExactly("1", "2", "3");

            verify(client, times(2)).fetchAuctionHistory(eq(ItemCategory.SWORD), any());
            verify(duplicateChecker, times(2)).checkDuplicateInBatch(any(), eq(ItemCategory.SWORD));
        }
    }

    @Nested
    @DisplayName("OPEN API 호출 중단 시나리오")
    class EarlyBreakFlow {

        @Test
        @DisplayName("첫 배치 첫 항목에서 중복이면 빈 리스트를 반환한다")
        void stopOnDuplicateAtFirstItem() {
            // given
            var page1 =
                    new OpenApiAuctionHistoryListResponse(
                            List.of(dummy("1"), dummy("2")), "cursor-1");

            when(client.fetchAuctionHistory(ItemCategory.SWORD, "")).thenReturn(Mono.just(page1));
            when(duplicateChecker.checkDuplicateInBatch(page1.auctionHistory(), ItemCategory.SWORD))
                    .thenReturn(OptionalInt.of(0));

            // when
            var result = fetcher.fetch(ItemCategory.SWORD);

            // then
            assertThat(result).isEmpty();
            verify(client, times(1)).fetchAuctionHistory(ItemCategory.SWORD, "");
            verifyNoMoreInteractions(client);
        }

        @Test
        @DisplayName("첫 배치 중간에서 중복이면 중복 전까지만 반환한다")
        void stopOnDuplicateAtMiddle() {
            // given
            var batch = List.of(dummy("1"), dummy("2"), dummy("3"));
            var page1 = new OpenApiAuctionHistoryListResponse(batch, "cursor-1");

            when(client.fetchAuctionHistory(ItemCategory.SWORD, "")).thenReturn(Mono.just(page1));
            when(duplicateChecker.checkDuplicateInBatch(batch, ItemCategory.SWORD))
                    .thenReturn(OptionalInt.of(2));

            // when
            var result = fetcher.fetch(ItemCategory.SWORD);

            // then
            assertThat(result)
                    .hasSize(2)
                    .extracting(OpenApiAuctionHistoryResponse::auctionBuyId)
                    .containsExactly("1", "2");
            verify(client, times(1)).fetchAuctionHistory(ItemCategory.SWORD, "");
            verifyNoMoreInteractions(client);
        }

        @Test
        @DisplayName("두 번째 배치에서 중복이면 첫 배치 전체 + 중복 전까지만 반환한다")
        void stopOnDuplicateAtSecondBatch() {
            // given
            var batch1 = List.of(dummy("1"), dummy("2"));
            var batch2 = List.of(dummy("3"), dummy("4"), dummy("5"));
            var page1 = new OpenApiAuctionHistoryListResponse(batch1, "cursor-1");
            var page2 = new OpenApiAuctionHistoryListResponse(batch2, "cursor-2");

            when(client.fetchAuctionHistory(ItemCategory.SWORD, "")).thenReturn(Mono.just(page1));
            when(client.fetchAuctionHistory(ItemCategory.SWORD, "cursor-1"))
                    .thenReturn(Mono.just(page2));
            when(duplicateChecker.checkDuplicateInBatch(batch1, ItemCategory.SWORD))
                    .thenReturn(OptionalInt.empty());
            when(duplicateChecker.checkDuplicateInBatch(batch2, ItemCategory.SWORD))
                    .thenReturn(OptionalInt.of(1));

            // when
            var result = fetcher.fetch(ItemCategory.SWORD);

            // then
            assertThat(result)
                    .hasSize(3)
                    .extracting(OpenApiAuctionHistoryResponse::auctionBuyId)
                    .containsExactly("1", "2", "3");
            verify(client, times(2)).fetchAuctionHistory(eq(ItemCategory.SWORD), any());
        }

        @Test
        @DisplayName("첫 응답이 null(Mono.empty)이면 빈 리스트를 반환한다")
        void responseNull() {
            when(client.fetchAuctionHistory(ItemCategory.SWORD, "")).thenReturn(Mono.empty());

            var result = fetcher.fetch(ItemCategory.SWORD);

            assertThat(result).isEmpty();
            verify(duplicateChecker, never()).checkDuplicateInBatch(any(), any());
        }

        @Test
        @DisplayName("auctionHistory()가 비어있으면 빈 리스트를 반환한다")
        void auctionHistoryEmpty() {
            var page = new OpenApiAuctionHistoryListResponse(List.of(), "ignored");

            when(client.fetchAuctionHistory(ItemCategory.SWORD, "")).thenReturn(Mono.just(page));

            var result = fetcher.fetch(ItemCategory.SWORD);

            assertThat(result).isEmpty();
            verify(duplicateChecker, never()).checkDuplicateInBatch(any(), any());
        }

        @Test
        @DisplayName("nextCursor가 빈 문자열이면 수집을 중단한다")
        void stopWhenNextCursorIsEmptyString() {
            // given
            var page1 = new OpenApiAuctionHistoryListResponse(List.of(dummy("1")), "");
            when(client.fetchAuctionHistory(ItemCategory.SWORD, "")).thenReturn(Mono.just(page1));
            when(duplicateChecker.checkDuplicateInBatch(any(), eq(ItemCategory.SWORD)))
                    .thenReturn(OptionalInt.empty());

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
            var emptyPage = new OpenApiAuctionHistoryListResponse(List.of(), "cursor-2");

            when(client.fetchAuctionHistory(ItemCategory.SWORD, "")).thenReturn(Mono.just(page1));
            when(client.fetchAuctionHistory(ItemCategory.SWORD, "cursor-1"))
                    .thenReturn(Mono.just(emptyPage));
            when(duplicateChecker.checkDuplicateInBatch(any(), eq(ItemCategory.SWORD)))
                    .thenReturn(OptionalInt.empty());

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
            verify(duplicateChecker, never()).checkDuplicateInBatch(any(), any());
        }
    }
}
