package until.the.eternity.auctionhistory.interfaces.rest.dto.response;

import java.util.List;

public record AuctionHistoryListResponse(
        int count,
        List<AuctionHistoryDetailResponse<ItemOptionResponse>> auctionHistoryDetailResponses) {}
