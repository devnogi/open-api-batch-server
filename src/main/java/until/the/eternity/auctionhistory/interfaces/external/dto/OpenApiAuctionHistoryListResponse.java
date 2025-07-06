package until.the.eternity.auctionhistory.interfaces.external.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record OpenApiAuctionHistoryListResponse(
        @JsonProperty("auction_history") List<OpenApiAuctionHistoryResponse> auctionHistory,
        @JsonProperty("next_cursor") String nextCursor) {}
