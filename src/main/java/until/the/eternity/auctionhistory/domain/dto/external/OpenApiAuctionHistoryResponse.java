package until.the.eternity.auctionhistory.domain.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import until.the.eternity.itemoption.domain.dto.external.OpenApiItemOptionResponse;

import java.util.List;

public record OpenApiAuctionHistoryResponse(
        @JsonProperty("item_name") String itemName,
        @JsonProperty("item_display_name") String itemDisplayName,
        @JsonProperty("auction_item_category") String itemSubCategory,
        @JsonProperty("item_count") long itemCount,
        @JsonProperty("auction_price_per_unit") long auctionPricePerUnit,
        @JsonProperty("date_auction_buy") String dateAuctionBuy,
        @JsonProperty("auction_buy_id") String auctionBuyId,
        @JsonProperty("item_option") List<OpenApiItemOptionResponse> openApiItemOptionResponses) {}
