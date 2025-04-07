package until.the.eternity.auction.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;

@Data
@Getter
public class AuctionHistoryDto {
    @JsonProperty("item_name")
    private String itemName;

    @JsonProperty("item_display_name")
    private String itemDisplayName;

    @JsonProperty("item_count")
    private long itemCount;

    @JsonProperty("auction_price_per_unit")
    private long auctionPricePerUnit;

    @JsonProperty("date_auction_buy")
    private String dateAuctionBuy;

    @JsonProperty("auction_buy_id")
    private String auctionBuyId;
}
