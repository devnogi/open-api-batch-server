package until.the.eternity.auction.domain.mapper;

import java.time.Instant;
import java.util.List;
import org.mapstruct.*;
import until.the.eternity.auction.domain.dto.external.OpenApiAuctionHistoryResponse;
import until.the.eternity.auction.domain.dto.external.OpenApiItemOptionResponse;
import until.the.eternity.auction.domain.model.AuctionHistory;
import until.the.eternity.auction.domain.model.ItemOption;
import until.the.eternity.common.enums.ItemCategory;

@Mapper(componentModel = "spring", uses = OpenApiItemOptionMapper.class)
public interface OpenApiAuctionHistoryMapper {

    @Mapping(target = "id", ignore = true) // PK 자동 생성
    @Mapping(
            source = "dateAuctionBuy",
            target = "dateAuctionBuy",
            qualifiedByName = "stringToInstant") // String → Instant
    @Mapping(source = "openApiItemOptionResponses", target = "itemOptions")
    @Mapping(
            target = "itemTopCategory",
            expression = "java(ItemCategory.findTopCategory(dto.itemSubCategory()))")
    AuctionHistory toEntity(OpenApiAuctionHistoryResponse dto, @Context ItemCategory itemCategory);

    List<ItemOption> toItemOptionList(List<OpenApiItemOptionResponse> options);

    @Named("stringToInstant")
    default Instant stringToInstant(String value) {
        return Instant.parse(value); // ISO‑8601 포맷 전제
    }

    @AfterMapping
    default void linkOptions(@MappingTarget AuctionHistory history) {
        if (history.getItemOptions() != null) {
            history.getItemOptions().forEach(opt -> opt.setAuctionHistory(history));
        }
    }
}
