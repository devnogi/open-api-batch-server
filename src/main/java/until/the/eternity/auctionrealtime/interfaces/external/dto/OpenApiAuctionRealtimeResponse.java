package until.the.eternity.auctionrealtime.interfaces.external.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.List;
import until.the.eternity.auctionitemoption.domain.dto.external.OpenApiAuctionItemOptionResponse;

/** Nexon Open API /auction/list 응답 DTO. 현재 경매장에서 판매 중인 아이템 정보. */
public record OpenApiAuctionRealtimeResponse(
        @JsonProperty("item_name") String itemName,
        @JsonProperty("item_display_name") String itemDisplayName,
        @JsonProperty("item_count") long itemCount,
        @JsonProperty("auction_price_per_unit") long auctionPricePerUnit,
        @JsonProperty("date_auction_expire")
                @JsonFormat(shape = JsonFormat.Shape.STRING, timezone = "Asia/Seoul")
                Instant dateAuctionExpire,
        @JsonProperty("item_option") List<OpenApiAuctionItemOptionResponse> itemOptions) {}
