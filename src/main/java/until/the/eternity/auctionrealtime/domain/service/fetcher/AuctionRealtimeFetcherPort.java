package until.the.eternity.auctionrealtime.domain.service.fetcher;

import java.time.Instant;
import java.util.List;
import until.the.eternity.auctionrealtime.interfaces.external.dto.OpenApiAuctionRealtimeResponse;
import until.the.eternity.common.enums.ItemCategory;

/** 실시간 경매장 데이터 Fetcher Port. */
public interface AuctionRealtimeFetcherPort {

    /**
     * 페칭 결과.
     *
     * @param items 수집된 아이템 리스트
     * @param hasEqualDate 동일 날짜 데이터 존재 여부 (삭제 후 재저장 필요)
     * @param latestDate DB의 최신 date_auction_expire
     */
    record FetchResult(
            List<OpenApiAuctionRealtimeResponse> items, boolean hasEqualDate, Instant latestDate) {

        public static FetchResult empty() {
            return new FetchResult(List.of(), false, null);
        }
    }

    /**
     * 해당 카테고리의 실시간 경매장 데이터를 수집한다.
     *
     * @param category 아이템 카테고리
     * @return 페칭 결과
     */
    FetchResult fetch(ItemCategory category);
}
