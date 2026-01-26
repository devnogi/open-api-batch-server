package until.the.eternity.auctionrealtime.domain.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import until.the.eternity.auctionitem.domain.entity.AuctionRealtimeItemOption;
import until.the.eternity.auctionitemoption.domain.dto.external.OpenApiAuctionItemOptionResponse;

/** OpenApiAuctionItemOptionResponse → AuctionRealtimeItemOption Entity 변환 Mapper. */
@Mapper(componentModel = "spring")
public interface OpenApiRealtimeItemOptionMapper {

    @Mapping(target = "id", ignore = true) // PK 자동 생성
    @Mapping(target = "auctionRealtimeItem", ignore = true)
    AuctionRealtimeItemOption toEntity(OpenApiAuctionItemOptionResponse itemOption);
}
