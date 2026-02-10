package until.the.eternity.auctionrealtime.application.service.persister;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import until.the.eternity.auctionitem.domain.entity.AuctionRealtimeItem;
import until.the.eternity.auctionrealtime.domain.mapper.OpenApiAuctionRealtimeMapper;
import until.the.eternity.auctionrealtime.domain.service.persister.AuctionRealtimePersisterPort;
import until.the.eternity.auctionrealtime.interfaces.external.dto.OpenApiAuctionRealtimeResponse;
import until.the.eternity.common.enums.ItemCategory;

/** 실시간 경매장 데이터 Persister 구현체. */
@Slf4j
@RequiredArgsConstructor
@Component
public class AuctionRealtimePersister implements AuctionRealtimePersisterPort {

    private final OpenApiAuctionRealtimeMapper mapper;

    @Override
    public List<AuctionRealtimeItem> prepareEntities(
            List<OpenApiAuctionRealtimeResponse> dtoList, ItemCategory category) {

        // Entity 변환
        List<AuctionRealtimeItem> entities = mapper.toEntityList(dtoList, category);

        // 아이템 옵션 링크 설정
        entities.forEach(AuctionRealtimeItem::linkItemOptions);

        if (entities.isEmpty()) {
            log.info(
                    "[REALTIME] [{}] No new auction realtime items to save",
                    category.getSubCategory());
        } else {
            log.info(
                    "[REALTIME] [{}] Prepared {} auction realtime items for save",
                    category.getSubCategory(),
                    entities.size());
        }

        return entities;
    }
}
