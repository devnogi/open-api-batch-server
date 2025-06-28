package until.the.eternity.auctionhistory.domain.mapper;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import until.the.eternity.auctionhistory.domain.dto.internal.response.AuctionHistoryDetailResponse;
import until.the.eternity.auctionhistory.domain.dto.internal.response.ItemOptionResponse;
import until.the.eternity.auctionhistory.domain.entity.AuctionHistory;
import until.the.eternity.itemoption.domain.entity.ItemOption;

@Mapper(componentModel = "spring")
public interface AuctionHistoryMapper {

    // Entity → DTO
    @Mapping(target = "itemOptions", source = "itemOptions")
    AuctionHistoryDetailResponse<ItemOptionResponse> toDto(AuctionHistory entity);

    // 하위 매핑
    ItemOption toEntity(ItemOptionResponse dto);

    ItemOptionResponse toDto(ItemOption entity);

    List<ItemOption> toEntityList(List<ItemOptionResponse> dtoList);

    List<ItemOptionResponse> toDtoList(List<ItemOption> entityList);
}
