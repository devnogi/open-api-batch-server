package until.the.eternity.auctionhistory.domain.component;

import until.the.eternity.auctionhistory.domain.dto.external.OpenApiAuctionHistoryResponse;
import until.the.eternity.common.enums.ItemCategory;

import java.util.List;

public interface AuctionHistoryFetcher {
    List<OpenApiAuctionHistoryResponse> fetch(ItemCategory category);
}
