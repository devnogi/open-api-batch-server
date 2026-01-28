package until.the.eternity.auctionrealtime.application.service.fetcher;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import until.the.eternity.auctionrealtime.domain.service.AuctionRealtimeDuplicateChecker;
import until.the.eternity.auctionrealtime.domain.service.AuctionRealtimeDuplicateChecker.DuplicateCheckResult;
import until.the.eternity.auctionrealtime.domain.service.fetcher.AuctionRealtimeFetcherPort.FetchResult;
import until.the.eternity.auctionrealtime.infrastructure.client.AuctionRealtimeClient;
import until.the.eternity.auctionrealtime.interfaces.external.dto.OpenApiAuctionRealtimeListResponse;
import until.the.eternity.auctionrealtime.interfaces.external.dto.OpenApiAuctionRealtimeResponse;
import until.the.eternity.common.enums.ItemCategory;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuctionRealtimeFetcherTest {

    @Mock AuctionRealtimeClient client;

    @Mock AuctionRealtimeDuplicateChecker duplicateChecker;

    @InjectMocks AuctionRealtimeFetcher fetcher;

    private OpenApiAuctionRealtimeResponse dummy() {
        return new OpenApiAuctionRealtimeResponse(
                "페러시우스 타이탄 블레이드", "신성한 페러시우스 타이탄 블레이드", 1L, 100L, Instant.now(), null);
    }

    @Nested
    @DisplayName("OPEN API 끝까지 호출 시나리오")
    class NormalFlow {

        @Test
        @DisplayName("모든 페이지를 수집하고 cursor가 null이면 종료한다")
        void fetchAllPages() {
            // given
            var page1 =
                    new OpenApiAuctionRealtimeListResponse(List.of(dummy(), dummy()), "cursor-1");
            var page2 = new OpenApiAuctionRealtimeListResponse(List.of(dummy()), null);

            when(client.fetchAuctionList(ItemCategory.SWORD, "")).thenReturn(Mono.just(page1));
            when(client.fetchAuctionList(ItemCategory.SWORD, "cursor-1"))
                    .thenReturn(Mono.just(page2));
            when(duplicateChecker.checkDuplicateInBatch(any(), eq(ItemCategory.SWORD)))
                    .thenReturn(DuplicateCheckResult.noDuplicate());

            // when
            FetchResult result = fetcher.fetch(ItemCategory.SWORD);

            // then
            assertThat(result.items()).hasSize(3);
            assertThat(result.hasEqualDate()).isFalse();

            verify(client, times(2)).fetchAuctionList(eq(ItemCategory.SWORD), any());
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
                    new OpenApiAuctionRealtimeListResponse(List.of(dummy(), dummy()), "cursor-1");
            Instant latestDate = Instant.parse("2024-01-01T00:00:00Z");

            when(client.fetchAuctionList(ItemCategory.SWORD, "")).thenReturn(Mono.just(page1));
            when(duplicateChecker.checkDuplicateInBatch(page1.auctionItems(), ItemCategory.SWORD))
                    .thenReturn(DuplicateCheckResult.duplicateFound(0, latestDate));

            // when
            FetchResult result = fetcher.fetch(ItemCategory.SWORD);

            // then
            assertThat(result.items()).isEmpty();
            assertThat(result.latestDate()).isEqualTo(latestDate);
            verify(client, times(1)).fetchAuctionList(ItemCategory.SWORD, "");
            verifyNoMoreInteractions(client);
        }

        @Test
        @DisplayName("첫 배치 중간에서 중복이면 중복 전까지만 반환한다")
        void stopOnDuplicateAtMiddle() {
            // given
            var batch = List.of(dummy(), dummy(), dummy());
            var page1 = new OpenApiAuctionRealtimeListResponse(batch, "cursor-1");
            Instant latestDate = Instant.parse("2024-01-01T00:00:00Z");

            when(client.fetchAuctionList(ItemCategory.SWORD, "")).thenReturn(Mono.just(page1));
            when(duplicateChecker.checkDuplicateInBatch(batch, ItemCategory.SWORD))
                    .thenReturn(DuplicateCheckResult.duplicateFound(2, latestDate));

            // when
            FetchResult result = fetcher.fetch(ItemCategory.SWORD);

            // then
            assertThat(result.items()).hasSize(2);
            verify(client, times(1)).fetchAuctionList(ItemCategory.SWORD, "");
            verifyNoMoreInteractions(client);
        }

        @Test
        @DisplayName("동일 날짜 데이터가 감지되면 hasEqualDate가 true로 반환된다")
        void stopOnEqualDate() {
            // given
            var batch = List.of(dummy(), dummy());
            var page1 = new OpenApiAuctionRealtimeListResponse(batch, "cursor-1");
            Instant latestDate = Instant.parse("2024-01-01T00:00:00Z");

            when(client.fetchAuctionList(ItemCategory.SWORD, "")).thenReturn(Mono.just(page1));
            when(duplicateChecker.checkDuplicateInBatch(batch, ItemCategory.SWORD))
                    .thenReturn(DuplicateCheckResult.equalDateFound(1, latestDate));

            // when
            FetchResult result = fetcher.fetch(ItemCategory.SWORD);

            // then
            assertThat(result.items()).hasSize(1);
            assertThat(result.hasEqualDate()).isTrue();
            assertThat(result.latestDate()).isEqualTo(latestDate);
        }

        @Test
        @DisplayName("두 번째 배치에서 중복이면 첫 배치 전체 + 중복 전까지만 반환한다")
        void stopOnDuplicateAtSecondBatch() {
            // given
            var batch1 = List.of(dummy(), dummy());
            var batch2 = List.of(dummy(), dummy(), dummy());
            var page1 = new OpenApiAuctionRealtimeListResponse(batch1, "cursor-1");
            var page2 = new OpenApiAuctionRealtimeListResponse(batch2, "cursor-2");
            Instant latestDate = Instant.parse("2024-01-01T00:00:00Z");

            when(client.fetchAuctionList(ItemCategory.SWORD, "")).thenReturn(Mono.just(page1));
            when(client.fetchAuctionList(ItemCategory.SWORD, "cursor-1"))
                    .thenReturn(Mono.just(page2));
            when(duplicateChecker.checkDuplicateInBatch(batch1, ItemCategory.SWORD))
                    .thenReturn(DuplicateCheckResult.noDuplicate());
            when(duplicateChecker.checkDuplicateInBatch(batch2, ItemCategory.SWORD))
                    .thenReturn(DuplicateCheckResult.duplicateFound(1, latestDate));

            // when
            FetchResult result = fetcher.fetch(ItemCategory.SWORD);

            // then
            assertThat(result.items()).hasSize(3); // 2 from batch1 + 1 from batch2
            verify(client, times(2)).fetchAuctionList(eq(ItemCategory.SWORD), any());
        }

        @Test
        @DisplayName("첫 응답이 null(Mono.empty)이면 빈 리스트를 반환한다")
        void responseNull() {
            when(client.fetchAuctionList(ItemCategory.SWORD, "")).thenReturn(Mono.empty());

            FetchResult result = fetcher.fetch(ItemCategory.SWORD);

            assertThat(result.items()).isEmpty();
            verify(duplicateChecker, never()).checkDuplicateInBatch(any(), any());
        }

        @Test
        @DisplayName("auctionItems()가 비어있으면 빈 리스트를 반환한다")
        void auctionItemsEmpty() {
            var page = new OpenApiAuctionRealtimeListResponse(List.of(), "ignored");

            when(client.fetchAuctionList(ItemCategory.SWORD, "")).thenReturn(Mono.just(page));

            FetchResult result = fetcher.fetch(ItemCategory.SWORD);

            assertThat(result.items()).isEmpty();
            verify(duplicateChecker, never()).checkDuplicateInBatch(any(), any());
        }

        @Test
        @DisplayName("nextCursor가 빈 문자열이면 수집을 중단한다")
        void stopWhenNextCursorIsEmptyString() {
            // given
            var page1 = new OpenApiAuctionRealtimeListResponse(List.of(dummy()), "");
            when(client.fetchAuctionList(ItemCategory.SWORD, "")).thenReturn(Mono.just(page1));
            when(duplicateChecker.checkDuplicateInBatch(any(), eq(ItemCategory.SWORD)))
                    .thenReturn(DuplicateCheckResult.noDuplicate());

            // when
            FetchResult result = fetcher.fetch(ItemCategory.SWORD);

            // then
            assertThat(result.items()).hasSize(1);
            verify(client, times(1)).fetchAuctionList(eq(ItemCategory.SWORD), any());
            verifyNoMoreInteractions(client);
        }

        @Test
        @DisplayName("중간 페이지의 auctionItems가 비어있으면 수집을 중단한다")
        void stopWhenMiddlePageIsEmpty() {
            // given
            var page1 = new OpenApiAuctionRealtimeListResponse(List.of(dummy()), "cursor-1");
            var emptyPage = new OpenApiAuctionRealtimeListResponse(List.of(), "cursor-2");

            when(client.fetchAuctionList(ItemCategory.SWORD, "")).thenReturn(Mono.just(page1));
            when(client.fetchAuctionList(ItemCategory.SWORD, "cursor-1"))
                    .thenReturn(Mono.just(emptyPage));
            when(duplicateChecker.checkDuplicateInBatch(any(), eq(ItemCategory.SWORD)))
                    .thenReturn(DuplicateCheckResult.noDuplicate());

            // when
            FetchResult result = fetcher.fetch(ItemCategory.SWORD);

            // then
            assertThat(result.items()).hasSize(1);
            verify(client, times(2)).fetchAuctionList(eq(ItemCategory.SWORD), any());
            verifyNoMoreInteractions(client);
        }

        @Test
        @DisplayName("응답 내 auctionItems 리스트가 null이면 수집을 중단한다")
        void stopWhenAuctionItemsListIsNull() {
            // given
            var pageWithNullList = new OpenApiAuctionRealtimeListResponse(null, "cursor-1");
            when(client.fetchAuctionList(ItemCategory.SWORD, ""))
                    .thenReturn(Mono.just(pageWithNullList));

            // when
            FetchResult result = fetcher.fetch(ItemCategory.SWORD);

            // then
            assertThat(result.items()).isEmpty();
            verify(client, times(1)).fetchAuctionList(eq(ItemCategory.SWORD), any());
            verifyNoMoreInteractions(client);
            verify(duplicateChecker, never()).checkDuplicateInBatch(any(), any());
        }
    }
}
