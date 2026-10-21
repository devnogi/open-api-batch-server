package until.the.eternity.auctionrealtime.interfaces.rest.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.Instant;
import java.util.List;

public record AuctionRealtimeDetailResponse<T>(
        Long id,
        String itemName,
        String itemDisplayName,
        Long itemCount,
        Long auctionPricePerUnit,
        @JsonFormat(shape = JsonFormat.Shape.STRING) Instant dateAuctionExpire,
        @JsonFormat(shape = JsonFormat.Shape.STRING) Instant dateRegister,
        String itemSubCategory,
        String itemTopCategory,
        List<T> itemOptions) {}
