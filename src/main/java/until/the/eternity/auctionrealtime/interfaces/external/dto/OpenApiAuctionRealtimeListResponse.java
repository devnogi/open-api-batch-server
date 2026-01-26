package until.the.eternity.auctionrealtime.interfaces.external.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/** Nexon Open API /auction/list 응답 리스트 DTO. */
public record OpenApiAuctionRealtimeListResponse(
        @JsonProperty("auction_item") List<OpenApiAuctionRealtimeResponse> auctionItems,
        @JsonProperty("next_cursor") String nextCursor) {}
