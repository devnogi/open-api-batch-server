package until.the.eternity.auctionrealtime.application.service.fetcher;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import until.the.eternity.auctionrealtime.domain.service.AuctionRealtimeDuplicateChecker;
import until.the.eternity.auctionrealtime.domain.service.AuctionRealtimeDuplicateChecker.DuplicateCheckResult;
import until.the.eternity.auctionrealtime.domain.service.fetcher.AuctionRealtimeFetcherPort;
import until.the.eternity.auctionrealtime.infrastructure.client.AuctionRealtimeClient;
import until.the.eternity.auctionrealtime.interfaces.external.dto.OpenApiAuctionRealtimeListResponse;
import until.the.eternity.auctionrealtime.interfaces.external.dto.OpenApiAuctionRealtimeResponse;
import until.the.eternity.common.enums.ItemCategory;

/** 실시간 경매장 데이터 Fetcher 구현체. Cursor 기반 페이징으로 API를 호출하고, 중복 감지 시 호출을 중단한다. */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuctionRealtimeFetcher implements AuctionRealtimeFetcherPort {

    private final AuctionRealtimeClient client;
    private final AuctionRealtimeDuplicateChecker duplicateChecker;

    @Override
    public FetchResult fetch(ItemCategory category) {
        List<OpenApiAuctionRealtimeResponse> result = new ArrayList<>();
        String cursor = "";
        boolean hasEqualDate = false;
        Instant latestDate = null;

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

            List<OpenApiAuctionRealtimeResponse> batch = response.auctionItems();

            // 중복 체크
            DuplicateCheckResult checkResult =
                    duplicateChecker.checkDuplicateInBatch(batch, category);

            if (checkResult.isDuplicate()) {
                int index = checkResult.duplicateIndex();
                latestDate = checkResult.latestDate();
                hasEqualDate = checkResult.hasEqualDate();

                if (index > 0) {
                    result.addAll(batch.subList(0, index));
                }

                if (hasEqualDate) {
                    log.debug(
                            "[REALTIME] [{}] equal date found at index {}, need to delete and re-save, added {} items",
                            category.getSubCategory(),
                            index,
                            index);
                } else {
                    log.debug(
                            "[REALTIME] [{}] duplicate found at index {}, added {} items before duplicate",
                            category.getSubCategory(),
                            index,
                            index);
                }
                break;
            }

            latestDate = checkResult.latestDate();
            result.addAll(batch);

            cursor = response.nextCursor();

            if (cursor == null || cursor.isEmpty()) {
                log.debug(
                        "[REALTIME] [{}] response cursor is null, fetched end",
                        category.getSubCategory());
                break;
            }
        }

        return new FetchResult(result, hasEqualDate, latestDate);
    }
}
