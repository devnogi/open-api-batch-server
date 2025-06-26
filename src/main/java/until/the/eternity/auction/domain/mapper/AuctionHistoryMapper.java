package until.the.eternity.auction.domain.mapper;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import until.the.eternity.auction.domain.dto.internal.response.AuctionHistoryDetailResponse;
import until.the.eternity.auction.domain.dto.internal.response.ItemOptionResponse;
import until.the.eternity.auction.domain.model.AuctionHistory;
import until.the.eternity.auction.domain.model.ItemOption;

@Mapper(componentModel = "spring")
public interface AuctionHistoryMapper {

    // DTO → Entity
    @Mapping(target = "itemOptions", source = "itemOptions")
    AuctionHistory toEntity(AuctionHistoryDetailResponse<ItemOptionResponse> dto);

    // Entity → DTO
    @Mapping(target = "itemOptions", source = "itemOptions")
    AuctionHistoryDetailResponse<ItemOptionResponse> toDto(AuctionHistory entity);

    // 하위 매핑
    ItemOption toEntity(ItemOptionResponse dto);

    ItemOptionResponse toDto(ItemOption entity);

    List<ItemOption> toEntityList(List<ItemOptionResponse> dtoList);

    List<ItemOptionResponse> toDtoList(List<ItemOption> entityList);
}
