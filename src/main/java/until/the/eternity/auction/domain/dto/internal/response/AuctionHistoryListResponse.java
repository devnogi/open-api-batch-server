package until.the.eternity.auction.domain.dto.internal.response;

import java.util.List;

public record AuctionHistoryListResponse(
        int count,
        List<AuctionHistoryDetailResponse<ItemOptionResponse>> auctionHistoryDetailResponses
) {}
