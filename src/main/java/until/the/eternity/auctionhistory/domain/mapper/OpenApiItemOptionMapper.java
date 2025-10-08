package until.the.eternity.auctionhistory.domain.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import until.the.eternity.auctionitemoption.domain.dto.external.OpenApiAuctionItemOptionResponse;
import until.the.eternity.auctionitemoption.domain.entity.AuctionItemOption;

@Mapper(componentModel = "spring")
public interface OpenApiItemOptionMapper {

    @Mapping(target = "id", ignore = true) // PK 자동 생성
    @Mapping(target = "auctionHistory", ignore = true)
    @Mapping(target = "auctionItem", ignore = true)
    AuctionItemOption toEntity(OpenApiAuctionItemOptionResponse itemOption);
}
