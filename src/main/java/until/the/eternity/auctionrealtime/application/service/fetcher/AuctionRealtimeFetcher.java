package until.the.eternity.auctionrealtime.application.service.fetcher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import until.the.eternity.auctionrealtime.domain.service.fetcher.AuctionRealtimeFetcherPort;
import until.the.eternity.auctionrealtime.infrastructure.client.AuctionRealtimeClient;
import until.the.eternity.auctionrealtime.interfaces.external.dto.OpenApiAuctionRealtimeListResponse;
import until.the.eternity.auctionrealtime.interfaces.external.dto.OpenApiAuctionRealtimeResponse;
import until.the.eternity.common.enums.ItemCategory;

import java.util.ArrayList;
import java.util.List;

/**
 * 실시간 경매장 데이터 Fetcher 구현체.
 *
 * <p>Cursor 기반 페이징으로 API를 호출하여 해당 카테고리의 전체 데이터를 수집한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuctionRealtimeFetcher implements AuctionRealtimeFetcherPort {

    private final AuctionRealtimeClient client;

    @Override
    public FetchResult fetch(ItemCategory category) {
        List<OpenApiAuctionRealtimeResponse> result = new ArrayList<>();
        String cursor = "";

        while (true) {
            OpenApiAuctionRealtimeListResponse response =
                    client.fetchAuctionList(category, cursor).block();

            if (response == null || response.auctionItems() == null) {
                log.warn(
                        "[REALTIME] [{}] response or its items is null, something is wrong with open api call",
                        category.getSubCategory());
                break;
            }

            log.debug(
                    "[REALTIME] [{}] fetched '{}' data",
                    category.getSubCategory(),
                    response.auctionItems().size());

            if (response.auctionItems().isEmpty()) {
                log.debug("[REALTIME] [{}] fetched no data", category.getSubCategory());
                break;
            }

            result.addAll(response.auctionItems());

            cursor = response.nextCursor();

            if (cursor == null || cursor.isEmpty()) {
                log.debug(
                        "[REALTIME] [{}] response cursor is null, fetched end",
                        category.getSubCategory());
                break;
            }
        }

        return new FetchResult(result);
    }
}
