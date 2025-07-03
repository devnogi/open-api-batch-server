package until.the.eternity.auctionhistory.domain.mapper;

import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.stereotype.Component;
import until.the.eternity.auctionhistory.domain.dto.external.OpenApiAuctionHistoryResponse;
import until.the.eternity.auctionhistory.domain.dto.internal.response.AuctionHistoryDetailResponse;
import until.the.eternity.auctionhistory.domain.dto.internal.response.ItemOptionResponse;
import until.the.eternity.common.enums.ItemCategory;

@Component
public class OpenApiAuctionHistoryAssembler {

    public AuctionHistoryDetailResponse<ItemOptionResponse> toDetailResponse(
            OpenApiAuctionHistoryResponse dto, ItemCategory category) {
        List<ItemOptionResponse> itemOptions =
                dto.openApiItemOptionResponses() == null
                        ? List.of()
                        : dto.openApiItemOptionResponses().stream()
                                .map(
                                        opt ->
                                                new ItemOptionResponse(
                                                        null,
                                                        opt.optionType(),
                                                        opt.optionSubType(),
                                                        opt.optionValue(),
                                                        opt.optionValue2(),
                                                        opt.optionDesc()))
                                .toList();

        return new AuctionHistoryDetailResponse<>(
                null,
                dto.itemName(),
                dto.itemDisplayName(),
                dto.itemCount(),
                dto.auctionPricePerUnit(),
                OffsetDateTime.parse(dto.dateAuctionBuy()).toInstant(),
                dto.auctionBuyId(),
                category.getSubCategory(),
                category.getTopCategory(),
                itemOptions);
    }
}
