package until.the.eternity.auctionhistory.domain.mapper;

import java.time.Instant;
import java.util.List;
import org.mapstruct.*;
import until.the.eternity.auctionhistory.domain.entity.AuctionHistory;
import until.the.eternity.auctionhistory.interfaces.external.dto.OpenApiAuctionHistoryResponse;
import until.the.eternity.common.enums.ItemCategory;

@Mapper(componentModel = "spring", uses = OpenApiItemOptionMapper.class)
public interface OpenApiAuctionHistoryMapper {

    @Named("toEntity(OpenApiAuctionHistoryResponse, ItemCategory)")
    @Mapping(
            source = "dateAuctionBuy",
            target = "dateAuctionBuy",
            qualifiedByName = "stringToInstant")
    @Mapping(source = "openApiAuctionItemOptionResponse", target = "auctionItemOptions")
    @Mapping(
            target = "itemTopCategory",
            expression = "java(ItemCategory.findTopCategory(dto.itemSubCategory()))")
    AuctionHistory toEntity(OpenApiAuctionHistoryResponse dto, @Context ItemCategory itemCategory);

    @IterableMapping(qualifiedByName = "toEntity(OpenApiAuctionHistoryResponse, ItemCategory)")
    List<AuctionHistory> toEntityList(
            List<OpenApiAuctionHistoryResponse> dtoList, @Context ItemCategory itemCategory);

    @Named("stringToInstant")
    default Instant stringToInstant(String value) {
        return Instant.parse(value);
    }
}
