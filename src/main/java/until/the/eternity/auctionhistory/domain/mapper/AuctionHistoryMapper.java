package until.the.eternity.auctionhistory.domain.mapper;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import until.the.eternity.auctionhistory.domain.entity.AuctionHistory;
import until.the.eternity.auctionhistory.interfaces.rest.dto.response.AuctionHistoryDetailResponse;
import until.the.eternity.auctionhistory.interfaces.rest.dto.response.ItemOptionResponse;
import until.the.eternity.auctionitemoption.domain.entity.AuctionHistoryItemOption;

/**
 * AuctionHistory Entity to internal.responseDto transfer mapper class 데이터 흐름은 external.responseDto
 * -> entity -> internal.responseDto 단방향으로 흐름
 */
@Mapper(componentModel = "spring")
public interface AuctionHistoryMapper {

    // Entity → DTO
    @Mapping(target = "itemOptions", source = "auctionHistoryItemOptions")
    AuctionHistoryDetailResponse<ItemOptionResponse> toDto(AuctionHistory entity);

    // 하위 매핑
    @Mapping(target = "auctionHistory", ignore = true)
    AuctionHistoryItemOption toEntity(ItemOptionResponse dto);

    ItemOptionResponse toDto(AuctionHistoryItemOption entity);

    List<AuctionHistoryItemOption> toEntityList(List<ItemOptionResponse> dtoList);

    List<ItemOptionResponse> toDtoList(List<AuctionHistoryItemOption> entityList);
}
