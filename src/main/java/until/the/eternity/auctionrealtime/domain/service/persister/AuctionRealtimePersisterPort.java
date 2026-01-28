package until.the.eternity.auctionrealtime.domain.service.persister;

import until.the.eternity.auctionitem.domain.entity.AuctionRealtimeItem;
import until.the.eternity.auctionrealtime.interfaces.external.dto.OpenApiAuctionRealtimeResponse;
import until.the.eternity.common.enums.ItemCategory;

import java.time.Instant;
import java.util.List;

/** 실시간 경매장 데이터 Persister Port. */
public interface AuctionRealtimePersisterPort {

    /**
     * API 응답 데이터를 Entity로 변환하고 필터링한다.
     *
     * @param dtoList API 응답 DTO 리스트
     * @param category 아이템 카테고리
     * @param latestDate DB의 최신 date_auction_expire (null이면 모두 저장)
     * @return 저장할 Entity 리스트
     */
    List<AuctionRealtimeItem> prepareEntities(
            List<OpenApiAuctionRealtimeResponse> dtoList,
            ItemCategory category,
            Instant latestDate);
}
