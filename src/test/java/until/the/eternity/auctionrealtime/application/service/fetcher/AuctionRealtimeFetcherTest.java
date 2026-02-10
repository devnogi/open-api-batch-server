package until.the.eternity.auctionrealtime.application.service.fetcher;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
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

    @InjectMocks AuctionRealtimeFetcher fetcher;

    private OpenApiAuctionRealtimeResponse dummy() {
        return new OpenApiAuctionRealtimeResponse(
                "페러시우스 타이탄 블레이드", "신성한 페러시우스 타이탄 블레이드", 1L, 100L, Instant.now(), null);
    }

    @Nested
    @DisplayName("전체 페이지 수집 시나리오")
    class FullFetchFlow {

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

            // when
            FetchResult result = fetcher.fetch(ItemCategory.SWORD);

            // then
            assertThat(result.items()).hasSize(3);
            verify(client, times(2)).fetchAuctionList(eq(ItemCategory.SWORD), any());
        }

        @Test
        @DisplayName("3페이지 이상도 끝까지 수집한다")
        void fetchMultiplePages() {
            // given
            var page1 =
                    new OpenApiAuctionRealtimeListResponse(List.of(dummy(), dummy()), "cursor-1");
            var page2 = new OpenApiAuctionRealtimeListResponse(List.of(dummy()), "cursor-2");
            var page3 = new OpenApiAuctionRealtimeListResponse(List.of(dummy(), dummy()), null);

            when(client.fetchAuctionList(ItemCategory.SWORD, "")).thenReturn(Mono.just(page1));
            when(client.fetchAuctionList(ItemCategory.SWORD, "cursor-1"))
                    .thenReturn(Mono.just(page2));
            when(client.fetchAuctionList(ItemCategory.SWORD, "cursor-2"))
                    .thenReturn(Mono.just(page3));

            // when
            FetchResult result = fetcher.fetch(ItemCategory.SWORD);

            // then
            assertThat(result.items()).hasSize(5);
            verify(client, times(3)).fetchAuctionList(eq(ItemCategory.SWORD), any());
        }
    }

    @Nested
    @DisplayName("수집 중단 시나리오")
    class EarlyBreakFlow {

        @Test
        @DisplayName("첫 응답이 null(Mono.empty)이면 빈 리스트를 반환한다")
        void responseNull() {
            when(client.fetchAuctionList(ItemCategory.SWORD, "")).thenReturn(Mono.empty());

            FetchResult result = fetcher.fetch(ItemCategory.SWORD);

            assertThat(result.items()).isEmpty();
        }

        @Test
        @DisplayName("auctionItems()가 비어있으면 빈 리스트를 반환한다")
        void auctionItemsEmpty() {
            var page = new OpenApiAuctionRealtimeListResponse(List.of(), "ignored");

            when(client.fetchAuctionList(ItemCategory.SWORD, "")).thenReturn(Mono.just(page));

            FetchResult result = fetcher.fetch(ItemCategory.SWORD);

            assertThat(result.items()).isEmpty();
        }

        @Test
        @DisplayName("nextCursor가 빈 문자열이면 수집을 중단한다")
        void stopWhenNextCursorIsEmptyString() {
            // given
            var page1 = new OpenApiAuctionRealtimeListResponse(List.of(dummy()), "");
            when(client.fetchAuctionList(ItemCategory.SWORD, "")).thenReturn(Mono.just(page1));

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
        }
    }
}
