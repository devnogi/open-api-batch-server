package until.the.eternity.auctionrealtime.domain.service.persister;

import java.util.List;
import until.the.eternity.auctionitem.domain.entity.AuctionRealtimeItem;
import until.the.eternity.auctionrealtime.interfaces.external.dto.OpenApiAuctionRealtimeResponse;
import until.the.eternity.common.enums.ItemCategory;

/** 실시간 경매장 데이터 Persister Port. */
public interface AuctionRealtimePersisterPort {

    /**
     * API 응답 데이터를 Entity로 변환한다.
     *
     * @param dtoList API 응답 DTO 리스트
     * @param category 아이템 카테고리
     * @return 저장할 Entity 리스트
     */
    List<AuctionRealtimeItem> prepareEntities(
            List<OpenApiAuctionRealtimeResponse> dtoList, ItemCategory category);
}
