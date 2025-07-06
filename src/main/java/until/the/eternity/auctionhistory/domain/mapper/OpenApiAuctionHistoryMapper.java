package until.the.eternity.auctionhistory.domain.mapper;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import until.the.eternity.auctionhistory.domain.entity.AuctionHistory;
import until.the.eternity.auctionhistory.interfaces.external.dto.OpenApiAuctionHistoryResponse;
import until.the.eternity.common.enums.ItemCategory;

import java.time.Instant;

@Mapper(componentModel = "spring", uses = OpenApiItemOptionMapper.class)
public interface OpenApiAuctionHistoryMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(
            source = "dateAuctionBuy",
            target = "dateAuctionBuy",
            qualifiedByName = "stringToInstant")
    @Mapping(source = "openApiItemOptionResponses", target = "itemOptions")
    @Mapping(
            target = "itemTopCategory",
            expression = "java(ItemCategory.findTopCategory(dto.itemSubCategory()))")
    AuctionHistory toEntity(OpenApiAuctionHistoryResponse dto, @Context ItemCategory itemCategory);

    @Named("stringToInstant")
    default Instant stringToInstant(String value) {
        return Instant.parse(value);
    }
}
