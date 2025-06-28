package until.the.eternity.auctionhistory.domain.dto.internal.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.Instant;
import java.util.List;

public record AuctionHistoryDetailResponse<ItemOptionResponse>(
        Long id,
        String itemName,
        String itemDisplayName,
        Long itemCount,
        Long auctionPricePerUnit,
        @JsonFormat(shape = JsonFormat.Shape.STRING) // ISO-8601 문자열로 직렬화
                Instant dateAuctionBuy,
        String auctionBuyId,
        String itemSubCategory,
        String itemTopCategory,
        List<ItemOptionResponse> itemOptions) {}
