package until.the.eternity.auctionrealtime.domain.service.fetcher;

import java.util.List;
import until.the.eternity.auctionrealtime.interfaces.external.dto.OpenApiAuctionRealtimeResponse;
import until.the.eternity.common.enums.ItemCategory;

/** 실시간 경매장 데이터 Fetcher Port. */
public interface AuctionRealtimeFetcherPort {

    /**
     * 페칭 결과.
     *
     * @param items 수집된 아이템 리스트
     */
    record FetchResult(List<OpenApiAuctionRealtimeResponse> items) {

        public static FetchResult empty() {
            return new FetchResult(List.of());
        }
    }

    /**
     * 해당 카테고리의 실시간 경매장 데이터를 전체 수집한다.
     *
     * @param category 아이템 카테고리
     * @return 페칭 결과
     */
    FetchResult fetch(ItemCategory category);
}
