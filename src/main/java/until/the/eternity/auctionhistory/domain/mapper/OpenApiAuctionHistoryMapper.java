package until.the.eternity.auctionhistory.domain.mapper;

import org.mapstruct.*;
import until.the.eternity.auctionhistory.domain.entity.AuctionHistory;
import until.the.eternity.auctionhistory.interfaces.external.dto.OpenApiAuctionHistoryResponse;
import until.the.eternity.common.enums.ItemCategory;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Mapper(componentModel = "spring", uses = OpenApiItemOptionMapper.class)
public interface OpenApiAuctionHistoryMapper {

    @Named("toEntity(OpenApiAuctionHistoryResponse, ItemCategory)")
    @Mapping(source = "dateAuctionBuy", target = "dateAuctionBuy", qualifiedByName = "utcToKst")
    @Mapping(source = "openApiAuctionItemOptionResponse", target = "auctionItemOptions")
    @Mapping(
            target = "itemTopCategory",
            expression = "java(ItemCategory.findTopCategory(dto.itemSubCategory()))")
    AuctionHistory toEntity(OpenApiAuctionHistoryResponse dto, @Context ItemCategory itemCategory);

    @IterableMapping(qualifiedByName = "toEntity(OpenApiAuctionHistoryResponse, ItemCategory)")
    List<AuctionHistory> toEntityList(
            List<OpenApiAuctionHistoryResponse> dtoList, @Context ItemCategory itemCategory);

    @Named("utcToKst")
    default Instant utcToKst(Instant utcTime) {
        // API에서 받은 UTC 시간에 9시간을 더하여 KST로 변환
        return utcTime != null ? utcTime.plus(9, ChronoUnit.HOURS) : null;
    }
}
