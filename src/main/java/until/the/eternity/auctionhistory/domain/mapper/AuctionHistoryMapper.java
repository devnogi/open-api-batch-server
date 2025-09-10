package until.the.eternity.auctionhistory.domain.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import until.the.eternity.auctionhistory.domain.entity.AuctionHistory;
import until.the.eternity.auctionhistory.interfaces.rest.dto.response.AuctionHistoryDetailResponse;
import until.the.eternity.auctionhistory.interfaces.rest.dto.response.ItemOptionResponse;
import until.the.eternity.itemoption.domain.entity.ItemOption;

import java.util.List;

/**
 * AuctionHistory Entity to internal.responseDto transfer mapper class 데이터 흐름은 external.responseDto
 * -> entity -> internal.responseDto 단방향으로 흐름
 */
@Mapper(componentModel = "spring")
public interface AuctionHistoryMapper {

    // Entity → DTO
    @Mapping(target = "itemOptions", source = "itemOptions")
    AuctionHistoryDetailResponse<ItemOptionResponse> toDto(AuctionHistory entity);

    // 하위 매핑
    @Mapping(target = "auctionHistory", ignore = true)
    @Mapping(target = "auctionItem", ignore = true)
    ItemOption toEntity(ItemOptionResponse dto);

    ItemOptionResponse toDto(ItemOption entity);

    List<ItemOption> toEntityList(List<ItemOptionResponse> dtoList);

    List<ItemOptionResponse> toDtoList(List<ItemOption> entityList);
}
